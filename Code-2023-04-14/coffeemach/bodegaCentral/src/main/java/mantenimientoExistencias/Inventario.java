package mantenimientoExistencias;
 
import java.util.List;
 
public interface Inventario {
 
    // ── Métodos para la GUI de consola (abastecimiento masivo) ───────────────
    void abastecerIngredientes();
    void abastecerMonedas();
    void abastecerSuministros();
 
    // ── Métodos usados por BodegaServiceImpl ─────────────────────────────────
    List<String> consultarInventario();
    String retirarParaAlarma(int tipoAlarma);
    void abastecerExistencia(String codigo, int cantidad);
}
 