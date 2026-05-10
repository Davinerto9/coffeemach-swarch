import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import controlAlarma.ControladorAlarmas;
import gui.LoginFrame;
import servicios.ServicioAbastecimientoPrx;
import servicios.ServicioBodegaPrx;
import servicios.ServicioComLogisticaPrx;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada de cmLogistics.
 * Inicializa el comunicador Ice y lanza la GUI Swing.
 */
public class CmLogistics {

    public static void main(String[] args) {
        // Aspecto nativo del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        List<String> extArgs = new ArrayList<>();
        Communicator communicator = Util.initialize(args, "CmLogistic.cfg", extArgs);

        ServicioComLogisticaPrx logistica =
            ServicioComLogisticaPrx.checkedCast(
                communicator.propertyToProxy("ServerCentral"))
            .ice_twoway();

        ServicioAbastecimientoPrx maquina =
            ServicioAbastecimientoPrx.checkedCast(
                communicator.propertyToProxy("MaquinaCafe"))
            .ice_twoway();

        ServicioBodegaPrx bodega =
            ServicioBodegaPrx.checkedCast(
                communicator.propertyToProxy("BodegaCentral"))
            .ice_twoway();

        ControladorAlarmas controlador =
                new ControladorAlarmas(logistica, maquina, bodega);

        // Lanzar GUI en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame(controlador);
            login.setVisible(true);
        });

        // Esperar hasta que todas las ventanas estén cerradas
        // (el shutdown de Ice se hace al terminar el proceso)
        try {
            // Mantener el proceso vivo mientras haya ventanas Swing abiertas
            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("Error en comunicador Ice: " + e.getMessage());
        }
    }
}