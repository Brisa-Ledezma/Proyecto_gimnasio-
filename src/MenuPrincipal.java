import javax.swing.*;
import javax.swing.JSplitPane;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private JPanel principalPanel;
    String valorusado;
    private CardLayout despliegue;


    private JButton menuButton;
    private JSplitPane splitPane;
    private boolean menuCollapsed = false;
    private ClientesPanel clientesPanel;
    private PagosPanel pagosPanel;

    public MenuPrincipal() {
        despliegue = new CardLayout();
        panelContenido.setLayout(despliegue);

        panelContenido.add(cardClientes, "Clientes");
        panelContenido.add(cardProfesor, "Profesor");
        panelContenido.add(cardPago, "Pagos");
        panelContenido.add(cardProductos, "Productos");
        panelContenido.add(cardEjercicios, "Ejercicios");
        panelContenido.add(cardRutinas, "Rutinas");
        panelContenido.add(cardRegistrosDeProgreso, "Registros de Progreso");


        clientesPanel = new ClientesPanel();
        pagosPanel = new PagosPanel();

        panelContenido.remove(cardClientes);
        panelContenido.remove(cardPago);
        panelContenido.add(clientesPanel.getPanel(), "Clientes");
        panelContenido.add(pagosPanel.getPanel(), "Pagos");


        if (splitPane != null) {
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerSize(8);
            // set initial width for sidebar
            splitPane.setDividerLocation(220);
        }

        if (menuButton != null) {
            menuButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (splitPane != null) {
                        if (menuCollapsed) {
                            splitPane.setDividerLocation(220);
                        } else {
                            splitPane.setDividerLocation(0);
                        }
                        menuCollapsed = !menuCollapsed;
                    }
                }
            });
        }


        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String valorUsado = String.valueOf(menuList.getSelectedValue());
                //System.out.println("estoy en: " + valorUsado);
                despliegue.show(panelContenido, valorUsado);
            }
        });
        volverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarventana(principalPanel);
            }
        });

    }

    //Creo el metodo para poder cerrar una ventana, utilizando parametros del Java Swing UI
    private void cerrarventana(JPanel panel) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        frame.dispose();
    }
    //Creo el metodo para mostrar una venta, utilizando parametros del Java Swing UI
        public void mostrarmenu(){
        JFrame frame = new JFrame("Principal");
        frame.setContentPane(principalPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        frame.setVisible(true);
    }

}
