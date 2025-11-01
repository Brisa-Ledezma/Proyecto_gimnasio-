import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;



public class ClientesCard {

    private final JTable tablaClientes;          // tabla1
    private final JTextField dniField;           // dniClienteTxt
    private final JTextField nombreField;        // nombreClientetxt
    private final JTextField apellidoField;      // apellidoClienteTxt
    private final JTextField emailField;         // emailTxt
    private final JButton agregarButton;         // agregarSocioButton
    private final JButton modificarButton;       // modificarSocioButton
    private final JButton eliminarButton;        // eliminarSocioButton
    private final JButton cancelarButton;        // cancelarButton
    private DefaultTableModel modelo;
    private int idClienteSeleccionado = -1;
    //Te dice si el formulario está en modo "nuevo" (true) o "modificar" (false)
    private boolean esNuevo = true;


    //Constructor. Recibe de referencia a los componentes creados en MenuPrincipal.
    // Es importante esto porque si no, no me anda nada

    public ClientesCard(JTable tablaClientes, JTextField dniField, JTextField nombreField,
                        JTextField apellidoField, JTextField emailField, JButton agregarButton,
                        JButton modificarButton, JButton eliminarButton, JButton cancelarButton) {
        this.tablaClientes = tablaClientes;
        this.dniField = dniField;
        this.nombreField = nombreField;
        this.apellidoField = apellidoField;
        this.emailField = emailField;
        this.agregarButton = agregarButton;
        this.modificarButton = modificarButton;
        this.eliminarButton = eliminarButton;
        this.cancelarButton = cancelarButton;
    }


    public void inicializar() {
        // Configuro el modelo de la tabla
        modelo = new DefaultTableModel();
        modelo.addColumn("ID");// Oculta al usuario
        modelo.addColumn("DNI");
        modelo.addColumn("Nombre");
        modelo.addColumn("Apellido");
        modelo.addColumn("Email");
        tablaClientes.setModel(modelo);
        // Oculto la columna ID
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setWidth(0);

        // Carga de clientes activos
        cargarClientesEnTabla();



        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                int fila = tablaClientes.getSelectedRow();
                idClienteSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
                // Relleno campos
                dniField.setText(modelo.getValueAt(fila, 1).toString());
                nombreField.setText(modelo.getValueAt(fila, 2).toString());
                apellidoField.setText(modelo.getValueAt(fila, 3).toString());
                emailField.setText(modelo.getValueAt(fila, 4).toString());
                esNuevo = false;
                setModoEdicion(true); // habilita Modificar/Eliminar y deshabilita Agregar
            }
        });

        // Crea o reactiva cliente
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCliente();
                limpiarCampos();
            }
        });

        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (idClienteSeleccionado == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione un cliente para modificar.");
                    return;
                }

                //Tomamos los datos actualizados del formulario
                String dni = dniField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String email = emailField.getText().trim();


                if (!dni.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "El DNI solo debe contener números.");
                    return;
                }

                if (dni.length() < 7 || dni.length() > 10) {
                    JOptionPane.showMessageDialog(null, "El DNI debe tener entre 7 y 10 dígitos.");
                    return;
                }

                if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "DNI, Nombre y Apellido son obligatorios.");
                    return;
                }

                try (Connection con = conexion.conectar()) {
                    String update = "UPDATE Cliente SET nombre=?, apellido=?, DNI=?, email=? WHERE id_cliente=?";
                    PreparedStatement ps = con.prepareStatement(update);
                    ps.setString(1, nombre);
                    ps.setString(2, apellido);
                    ps.setString(3, dni);
                    ps.setString(4, email);
                    ps.setInt(5, idClienteSeleccionado);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Cliente modificado correctamente.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error al modificar cliente: " + ex.getMessage());
                }

                cargarClientesEnTabla();
                limpiarCampos();
            }
        });

        // Desactiva el cliente y actualiza la bendita tabla
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (idClienteSeleccionado == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione un cliente para desactivar.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(null, "¿Está seguro de desactivar este cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    desactivarCliente(idClienteSeleccionado);
                    cargarClientesEnTabla();
                    limpiarCampos();
                }
            }
        });

        // limpia campos y resetea
        if (cancelarButton != null) {
            cancelarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    limpiarCampos();
                }
            });
        }
        dniField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();

                if (!Character.isDigit(c)) {
                    e.consume(); // te cancela la tecla
                }
            }
        });

        aplicarColores();
        agregarButton.setEnabled(true);
        modificarButton.setEnabled(false);
        eliminarButton.setEnabled(false);
    }

    //colorcito
    private void aplicarColores() {
        Tema.aplicarCampoTexto(dniField);
        Tema.aplicarCampoTexto(nombreField);
        Tema.aplicarCampoTexto(apellidoField);
        Tema.aplicarCampoTexto(emailField);
        Tema.aplicarBoton(agregarButton);
        Tema.aplicarBoton(modificarButton);
        Tema.aplicarBoton(eliminarButton);
        if (cancelarButton != null) {
            Tema.aplicarBoton(cancelarButton);
        }
    }
    // Cambia el estado de los botones según estemos editando o creando
    private void setModoEdicion(boolean editar) {
        modificarButton.setEnabled(editar);
        eliminarButton.setEnabled(editar);
        agregarButton.setEnabled(!editar);
    }


     //Inserta un cliente nuevo o actualiza uno existente.

    private void guardarCliente() {
        String dni = dniField.getText().trim();
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String email = emailField.getText().trim();

        // Validación
        if (!dni.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "El DNI solo debe contener números.");
            return;
        }

        // Valido que el dni tenga el largo que quiero
        if (dni.length() < 7 || dni.length() > 10) {
            JOptionPane.showMessageDialog(null, "El DNI debe tener entre 7 y 10 dígitos.");
            return;
        }
        // Validacion
        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(null, "DNI, Nombre y Apellido son obligatorios.");
            return;
        }

        try (Connection con = conexion.conectar()) {
            // Si es nuevo, compruebo si ya existe un cliente con ese DNI
            if (esNuevo) {
                String query = "SELECT c.id_cliente, c.id_usuario FROM Cliente c WHERE c.DNI = ?";
                try (PreparedStatement psCheck = con.prepareStatement(query)) {
                    psCheck.setString(1, dni);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        // si el cliente existe pregunto si quiere reactivarlo
                        int idCli = rs.getInt("id_cliente");
                        int idUsu = rs.getInt("id_usuario");
                        int resp = JOptionPane.showConfirmDialog(null, "El cliente ya existe y está inactivo. ¿Desea reactivarlo?", "Reactivar", JOptionPane.YES_NO_OPTION);
                        if (resp == JOptionPane.YES_OPTION) {
                            // Reactivo usuario y actualizo datos
                            String updCli = "UPDATE Cliente SET nombre=?, apellido=?, email=? WHERE id_cliente=?";
                            try (PreparedStatement ps1 = con.prepareStatement(updCli)) {
                                ps1.setString(1, nombre);
                                ps1.setString(2, apellido);
                                ps1.setString(3, email);
                                ps1.setInt(4, idCli);
                                ps1.executeUpdate();
                            }
                            String updUsu = "UPDATE Usuario SET activa=TRUE WHERE id_usuario=?";
                            try (PreparedStatement ps2 = con.prepareStatement(updUsu)) {
                                ps2.setInt(1, idUsu);
                                ps2.executeUpdate();
                            }
                            JOptionPane.showMessageDialog(null, "Cliente reactivado y actualizado.");
                        }
                    } else {
                        // Para un cliente completamente nuevo creo usuario y cliente
                        String insUsu = "INSERT INTO Usuario (nom_usuario, contrasenia, rol, activa) VALUES (?, ?, 'USER', TRUE)";
                        try (PreparedStatement psUsu = con.prepareStatement(insUsu, Statement.RETURN_GENERATED_KEYS)) {
                            psUsu.setString(1, dni);
                            psUsu.setString(2, dni);
                            psUsu.executeUpdate();
                            ResultSet gen = psUsu.getGeneratedKeys();
                            int idUsuario = gen.next() ? gen.getInt(1) : -1;

                            String insCli = "INSERT INTO Cliente (id_usuario, nombre, apellido, DNI, email) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement psCli = con.prepareStatement(insCli)) {
                                psCli.setInt(1, idUsuario);
                                psCli.setString(2, nombre);
                                psCli.setString(3, apellido);
                                psCli.setString(4, dni);
                                psCli.setString(5, email);
                                psCli.executeUpdate();
                            }
                            JOptionPane.showMessageDialog(null, "Cliente agregado correctamente.");
                        }
                    }
                }
            } else {
                // Actualizo campos
                String update = "UPDATE Cliente SET nombre=?, apellido=?, DNI=?, email=? WHERE id_cliente=?";
                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setString(1, nombre);
                    ps.setString(2, apellido);
                    ps.setString(3, dni);
                    ps.setString(4, email);
                    ps.setInt(5, idClienteSeleccionado);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Cliente actualizado correctamente.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar cliente: " + ex.getMessage());
        }

        // Refresco la tabla y limpia los campos
        cargarClientesEnTabla();
        limpiarCampos();
    }


    //Desactivo el cliente indicado.

    private void desactivarCliente(int idCliente) {
        try (Connection con = conexion.conectar()) {
            String sql = "UPDATE Usuario u JOIN Cliente c ON u.id_usuario = c.id_usuario SET u.activa=FALSE WHERE c.id_cliente=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Cliente desactivado correctamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al desactivar cliente: " + ex.getMessage());
        }
    }


     //Limpio campos y reseteo las variables

    private void limpiarCampos() {
        dniField.setText("");
        nombreField.setText("");
        apellidoField.setText("");
        emailField.setText("");
        idClienteSeleccionado = -1;
        esNuevo = true;
        tablaClientes.clearSelection();
        setModoEdicion(false);//  vuelve a habilitar Agregar y deshabilita Modificar/Eliminar
    }

    //Cargo únicamente los clientes activos en la tabla.

    private void cargarClientesEnTabla() {
        modelo.setRowCount(0);
        try (Connection con = conexion.conectar()) {
            // Solo traigo clientes cuyo usuario asociado esté activo
            String sql = "SELECT c.id_cliente, c.DNI, c.nombre, c.apellido, c.email " +
                    "FROM Cliente c JOIN Usuario u ON c.id_usuario = u.id_usuario " +
                    "WHERE u.activa = TRUE";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                            rs.getInt("id_cliente"),
                            rs.getString("DNI"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("email")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar clientes: " + ex.getMessage());
        }
    }
}
