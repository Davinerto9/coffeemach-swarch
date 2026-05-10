package guiInventario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mantenimientoExistencias.Inventario;

/**
 * Implementación de Inventario.
 * Mantiene existencias en memoria y expone operaciones de consulta,
 * abastecimiento y retiro usadas por BodegaServiceImpl y la Interfaz.
 */
public class InventarioImpl implements Inventario {

    public static final String PREF_INGREDIENTES = "INGREDIENTES";
    public static final String PREF_MONEDAS = "MONEDAS";
    public static final String PREF_SUMINISTROS = "SUMINISTROS";

    private final Map<String, Integer> existencias =
            new LinkedHashMap<String, Integer>();

    public InventarioImpl() {

        existencias.put("INGREDIENTES_agua", 500);
        existencias.put("INGREDIENTES_cafe_molido", 200);
        existencias.put("INGREDIENTES_azucar", 150);
        existencias.put("INGREDIENTES_leche_polvo", 100);
        existencias.put("INGREDIENTES_chocolate", 80);

        existencias.put("MONEDAS_100", 500);
        existencias.put("MONEDAS_200", 300);
        existencias.put("MONEDAS_500", 200);

        existencias.put("SUMINISTROS_vasos", 300);
        existencias.put("SUMINISTROS_palitos", 300);
        existencias.put("SUMINISTROS_kit_reparacion", 10);
    }

    // ── Métodos de abastecimiento masivo ────────────────────────────────────

    @Override
    public void abastecerIngredientes() {

        existencias.replaceAll((k, v) ->
                k.startsWith(PREF_INGREDIENTES) ? v + 50 : v);

        System.out.println(
                "Ingredientes abastecidos (+50 cada uno).");
    }

    @Override
    public void abastecerMonedas() {

        existencias.replaceAll((k, v) ->
                k.startsWith(PREF_MONEDAS) ? v + 100 : v);

        System.out.println(
                "Monedas abastecidas (+100 cada denominacion).");
    }

    @Override
    public void abastecerSuministros() {

        existencias.replaceAll((k, v) ->
                k.startsWith(PREF_SUMINISTROS) ? v + 50 : v);

        System.out.println(
                "Suministros abastecidos (+50 cada uno).");
    }

    // ── Métodos usados por BodegaServiceImpl ───────────────────────────────

    @Override
    public List<String> consultarInventario() {

        List<String> resultado = new ArrayList<String>();

        for (Map.Entry<String, Integer> e : existencias.entrySet()) {

            resultado.add(
                    e.getKey() + " : " + e.getValue());
        }

        return resultado;
    }

    @Override
public synchronized String retirarParaAlarma(int tipoAlarma) {

    switch (tipoAlarma) {

        case 1:
            return "SIN_RETIRO#- Alarma general no requiere material físico";

        case 2:
        case 3:
            return retirarCodigo("MONEDAS_100", 20);

        case 4:
        case 5:
            return retirarCodigo("MONEDAS_200", 20);

        case 6:
        case 7:
            return retirarCodigo("MONEDAS_500", 20);

        case 8:
        case 12:
            return retirarCodigo("INGREDIENTES_agua", 50);

        case 9:
        case 13:
            return retirarCodigo("INGREDIENTES_cafe_molido", 50);

        case 10:
        case 14:
            return retirarCodigo("INGREDIENTES_azucar", 50);

        case 11:
        case 15:
            return retirarCodigo("SUMINISTROS_vasos", 50);

        default:
            return "ERROR#- Tipo de alarma no soportado en bodega: " + tipoAlarma;
    }
}

private String retirarCodigo(String codigo, int cantidadSolicitada) {

    Integer disponible = existencias.get(codigo);

    if (disponible == null) {
        return "ERROR#- No existe el código en inventario: " + codigo;
    }

    if (disponible <= 0) {
        return "ERROR#- Sin stock disponible para: " + codigo;
    }

    int cantidadRetirada = Math.min(cantidadSolicitada, disponible);
    existencias.put(codigo, disponible - cantidadRetirada);

    return codigo + "=" + cantidadRetirada;
}

    @Override
    public synchronized void abastecerExistencia(
            String codigo,
            int cantidad) {

        existencias.merge(
                codigo,
                cantidad,
                Integer::sum);

        System.out.println(
                "Abastecido "
                        + codigo
                        + " +"
                        + cantidad
                        + " -> "
                        + existencias.get(codigo));
    }

    public Map<String, Integer> getExistencias() {
        return existencias;
    }
}