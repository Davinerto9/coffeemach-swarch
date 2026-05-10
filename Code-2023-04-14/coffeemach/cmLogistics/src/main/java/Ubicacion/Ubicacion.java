package Ubicacion;

import java.util.HashMap;
import java.util.Map;

public class Ubicacion {

    // Mapa simple de ubicaciones conocidas a instrucciones de ruta
    private static final Map<String, String> RUTAS = new HashMap<>();

    static {
        RUTAS.put("Edificio A", "Tomar ascensor piso 1, pasillo norte, local 101");
        RUTAS.put("Edificio B", "Entrada principal, subir escaleras, oficina 205");
        RUTAS.put("Cafeteria", "Planta baja, ala sur, junto a recepcion");
    }

    public String calcularRuta(String ubicacion) {
        String instrucciones = RUTAS.getOrDefault(
                ubicacion,
                "Dirigirse a: " + ubicacion + " (sin ruta predefinida)");
        return "Ruta -> " + ubicacion + ": " + instrucciones;
    }
}