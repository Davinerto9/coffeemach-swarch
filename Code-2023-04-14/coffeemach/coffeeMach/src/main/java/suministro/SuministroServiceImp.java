package suministro;

import java.util.ArrayList;
import java.util.List;

import ingrediente.Ingrediente;
import ingrediente.IngredienteRepositorio;

public class SuministroServiceImp implements SuministroService {

    private final IngredienteRepositorio ingredientes = IngredienteRepositorio.getInstance();

    @Override
    public boolean verificatExistenciaSuministro(String sumId) {
        Ingrediente suministro = buscarSuministro(sumId);
        boolean existe = suministro != null && suministro.getCantidad() > 0;
        System.out.println("[CoffeeMach] Verificacion suministro " + sumId
                + ": " + existe);
        return existe;
    }

    @Override
    public String[] darInsumos() {
        List<String> insumos = new ArrayList<String>();

        for (Ingrediente ingrediente : ingredientes.getValues()) {
            if (ingrediente != null) {
                insumos.add(ingrediente.getNombre() + "#cantidad="
                        + ingrediente.getCantidad());
            }
        }

        if (insumos.isEmpty()) {
            Ingrediente vaso = ingredientes.findByKey("Vaso");
            if (vaso != null) {
                insumos.add("Vaso#cantidad=" + vaso.getCantidad());
            }
        }

        System.out.println("[CoffeeMach] Insumos consultados: "
                + insumos.size());
        return insumos.toArray(new String[0]);
    }

    @Override
    public void dispensarSuministro(String sumId) {
        Ingrediente suministro = buscarSuministro(sumId);
        if (suministro == null) {
            System.out.println("[CoffeeMach] No se dispenso suministro "
                    + sumId + ": recurso no encontrado.");
            return;
        }

        if (suministro.getCantidad() <= 0) {
            System.out.println("[CoffeeMach] No se dispenso suministro "
                    + suministro.getNombre() + ": stock insuficiente.");
            return;
        }

        suministro.setCantidad(suministro.getCantidad() - 1);
        ingredientes.addElement(suministro.getNombre(), suministro);
        System.out.println("[CoffeeMach] Suministro dispensado: "
                + suministro.getNombre() + ", cantidad restante: "
                + suministro.getCantidad());
    }

    private Ingrediente buscarSuministro(String sumId) {
        if (sumId == null || sumId.trim().isEmpty()) {
            return null;
        }

        String normalizado = sumId.trim().toLowerCase();
        if (normalizado.equals("vaso")
                || normalizado.equals("vasos")
                || normalizado.equals("11")
                || normalizado.equals("15")
                || normalizado.equals("suministro")
                || normalizado.equals("suministros")) {
            return ingredientes.findByKey("Vaso");
        }

        for (Ingrediente ingrediente : ingredientes.getValues()) {
            if (ingrediente != null
                    && ingrediente.getNombre() != null
                    && ingrediente.getNombre().trim().equalsIgnoreCase(sumId.trim())) {
                return ingrediente;
            }
        }

        return null;
    }
}
