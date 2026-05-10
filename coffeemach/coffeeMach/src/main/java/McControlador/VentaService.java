package McControlador;

import java.util.Date;
import productoReceta.Receta;
import productoReceta.RecetaRepositorio;
import ingrediente.IngredienteService;

public class VentaService {

    private VentaRepositorio ventaRepo = VentaRepositorio.getInstance();
    private RecetaRepositorio recetaRepo = RecetaRepositorio.getInstance();
    private IngredienteService ingredienteService;

    public void setIngredienteService(IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    public boolean procesarVenta(String idReceta) {
        Receta receta = recetaRepo.findByKey(idReceta);
        if (receta == null) return false;

        if (ingredienteService.validarDisponibilidad(receta)) {
            ingredienteService.consumirIngredientes(receta);
            
            Venta nuevaVenta = new Venta(
                receta.getDescripcion(), 
                receta.getId(), 
                receta.getValor(), 
                new Date()
            );
            ventaRepo.addElement(String.valueOf(System.currentTimeMillis()), nuevaVenta);
            return true;
        }
        return false;
    }
}
