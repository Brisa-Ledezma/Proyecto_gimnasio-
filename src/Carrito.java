import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Ventana de carrito de compras que utiliza los mismos nombres de campos
 * definidos en el formulario original (table1, seguirAgregandoAlCarritoButton,
 * finalizarCompraButton, eliminarProductoButton, volverButton y
 * modifcarCantidadButton).  Construye la interfaz de forma manual,
 * aplicando el tema y conectando las acciones para modificar cantidades,
 * eliminar productos, continuar comprando o finalizar la compra.
 */
public class Carrito {
    // Componentes con los mismos nombres que en el .form
    private JTable table1;
    private JButton seguirAgregandoAlCarritoButton;
    private JButton finalizarCompraButton;
    private JButton eliminarProductoButton;
    private JButton volverButton;
    private JButton modifcarCantidadButton;
    private JLabel subtotalLabel;

    // Datos y referencias
    private DefaultTableModel modeloCarrito;
    private final List<ProductosCard.CartItem> items;
    private final ProductosCard productosCard;
    private double subtotal;

    /**
     * Constructor.  Recibe la lista de ítems que se encuentran en el
     * carrito y una referencia a la tarjeta de productos para ajustar
     * stock y subtotales.
     */
    public Carrito(List<ProductosCard.CartItem> items, ProductosCard productosCard) {
        this.items = items;
        this.productosCard = productosCard;
        subtotal = 0.0;
        for (ProductosCard.CartItem item : items) {
            subtotal += item.getTotal();
        }
    }

    /**
     * Construye y muestra la interfaz gráfica del carrito.  Se crea el
     * layout de manera manual, respetando los nombres de los campos para
     * que coincidan con los definidos en el formulario original.
     */
    public void mostrarCarrito() {
        JFrame frame = new JFrame("Carrito");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(null);

        // Crear tabla y modelo
        modeloCarrito = new DefaultTableModel();
        modeloCarrito.addColumn("ID");
        modeloCarrito.addColumn("Producto");
        modeloCarrito.addColumn("Precio unitario");
        modeloCarrito.addColumn("Cantidad");
        modeloCarrito.addColumn("Total");
        table1 = new JTable(modeloCarrito) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // Ocultar ID
        table1.getColumnModel().getColumn(0).setMinWidth(0);
        table1.getColumnModel().getColumn(0).setMaxWidth(0);
        table1.getColumnModel().getColumn(0).setWidth(0);
        cargarTabla();
        JScrollPane scrollPane = new JScrollPane(table1);

        // Crear botones con mismos nombres
        seguirAgregandoAlCarritoButton = new JButton("Seguir agregando al carrito");
        finalizarCompraButton = new JButton("Finalizar compra");
        eliminarProductoButton = new JButton("Eliminar producto");
        volverButton = new JButton("Volver");
        modifcarCantidadButton = new JButton("Modificar cantidad");

        // Crear etiqueta de subtotal
        subtotalLabel = new JLabel();
        actualizarSubtotalLabel();

        // Panel superior: volver, eliminar y modificar
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTop.add(volverButton);
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.add(eliminarProductoButton);
        rightTop.add(modifcarCantidadButton);
        topPanel.add(leftTop, BorderLayout.WEST);
        topPanel.add(rightTop, BorderLayout.EAST);

        // Panel inferior: subtotal, seguir agregando y finalizar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.add(subtotalLabel);
        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottom.add(seguirAgregandoAlCarritoButton);
        rightBottom.add(finalizarCompraButton);
        bottomPanel.add(leftBottom, BorderLayout.WEST);
        bottomPanel.add(rightBottom, BorderLayout.EAST);

        // Aplicar tema a paneles y botones
        Tema.aplicarPanel(topPanel);
        Tema.aplicarPanel(bottomPanel);
        Tema.aplicarBoton(seguirAgregandoAlCarritoButton);
        Tema.aplicarBoton(finalizarCompraButton);
        Tema.aplicarBoton(eliminarProductoButton);
        Tema.aplicarBoton(volverButton);
        Tema.aplicarBoton(modifcarCantidadButton);
        Tema.aplicarEtiqueta(subtotalLabel);

        // Listeners
        modifcarCantidadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarCantidad();
            }
        });
        eliminarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarProducto();
            }
        });
        seguirAgregandoAlCarritoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        volverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        finalizarCompraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarCompra();
                frame.dispose();
            }
        });

        // Componer la interfaz
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /** Carga las filas del carrito en la tabla. */
    private void cargarTabla() {
        modeloCarrito.setRowCount(0);
        for (ProductosCard.CartItem item : items) {
            modeloCarrito.addRow(new Object[]{
                    item.idProducto,
                    item.nombre,
                    item.precio,
                    item.cantidad,
                    String.format("%.2f", item.getTotal())
            });
        }
    }

    /** Actualiza el texto de la etiqueta de subtotal. */
    private void actualizarSubtotalLabel() {
        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
    }

    /** Modifica la cantidad del producto seleccionado en el carrito. */
    private void modificarCantidad() {
        int fila = table1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto del carrito para modificar su cantidad.");
            return;
        }
        int id = Integer.parseInt(modeloCarrito.getValueAt(fila, 0).toString());
        ProductosCard.CartItem seleccionado = null;
        for (ProductosCard.CartItem item : items) {
            if (item.idProducto == id) {
                seleccionado = item;
                break;
            }
        }
        if (seleccionado == null) return;
        String nuevaCantidadStr = JOptionPane.showInputDialog(null,
                "Ingrese la nueva cantidad para " + seleccionado.nombre + ":",
                seleccionado.cantidad);
        if (nuevaCantidadStr == null) return;
        nuevaCantidadStr = nuevaCantidadStr.trim();
        if (!nuevaCantidadStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Cantidad inválida. Introduzca un número entero.");
            return;
        }
        int nuevaCantidad = Integer.parseInt(nuevaCantidadStr);
        if (nuevaCantidad <= 0) {
            JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a cero.");
            return;
        }
        int diferencia = nuevaCantidad - seleccionado.cantidad;
        if (diferencia != 0) {
            // Ajustar stock en ProductosCard
            productosCard.ajustarStockEnTabla(id, -diferencia);
            // Ajustar subtotal tanto en productosCard como local
            double delta = diferencia * seleccionado.precio;
            productosCard.ajustarSubtotal(delta);
            subtotal += delta;
            seleccionado.cantidad = nuevaCantidad;
            cargarTabla();
            actualizarSubtotalLabel();
        }
    }

    /** Elimina el producto seleccionado del carrito y devuelve stock. */
    private void eliminarProducto() {
        int fila = table1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto del carrito para eliminar.");
            return;
        }
        int id = Integer.parseInt(modeloCarrito.getValueAt(fila, 0).toString());
        ProductosCard.CartItem aEliminar = null;
        for (ProductosCard.CartItem item : items) {
            if (item.idProducto == id) {
                aEliminar = item;
                break;
            }
        }
        if (aEliminar == null) return;
        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Desea eliminar " + aEliminar.nombre + " del carrito?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        productosCard.ajustarStockEnTabla(id, aEliminar.cantidad);
        double importe = aEliminar.getTotal();
        productosCard.ajustarSubtotal(-importe);
        subtotal -= importe;
        items.remove(aEliminar);
        cargarTabla();
        actualizarSubtotalLabel();
    }

    /** Finaliza la compra mostrando un ticket y actualizando la base de datos. */
    private void finalizarCompra() {
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El carrito está vacío.");
            return;
        }
        StringBuilder detalle = new StringBuilder("Detalle de la compra:\n");
        for (ProductosCard.CartItem item : items) {
            detalle.append(item.cantidad).append(" x ")
                    .append(item.nombre)
                    .append(" ($").append(item.precio).append(") = $")
                    .append(String.format("%.2f", item.getTotal()))
                    .append("\n");
        }
        detalle.append("Total a abonar: $").append(String.format("%.2f", subtotal));
        int ok = JOptionPane.showConfirmDialog(null, detalle.toString(),
                "Confirmar compra", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        // Actualizar base de datos y limpiar carrito
        productosCard.actualizarStockEnBase();
        productosCard.limpiarCarrito();
        items.clear();
        subtotal = 0.0;
    }
}
