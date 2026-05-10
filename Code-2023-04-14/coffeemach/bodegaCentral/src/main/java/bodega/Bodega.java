package bodega;

import java.util.List;

public interface Bodega {

    // Consultas de inventario por categoría
    List<String> consultarMonedas();
    List<String> consultarIngredientes();
    List<String> consultarSuministros();

    // Flujo de órdenes de entrega
    String entregaKitReparacion(int idOrden, int codMaquina);
    String retirarExistencias(int tipoAlarma);
    void   abastecerExistencia(String codigo, int cantidad);
    String separarExistencias(int idOrden, int tipoAlarma);
}