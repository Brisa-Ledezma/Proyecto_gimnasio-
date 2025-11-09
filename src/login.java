import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class login {
    private JLabel usuario;
    private JTextField txtUsuario;
    private JLabel Contraseña;
    private JPasswordField txtpassword;
    private JButton ingresarButton;
    private JRadioButton clienteRadioButton;
    private JRadioButton profesorRadioButton;
    private JRadioButton administradorRadioButton;
    private JButton registrarseButton;

    // Agrupo los radios para que solo se pueda seleccionar uno a la vez
    private ButtonGroup grupoRoles;

    public login() {
        // Crea el grupo y agrega los radio buttons para que sean mutuamente excluyentes
        grupoRoles = new ButtonGroup();
        grupoRoles.add(clienteRadioButton);
        grupoRoles.add(profesorRadioButton);
        grupoRoles.add(administradorRadioButton);

        aplicarColores();

        // valida los campos y ejecuta la consulta SQL
        ingresarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();

            }
        });

        // abre la ventana de registro y cierra el login
        registrarseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // crea e inicia la ventana de registro
                Registro reg = new Registro();
                reg.mostrarRegistro();
                // cierra la ventana actual de login
                JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(ingresarButton);
                if (ventana != null) {
                    ventana.dispose();
                }
            }
        });
    }

    private void aplicarColores() {
        // Etiquetas
        Tema.aplicarEtiqueta(usuario);
        Tema.aplicarEtiqueta(Contraseña);
        // Campos de texto
        Tema.aplicarCampoTexto(txtUsuario);
        Tema.aplicarCampoTexto(txtpassword);
        // Botones
        Tema.aplicarBoton(ingresarButton);
        Tema.aplicarBoton(registrarseButton);
        // Radio buttons
        if (clienteRadioButton != null) {
            clienteRadioButton.setBackground(Tema.COLOR_BACKGROUND);
            clienteRadioButton.setForeground(Tema.COLOR_TEXT);
        }
        if (profesorRadioButton != null) {
            profesorRadioButton.setBackground(Tema.COLOR_BACKGROUND);
            profesorRadioButton.setForeground(Tema.COLOR_TEXT);
        }
        if (administradorRadioButton != null) {
            administradorRadioButton.setBackground(Tema.COLOR_BACKGROUND);
            administradorRadioButton.setForeground(Tema.COLOR_TEXT);
        }
    }


    private void realizarLogin() {
        String usu = txtUsuario.getText().trim();
        String pass = new String(txtpassword.getPassword()).trim();

        // Verifica que se hayan completado usuario, contraseña y seleccionado un rol
        if (usu.isEmpty() || pass.isEmpty() || (!clienteRadioButton.isSelected() && !profesorRadioButton.isSelected() && !administradorRadioButton.isSelected())) {
            JOptionPane.showMessageDialog(null, "Debe completar usuario, contraseña y seleccionar un rol.");
            return;
        }

        // Determina el rol esperado según el radio seleccionado
        String rolEsperado;
        if (clienteRadioButton.isSelected()) {
            rolEsperado = "USER";
        } else if (profesorRadioButton.isSelected()) {
            rolEsperado = "PROFESOR";
        } else {
            rolEsperado = "ADMINISTRADOR";
        }

        // Consulta en la base de datos
        try (Connection con = conexion.conectar()) {
            String sql = "SELECT rol, activa FROM Usuario WHERE nom_usuario=? AND contrasenia=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usu);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        boolean activa = rs.getBoolean("activa");
                        String rolBD = rs.getString("rol");
                        if (!activa) {
                            JOptionPane.showMessageDialog(null, "El usuario está inactivo.");
                            return;
                        }
                        if (!rolBD.equals(rolEsperado)) {
                            JOptionPane.showMessageDialog(null, "El rol seleccionado no coincide con el usuario.");
                            return;
                        }
                        // Inicio de sesión correcto
                        JOptionPane.showMessageDialog(null, "Inicio de sesión exitoso.");
                        // Abrir menú principal
                        MenuPrincipal menu = new MenuPrincipal();
                        menu.mostrarmenu();
                        // Cerrar la ventana de login
                        JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(ingresarButton);
                        if (ventana != null) {
                            ventana.dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.");
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos: " + ex.getMessage());
        }
    }

    public void mostrarLogin() {
        JFrame frame = new JFrame("Iniciar sesión");
        frame.setContentPane((JPanel) txtUsuario.getParent());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
