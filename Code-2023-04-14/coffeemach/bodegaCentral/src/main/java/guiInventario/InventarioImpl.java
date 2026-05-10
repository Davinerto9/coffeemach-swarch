package mantenimientoExistencias;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        String prefijo;

        switch (tipoAlarma) {

            case 1:
                prefijo = PREF_INGREDIENTES;
                break;

            case 2:
                prefijo = PREF_MONEDAS;
                break;

            case 3:
            case 4:
                prefijo = PREF_SUMINISTROS;
                break;

            case 6:
                prefijo = PREF_SUMINISTROS;
                break;

            default:
                prefijo = PREF_INGREDIENTES;
                break;
        }

        StringBuilder retirado = new StringBuilder();

        for (Map.Entry<String, Integer> e : existencias.entrySet()) {

            if (e.getKey().startsWith(prefijo)
                    && e.getValue() > 0) {

                int cantidad =
                        Math.min(50, e.getValue());

                e.setValue(e.getValue() - cantidad);

                retirado.append(e.getKey())
                        .append("=")
                        .append(cantidad)
                        .append(" ");
            }
        }

        return retirado.toString().trim();
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