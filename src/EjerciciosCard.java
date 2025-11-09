/*
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class EjerciciosCard {
    private JTable tabla;
    private JPanel panel;

    private JLabel imagenLabel;
    private DefaultTableModel modelo;
    private volatile String urlImagenDeseada = "";

    public EjerciciosCard() {
        construirUI();
        configurarTabla();
        configurarEventos();

        // Cargar la tabla desde la API al iniciar
        cargarTablaDesdeApi();

        aplicarTema();
    }


    private void construirUI() {
        panel = new JPanel(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel titulo = new JLabel("Catálogo de Ejercicios (desde API)");
        top.add(titulo);

        tabla = new JTable();
        tabla.setModel(new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        });
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(tabla);

        imagenLabel = new JLabel("Seleccione un ejercicio", SwingConstants.CENTER);
        imagenLabel.setPreferredSize(new Dimension(260,260));
        imagenLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(imagenLabel, BorderLayout.EAST);
    }

    private void configurarTabla() {
        modelo = (DefaultTableModel) tabla.getModel();
        // Columnas simples:
        modelo.addColumn("Nombre");
        modelo.addColumn("Descripción");
        modelo.addColumn("URL Imagen");

        // Ocultar columna de URL
        tabla.getColumnModel().getColumn(2).setMinWidth(0);
        tabla.getColumnModel().getColumn(2).setMaxWidth(0);
        tabla.getColumnModel().getColumn(2).setWidth(0);
    }

    private void aplicarTema() {
        try {
            Tema.aplicarPanel(panel);
            Tema.aplicarEtiqueta(imagenLabel);
        } catch (Throwable ignore) { */
/* Ignorar *//*
 }
    }

    private void configurarEventos() {
        // Evento de selección de fila (para la imagen)
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int filaSeleccionada = tabla.getSelectedRow();

            if (filaSeleccionada == -1) {
                urlImagenDeseada = "";
                imagenLabel.setIcon(null);
                imagenLabel.setText("Seleccione un ejercicio");
                return;
            }

            int filaModelo = tabla.convertRowIndexToModel(filaSeleccionada);

            // Columna 2 es "URL Imagen"
            Object valorUrl = modelo.getValueAt(filaModelo, 2);
            String url = (valorUrl == null) ? null : valorUrl.toString();
            mostrarImagen(url);
        });
    }


    private void cargarTablaDesdeApi() {

        // Ponemos un 'cargando' en la tabla
        modelo.setRowCount(0);
        modelo.addRow(new Object[]{"Cargando ejercicios desde la API...", "", ""});

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> filas = new ArrayList<>();

                //Trae la lista de 20 ejercicios
                JSONObject page = WgerService.fetchExercisesPage(20, 0);
                JSONArray results = page.getJSONArray("results");

                // Por cada ejercicio, traer su imagen
                for (int i=0; i < results.length(); i++) {
                    JSONObject ex = results.getJSONObject(i);
                    String nombre = ex.optString("name", "Sin nombre");
                    String desc = WgerService.stripHtml(ex.optString("description", ""));
                    int idApi = ex.getInt("id");

                    // 3. Llamada individual a la API por CADA imagen
                    String urlImg = WgerService.fetchMainImageUrl(idApi);

                    filas.add(new Object[]{nombre, desc, urlImg});
                }
                return filas;
            }

            @Override
            protected void done() {
                modelo.setRowCount(0);
                try {
                    List<Object[]> filas = get();
                    for (Object[] fila : filas) {
                        modelo.addRow(fila);
                    }
                } catch (Exception e) {
                    // Si falla (ej. no hay internet), mostrar error
                    modelo.addRow(new Object[]{"Error al cargar de API: " + e.getMessage(), "", ""});
                }
            }
        };
        worker.execute();
    }


    private void mostrarImagen(String url) {

        //  Validar la URL
        if (url == null || url.isEmpty() || url.equals("null")) {
            urlImagenDeseada = "";
            imagenLabel.setIcon(null);
            imagenLabel.setText("Sin imagen");
            return;
        }

        // Si es la misma imagen, no hacer nada
        if (url.equals(urlImagenDeseada)) {
            return;
        }

        urlImagenDeseada = url;

        imagenLabel.setIcon(null);
        imagenLabel.setText("Cargando imagen...");

        //SwingWorker para cargar la imagen en segundo plano
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {

            @Override
            protected ImageIcon doInBackground() throws Exception {
                // Esta es la URL que este hilo específico está cargando
                String urlHilo = urlImagenDeseada;

                try {
                    // Verificar si la URL deseada cambió mientras estábamos trabajando
                    if (!urlHilo.equals(urlImagenDeseada)) {
                        return null;
                    }

                    URL imageUrl = new URL(urlHilo);
                    HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Finge ser un navegador

                    BufferedImage img;
                    try (java.io.InputStream in = conn.getInputStream()) {
                        img = ImageIO.read(in);
                    }

                    if (img == null) {
                        throw new Exception("ImageIO.read devolvió null (formato no soportado?)");
                    }

                    // Verificar otra vez
                    if (!urlHilo.equals(urlImagenDeseada)) {
                        return null;
                    }

                    Image scaled = img.getScaledInstance(imagenLabel.getWidth(), imagenLabel.getHeight(), Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);

                } catch (Exception e) {
                    // Si falla la descarga, relanzar la excepción
                    throw new Exception("Error al leer URL: " + urlHilo, e);
                }
            }

            @Override
            protected void done() {
                String urlHilo = urlImagenDeseada;

                try {
                    ImageIcon icon = get();

                    // Mostrar solo si la imagen que terminamos de cargar
                    // sigue siendo la que el usuario quiere ver
                    if (icon != null && url.equals(urlHilo)) {
                        imagenLabel.setIcon(icon);
                        imagenLabel.setText(null);
                    }
                } catch (Exception e) {
                    if (url.equals(urlHilo)) {
                        imagenLabel.setIcon(null);
                        imagenLabel.setText("Error al cargar");
                    }
                }
            }
        };
        worker.execute();
    }

    public JPanel getPanel() {
        return panel;
    }
}*/
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EjerciciosCard {
    private JTable tabla;
    private JPanel panel;
    private JLabel imagenLabel;
    private DefaultTableModel modelo;
    private volatile String urlImagenDeseada = "";

    public EjerciciosCard() {
        construirUI();
        configurarTabla();
        configurarEventos();
        cargarTablaDesdeApi();
    }

    // Construye la interfaz
    private void construirUI() {
        panel = new JPanel(new BorderLayout(10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titulo = new JLabel("Catálogo de Ejercicios (DEBUG)");
        top.add(titulo);

        tabla = new JTable();
        tabla.setModel(new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        JScrollPane sp = new JScrollPane(tabla);
        imagenLabel = new JLabel("Seleccione un ejercicio", SwingConstants.CENTER);
        imagenLabel.setPreferredSize(new Dimension(260, 260));
        imagenLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        panel.add(top, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(imagenLabel, BorderLayout.EAST);
    }

    // Configura columnas del modelo
    private void configurarTabla() {
        modelo = (DefaultTableModel) tabla.getModel();
        modelo.addColumn("Nombre");
        modelo.addColumn("Descripción");
        modelo.addColumn("URL Imagen");

        // Ocultar la columna de URL (para no mostrar texto feo)
        if (tabla.getColumnCount() >= 3) {
            tabla.getColumnModel().getColumn(2).setMinWidth(0);
            tabla.getColumnModel().getColumn(2).setMaxWidth(0);
            tabla.getColumnModel().getColumn(2).setWidth(0);
        }

        System.out.println("✅ Tabla configurada. Columnas: " + modelo.getColumnCount());
    }

    // Listener de selección de fila
    private void configurarEventos() {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada == -1) {
                imagenLabel.setIcon(null);
                imagenLabel.setText("Seleccione un ejercicio");
                return;
            }
            String url = modelo.getValueAt(filaSeleccionada, 2).toString();
            System.out.println("🖼️ Mostrando imagen: " + url);
            mostrarImagen(url);
        });
    }

    // Carga los datos desde la API
    private void cargarTablaDesdeApi() {
        modelo.setRowCount(0);
        modelo.addRow(new Object[]{"Cargando ejercicios...", "", ""});

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                System.out.println("🌐 Llamando a la API...");
                List<Object[]> filas = new ArrayList<>();

                JSONObject page = WgerService.fetchExercisesPage(20, 0); // 10 primeros ejercicios
                JSONArray results = page.getJSONArray("results");
                System.out.println("📦 Recibidos " + results.length() + " ejercicios.");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject ex = results.getJSONObject(i);
                    String nombre = ex.optString("name", "Sin nombre");
                    String desc = WgerService.stripHtml(ex.optString("description", ""));
                    int idApi = ex.getInt("id");

                    // Buscar imagen
                    String urlImg = WgerService.fetchMainImageUrl(idApi);
                    System.out.println("🧩 " + nombre + " -> " + urlImg);

                    filas.add(new Object[]{nombre, desc, urlImg});
                }
                return filas;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> filas = get();
                    modelo.setRowCount(0);

                    if (filas.isEmpty()) {
                        modelo.addRow(new Object[]{"Sin resultados", "", ""});
                        System.out.println("⚠️ No se recibieron ejercicios.");
                    } else {
                        for (Object[] fila : filas) {
                            modelo.addRow(fila);
                        }
                        System.out.println("✅ Tabla actualizada con " + filas.size() + " filas.");
                    }

                    tabla.revalidate();
                    tabla.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    modelo.setRowCount(0);
                    modelo.addRow(new Object[]{"Error: " + e.getMessage(), "", ""});
                }
            }
        };
        worker.execute();
    }

    // Muestra una imagen en el label
    private void mostrarImagen(String url) {
        if (url == null || url.isEmpty() || url.equals("null")) {
            imagenLabel.setText("Sin imagen");
            imagenLabel.setIcon(null);
            return;
        }

        imagenLabel.setText("Cargando imagen...");
        imagenLabel.setIcon(null);

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                try (var in = conn.getInputStream()) {
                    BufferedImage img = ImageIO.read(in);
                    if (img == null) throw new Exception("Formato no soportado");
                    Image scaled = img.getScaledInstance(
                            imagenLabel.getWidth(),
                            imagenLabel.getHeight(),
                            Image.SCALE_SMOOTH
                    );
                    return new ImageIcon(scaled);
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    imagenLabel.setIcon(icon);
                    imagenLabel.setText(null);
                    System.out.println("✅ Imagen mostrada correctamente.");
                } catch (Exception e) {
                    imagenLabel.setText("Error al cargar");
                    System.err.println("❌ Error cargando imagen: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public JPanel getPanel() {
        return panel;
    }

    // Ejemplo de ejecución directa
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EjerciciosCard (Debug)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new EjerciciosCard().getPanel());
            frame.setSize(900, 500);
            frame.setVisible(true);
        });
    }
}
