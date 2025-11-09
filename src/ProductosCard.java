import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica y control para la sección de Productos dentro del menú principal.
 *
 * Este controlador se encarga de poblar la tabla de productos desde la base
 * de datos, permitir al usuario agregar artículos a un carrito de compras,
 * llevar la cuenta del subtotal y abrir la ventana del carrito para ver
 * y modificar los productos seleccionados.  La implementación sigue el
 * estilo utilizado en otras tarjetas de la aplicación (ClientesCard,
 * ProfesoresCard y PagosCard), utilizando DefaultTableModel para las
 * tablas, ActionListeners en los botones y validaciones mediante
 * JOptionPane.
 */
public class ProductosCard {

    /** Elementos de la interfaz proporcionados por MenuPrincipal.form. */
    private final JTable tablaProductos;      // table2
    private final JPanel panelProductos;      // panelProductos
    private final JButton agregarCarritoBtn;  // agregarAlCarritoButton
    private final JButton verCarritoBtn;      // verCarritoButton

    /** Modelo para la tabla de productos. */
    private DefaultTableModel modeloProductos;

    /**
     * Lista de artículos en el carrito.  Cada CartItem contiene id,
     * nombre, precio y cantidad.
     */
    private final List<CartItem> carritoItems = new ArrayList<>();

    /** Sumatoria de todos los productos agregados al carrito. */
    private double subtotal = 0.0;

    /**
     * Etiqueta para mostrar el subtotal.  Se detecta automáticamente
     * buscando un JLabel con texto "Subtotal" dentro de panelProductos.
     */
    private JLabel subtotalLabel;

    /**
     * Constructor.  Recibe las referencias a los componentes de la UI.
     *
     * @param tablaProductos    Tabla donde se listan los productos disponibles
     * @param panelProductos    Panel contenedor para aplicar el tema
     * @param agregarCarritoBtn Botón para agregar el producto seleccionado al carrito
     * @param verCarritoBtn     Botón para abrir la ventana del carrito
     */
    public ProductosCard(JTable tablaProductos, JPanel panelProductos,
                         JButton agregarCarritoBtn, JButton verCarritoBtn) {
        this.tablaProductos = tablaProductos;
        this.panelProductos = panelProductos;
        this.agregarCarritoBtn = agregarCarritoBtn;
        this.verCarritoBtn = verCarritoBtn;
    }

    /**
     * Configura la tabla, carga los productos desde la base de datos,
     * asigna los listeners de botones y aplica los colores de tema.
     */
    public void inicializar() {
        // Modelo con columnas: ID (oculta), Nombre, Precio, Stock
        modeloProductos = new DefaultTableModel();
        modeloProductos.addColumn("ID");
        modeloProductos.addColumn("Nombre");
        modeloProductos.addColumn("Precio");
        modeloProductos.addColumn("Stock");
        tablaProductos.setModel(modeloProductos);
        // Oculto la columna ID al usuario
        tablaProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setWidth(0);

        // Busca y asigna la etiqueta de subtotal
        buscarEtiquetaSubtotal();
        actualizarSubtotalLabel();

        // Carga los datos de productos
        cargarProductosEnTabla();

        // Listeners de los botones
        agregarCarritoBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProductoAlCarrito();
            }
        });
        verCarritoBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaCarrito();
            }
        });

        // Aplica el tema de colores a los controles
        aplicarColores();
    }

    /**
     * Realiza la consulta a la base de datos para obtener los productos.
     * Espera que exista una tabla Producto con columnas id_producto,
     * nombre, precio y stock.  Si la tabla no existe o falla la consulta,
     * se captura la excepción y se informa al usuario.
     */
    private void cargarProductosEnTabla() {
        modeloProductos.setRowCount(0);
        try (Connection con = conexion.conectar()) {
            String query = "SELECT id_producto, nombre, precio, stock " +
                    "FROM Producto WHERE activo = TRUE ORDER BY nombre";
            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_producto");
                    String nombre = rs.getString("nombre");
                    double precio = rs.getDouble("precio");
                    int stock = rs.getInt("stock");
                    modeloProductos.addRow(new Object[]{id, nombre, precio, stock});
                }
            }
        } catch (SQLException ex) {
            // Si no existe la tabla o hay cualquier otro error, informamos y no
            // interrumpimos la aplicación para permitir pruebas con datos vacíos.
            JOptionPane.showMessageDialog(null, "Error al cargar productos: " + ex.getMessage());
        }
    }

    /**
     * Busca de forma recursiva un JLabel en panelProductos cuyo texto
     * contenga la palabra "Subtotal".  Esto permite reutilizar la etiqueta
     * definida en el diseñador sin añadir otro componente.  Si no se
     * encuentra se crea una nueva etiqueta al final del panel.
     */
    private void buscarEtiquetaSubtotal() {
        this.subtotalLabel = encontrarEtiqueta(panelProductos);
        if (this.subtotalLabel == null) {
            // Si no se encuentra, creo una nueva debajo de la tabla
            subtotalLabel = new JLabel();
            subtotalLabel.setText("Subtotal: $0.0");
            panelProductos.add(subtotalLabel);
        }
    }

    /** Recorre recursivamente un contenedor en busca de la etiqueta de subtotal. */
    private JLabel encontrarEtiqueta(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                String txt = lbl.getText();
                if (txt != null && txt.toLowerCase().contains("subtotal")) {
                    return lbl;
                }
            }
            if (comp instanceof Container) {
                JLabel result = encontrarEtiqueta((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }

    /** Actualiza el texto de la etiqueta de subtotal con el valor actual del subtotal. */
    private void actualizarSubtotalLabel() {
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
            Tema.aplicarEtiqueta(subtotalLabel);
        }
    }

    /**
     * Valida la selección y permite agregar el producto y cantidad
     * seleccionados al carrito.  Verifica que la cantidad ingresada
     * sea numérica, positiva y que no exceda el stock disponible.
     */
    private void agregarProductoAlCarrito() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto en la tabla.");
            return;
        }
        // Datos del producto seleccionado
        int id = Integer.parseInt(modeloProductos.getValueAt(fila, 0).toString());
        String nombre = modeloProductos.getValueAt(fila, 1).toString();
        double precio = Double.parseDouble(modeloProductos.getValueAt(fila, 2).toString());
        int stockDisponible = Integer.parseInt(modeloProductos.getValueAt(fila, 3).toString());

        // Pedir cantidad
        String cantidadStr = JOptionPane.showInputDialog(null, "Ingrese la cantidad de \"" + nombre + "\" que desea agregar:");
        if (cantidadStr == null) return; // cancelado
        cantidadStr = cantidadStr.trim();
        if (!cantidadStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Ingrese un número entero válido para la cantidad.");
            return;
        }
        int cantidad = Integer.parseInt(cantidadStr);
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a cero.");
            return;
        }
        if (cantidad > stockDisponible) {
            JOptionPane.showMessageDialog(null, "Cantidad solicitada supera el stock disponible (" + stockDisponible + ").");
            return;
        }

        // Verificar si ya existe el producto en el carrito
        CartItem existente = null;
        for (CartItem item : carritoItems) {
            if (item.idProducto == id) {
                existente = item;
                break;
            }
        }
        if (existente != null) {
            // Se suman las cantidades existentes
            if (existente.cantidad + cantidad > stockDisponible) {
                JOptionPane.showMessageDialog(null, "No puede exceder el stock total disponible al modificar la cantidad.");
                return;
            }
            existente.cantidad += cantidad;
        } else {
            // Se crea un nuevo item en el carrito
            carritoItems.add(new CartItem(id, nombre, precio, cantidad));
        }
        // Reducir el stock en la tabla de productos
        int nuevoStock = stockDisponible - cantidad;
        modeloProductos.setValueAt(nuevoStock, fila, 3);
        // Actualizar subtotal
        subtotal += cantidad * precio;
        actualizarSubtotalLabel();
    }

    /**
     * Abre la ventana del carrito.  La lista de artículos y el subtotal se
     * pasan al constructor de Carrito para que el usuario pueda visualizar,
     * modificar o finalizar su compra.  La ventana principal se mantiene
     * abierta en segundo plano.
     */
    private void abrirVentanaCarrito() {
        if (carritoItems.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El carrito está vacío. Agregue productos primero.");
            return;
        }
        Carrito carrito = new Carrito(carritoItems, this);
        carrito.mostrarCarrito();
    }

    /**
     * Restaura o ajusta el stock de un producto en la tabla cuando se elimina o
     * modifica una cantidad desde el carrito.
     *
     * @param idProducto  ID del producto a ajustar
     * @param delta       Cantidad ajustada (positiva si se devuelve stock, negativa si se reduce)
     */
    public void ajustarStockEnTabla(int idProducto, int delta) {
        for (int i = 0; i < modeloProductos.getRowCount(); i++) {
            int id = Integer.parseInt(modeloProductos.getValueAt(i, 0).toString());
            if (id == idProducto) {
                int stockActual = Integer.parseInt(modeloProductos.getValueAt(i, 3).toString());
                int nuevoStock = stockActual + delta;
                modeloProductos.setValueAt(nuevoStock, i, 3);
                break;
            }
        }
    }

    /**
     * Invocado desde Carrito cuando se elimina un ítem o se modifica su
     * cantidad.  Ajusta el subtotal acumulado en esta vista y actualiza
     * la etiqueta correspondiente.
     *
     * @param deltaImporte Cambio en el importe total (positivo o negativo)
     */
    public void ajustarSubtotal(double deltaImporte) {
        subtotal += deltaImporte;
        if (subtotal < 0) subtotal = 0;
        actualizarSubtotalLabel();
    }

    /**
     * Persiste en la base de datos la reducción de stock de cada producto
     * comprado.  Este método es invocado al finalizar la compra.  En caso
     * de que falle la actualización se informa al usuario mediante un
     * mensaje, pero no se revierte la operación en memoria.
     */
    public void actualizarStockEnBase() {
        try (Connection con = conexion.conectar()) {
            con.setAutoCommit(false);
            String update = "UPDATE Producto SET stock = stock - ? WHERE id_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(update)) {
                for (CartItem item : carritoItems) {
                    ps.setInt(1, item.cantidad);
                    ps.setInt(2, item.idProducto);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al actualizar stock en base de datos: " + ex.getMessage());
        }
    }

    /**
     * Limpia los datos del carrito y resetea el subtotal después de una
     * compra exitosa.  Se vuelve a cargar la lista de productos para
     * reflejar el stock actualizado en la base de datos.
     */
    public void limpiarCarrito() {
        carritoItems.clear();
        subtotal = 0.0;
        actualizarSubtotalLabel();
        cargarProductosEnTabla();
    }

    /** Aplica el tema de la aplicación a los componentes de esta vista. */
    private void aplicarColores() {
        Tema.aplicarPanel(panelProductos);
        Tema.aplicarBoton(agregarCarritoBtn);
        Tema.aplicarBoton(verCarritoBtn);
        if (subtotalLabel != null) Tema.aplicarEtiqueta(subtotalLabel);
    }

    /** Clase interna que representa un elemento del carrito de compras. */
    public static class CartItem {
        public int idProducto;
        public String nombre;
        public double precio;
        public int cantidad;

        public CartItem(int idProducto, String nombre, double precio, int cantidad) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        public double getTotal() {
            return precio * cantidad;
        }
    }
}
