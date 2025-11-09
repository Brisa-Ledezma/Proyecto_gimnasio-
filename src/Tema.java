import javax.swing.*;
import java.awt.*;


public class Tema {

    // Colores base
    public static final Color COLOR_TEXT = Color.decode("#101816");       // texto
    public static final Color COLOR_BACKGROUND = Color.decode("#edf3f1"); // fondo
    public static final Color COLOR_PRIMARY = Color.decode("#334d45");    // botones
    public static final Color COLOR_SECONDARY = Color.decode("#9ab1bc");  // secundario
    public static final Color COLOR_ACCENT = Color.decode("#607b90");     // acento

    // Colores derivados
    public static final Color COLOR_TEXTFIELD = Color.decode("#ccd8dd");  // campos de texto
    public static final Color COLOR_LIST_SELECTION = Color.decode("#cfd7dd"); // selección en listas

    //fondo del panel
    public static void aplicarPanel(JPanel panel) {
        panel.setBackground(COLOR_BACKGROUND);
    }

    //boton
    public static void aplicarBoton(JButton boton) {
        boton.setBackground(COLOR_PRIMARY);
        boton.setForeground(COLOR_BACKGROUND);
        boton.setFocusPainted(false);
    }

    //campo de texto
    public static void aplicarCampoTexto(JTextField campo) {
        campo.setBackground(COLOR_TEXTFIELD);
        campo.setForeground(COLOR_TEXT);
        campo.setCaretColor(COLOR_TEXT);
    }

    //label
    public static void aplicarEtiqueta(JLabel etiqueta) {
        etiqueta.setForeground(COLOR_TEXT);
    }

    //JList
    public static void aplicarLista(JList<?> lista) {
        lista.setBackground(COLOR_BACKGROUND);
        lista.setForeground(COLOR_TEXT);
        lista.setSelectionBackground(COLOR_LIST_SELECTION);
        lista.setSelectionForeground(COLOR_TEXT);
    }
    // ======= Tablas =======
    public static void aplicarTabla(JTable t) {
        if (t == null) return;

        // Header con primario
        if (t.getTableHeader() != null) {
            t.getTableHeader().setBackground(COLOR_PRIMARY);
            t.getTableHeader().setForeground(Color.WHITE);
        }

        // Filas: fondo un poco más claro que el colorFondo para contraste
        t.setBackground(COLOR_BACKGROUND.brighter());
        t.setForeground(Color.BLACK);                 // texto oscuro para leer bien
        t.setSelectionBackground(COLOR_SECONDARY);    // tu secundario para selección
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(COLOR_BACKGROUND.darker());
        t.setRowHeight(22);
        t.setFillsViewportHeight(true);
    }

}