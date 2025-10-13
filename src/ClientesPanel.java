import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;


public class ClientesPanel {

    private JPanel panelClientes;
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;
    private JTextField nombreField;
    private JTextField apellidoField;
    private JTextField dniField;
    private JTextField emailField;
    private JButton agregarButton;
    private JButton modificarButton;
    private JButton eliminarButton;
    private JButton limpiarButton;

    public ClientesPanel() {

        panelClientes = new JPanel(new BorderLayout(10, 10));
        panelClientes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        modeloClientes = new DefaultTableModel();
        modeloClientes.addColumn("Nombre");
        modeloClientes.addColumn("Apellido");
        modeloClientes.addColumn("DNI");
        modeloClientes.addColumn("Email");


        tablaClientes = new JTable(modeloClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        panelClientes.add(scrollPane, BorderLayout.CENTER);


        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));


        formPanel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        formPanel.add(nombreField);

        formPanel.add(new JLabel("Apellido:"));
        apellidoField = new JTextField();
        formPanel.add(apellidoField);

        formPanel.add(new JLabel("DNI:"));
        dniField = new JTextField();
        formPanel.add(dniField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        panelClientes.add(formPanel, BorderLayout.NORTH);


        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        agregarButton = new JButton("Agregar");
        modificarButton = new JButton("Modificar");
        eliminarButton = new JButton("Eliminar");
        limpiarButton = new JButton("Limpiar");
        buttonsPanel.add(agregarButton);
        buttonsPanel.add(modificarButton);
        buttonsPanel.add(eliminarButton);
        buttonsPanel.add(limpiarButton);
        panelClientes.add(buttonsPanel, BorderLayout.SOUTH);

        // Cargar datos iniciales en la tabla
        cargarClientesEnTabla();

        // aca es para la selección de la tabla (carga los datos en los campos)
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                int fila = tablaClientes.getSelectedRow();
                nombreField.setText(modeloClientes.getValueAt(fila, 0).toString());
                apellidoField.setText(modeloClientes.getValueAt(fila, 1).toString());
                dniField.setText(modeloClientes.getValueAt(fila, 2).toString());
                emailField.setText(modeloClientes.getValueAt(fila, 3) != null ? modeloClientes.getValueAt(fila, 3).toString() : "");
            }
        });

        // agrego un cliente
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarCliente();
            }
        });

        // modifico un cliente existente
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarCliente();
            }
        });

        // elimino un cliente por DNI
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarCliente();
            }
        });

        // limpiar campos
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });
    }

     //Devuelve el panel para ponerlo en la ventana principal

    public JPanel getPanel() {
        return panelClientes;
    }



     //Inserta un nuevo cliente en la base de datos después de validar los campos
    private void agregarCliente() {
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String dni = dniField.getText().trim();
        String email = emailField.getText().trim();

        // Validación
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty()) {
            JOptionPane.showMessageDialog(panelClientes, "Nombre, apellido y DNI son obligatorios.");
            return;
        }
        // Verifico si ya existe un cliente con ese DNI
        try (Connection con = conexion.conectar()) {
            String checkSql = "SELECT id_cliente FROM Cliente WHERE DNI=?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, dni);
            ResultSet rsCheck = checkPs.executeQuery();
            if (rsCheck.next()) {
                JOptionPane.showMessageDialog(panelClientes, "Ya existe un cliente con ese DNI.");
                return;
            }
            // Insertar nuevo cliente (sin un usuario asociado)
            String sql = "INSERT INTO Cliente (id_usuario, nombre, apellido, DNI, email) VALUES (NULL, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, dni);
            ps.setString(4, email);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(panelClientes, "Cliente agregado correctamente.");
            limpiarCampos();
            cargarClientesEnTabla();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelClientes, "Error al agregar cliente: " + ex.getMessage());
        }
    }


     //Actualiza los datos de un cliente basado en el DNI

    private void modificarCliente() {
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String dni = dniField.getText().trim();
        String email = emailField.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty()) {
            JOptionPane.showMessageDialog(panelClientes, "Nombre, apellido y DNI son obligatorios para modificar.");
            return;
        }
        try (Connection con = conexion.conectar()) {
            String sql = "UPDATE Cliente SET nombre=?, apellido=?, email=? WHERE DNI=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, email);
            ps.setString(4, dni);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(panelClientes, "Cliente actualizado correctamente.");
                limpiarCampos();
                cargarClientesEnTabla();
            } else {
                JOptionPane.showMessageDialog(panelClientes, "No se encontró un cliente con ese DNI.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelClientes, "Error al actualizar cliente: " + ex.getMessage());
        }
    }

     //Elimina un cliente de la BD basándose en el DNI

    private void eliminarCliente() {
        String dni = dniField.getText().trim();
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(panelClientes, "Ingrese un DNI para eliminar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(panelClientes, "¿Desea eliminar este cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection con = conexion.conectar()) {
            String sql = "DELETE FROM Cliente WHERE DNI=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, dni);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(panelClientes, "Cliente eliminado correctamente.");
                limpiarCampos();
                cargarClientesEnTabla();
            } else {
                JOptionPane.showMessageDialog(panelClientes, "No se encontró un cliente con ese DNI.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelClientes, "Error al eliminar cliente: " + ex.getMessage());
        }
    }


   //Limpia todos los campos

    private void limpiarCampos() {
        nombreField.setText("");
        apellidoField.setText("");
        dniField.setText("");
        emailField.setText("");
        tablaClientes.clearSelection();
    }


     //Consulta y carga los clientes en la tabla

    private void cargarClientesEnTabla() {
        // Eliminar filas previas
        modeloClientes.setRowCount(0);
        try (Connection con = conexion.conectar(); Statement stmt = con.createStatement()) {
            String sql = "SELECT nombre, apellido, DNI, email FROM Cliente";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                modeloClientes.addRow(new Object[]{
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("DNI"),
                        rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelClientes, "Error al cargar clientes: " + ex.getMessage());
        }
    }
}