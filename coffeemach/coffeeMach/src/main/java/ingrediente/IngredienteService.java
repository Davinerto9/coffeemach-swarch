package ingrediente;

import productoReceta.Receta;

public interface IngredienteService {

    public void darExistencia();

    public boolean validarDisponibilidad(Receta receta);

    public void consumirIngredientes(Receta receta);
}
