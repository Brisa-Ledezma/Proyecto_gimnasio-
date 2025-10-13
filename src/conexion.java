
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    static Connection con; // creo una variable de tipo conexión para luego realizarla


    public static Connection conectar() {
        String driver = "com.mysql.cj.jdbc.Driver"; // driver
        String url = "jdbc:mysql://localhost:3306/gimnasio?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";      // usuario DB
        String password = "root";  // contraseña

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, password); // ejecuto la conexión y la guardo
            if (con != null && !con.isClosed()) { // valido la conexión
                System.out.println("Conexión exitosa");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return con;
    }

    public static void cerrar() {
        // Cierra la conexión si existe
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            // Imprime la excepción para depuración
            e.printStackTrace();
        } finally {
            con = null;
        }
    }

}