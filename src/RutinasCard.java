import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;


public class RutinasCard {

    private JTable table3; // Lista de rutinas (arriba)
    private JTable table5; // Detalle de la rutina (abajo)

    private JComboBox<ComboItem> comboClienteRutina;
    private JComboBox<ComboItem> comboProfesorRutina;
    private JCheckBox rutinaActivaCheckBox;
    private JTextField nombreRutina;
    private JTextField fechaRutina;

    private JComboBox<ComboItem> comboEjercicio;
    private JTextField textField4; // Series
    private JTextField textField3; // Repeticiones
    private JTextField textField5; // Peso recomendado

    private JButton agregarRutinaButton;
    private JButton modificarRutinaButton;
    private JButton desactivarRutinaButton;

    private JButton agregarEjercicioButton1;
    private JButton sacarEjercicioButton;

    private JPanel panelRutinas; // El panel principal de la card
    private JPanel cardRutinas;

    private DefaultTableModel modeloRutinas;
    private DefaultTableModel modeloDetalle;

    private Integer rutinaSeleccionadaId = null;

    // Acepta todos los componentes definidos en MenuPrincipal
    public RutinasCard(
            JPanel cardRutinas, JPanel panelRutinas, JTable table3, JTable table5,
            JComboBox<ComboItem> comboClienteRutina, JComboBox<ComboItem> comboProfesorRutina,
            JCheckBox rutinaActivaCheckBox, JTextField nombreRutina, JTextField fechaRutina,
            JComboBox<ComboItem> comboEjercicio, JTextField textField4, JTextField textField3,
            JTextField textField5, JButton agregarRutinaButton, JButton modificarRutinaButton,
            JButton desactivarRutinaButton, JButton agregarEjercicioButton1, JButton sacarEjercicioButton
    ) {
        //Asignamos los componentes recibidos a los campos
        this.cardRutinas = cardRutinas;
        this.panelRutinas = panelRutinas;
        this.table3 = table3;
        this.table5 = table5;
        this.comboClienteRutina = comboClienteRutina;
        this.comboProfesorRutina = comboProfesorRutina;
        this.rutinaActivaCheckBox = rutinaActivaCheckBox;
        this.nombreRutina = nombreRutina;
        this.fechaRutina = fechaRutina;
        this.comboEjercicio = comboEjercicio;
        this.textField4 = textField4; // Series
        this.textField3 = textField3; // Reps
        this.textField5 = textField5; // Peso
        this.agregarRutinaButton = agregarRutinaButton;
        this.modificarRutinaButton = modificarRutinaButton;
        this.desactivarRutinaButton = desactivarRutinaButton;
        this.agregarEjercicioButton1 = agregarEjercicioButton1;
        this.sacarEjercicioButton = sacarEjercicioButton;
    }


    public void inicializar() {
        construirModelos();
        configurarTablas();
        configurarEventos();

        cargarCombos();
        cargarRutinas();

        setCamposRutinaHabilitados(false);
        setCamposDetalleHabilitados(false);

        // Aplicamos el Tema
        try {
            // Usamos los paneles que nos pasó el constructor
            if (panelRutinas != null) Tema.aplicarPanel(panelRutinas);
            if (cardRutinas != null) Tema.aplicarPanel(cardRutinas);

            Tema.aplicarTabla(table3);
            Tema.aplicarTabla(table5);
            Tema.aplicarBoton(agregarRutinaButton);
            Tema.aplicarBoton(modificarRutinaButton);
            Tema.aplicarBoton(desactivarRutinaButton);
            Tema.aplicarBoton(agregarEjercicioButton1);
            Tema.aplicarBoton(sacarEjercicioButton);
        } catch (Throwable ignore) {}
    }


    // ====== Modelos/Tablas ======
    private void construirModelos() {
        modeloRutinas = new DefaultTableModel();
        modeloRutinas.addColumn("ID");
        modeloRutinas.addColumn("Cliente");
        modeloRutinas.addColumn("Profesor");
        modeloRutinas.addColumn("Nombre");
        modeloRutinas.addColumn("Fecha");
        modeloRutinas.addColumn("Activa");

        modeloDetalle = new DefaultTableModel();
        modeloDetalle.addColumn("ID fila");
        modeloDetalle.addColumn("ID ejercicio");
        modeloDetalle.addColumn("Ejercicio");
        modeloDetalle.addColumn("Series");
        modeloDetalle.addColumn("Reps");
        modeloDetalle.addColumn("Peso rec.");
    }

    private void configurarTablas() {
        table3.setModel(modeloRutinas);
        ocultarColumna(table3, 0); // ID rutina

        table5.setModel(modeloDetalle);
        ocultarColumna(table5, 0); // ID fila
        ocultarColumna(table5, 1); // ID ejercicio
    }

    private void ocultarColumna(JTable t, int idx) {
        t.getColumnModel().getColumn(idx).setMinWidth(0);
        t.getColumnModel().getColumn(idx).setMaxWidth(0);
        t.getColumnModel().getColumn(idx).setWidth(0);
    }

    // ====== Eventos ======
    private void configurarEventos() {
        // Selección de rutina (habilita edición + carga detalle)
        table3.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onSeleccionRutina();
        });

        // Rutina
        agregarRutinaButton.addActionListener(e -> agregarRutina());
        modificarRutinaButton.addActionListener(e -> modificarRutina());
        desactivarRutinaButton.addActionListener(e -> desactivarRutina());

        // Detalle (ejercicios dentro de la rutina)
        agregarEjercicioButton1.addActionListener(e -> agregarEjercicioARutina());
        sacarEjercicioButton.addActionListener(e -> desactivarFilaDetalle());
    }

    private void setCamposRutinaHabilitados(boolean on) {
        comboClienteRutina.setEnabled(on);
        comboProfesorRutina.setEnabled(on);
        nombreRutina.setEnabled(on);
        fechaRutina.setEnabled(on);
        rutinaActivaCheckBox.setEnabled(on);
        modificarRutinaButton.setEnabled(on);
        desactivarRutinaButton.setEnabled(on);
    }

    private void setCamposDetalleHabilitados(boolean on) {
        comboEjercicio.setEnabled(on);
        textField4.setEnabled(on); // series
        textField3.setEnabled(on); // reps
        textField5.setEnabled(on); // peso rec.
        agregarEjercicioButton1.setEnabled(on);
        sacarEjercicioButton.setEnabled(on);
    }

    // ====== Carga de datos ======
    private void cargarCombos() {
        // Clientes
        comboClienteRutina.removeAllItems();
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_cliente, CONCAT(nombre,' ',apellido) " +
                             "FROM Cliente ORDER BY nombre")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                comboClienteRutina.addItem(new ComboItem(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cargando clientes: " + ex.getMessage());
        }

        // Profesores
        comboProfesorRutina.removeAllItems();
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_profesor, CONCAT(nombre,' ',apellido) " +
                             "FROM Profesor ORDER BY nombre")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                comboProfesorRutina.addItem(new ComboItem(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cargando profesores: " + ex.getMessage());
        }

        // Ejercicios activos (para detalle)
        comboEjercicio.removeAllItems();
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_ejercicio, nombre FROM Ejercicio " +
                             "WHERE activa = TRUE ORDER BY nombre")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                comboEjercicio.addItem(new ComboItem(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cargando ejercicios: " + ex.getMessage());
        }
    }

    private void cargarRutinas() {
        modeloRutinas.setRowCount(0);
        String sql = "SELECT r.id_rutina, " +
                "CONCAT(c.nombre,' ',c.apellido) AS cliente, " +
                "CONCAT(p.nombre,' ',p.apellido) AS profesor, " +
                "r.nombre, r.fecha_creacion, r.activa " +
                "FROM Rutina r " +
                "JOIN Cliente c ON c.id_cliente = r.id_cliente " +
                "JOIN Profesor p ON p.id_profesor = r.id_profesor " +
                "ORDER BY r.id_rutina DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloRutinas.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDate(5), rs.getBoolean(6)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cargando rutinas: " + ex.getMessage());
        }
    }

    private void cargarDetalle(int idRutina) {
        modeloDetalle.setRowCount(0);
        String sql =
                "SELECT rpe.id_rutina_por_ejercicio, e.id_ejercicio, e.nombre, " +
                        "rpe.series, rpe.repeticiones, rpe.peso_recomendado " +
                        "FROM Rutina_por_Ejercicio rpe " +
                        "JOIN Ejercicio e ON e.id_ejercicio = rpe.id_ejercicio " +
                        "WHERE rpe.id_rutina = ? AND rpe.activo = TRUE AND e.activa = TRUE " +
                        "ORDER BY rpe.id_rutina_por_ejercicio";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRutina);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloDetalle.addRow(new Object[]{
                        rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getObject(4), rs.getObject(5), rs.getObject(6)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error cargando detalle: " + ex.getMessage());
        }
    }

    // ====== Interacción de UI ======
    private void onSeleccionRutina() {
        int row = table3.getSelectedRow();
        if (row == -1) {
            rutinaSeleccionadaId = null;
            setCamposRutinaHabilitados(false);
            setCamposDetalleHabilitados(false);
            modeloDetalle.setRowCount(0);
            limpiarCamposRutina();
            return;
        }

        rutinaSeleccionadaId = (Integer) modeloRutinas.getValueAt(row, 0);
        String cliTxt = (String)  modeloRutinas.getValueAt(row, 1);
        String proTxt = (String)  modeloRutinas.getValueAt(row, 2);
        String nom    = (String)  modeloRutinas.getValueAt(row, 3);
        Object fecha  =           modeloRutinas.getValueAt(row, 4);
        Boolean act   = (Boolean) modeloRutinas.getValueAt(row, 5);

        seleccionarComboPorTexto(comboClienteRutina, cliTxt);
        seleccionarComboPorTexto(comboProfesorRutina, proTxt);
        nombreRutina.setText(nom);
        fechaRutina.setText(fecha == null ? "" : fecha.toString());
        rutinaActivaCheckBox.setSelected(act != null && act);

        setCamposRutinaHabilitados(true);
        setCamposDetalleHabilitados(true);

        cargarDetalle(rutinaSeleccionadaId);
    }

    private void limpiarCamposRutina() {
        nombreRutina.setText("");
        fechaRutina.setText("");
        rutinaActivaCheckBox.setSelected(true);
        comboClienteRutina.setSelectedIndex(-1);
        comboProfesorRutina.setSelectedIndex(-1);
    }

    private void seleccionarComboPorTexto(JComboBox<ComboItem> combo, String texto) {
        if (texto == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (texto.equals(combo.getItemAt(i).text)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // ====== ABM Rutina ======
    private void agregarRutina() {
        // Habilita los campos para poder agregar una nueva
        setCamposRutinaHabilitados(true);

        ComboItem cli = (ComboItem) comboClienteRutina.getSelectedItem();
        ComboItem pro = (ComboItem) comboProfesorRutina.getSelectedItem();
        String nom = nombreRutina.getText().trim();
        String fec = fechaRutina.getText().trim();
        if (cli == null || pro == null || nom.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cliente, Profesor y Nombre son obligatorios.");
            return;
        }
        if (fec.isEmpty()) fec = LocalDate.now().toString();

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Rutina(id_profesor,id_cliente,nombre,fecha_creacion,activa) " +
                             "VALUES (?,?,?,?,TRUE)")) {
            ps.setInt(1, pro.id);
            ps.setInt(2, cli.id);
            ps.setString(3, nom);
            ps.setDate(4, Date.valueOf(fec));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Rutina agregada.");
            cargarRutinas();
            limpiarSeleccion();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error agregando rutina: " + ex.getMessage());
        }
    }

    private void modificarRutina() {
        if (rutinaSeleccionadaId == null) { JOptionPane.showMessageDialog(null, "Seleccione una rutina."); return; }
        ComboItem cli = (ComboItem) comboClienteRutina.getSelectedItem();
        ComboItem pro = (ComboItem) comboProfesorRutina.getSelectedItem();
        String nom = nombreRutina.getText().trim();
        String fec = fechaRutina.getText().trim();
        boolean act = rutinaActivaCheckBox.isSelected();

        if (cli == null || pro == null || nom.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cliente, Profesor y Nombre son obligatorios.");
            return;
        }
        if (fec.isEmpty()) fec = LocalDate.now().toString();

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Rutina SET id_profesor=?, id_cliente=?, nombre=?, fecha_creacion=?, activa=? WHERE id_rutina=?")) {
            ps.setInt(1, pro.id);
            ps.setInt(2, cli.id);
            ps.setString(3, nom);
            ps.setDate(4, Date.valueOf(fec));
            ps.setBoolean(5, act);
            ps.setInt(6, rutinaSeleccionadaId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Rutina actualizada.");
            cargarRutinas();
            reseleccionarRutina();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error modificando rutina: " + ex.getMessage());
        }
    }

    private void desactivarRutina() {
        if (rutinaSeleccionadaId == null) { JOptionPane.showMessageDialog(null, "Seleccione una rutina."); return; }
        int ok = JOptionPane.showConfirmDialog(null, "¿Desactivar rutina?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Rutina SET activa = FALSE WHERE id_rutina=?")) {
            ps.setInt(1, rutinaSeleccionadaId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Rutina desactivada.");
            cargarRutinas();
            limpiarSeleccion();
            modeloDetalle.setRowCount(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error desactivando rutina: " + ex.getMessage());
        }
    }

    // ====== ABM Detalle ======
    private void agregarEjercicioARutina() {
        if (rutinaSeleccionadaId == null) { JOptionPane.showMessageDialog(null, "Seleccione una rutina."); return; }
        ComboItem ej = (ComboItem) comboEjercicio.getSelectedItem();
        if (ej == null) { JOptionPane.showMessageDialog(null, "Seleccione un ejercicio."); return; }

        Integer series = parseIntOrNull(textField4.getText());
        Integer reps   = parseIntOrNull(textField3.getText());
        Integer peso   = parseIntOrNull(textField5.getText());

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Rutina_por_Ejercicio (id_rutina,id_ejercicio,repeticiones,series,peso_recomendado,activo) " +
                             "VALUES (?,?,?,?,?,TRUE)")) {
            ps.setInt(1, rutinaSeleccionadaId);
            ps.setInt(2, ej.id);
            setNullableInt(ps, 3, reps);
            setNullableInt(ps, 4, series);
            setNullableInt(ps, 5, peso);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Ejercicio agregado.");
            cargarDetalle(rutinaSeleccionadaId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error agregando: " + ex.getMessage());
        }
    }

    private void desactivarFilaDetalle() {
        int row = table5.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(null, "Seleccione una fila del detalle."); return; }
        int idFila = (int) modeloDetalle.getValueAt(row, 0);

        int ok = JOptionPane.showConfirmDialog(null, "¿Desactivar fila seleccionada?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Rutina_por_Ejercicio SET activo = FALSE WHERE id_rutina_por_ejercicio=?")) {
            ps.setInt(1, idFila);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Fila desactivada.");
            cargarDetalle(rutinaSeleccionadaId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error desactivando fila: " + ex.getMessage());
        }
    }

    // ====== Helpers ======
    private Integer parseIntOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException nfe) { return null; }
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, v);
    }

    private void limpiarSeleccion() {
        rutinaSeleccionadaId = null;
        table3.clearSelection();
        limpiarCamposRutina();
        setCamposRutinaHabilitados(false);
        setCamposDetalleHabilitados(false);
    }

    private void reseleccionarRutina() {
        if (rutinaSeleccionadaId == null) return;
        for (int i = 0; i < modeloRutinas.getRowCount(); i++) {
            if (((Integer) modeloRutinas.getValueAt(i, 0)).equals(rutinaSeleccionadaId)) {
                table3.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    // Combo item simple
    // 6. Hecho 'static' para que no dependa de una instancia de RutinasCard
    private static class ComboItem {
        final int id; final String text;
        ComboItem(int id, String text) { this.id = id; this.text = text; }
        public String toString() { return text; }
    }


}