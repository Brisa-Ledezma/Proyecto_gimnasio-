import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuPrincipal {
    private JPanel BorderLayout;
    private JPanel panelBarra;
    private JButton volverButton;
    private JLabel dni;
    private JTextField dniTxt;
    private JButton buscarButton;
    private JButton cobrarButton;
    private JButton nuevaVentaButton;
    private JButton cerrarSesionButton;
    private JScrollPane barraPanel;
    private JList menuList;
    private JPanel panelContenido;
    private JPanel cardClientes;
    private JPanel panelSocios;
    private JTable table1;
    private JButton agregarSocioButton;
    private JButton modificarSocioButton;
    private JButton eliminarSocioButton;
    private JTextField dniClienteTxt;
    private JTextField nombreClientetxt;
    private JTextField apellidoClienteTxt;
    private JTextField emailTxt;
    private JButton guardarButton;
    private JButton cancelarButton;
    private JPanel cardPago;
    private JPanel panelPago;
    private JButton registrarPagoButton;
    private JButton modificarPagoButton;
    private JButton eliminarPagoButton;
    private JTable tablePagos;
    private JComboBox comboCliente;
    private JTextField fechaPagotxt;
    private JTextField vencimientoPagotxt;
    private JTextField montotxt;
    private JComboBox estadoCombo;
    private JPanel cardProductos;
    private JPanel cardEjercicios;
    private JPanel cardRutinas;
    private JPanel cardRegistrosDeProgreso;
    private JPanel cardProfesor;
    private JPanel panelProfesor;
    private JTable tablaProfesor;
    private JButton botonModificar;
    private JButton eliminarBoton;
    private JButton botonAgregar;
    private JTextField nombreProfesorTxt;
    private JTextField apellidoProfesorTxt;
    private JTextField especialidadTxt;
    private JButton cancelarBoton;
    private JButton guardarBoton;
    String valorusado;

    public MenuPrincipal() {
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String valorUsado = String.valueOf(menuList.getSelectedValue());
                System.out.println("Seleccionaste: " + valorUsado);

                switch (valorUsado){
                    case "Profesor":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Pagos":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Productos":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Ejercicios":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Rutinas":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Registros de Progreso":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;
                    case "Clientes":
                        agregarSocioButton.setText("Agregar "+valorUsado);
                        break;

                }
            }
        });

    }
        public void mostrarmenu(){
        JFrame frame = new JFrame("Principal");
        frame.setContentPane(BorderLayout);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        frame.setVisible(true);
    }
}
