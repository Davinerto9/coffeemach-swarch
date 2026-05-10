package ingrediente;

import java.util.Map;
import alarma.AlarmaService;
import productoReceta.Receta;

public class IngredienteServiceImp implements IngredienteService {

    private IngredienteRepositorio repo = IngredienteRepositorio.getInstance();
    private AlarmaService alarmaService;

    public void setAlarmaService(AlarmaService alarmaService) {
        this.alarmaService = alarmaService;
    }

    @Override
    public void darExistencia() {
        repo.getValues().forEach(ing -> 
            System.out.println(ing.getNombre() + ": " + ing.getCantidad()));
    }

    @Override
    public boolean validarDisponibilidad(Receta receta) {
        for (Map.Entry<Ingrediente, Double> entry : receta.getListaIngredientes().entrySet()) {
            Ingrediente stock = repo.findByKey(entry.getKey().getNombre());
            if (stock == null || stock.getCantidad() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void consumirIngredientes(Receta receta) {
        for (Map.Entry<Ingrediente, Double> entry : receta.getListaIngredientes().entrySet()) {
            Ingrediente stock = repo.findByKey(entry.getKey().getNombre());
            if (stock != null) {
                double nuevaCantidad = stock.getCantidad() - entry.getValue();
                stock.setCantidad(nuevaCantidad);
                repo.addElement(stock.getNombre(), stock);
                
                if (nuevaCantidad <= stock.getCritico()) {
                    alarmaService.notificarMalFuncionamiento();
                } else if (nuevaCantidad <= stock.getMinimo()) {
                    alarmaService.notificarEscazesIngredientes();
                }
            }
        }
    }
}
