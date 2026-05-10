package suministro;

import ingrediente.Ingrediente;
import ingrediente.IngredienteRepositorio;

public class SuministroServiceImp implements SuministroService {

    private IngredienteRepositorio repo = IngredienteRepositorio.getInstance();

    @Override
    public boolean verificatExistenciaSuministro(String sumId) {
        Ingrediente ing = repo.findByKey(sumId);
        return ing != null && ing.getCantidad() > ing.getMinimo();
    }

    @Override
    public String[] darInsumos() {
        return repo.getKeys().toArray(new String[0]);
    }

    @Override
    public void dispensarSuministro(String sumId) {
        Ingrediente ing = repo.findByKey(sumId);
        if (ing != null && ing.getCantidad() > 0) {
            ing.setCantidad(ing.getCantidad() - 1);
            repo.addElement(sumId, ing);
        }
    }
    
    public void reabastecer(String sumId, double cantidad) {
        Ingrediente ing = repo.findByKey(sumId);
        if (ing != null) {
            ing.setCantidad(Math.min(ing.getMaximo(), ing.getCantidad() + cantidad));
            repo.addElement(sumId, ing);
        }
    }
}
