import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PagosCard {

    private final JTable tablaPagos;         // tablePagos
    private final JComboBox comboCliente;    // comboCliente
    private final JTextField vencimientoTxt; // vencimientoPagotxt  (formato: yyyy-MM-dd)
    private final JTextField montoTxt;       // montotxt
    private final JComboBox estadoCombo;     // estadoCombo  (PENDIENTE, PAGADO, VENCIDO, INACTIVO)
    private final JButton registrarButton;   // registrarPagoButton
    private final JButton modificarButton;   // modificarPagoButton
    private final JButton cancelarButton;    // botón para cancelar/limpiar
    private final JTextField fechaPagoTxt;

    private DefaultTableModel modelo;
    private int idPagoSeleccionado = -1;

    private final Map<Integer, Integer> clientesIndex = new LinkedHashMap<>();

    public PagosCard(JTable tablePagos,
                     JComboBox comboCliente,
                     JTextField vencimientoPagotxt,
                     JTextField montotxt,
                     JComboBox estadoCombo,
                     JTextField fechaPagotxt,
                     JButton registrarPagoButton,
                     JButton modificarPagoButton,
                     JButton cancelarPagoButton) {
        this.tablaPagos = tablePagos;
        this.comboCliente = comboCliente;
        this.vencimientoTxt = vencimientoPagotxt;
        this.montoTxt = montotxt;
        this.estadoCombo = estadoCombo;
        this.registrarButton = registrarPagoButton;
        this.modificarButton = modificarPagoButton;
        this.cancelarButton = cancelarPagoButton;
        this.fechaPagoTxt = fechaPagotxt;
    }

    public void inicializar() {
        modelo = new DefaultTableModel();
        modelo.addColumn("ID");               // oculto
        modelo.addColumn("Nombre completo");  // nombre y apellido del cliente
        modelo.addColumn("Fecha de pago");    // fecha de pago
        modelo.addColumn("Fecha de vencimiento"); // fecha de vencimiento
        modelo.addColumn("Estado");           // estado del pago
        modelo.addColumn("Monto");            // importe
        tablaPagos.setModel(modelo);

        // Ocultamos la columna ID
        /*tablaPagos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaPagos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaPagos.getColumnModel().getColumn(0).setWidth(0);*/

        // Carga combos y tabla inicial
        cargarClientesEnCombo();
        cargarPagosEnTabla();
        // Cargar estados peteros del combo
        estadoCombo.removeAllItems();
        estadoCombo.addItem("PAGADO");
        estadoCombo.addItem("PENDIENTE");
        estadoCombo.addItem("VENCIDO");
        estadoCombo.addItem("INACTIVO");

        // carga datos en los campos
        tablaPagos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPagos.getSelectedRow() != -1) {
                int fila = tablaPagos.getSelectedRow();
                idPagoSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());

                // Selecciona en el combo el cliente asociado
                String nombre = modelo.getValueAt(fila, 1).toString();
                for (int i = 0; i < comboCliente.getItemCount(); i++) {
                    Object item = comboCliente.getItemAt(i);
                    if (item != null && item.toString().equals(nombre)) {
                        comboCliente.setSelectedIndex(i);
                        break;
                    }
                }
                fechaPagoTxt.setText(modelo.getValueAt(fila, 2).toString());
                vencimientoTxt.setText(modelo.getValueAt(fila, 3).toString());
                String estado = modelo.getValueAt(fila, 4).toString();
                estadoCombo.setSelectedItem(estado);
                montoTxt.setText(modelo.getValueAt(fila, 5).toString());

                // Deshabilita botón Registrar mientras hay una fila seleccionada
                registrarButton.setEnabled(false);
            }
        });

        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearPago();
                limpiarCampos();
            }
        });
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (idPagoSeleccionado == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione un pago para modificar.");
                    return;
                }
                actualizarPago();
                limpiarCampos();
            }
        });

        // Cancelar limpia los campos
        if (cancelarButton != null) {
            cancelarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    limpiarCampos();
                }
            });
        }
        aplicarColores();
    }

    private void aplicarColores() {
        // Campos de texto
        Tema.aplicarCampoTexto(vencimientoTxt);
        Tema.aplicarCampoTexto(montoTxt);
        // Botones
        Tema.aplicarBoton(registrarButton);
        Tema.aplicarBoton(modificarButton);
        if (cancelarButton != null) Tema.aplicarBoton(cancelarButton);
        // Combos con fondo claro y texto oscuro
        if (estadoCombo != null) {
            estadoCombo.setBackground(Tema.COLOR_TEXTFIELD);
            estadoCombo.setForeground(Tema.COLOR_TEXT);
        }
        if (comboCliente != null) {
            comboCliente.setBackground(Tema.COLOR_TEXTFIELD);
            comboCliente.setForeground(Tema.COLOR_TEXT);
        }
    }

    private void limpiarCampos() {
        tablaPagos.clearSelection();
        idPagoSeleccionado = -1;
        vencimientoTxt.setText("");
        montoTxt.setText("");
        if (estadoCombo.getItemCount() > 0) estadoCombo.setSelectedIndex(0);
        if (comboCliente.getItemCount() > 0) comboCliente.setSelectedIndex(0);
        registrarButton.setEnabled(true);
    }

    private void cargarClientesEnCombo() {
        clientesIndex.clear();
        comboCliente.removeAllItems();
        try (Connection con = conexion.conectar()) {
            //clientes activos se muestran nombre y apellido
            String sql = "SELECT c.id_cliente, c.nombre, c.apellido " +
                    "FROM Cliente c JOIN Usuario u ON c.id_usuario = u.id_usuario " +
                    "WHERE u.activa = TRUE ORDER BY c.apellido, c.nombre";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                int idx = 0;
                while (rs.next()) {
                    int idCli = rs.getInt("id_cliente");
                    String label = rs.getString("nombre") + " " + rs.getString("apellido");
                    comboCliente.addItem(label);
                    clientesIndex.put(idx++, idCli);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar clientes: " + ex.getMessage());
        }
    }

    private void cargarPagosEnTabla() {
        modelo.setRowCount(0);
        try (Connection con = conexion.conectar()) {
            // pagos activos y nombre completo del cliente
            String sql = "SELECT p.id_pago, CONCAT(c.nombre,' ',c.apellido) AS cliente, " +
                    "p.fecha_pago, p.fecha_vencimiento, p.estado_pago, p.monto " +
                    "FROM Pago p JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                    "LEFT JOIN Usuario u ON c.id_usuario = u.id_usuario " +
                    "WHERE COALESCE(p.estado_pago,'') <> 'INACTIVO' " +
                    "AND (u.id_usuario IS NULL OR u.activa = TRUE) " +
                    "ORDER BY p.fecha_pago DESC, p.id_pago DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                            rs.getInt("id_pago"),
                            rs.getString("cliente"),
                            rs.getDate("fecha_pago").toString(),
                            rs.getDate("fecha_vencimiento").toString(),
                            rs.getString("estado_pago") == null ? "" : rs.getString("estado_pago"),
                            rs.getBigDecimal("monto").setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar pagos: " + ex.getMessage());
        }
    }

    private void crearPago() {
        if (comboCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un cliente.");
            return;
        }
        // Recupero el índice seleccionado y lo usamos para obtener el id del cliente
        int selIndex = comboCliente.getSelectedIndex();
        Integer idCliente = clientesIndex.get(selIndex);

        LocalDate hoy = LocalDate.now();
        LocalDate venc;
        try {
            venc = LocalDate.parse(vencimientoTxt.getText().trim()); // yyyy-MM-dd
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(null, "Fecha de vencimiento inválida. Use yyyy-MM-dd.");
            return;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoTxt.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Monto inválido.");
            return;
        }

        String estado = estadoCombo.getSelectedItem() == null ? "PENDIENTE" : estadoCombo.getSelectedItem().toString();

        try (Connection con = conexion.conectar()) {
            String insert = "INSERT INTO Pago (id_cliente, fecha_pago, fecha_vencimiento, estado_pago, monto) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setInt(1, idCliente);
                ps.setDate(2, Date.valueOf(hoy));
                ps.setDate(3, Date.valueOf(venc));
                ps.setString(4, estado);
                ps.setBigDecimal(5, monto);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Pago registrado.");
            cargarPagosEnTabla();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al registrar pago: " + ex.getMessage());
        }
    }

    private void actualizarPago() {
        if (idPagoSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un pago.");
            return;
        }
        if (comboCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un cliente.");
            return;
        }
        // Recupero el índice seleccionado y lo usamos para obtener el id del cliente
        int selIndex = comboCliente.getSelectedIndex();
        Integer idCliente = clientesIndex.get(selIndex);

        LocalDate venc;
        try {
            venc = LocalDate.parse(vencimientoTxt.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(null, "Fecha de vencimiento inválida. Use yyyy-MM-dd.");
            return;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoTxt.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Monto inválido.");
            return;
        }

        String estado = estadoCombo.getSelectedItem() == null ? "PENDIENTE": estadoCombo.getSelectedItem().toString();

        try (Connection con = conexion.conectar()) {
            String update = "UPDATE Pago SET id_cliente=?, fecha_vencimiento=?, estado_pago=?, monto=? WHERE id_pago=?";
            try (PreparedStatement ps = con.prepareStatement(update)) {
                ps.setInt(1, idCliente);
                ps.setDate(2, Date.valueOf(venc));
                ps.setString(3, estado);
                ps.setBigDecimal(4, monto);
                ps.setInt(5, idPagoSeleccionado);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Pago actualizado.");
            cargarPagosEnTabla();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al actualizar pago: " + ex.getMessage());
        }
    }
}
