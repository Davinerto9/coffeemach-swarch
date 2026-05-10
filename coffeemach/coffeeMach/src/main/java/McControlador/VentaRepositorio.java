package McControlador;

import interfaces.Repositorio;

public class VentaRepositorio extends Repositorio<String, Venta> {

    private static VentaRepositorio instance;

    public static VentaRepositorio getInstance() {
        if (instance == null) {
            instance = new VentaRepositorio();
        }
        return instance;
    }

    private VentaRepositorio() {
        super("ventas.bd");
    }

    @Override
    public void loadDataP() {
        // Initializes the repository. Since sales are recorded at runtime, 
        // this method remains available for future pre-loading of historical data if needed.
    }
}
