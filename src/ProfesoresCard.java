import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProfesoresCard {

    private final JTable tablaProfesor;          // tablaProfesor
    private final JTextField nombreField;        // nombreProfesorTxt
    private final JTextField apellidoField;      // apellidoProfesorTxt
    private final JTextField especialidadField;  // especialidadTxt
    private final JButton agregarButton;         // botonAgregar
    private final JButton modificarButton;       // botonModificar
    private final JButton eliminarButton;        // eliminarBoton
    private final JButton cancelarButton;        // cancelarBoton

    private DefaultTableModel modelo;
    private int idProfesorSeleccionado = -1;
    private Integer idUsuarioSeleccionado = null; // para baja logica
    private boolean esNuevo = true;

    public ProfesoresCard(JTable tablaProfesor,
                          JTextField nombreProfesorTxt,
                          JTextField apellidoProfesorTxt,
                          JTextField especialidadTxt,
                          JButton botonAgregar,
                          JButton botonModificar,
                          JButton eliminarBoton,
                          JButton cancelarBoton) {
        this.tablaProfesor = tablaProfesor;
        this.nombreField = nombreProfesorTxt;
        this.apellidoField = apellidoProfesorTxt;
        this.especialidadField = especialidadTxt;
        this.agregarButton = botonAgregar;
        this.modificarButton = botonModificar;
        this.eliminarButton = eliminarBoton;
        this.cancelarButton = cancelarBoton;
    }

    public void inicializar() {
        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nombre");
        modelo.addColumn("Apellido");
        modelo.addColumn("Especialidad");
        modelo.addColumn("ID usuario");
        tablaProfesor.setModel(modelo);
        ocultarColumna(0);
        ocultarColumna(4);
        cargarProfesoresEnTabla();
        setModoEdicion(false);

        tablaProfesor.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProfesor.getSelectedRow() != -1) {
                int fila = tablaProfesor.getSelectedRow();
                idProfesorSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
                nombreField.setText(modelo.getValueAt(fila, 1).toString());
                apellidoField.setText(modelo.getValueAt(fila, 2).toString());
                especialidadField.setText(modelo.getValueAt(fila, 3).toString());
                idUsuarioSeleccionado = modelo.getValueAt(fila, 4) == null ? null :
                        Integer.parseInt(modelo.getValueAt(fila, 4).toString());
                esNuevo = false;
                setModoEdicion(true);
            }
        });

        agregarButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                guardarProfesor(true);   // alta o reactivar
                limpiarCampos();
            }
        });

        modificarButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (idProfesorSeleccionado == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione un profesor para modificar.");
                    return;
                }
                guardarProfesor(false); // solo update
                limpiarCampos();
            }
        });

        eliminarButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (idProfesorSeleccionado == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione un profesor para eliminar.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(null,
                        "¿Desactivar este profesor?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                try (Connection con = conexion.conectar()) {
                    con.setAutoCommit(false);

                    Integer idUser = idUsuarioSeleccionado;
                    if (idUser == null) {
                        // si no tiene usuario vinculado, lo creo ya inactivo y lo vinculo
                        idUser = crearUsuario(con,
                                generarUsuario(nombreField.getText(), apellidoField.getText()),
                                "temporal", // placeholder
                                "PROFESOR",
                                false);
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE Profesor SET id_usuario=? WHERE id_profesor=?")) {
                            up.setInt(1, idUser);
                            up.setInt(2, idProfesorSeleccionado);
                            up.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE Usuario SET activa=FALSE WHERE id_usuario=?")) {
                            up.setInt(1, idUser);
                            up.executeUpdate();
                        }
                    }

                    con.commit();
                    JOptionPane.showMessageDialog(null, "Profesor desactivado.");
                    cargarProfesoresEnTabla();
                    limpiarCampos();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error al desactivar: " + ex.getMessage());
                }
            }
        });

        if (cancelarButton != null) {
            cancelarButton.addActionListener(e -> limpiarCampos());
        }
        aplicarColores();
    }

    private void aplicarColores() {
        Tema.aplicarCampoTexto(nombreField);
        Tema.aplicarCampoTexto(apellidoField);
        Tema.aplicarCampoTexto(especialidadField);
        Tema.aplicarBoton(agregarButton);
        Tema.aplicarBoton(modificarButton);
        Tema.aplicarBoton(eliminarButton);
        if (cancelarButton != null) {
            Tema.aplicarBoton(cancelarButton);
        }
    }
    private void ocultarColumna(int idx) {
        tablaProfesor.getColumnModel().getColumn(idx).setMinWidth(0);
        tablaProfesor.getColumnModel().getColumn(idx).setMaxWidth(0);
        tablaProfesor.getColumnModel().getColumn(idx).setWidth(0);
    }

    private void setModoEdicion(boolean editar) {
        modificarButton.setEnabled(editar);
        eliminarButton.setEnabled(editar);
        agregarButton.setEnabled(!editar);
    }

    private void limpiarCampos() {
        nombreField.setText("");
        apellidoField.setText("");
        especialidadField.setText("");
        tablaProfesor.clearSelection();
        idProfesorSeleccionado = -1;
        idUsuarioSeleccionado = null;
        esNuevo = true;
        setModoEdicion(false);
    }

    /**
     * Alta o Reactivación si existe inactivo por nombre+apellido.
     * Si es update, solo actualiza datos del profesor.
     */
    private void guardarProfesor(boolean intentarReactivar) {
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String especialidad = especialidadField.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nombre y Apellido son obligatorios.");
            return;
        }

        try (Connection con = conexion.conectar()) {
            con.setAutoCommit(false);

            if (esNuevo) {
                // ¿existe profesor con mismo nombre+apellido?
                Integer idProfExistente = null;
                Integer idUserExistente = null;
                Boolean activaUser = null;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT p.id_profesor, p.id_usuario, u.activa " +
                                "FROM Profesor p LEFT JOIN Usuario u ON p.id_usuario=u.id_usuario " +
                                "WHERE p.nombre=? AND p.apellido=? LIMIT 1")) {
                    ps.setString(1, nombre);
                    ps.setString(2, apellido);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        idProfExistente = rs.getInt("id_profesor");
                        idUserExistente = rs.getObject("id_usuario") == null ? null : rs.getInt("id_usuario");
                        activaUser = rs.getObject("activa") == null ? null : rs.getBoolean("activa");
                    }
                }

                if (intentarReactivar && idProfExistente != null && Boolean.FALSE.equals(activaUser)) {
                    // Reactivar: activa usuario y actualiza especialidad si vino distinta
                    try (PreparedStatement upUser = con.prepareStatement(
                            "UPDATE Usuario SET activa=TRUE WHERE id_usuario=?")) {
                        upUser.setInt(1, idUserExistente);
                        upUser.executeUpdate();
                    }
                    try (PreparedStatement upProf = con.prepareStatement(
                            "UPDATE Profesor SET especialidad=? WHERE id_profesor=?")) {
                        upProf.setString(1, especialidad.isEmpty() ? null : especialidad);
                        upProf.setInt(2, idProfExistente);
                        upProf.executeUpdate();
                    }
                    con.commit();
                    JOptionPane.showMessageDialog(null, "Profesor reactivado.");
                    cargarProfesoresEnTabla();
                    return;
                }

                // No existe o estaba activo: crear nuevo usuario y profesor
                Integer idUserNuevo = crearUsuario(con,
                        generarUsuario(nombre, apellido),
                        "temporal", // placeholder
                        "PROFESOR",
                        true);

                try (PreparedStatement ins = con.prepareStatement(
                        "INSERT INTO Profesor (id_usuario, nombre, apellido, especialidad) VALUES (?, ?, ?, ?)")) {
                    ins.setInt(1, idUserNuevo);
                    ins.setString(2, nombre);
                    ins.setString(3, apellido);
                    ins.setString(4, especialidad.isEmpty() ? null : especialidad);
                    ins.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(null, "Profesor agregado.");
                cargarProfesoresEnTabla();
                return;

            } else {
                // UPDATE datos profesor
                try (PreparedStatement up = con.prepareStatement(
                        "UPDATE Profesor SET nombre=?, apellido=?, especialidad=? WHERE id_profesor=?")) {
                    up.setString(1, nombre);
                    up.setString(2, apellido);
                    up.setString(3, especialidad.isEmpty() ? null : especialidad);
                    up.setInt(4, idProfesorSeleccionado);
                    up.executeUpdate();
                }
                con.commit();
                JOptionPane.showMessageDialog(null, "Profesor actualizado.");
                cargarProfesoresEnTabla();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar profesor: " + ex.getMessage());
        }
    }

    /** Carga solo profesores con Usuario.activa = TRUE */
    private void cargarProfesoresEnTabla() {
        modelo.setRowCount(0);
        try (Connection con = conexion.conectar()) {
            String sql =
                    "SELECT p.id_profesor, p.nombre, p.apellido, COALESCE(p.especialidad,'') AS especialidad, p.id_usuario " +
                            "FROM Profesor p JOIN Usuario u ON p.id_usuario = u.id_usuario " +
                            "WHERE u.activa = TRUE " +
                            "ORDER BY p.apellido, p.nombre";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                            rs.getInt("id_profesor"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("especialidad"),
                            rs.getInt("id_usuario")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar profesores: " + ex.getMessage());
        }
    }

    /** Helpers **/

    private static String generarUsuario(String nombre, String apellido) {
        // simple: prof_nombre_apellido en minúsculas, sin espacios
        String n = nombre.trim().toLowerCase().replaceAll("\\s+", "");
        String a = apellido.trim().toLowerCase().replaceAll("\\s+", "");
        return "prof_" + n + "_" + a;
    }

    private static Integer crearUsuario(Connection con, String nomUsuario, String contrasenia,
                                        String rol, boolean activa) throws SQLException {
        String sql = "INSERT INTO Usuario (nom_usuario, contrasenia, rol, activa) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomUsuario);
            ps.setString(2, contrasenia); // si luego usás hash, reemplazar aquí
            ps.setString(3, rol);
            ps.setBoolean(4, activa);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo crear Usuario.");
    }
}