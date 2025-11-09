import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

public class MenuPrincipal {
    private JPanel BorderLayout;
    private JPanel panelBarra;
    private JButton volverButton;
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
    private JButton cancelarButton;
    private JPanel cardPago;
    private JPanel panelPago;
    private JButton registrarPagoButton;
    private JButton modificarPagoButton;
    private JTable tablePagos;
    private JComboBox comboCliente;
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
    private JPanel principalPanel;
    private JFormattedTextField fechaPagoTxt;
    private JButton cancelarButton1;
    private JLabel diasMembresiaLabel;
    private JPanel panelProductos;
    private JTable table2;
    private JButton agregarAlCarritoButton;
    private JButton verCarritoButton;
    private JPanel panelRutinas;
    private JTable table3;
    private JTextField textField4;
    private JComboBox comboEjercicio;
    private JTextField textField3;
    private JTextField textField5;
    private JButton agregarRutinaButton;
    private JButton modificarRutinaButton;
    private JButton desactivarRutinaButton;
    private JPanel cardRegistroProgreso;
    private JPanel panelRegistro;
    private JTable table4;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField6;
    private JTextField textField7;
    private JButton registrarProgresoButton;
    private JButton modificarProgresoButton;
    private JButton eliminarProgresoButton;
    private JButton cancelarButton3;
    private JTable table5;
    private JComboBox comboClienteRutina;
    private JComboBox comboProfesorRutina;
    private JTextField nombreRutina;
    private JCheckBox rutinaActivaCheckBox;
    private JTextField fechaRutina;
    private JButton agregarEjercicioButton1;
    private JButton sacarEjercicioButton;
    String valorusado;
    private ProductosCard productosCard;
    private CardLayout despliegue;

    public MenuPrincipal() {
        despliegue = new CardLayout();

        // Inicializo Clientes
        ClientesCard clientesCard = new ClientesCard(
                table1, dniClienteTxt, nombreClientetxt, apellidoClienteTxt, emailTxt,
                agregarSocioButton, modificarSocioButton, eliminarSocioButton, cancelarButton
        );
        clientesCard.inicializar();

        // Inicializo Profesores
        ProfesoresCard profesoresCard = new ProfesoresCard(
                tablaProfesor, nombreProfesorTxt, apellidoProfesorTxt, especialidadTxt,
                botonAgregar, botonModificar, eliminarBoton, cancelarBoton
        );
        profesoresCard.inicializar();

        // Inicializo Pagos incluyendo el botón cancelar de la sección de pagos
        PagosCard pagosCard = new PagosCard(
                tablePagos, comboCliente, vencimientoPagotxt, montotxt,
                estadoCombo,fechaPagoTxt, registrarPagoButton, modificarPagoButton, cancelarButton1
        );
        pagosCard.inicializar();

        productosCard = new ProductosCard(
                table2, panelProductos, agregarAlCarritoButton, verCarritoButton
        );
        productosCard.inicializar();
        productosCard = new ProductosCard(
                table2, panelProductos, agregarAlCarritoButton, verCarritoButton
        );
        productosCard.inicializar();

        RutinasCard rutinasCard = new RutinasCard(
                cardRutinas, panelRutinas, table3, table5,
                comboClienteRutina, comboProfesorRutina,
                rutinaActivaCheckBox, nombreRutina, fechaRutina,
                comboEjercicio, textField4, textField3, textField5,
                agregarRutinaButton, modificarRutinaButton, desactivarRutinaButton,
                agregarEjercicioButton1, sacarEjercicioButton
        );
        rutinasCard.inicializar();


        // cardEjercicios es el JPanel vacío del CardLayout en MenuPrincipal
        cardEjercicios.setLayout(new BorderLayout()); // Aseguramos un layout

        //Creamos la instancia de EjerciciosCard
        EjerciciosCard ejerciciosCard = new EjerciciosCard();

        // Obtenemos su panel y lo agregamos a nuestro placeholder
        cardEjercicios.add(ejerciciosCard.getPanel(), java.awt.BorderLayout.CENTER);

        // Aplicamos el tema a los paneles principales
        Tema.aplicarPanel(principalPanel);
        Tema.aplicarPanel(panelContenido);
        if (panelSocios != null) Tema.aplicarPanel(panelSocios);
        if (panelProfesor != null) Tema.aplicarPanel(panelProfesor);
        if (panelPago != null) Tema.aplicarPanel(panelPago);

        // Aplicamos color y selección a la lista
        Tema.aplicarLista(menuList);
        Tema.aplicarCampoTexto(fechaPagoTxt);
        panelContenido.setLayout(despliegue);
        panelContenido.add(cardClientes, "Clientes");
        panelContenido.add(cardProfesor, "Profesor");
        panelContenido.add(cardPago, "Pagos");
        panelContenido.add(cardProductos, "Productos");
        panelContenido.add(cardEjercicios, "Ejercicios");
        panelContenido.add(cardRutinas, "Rutinas");
        panelContenido.add(cardRegistroProgreso, "Registros de Progreso");

        // Manejo del cambio de tarjetas según la selección de la lista
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String valorUsado = String.valueOf(menuList.getSelectedValue());
                despliegue.show(panelContenido, valorUsado);
            }
        });

        volverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login loginVentana = new login();
                loginVentana.mostrarLogin();
                cerrarventana(principalPanel);
            }
        });
    }

    private void cerrarventana(JPanel panel) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        frame.dispose();
    }


    public void mostrarmenu() {
        JFrame frame = new JFrame("Principal");
        frame.setContentPane(principalPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        // Se maximiza la ventana para que la interfaz se adapte al tamaño de pantalla
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Centra la ventana
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        frame.setVisible(true);
    }
}
