import bodega.BodegaServiceImpl;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import guiInventario.Interfaz;
import mantenimientoExistencias.InventarioImpl;

import javax.swing.SwingUtilities;

public class BodegaCentral {

    public static void main(String[] args) {

        try (Communicator communicator = Util.initialize(args, "BodegaCentral.cfg")) {

            // 1. Inventario en memoria
            InventarioImpl inventario = new InventarioImpl();

            // 2. Servicio de bodega
            BodegaServiceImpl bodegaService = new BodegaServiceImpl(inventario);

            // 3. Registrar el servant Ice
            ObjectAdapter adapter = communicator.createObjectAdapter("BodegaAdapter");
            adapter.add(bodegaService, Util.stringToIdentity("bodega"));
            adapter.activate();

            System.out.println("BodegaCentral iniciado en el adaptador Ice.");

            // 4. Lanzar la interfaz Swing en el Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                Interfaz interfaz = new Interfaz(inventario, bodegaService);
                interfaz.setVisible(true);
            });

            // 5. Bloquear hasta shutdown
            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println("Error al iniciar BodegaCentral: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}