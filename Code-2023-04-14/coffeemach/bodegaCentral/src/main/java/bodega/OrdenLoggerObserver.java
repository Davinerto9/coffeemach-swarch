package bodega;

public class OrdenLoggerObserver implements OrdenObserver {

    @Override
    public void onOrdenActualizada(OrdenBodega orden) {
        System.out.println(
            "[Observer-Bodega] Orden " + orden.getId()
            + " actualizada. Estado actual: " + orden.getEstado()
        );
    }

    @Override
    public String getObserverName() {
        return "OrdenLoggerObserver";
    }

}