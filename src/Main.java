import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

            /*MenuPrincipal menuPrincipal = new MenuPrincipal();
            menuPrincipal.mostrarmenu();*/

            // Abre la ventana de login boludaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            login loginVentana = new login();
            loginVentana.mostrarLogin();

            //verificamos que este bien conectada la BD
            Connection c = conexion.conectar();
            try {
                if (c != null && !c.isClosed()) {
                    System.out.println("Test: conexión OK");
                } else {
                    System.out.println("Test: conexión NULL o cerrada");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                conexion.cerrar();
            }

    }
}
