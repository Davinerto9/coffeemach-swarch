package bodega;

/**
 * Observer Pattern - Interfaz para observar cambios en órdenes de bodega.
 */
public interface OrdenObserver {

    /**
     * Se llama automáticamente cuando una orden cambia de estado.
     */
    void onOrdenActualizada(OrdenBodega orden);

    /**
     * Permite identificar al observer en logs.
     */
    String getObserverName();
}