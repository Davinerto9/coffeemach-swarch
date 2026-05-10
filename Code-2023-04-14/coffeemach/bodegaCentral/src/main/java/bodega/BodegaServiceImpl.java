package bodega;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mantenimientoExistencias.Inventario;
import servicios.ServicioBodega;

public class BodegaServiceImpl implements ServicioBodega, Bodega {

    private final Inventario inventario;
    private final Map<Integer, OrdenEntrega> ordenes = new LinkedHashMap<>();
    private final AtomicInteger contadorOrdenesEntrega = new AtomicInteger(1);

    public BodegaServiceImpl(Inventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public String generarOrdenEntrega(int codMaquina, int tipoAlarma, String descripcion, com.zeroc.Ice.Current current) {
        OrdenEntrega orden = new OrdenEntrega(contadorOrdenesEntrega.getAndIncrement(), codMaquina, tipoAlarma, descripcion);
        ordenes.put(orden.getId(), orden);
        return orden.getId() + "#" + orden.toResumen();
    }

    // ← SOLO este, el original fue eliminado
    @Override
    public String entregarMateriales(int idOrden, int codMaquina,
                                     int tipoAlarma, com.zeroc.Ice.Current current) {
        OrdenEntrega orden = ordenes.get(idOrden);
        if (orden == null)
            return "ERROR#- Orden de entrega no existe: " + idOrden;
        if (orden.getCodMaquina() != codMaquina)
            return "ERROR#- La orden no corresponde a la maquina " + codMaquina;
        if (orden.getEstado() != EstadoOrdenEntrega.EN_PICKING)
            return "ERROR#- La orden " + idOrden
                 + " no esta en estado EN_PICKING (estado actual: "
                 + orden.getEstado() + ")";
        String retiro = inventario.retirarParaAlarma(tipoAlarma);
        orden.setEstado(EstadoOrdenEntrega.DESPACHADA);
        return "DESPACHADA#" + orden.toResumen() + "#" + retiro;
    }

    @Override
    public void registrarRecepcionMateriales(int idOrden, String evidencia, com.zeroc.Ice.Current current) {
        OrdenEntrega orden = ordenes.get(idOrden);
        if (orden != null) {
            orden.setEstado(EstadoOrdenEntrega.RECIBIDA);
            orden.setSoporteRecepcion(evidencia);
        }
    }

    @Override
    public List<String> consultarInventario(com.zeroc.Ice.Current current) {
        return inventario.consultarInventario();
    }

    @Override
    public List<String> consultarMonedas(com.zeroc.Ice.Current current) {
        return filtrarInventario("MONEDAS");
    }

    @Override
    public List<String> consultarIngredientes(com.zeroc.Ice.Current current) {
        return filtrarInventario("INGREDIENTES");
    }

    @Override
    public List<String> consultarSuministros(com.zeroc.Ice.Current current) {
        return filtrarInventario("SUMINISTROS");
    }

    private List<String> filtrarInventario(String prefijo) {
        List<String> filtrados = new ArrayList<>();
        for (String item : inventario.consultarInventario()) {
            if (item.startsWith(prefijo)) filtrados.add(item);
        }
        return filtrados;
    }

    @Override
    public String entregaKitReparacion(int idOrden, int codMaquina, com.zeroc.Ice.Current current) {
        return entregarMateriales(idOrden, codMaquina, 6, current);
    }

    @Override
    public String retirarExistencias(int tipoAlarma, com.zeroc.Ice.Current current) {
        return inventario.retirarParaAlarma(tipoAlarma);
    }

    @Override
    public void abastecerExistencia(String codigo, int cantidad, com.zeroc.Ice.Current current) {
        inventario.abastecerExistencia(codigo, cantidad);
    }

    @Override
    public String separarExistencias(int idOrden, int tipoAlarma, com.zeroc.Ice.Current current) {
        OrdenEntrega orden = ordenes.get(idOrden);
        if (orden == null) return "ERROR#- Orden de entrega no existe: " + idOrden;
        orden.setEstado(EstadoOrdenEntrega.EN_PICKING);
        return "EN_PICKING#" + orden.toResumen() + "#tipoAlarma=" + tipoAlarma;
    }

    // --- Métodos de la interfaz Bodega (sin Current, uso interno) ---

    @Override
    public List<String> consultarMonedas() { return filtrarInventario("MONEDAS"); }

    @Override
    public List<String> consultarIngredientes() { return filtrarInventario("INGREDIENTES"); }

    @Override
    public List<String> consultarSuministros() { return filtrarInventario("SUMINISTROS"); }

    @Override
    public String entregaKitReparacion(int idOrden, int codMaquina) {
        return entregarMateriales(idOrden, codMaquina, 6, null);
    }

    @Override
    public String retirarExistencias(int tipoAlarma) {
        return inventario.retirarParaAlarma(tipoAlarma);
    }

    @Override
    public void abastecerExistencia(String codigo, int cantidad) {
        inventario.abastecerExistencia(codigo, cantidad);
    }

    @Override
    public String separarExistencias(int idOrden, int tipoAlarma) {
        OrdenEntrega orden = ordenes.get(idOrden);
        if (orden == null) return "ERROR#- Orden de entrega no existe: " + idOrden;
        orden.setEstado(EstadoOrdenEntrega.EN_PICKING);
        return "EN_PICKING#" + orden.toResumen() + "#tipoAlarma=" + tipoAlarma;
    }
}