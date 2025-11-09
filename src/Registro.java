import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Registro {

    private JTextField textField1;
    private JTextField textField2;
    private JCheckBox clienteCheckBox;
    private JCheckBox profesorCheckBox;
    private JButton registrarseButton;
    private JButton volverButton;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;

    public Registro() {
        //  check de cliente: deselecciona el otro y actualiza los campos
        clienteCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clienteCheckBox.isSelected()) {
                    profesorCheckBox.setSelected(false);
                }
                actualizarCampos();
            }
        });
        // check de profesor: deselecciona el otro y actualiza los campos
        profesorCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (profesorCheckBox.isSelected()) {
                    clienteCheckBox.setSelected(false);
                }
                actualizarCampos();
            }
        });

        // Acción del botón Registrarse
        registrarseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });

        // Volver
        volverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login loginVentana = new login();
                loginVentana.mostrarLogin();
                // cerrar la ventana de registro
                JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(registrarseButton);
                if (ventana != null) {
                    ventana.dispose();
                }
            }
        });

        // Aplico paleta de colores
        aplicarColores();
        // Deshabilitar campos específicos hasta que se seleccione un rol
        actualizarCampos();
    }


    private void aplicarColores() {
        // Campos de texto comunes
        Tema.aplicarCampoTexto(textField1);
        Tema.aplicarCampoTexto(textField2);
        // Datos de cliente
        Tema.aplicarCampoTexto(textField3);
        Tema.aplicarCampoTexto(textField4);
        Tema.aplicarCampoTexto(textField5);
        Tema.aplicarCampoTexto(textField6);
        // Datos de profesor
        Tema.aplicarCampoTexto(textField7);
        Tema.aplicarCampoTexto(textField8);
        Tema.aplicarCampoTexto(textField9);
        // Botones
        Tema.aplicarBoton(registrarseButton);
        Tema.aplicarBoton(volverButton);
        // Checkboxes
        if (clienteCheckBox != null) {
            clienteCheckBox.setBackground(Tema.COLOR_BACKGROUND);
            clienteCheckBox.setForeground(Tema.COLOR_TEXT);
        }
        if (profesorCheckBox != null) {
            profesorCheckBox.setBackground(Tema.COLOR_BACKGROUND);
            profesorCheckBox.setForeground(Tema.COLOR_TEXT);
        }
    }

    // Habilita o deshabilita los campos de datos dependiendo de qué rol esté seleccionado.
    private void actualizarCampos() {
        boolean esCliente = clienteCheckBox.isSelected();
        boolean esProfesor = profesorCheckBox.isSelected();
        // Habilitar campos de cliente
        textField3.setEnabled(esCliente);
        textField4.setEnabled(esCliente);
        textField5.setEnabled(esCliente);
        textField6.setEnabled(esCliente);
        // Si se deshabilitan, limpiar su contenido
        if (!esCliente) {
            textField3.setText("");
            textField4.setText("");
            textField5.setText("");
            textField6.setText("");
        }
        // Habilitar campos de profesor
        textField7.setEnabled(esProfesor);
        textField8.setEnabled(esProfesor);
        textField9.setEnabled(esProfesor);
        // Limpiar si no se selecciona
        if (!esProfesor) {
            textField7.setText("");
            textField8.setText("");
            textField9.setText("");
        }
    }


     //creación del usuario y del cliente o profesor según corresponda.
    private void registrarUsuario() {
        String usuario = textField1.getText().trim();
        String contrasenia = textField2.getText().trim();

        // Valida que se haya ingresado usuario y contraseña y seleccionado un rol
        if (usuario.isEmpty() || contrasenia.isEmpty() || (!clienteCheckBox.isSelected() && !profesorCheckBox.isSelected())) {
            JOptionPane.showMessageDialog(null, "Debe completar usuario, contraseña y seleccionar el rol Cliente o Profesor.");
            return;
        }

        // Valida que el nombre de usuario no exista
        try (Connection con = conexion.conectar()) {
            String queryUser = "SELECT id_usuario FROM Usuario WHERE nom_usuario = ?";
            try (PreparedStatement psu = con.prepareStatement(queryUser)) {
                psu.setString(1, usuario);
                try (ResultSet rsu = psu.executeQuery()) {
                    if (rsu.next()) {
                        JOptionPane.showMessageDialog(null, "El nombre de usuario ya está en uso. Elija otro.");
                        return;
                    }
                }
            }

            // Registro para cliente
            if (clienteCheckBox.isSelected()) {
                String nombre = textField3.getText().trim();
                String apellido = textField4.getText().trim();
                String dni = textField5.getText().trim();
                String email = textField6.getText().trim();
                // Validaciones de campos cliente
                if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nombre, Apellido y DNI son obligatorios para clientes.");
                    return;
                }
                if (!dni.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "El DNI solo debe contener números.");
                    return;
                }
                if (dni.length() < 7 || dni.length() > 10) {
                    JOptionPane.showMessageDialog(null, "El DNI debe tener entre 7 y 10 dígitos.");
                    return;
                }
                // Insertar usuario y cliente en la misma conexión
                con.setAutoCommit(false);
                try {
                    // Inserto en Usuario
                    String insUsu = "INSERT INTO Usuario (nom_usuario, contrasenia, rol, activa) VALUES (?, ?, 'USER', TRUE)";
                    int idUsuario;
                    try (PreparedStatement ps = con.prepareStatement(insUsu, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, usuario);
                        ps.setString(2, contrasenia);
                        ps.executeUpdate();
                        try (ResultSet gen = ps.getGeneratedKeys()) {
                            idUsuario = gen.next() ? gen.getInt(1) : -1;
                        }
                    }
                    // Inserto en Cliente
                    String insCli = "INSERT INTO Cliente (id_usuario, nombre, apellido, DNI, email) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psCli = con.prepareStatement(insCli)) {
                        psCli.setInt(1, idUsuario);
                        psCli.setString(2, nombre);
                        psCli.setString(3, apellido);
                        psCli.setString(4, dni);
                        if (email.isEmpty()) {
                            psCli.setNull(5, java.sql.Types.VARCHAR);
                        } else {
                            psCli.setString(5, email);
                        }
                        psCli.executeUpdate();
                    }
                    con.commit();
                    JOptionPane.showMessageDialog(null, "Cliente registrado correctamente.");
                    // Al finalizar, puede volver al login automáticamente
                    login loginVentana = new login();
                    loginVentana.mostrarLogin();
                    JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(registrarseButton);
                    if (ventana != null) {
                        ventana.dispose();
                    }
                } catch (SQLException ex) {
                    con.rollback();
                    JOptionPane.showMessageDialog(null, "Error al registrar cliente: " + ex.getMessage());
                } finally {
                    con.setAutoCommit(true);
                }
            } else {
                // Registro para profesor
                String nombreP = textField7.getText().trim();
                String apellidoP = textField8.getText().trim();
                String especialidad = textField9.getText().trim();
                if (nombreP.isEmpty() || apellidoP.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nombre y Apellido son obligatorios para profesores.");
                    return;
                }
                con.setAutoCommit(false);
                try {
                    // Inserto en Usuario con rol PROFESOR
                    String insUsu = "INSERT INTO Usuario (nom_usuario, contrasenia, rol, activa) VALUES (?, ?, 'PROFESOR', TRUE)";
                    int idUsuario;
                    try (PreparedStatement ps = con.prepareStatement(insUsu, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, usuario);
                        ps.setString(2, contrasenia);
                        ps.executeUpdate();
                        try (ResultSet gen = ps.getGeneratedKeys()) {
                            idUsuario = gen.next() ? gen.getInt(1) : -1;
                        }
                    }
                    // Insertar en Profesor
                    String insProf = "INSERT INTO Profesor (id_usuario, nombre, apellido, especialidad) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psProf = con.prepareStatement(insProf)) {
                        psProf.setInt(1, idUsuario);
                        psProf.setString(2, nombreP);
                        psProf.setString(3, apellidoP);
                        if (especialidad.isEmpty()) {
                            psProf.setNull(4, java.sql.Types.VARCHAR);
                        } else {
                            psProf.setString(4, especialidad);
                        }
                        psProf.executeUpdate();
                    }
                    con.commit();
                    JOptionPane.showMessageDialog(null, "Profesor registrado correctamente.");
                    // Volver al login
                    login loginVentana = new login();
                    loginVentana.mostrarLogin();
                    JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(registrarseButton);
                    if (ventana != null) {
                        ventana.dispose();
                    }
                } catch (SQLException ex) {
                    con.rollback();
                    JOptionPane.showMessageDialog(null, "Error al registrar profesor: " + ex.getMessage());
                } finally {
                    con.setAutoCommit(true);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al acceder a la base de datos: " + ex.getMessage());
        }
    }

    public void mostrarRegistro() {
        JFrame frame = new JFrame("Registro de usuario");
        frame.setContentPane((JPanel) textField1.getParent());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
