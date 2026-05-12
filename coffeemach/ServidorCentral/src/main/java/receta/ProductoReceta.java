package receta;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;

import modelo.ConexionBD;
import modelo.ManejadorDatos;
import servicios.RecetaService;

public class ProductoReceta implements RecetaService {

    private Communicator communicator;

    /**
     * @param communicator the communicator to set
     */
    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public String[] consultarIngredientes(Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        String[] ret = md.consultarIngredientes();

        cbd.cerrarConexion();

        return ret;
    }

    @Override
    public String[] consultarRecetas(Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        String[] ret = md.consultarRecetas();

        cbd.cerrarConexion();

        return ret;
    }

    @Override
    public String[] consultarProductos(Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        List<String> listaAsociada = md.consultaRecetasCompleta();

        cbd.cerrarConexion();

        if (!listaAsociada.equals(null)) {

            String[] retorno = new String[listaAsociada.size()];

            for (int i = 0; i < listaAsociada.size(); i++) {

                retorno[i] = listaAsociada.get(i);
            }

            return retorno;
        }

        return null;
    }

    @Override
    public void definirProducto(String nombre, int precio, Map<String, Integer> ingredientes, Current current) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("[ServidorCentral] No se define producto: nombre vacio.");
            return;
        }

        if (precio <= 0) {
            System.out.println("[ServidorCentral] No se define producto "
                    + nombre + ": precio invalido " + precio);
            return;
        }

        if (ingredientes == null || ingredientes.isEmpty()) {
            System.out.println("[ServidorCentral] No se define producto "
                    + nombre + ": no tiene ingredientes.");
            return;
        }

        ConexionBD cbd = null;
        String nombreProducto = nombre.trim();
        System.out.println("[ServidorCentral] Definiendo producto: nombre="
                + nombreProducto + ", precio=" + precio + ", ingredientes="
                + ingredientes.size());

        try {
            cbd = new ConexionBD(communicator);
            cbd.conectarBaseDatos();
            ManejadorDatos md = new ManejadorDatos();
            md.setConexion(cbd.getConnection());

            Integer idReceta = buscarIdRecetaPorNombre(md, nombreProducto);
            if (idReceta != null) {
                System.out.println("[ServidorCentral] Receta existente: "
                        + nombreProducto + " id=" + idReceta);
            } else {
                md.registrarReceta(nombreProducto, precio);
                idReceta = buscarIdRecetaPorNombre(md, nombreProducto);
                if (idReceta == null) {
                    System.out.println("[ServidorCentral] No se pudo confirmar ID de receta para "
                            + nombreProducto);
                    return;
                }
                System.out.println("[ServidorCentral] Receta creada: "
                        + nombreProducto + " id=" + idReceta);
            }

            Set<Integer> asociados = new HashSet<Integer>();
            for (Map.Entry<String, Integer> entrada : ingredientes.entrySet()) {
                String clave = entrada.getKey();
                Integer cantidad = entrada.getValue();

                if (cantidad == null || cantidad <= 0) {
                    System.out.println("[ServidorCentral] Ingrediente saltado para "
                            + nombreProducto + ": clave=" + clave
                            + ", cantidad=" + cantidad);
                    continue;
                }

                Integer idIngrediente = resolverIdIngrediente(md, clave);
                if (idIngrediente == null) {
                    String nombreIngrediente = extraerNombreIngrediente(clave);
                    if (nombreIngrediente != null && !nombreIngrediente.trim().isEmpty()) {
                        md.registrarIngrediente(nombreIngrediente.trim());
                        idIngrediente = buscarIdIngredientePorNombre(md, nombreIngrediente);
                    }
                }

                if (idIngrediente == null) {
                    System.out.println("[ServidorCentral] Ingrediente saltado para "
                            + nombreProducto + ": no se pudo resolver clave="
                            + clave);
                    continue;
                }

                if (asociados.contains(idIngrediente)) {
                    System.out.println("[ServidorCentral] Ingrediente duplicado saltado para "
                            + nombreProducto + ": idIngrediente=" + idIngrediente);
                    continue;
                }

                md.registrarRecetaIngrediente(idReceta, idIngrediente, cantidad);
                asociados.add(idIngrediente);
                System.out.println("[ServidorCentral] Ingrediente asociado: receta="
                        + idReceta + ", ingrediente=" + idIngrediente
                        + ", cantidad=" + cantidad);
            }

            System.out.println("[ServidorCentral] Producto definido finalizado: "
                    + nombreProducto + ", ingredientes asociados="
                    + asociados.size());
        } catch (Exception e) {
            System.out.println("[ServidorCentral] Error controlado definiendo producto "
                    + nombreProducto + ": " + e.getMessage());
        } finally {
            if (cbd != null) {
                cbd.cerrarConexion();
            }
        }
    }

    @Override
    public void borrarReceta(int cod, Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        md.borrarReceta(cod);

        cbd.cerrarConexion();
    }

    @Override
    public void definirRecetaIngrediente(int idReceta, int idIngrediente, int valor, Current current) {

        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        md.registrarRecetaIngrediente(idReceta, idIngrediente, valor);

        cbd.cerrarConexion();
    }

    @Override
    public String registrarReceta(String nombre, int precio, Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        String ret = md.registrarReceta(nombre, precio);

        cbd.cerrarConexion();

        return ret;
    }

    @Override
    public String registrarIngrediente(String nombre, Current current) {
        ConexionBD cbd = new ConexionBD(communicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        String ret = md.registrarIngrediente(nombre);

        cbd.cerrarConexion();

        return ret;
    }

    private Integer buscarIdRecetaPorNombre(ManejadorDatos md, String nombre) {
        String[] recetas = md.consultarRecetas();
        if (recetas == null) {
            return null;
        }

        String nombreNormalizado = normalizar(nombre);
        for (String receta : recetas) {
            if (receta == null) {
                continue;
            }

            String[] partes = receta.split("-", 3);
            if (partes.length < 2) {
                continue;
            }

            if (normalizar(partes[1]).equals(nombreNormalizado)) {
                return parsearEnteroSeguro(partes[0]);
            }
        }

        return null;
    }

    private Integer buscarIdIngredientePorNombre(ManejadorDatos md, String nombre) {
        String[] ingredientes = md.consultarIngredientes();
        if (ingredientes == null) {
            return null;
        }

        String nombreNormalizado = normalizar(nombre);
        for (String ingrediente : ingredientes) {
            if (ingrediente == null) {
                continue;
            }

            String[] partes = ingrediente.split("-", 2);
            if (partes.length < 2) {
                continue;
            }

            if (normalizar(partes[1]).equals(nombreNormalizado)) {
                return parsearEnteroSeguro(partes[0]);
            }
        }

        return null;
    }

    private Integer resolverIdIngrediente(ManejadorDatos md, String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            return null;
        }

        String[] partes = clave.trim().split("-");
        Integer id = parsearEnteroSeguro(partes[0]);
        if (id != null && existeIngredientePorId(md, id)) {
            return id;
        }

        String nombre = extraerNombreIngrediente(clave);
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        return buscarIdIngredientePorNombre(md, nombre);
    }

    private boolean existeIngredientePorId(ManejadorDatos md, int idIngrediente) {
        String[] ingredientes = md.consultarIngredientes();
        if (ingredientes == null) {
            return false;
        }

        for (String ingrediente : ingredientes) {
            if (ingrediente == null) {
                continue;
            }

            String[] partes = ingrediente.split("-", 2);
            if (partes.length > 0) {
                Integer id = parsearEnteroSeguro(partes[0]);
                if (id != null && id == idIngrediente) {
                    return true;
                }
            }
        }

        return false;
    }

    private String extraerNombreIngrediente(String clave) {
        if (clave == null) {
            return null;
        }

        String texto = clave.trim();
        if (texto.isEmpty()) {
            return null;
        }

        String[] partes = texto.split("-");
        Integer id = parsearEnteroSeguro(partes[0]);
        if (id == null) {
            return partes[0].trim();
        }

        if (partes.length > 1) {
            return partes[1].trim();
        }

        return null;
    }

    private Integer parsearEnteroSeguro(String texto) {
        if (texto == null) {
            return null;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toLowerCase();
    }

}
