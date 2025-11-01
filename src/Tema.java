import javax.swing.*;
import java.awt.*;


public class Tema {
    public static final Color COLOR_A = Color.decode("#818D92");
    public static final Color COLOR_B = Color.decode("#586A6A");
    public static final Color COLOR_C = Color.decode("#B9A394");
    public static final Color COLOR_D = Color.decode("#D4C5C7");
    public static final Color COLOR_E = Color.decode("#DAD4EF");

    //Aplica el color de fondo a un JPanel
    public static void aplicarPanel(JPanel panel) {
        panel.setBackground(COLOR_E);
    }

    // Aplica color a un botón: fondo oscuro y texto blanco
    public static void aplicarBoton(JButton boton) {
        boton.setBackground(COLOR_B);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
    }

    //Aplica color a un campo de texto
    public static void aplicarCampoTexto(JTextField campo) {
        campo.setBackground(COLOR_D);
        campo.setForeground(COLOR_B);
    }

    //Aplica color a una etiqueta
    public static void aplicarEtiqueta(JLabel etiqueta) {
        etiqueta.setForeground(COLOR_B);
    }
}