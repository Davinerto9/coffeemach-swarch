package bodega;

/**
 * Estados del ciclo de vida de una orden de entrega en bodega.
 */
public enum EstadoOrdenBodega {
    REGISTRADA,           // Orden creada
    EN_PREPARACION,       // Separando materiales (antes EN_PICKING)
    LISTA_PARA_DESPACHO,  // Materiales preparados
    DESPACHADA,           // Entregada al técnico/logística
    RECIBIDA              // Confirmada por el receptor
}