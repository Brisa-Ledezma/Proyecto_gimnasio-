import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PagosPanel {
    private JPanel panelPagos;
    private JTable tablaPagos;
    private DefaultTableModel modeloPagos;
    private JComboBox<ClienteItem> comboCliente;
    private JTextField fechaPagoField;
    private JTextField fechaVencimientoField;
    private JTextField montoField;
    private JComboBox<String> estadoCombo;
    private JButton registrarButton;
    private JButton modificarButton;
    private JButton eliminarButton;
    private JButton limpiarButton;

    public PagosPanel() {
        panelPagos = new JPanel(new BorderLayout(10, 10));
        panelPagos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Modelo de la tabla
        modeloPagos = new DefaultTableModel();
        modeloPagos.addColumn("ID");
        modeloPagos.addColumn("Cliente");
        modeloPagos.addColumn("Fecha de pago");
        modeloPagos.addColumn("Fecha de vencimiento");
        modeloPagos.addColumn("Estado");
        modeloPagos.addColumn("Monto");

        // Tabla
        tablaPagos = new JTable(modeloPagos);
        tablaPagos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Ocultar la columna ID para el usuario
        tablaPagos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaPagos.getColumnModel().getColumn(0).setMaxWidth(0);
        JScrollPane scroll = new JScrollPane(tablaPagos);
        panelPagos.add(scroll, BorderLayout.CENTER);

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del pago"));

        // Cliente
        formPanel.add(new JLabel("Cliente:"));
        comboCliente = new JComboBox<>();
        cargarClientes();
        formPanel.add(comboCliente);

        // Fecha de pago
        formPanel.add(new JLabel("Fecha de pago (AAAA-MM-DD):"));
        fechaPagoField = new JTextField();
        formPanel.add(fechaPagoField);

        // Fecha de vencimiento
        formPanel.add(new JLabel("Fecha de vencimiento (AAAA-MM-DD):"));
        fechaVencimientoField = new JTextField();
        formPanel.add(fechaVencimientoField);

        // Estado
        formPanel.add(new JLabel("Estado:"));
        estadoCombo = new JComboBox<>(new String[]{"PAGO", "PENDIENTE", "VENCIDO"});
        formPanel.add(estadoCombo);

        // Monto
        formPanel.add(new JLabel("Monto:"));
        montoField = new JTextField();
        formPanel.add(montoField);

        panelPagos.add(formPanel, BorderLayout.NORTH);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        registrarButton = new JButton("Registrar");
        modificarButton = new JButton("Modificar");
        eliminarButton = new JButton("Eliminar");
        limpiarButton = new JButton("Limpiar");
        buttonsPanel.add(registrarButton);
        buttonsPanel.add(modificarButton);
        buttonsPanel.add(eliminarButton);
        buttonsPanel.add(limpiarButton);
        panelPagos.add(buttonsPanel, BorderLayout.SOUTH);

        // Cargo datos en tabla
        cargarPagosEnTabla();

        // Selección de tabla
        tablaPagos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPagos.getSelectedRow() != -1) {
                int fila = tablaPagos.getSelectedRow();
                // Obtengo datos

                String clienteNombre = modeloPagos.getValueAt(fila, 1).toString();
                String fechaPago = modeloPagos.getValueAt(fila, 2).toString();
                String fechaVenc = modeloPagos.getValueAt(fila, 3).toString();
                String estado = modeloPagos.getValueAt(fila, 4).toString();
                String monto = modeloPagos.getValueAt(fila, 5).toString();

                // Actualizar los campos
                seleccionarClientePorNombre(clienteNombre);
                fechaPagoField.setText(fechaPago);
                fechaVencimientoField.setText(fechaVenc);
                estadoCombo.setSelectedItem(estado);
                montoField.setText(monto);
            }
        });

        // Botón registrar
        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarPago();
            }
        });
        // Botón modificar
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarPago();
            }
        });
        // Botón eliminar
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarPago();
            }
        });
        // Botón limpiar
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });
    }


    //Devuelve el panel para la ventana principal

    public JPanel getPanel() {
        return panelPagos;
    }


     //Carga los clientes en el combo box desde la BD

    private void cargarClientes() {
        comboCliente.removeAllItems();
        try (Connection con = conexion.conectar(); Statement stmt = con.createStatement()) {
            String sql = "SELECT id_cliente, nombre, apellido FROM Cliente";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id_cliente");
                String nombre = rs.getString("nombre") + " " + rs.getString("apellido");
                comboCliente.addItem(new ClienteItem(id, nombre));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPagos, "Error al cargar clientes: " + ex.getMessage());
        }
    }


    // Carga los pagos en la tabla desde la base de datos

    private void cargarPagosEnTabla() {
        modeloPagos.setRowCount(0);
        try (Connection con = conexion.conectar(); Statement stmt = con.createStatement()) {
            String sql = "SELECT p.id_pago, c.nombre, c.apellido, p.fecha_pago, p.fecha_vencimiento, p.estado_pago, p.monto " +
                    "FROM Pago p JOIN Cliente c ON p.id_cliente = c.id_cliente";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int idPago = rs.getInt("id_pago");
                String clienteNombre = rs.getString("nombre") + " " + rs.getString("apellido");
                String fechaPago = rs.getString("fecha_pago");
                String fechaVenc = rs.getString("fecha_vencimiento");
                String estado = rs.getString("estado_pago");
                double monto = rs.getDouble("monto");
                modeloPagos.addRow(new Object[]{idPago, clienteNombre, fechaPago, fechaVenc, estado, monto});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPagos, "Error al cargar pagos: " + ex.getMessage());
        }
    }


     //Selecciona un elemento del combo por su nombre completo

    private void seleccionarClientePorNombre(String nombreCompleto) {
        ComboBoxModel<ClienteItem> model = comboCliente.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ClienteItem item = model.getElementAt(i);
            if (item.getNombreCompleto().equals(nombreCompleto)) {
                comboCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    //limpioooo
    private void limpiarCampos() {
        comboCliente.setSelectedIndex(-1);
        fechaPagoField.setText("");
        fechaVencimientoField.setText("");
        estadoCombo.setSelectedIndex(0);
        montoField.setText("");
        tablaPagos.clearSelection();
    }

   // creo un registro de pago
    private void registrarPago() {
        ClienteItem clienteItem = (ClienteItem) comboCliente.getSelectedItem();
        if (clienteItem == null) {
            JOptionPane.showMessageDialog(panelPagos, "Seleccione un cliente.");
            return;
        }
        String fechaPago = fechaPagoField.getText().trim();
        String fechaVenc = fechaVencimientoField.getText().trim();
        String estado = (String) estadoCombo.getSelectedItem();
        String montoStr = montoField.getText().trim();
        if (fechaPago.isEmpty() || fechaVenc.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(panelPagos, "Todos los campos son obligatorios.");
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panelPagos, "Monto inválido.");
            return;
        }
        try (Connection con = conexion.conectar()) {
            String sql = "INSERT INTO Pago (id_cliente, fecha_pago, fecha_vencimiento, estado_pago, monto) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, clienteItem.getId());
            ps.setDate(2, java.sql.Date.valueOf(fechaPago));
            ps.setDate(3, java.sql.Date.valueOf(fechaVenc));
            ps.setString(4, estado);
            ps.setDouble(5, monto);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(panelPagos, "Pago registrado correctamente.");
            limpiarCampos();
            cargarPagosEnTabla();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPagos, "Error al registrar pago: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panelPagos, "Formato de fecha inválido. Use AAAA-MM-DD.");
        }
    }

    //Modifica el pago seleccionado en la tabla

    private void modificarPago() {
        int fila = tablaPagos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(panelPagos, "Seleccione un pago para modificar.");
            return;
        }
        // Obtener id pago oculto
        int idPago = Integer.parseInt(modeloPagos.getValueAt(fila, 0).toString());
        ClienteItem clienteItem = (ClienteItem) comboCliente.getSelectedItem();
        if (clienteItem == null) {
            JOptionPane.showMessageDialog(panelPagos, "Seleccione un cliente.");
            return;
        }
        String fechaPago = fechaPagoField.getText().trim();
        String fechaVenc = fechaVencimientoField.getText().trim();
        String estado = (String) estadoCombo.getSelectedItem();
        String montoStr = montoField.getText().trim();
        if (fechaPago.isEmpty() || fechaVenc.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(panelPagos, "Todos los campos son obligatorios.");
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panelPagos, "Monto inválido.");
            return;
        }
        try (Connection con = conexion.conectar()) {
            String sql = "UPDATE Pago SET id_cliente=?, fecha_pago=?, fecha_vencimiento=?, estado_pago=?, monto=? WHERE id_pago=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, clienteItem.getId());
            ps.setDate(2, java.sql.Date.valueOf(fechaPago));
            ps.setDate(3, java.sql.Date.valueOf(fechaVenc));
            ps.setString(4, estado);
            ps.setDouble(5, monto);
            ps.setInt(6, idPago);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(panelPagos, "Pago actualizado.");
                limpiarCampos();
                cargarPagosEnTabla();
            } else {
                JOptionPane.showMessageDialog(panelPagos, "No se encontró el pago.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPagos, "Error al modificar pago: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panelPagos, "Formato de fecha inválido. Use AAAA-MM-DD.");
        }
    }

     //Elimina el pago seleccionado de la base de datos.

    private void eliminarPago() {
        int fila = tablaPagos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(panelPagos, "Seleccione un pago para eliminar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(panelPagos, "¿Desea eliminar este pago?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int idPago = Integer.parseInt(modeloPagos.getValueAt(fila, 0).toString());
        try (Connection con = conexion.conectar()) {
            String sql = "DELETE FROM Pago WHERE id_pago=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idPago);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(panelPagos, "Pago eliminado correctamente.");
                limpiarCampos();
                cargarPagosEnTabla();
            } else {
                JOptionPane.showMessageDialog(panelPagos, "No se encontró el pago.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPagos, "Error al eliminar pago: " + ex.getMessage()); // CORREGIR ESTE ERRORRRRRRRRRRRRRRRRRRRRRRRRRR
        }
    }

    //Clase  para almacenar id y nombre de un cliente.

    private static class ClienteItem {
        private final int id;
        private final String nombreCompleto;
        ClienteItem(int id, String nombreCompleto) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
        }
        public int getId() { return id; }
        public String getNombreCompleto() { return nombreCompleto; }
        @Override
        public String toString() { return nombreCompleto; }
    }
}