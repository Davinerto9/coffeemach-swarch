package bodega;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mantenimientoExistencias.Inventario;
import servicios.ServicioBodega;

public class BodegaServiceImpl implements ServicioBodega, Bodega {

    private final Inventario inventario;
    private final Map<Integer, OrdenBodega> ordenes = new LinkedHashMap<>();
    private final AtomicInteger contadorOrdenes = new AtomicInteger(1);

    public BodegaServiceImpl(Inventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public String generarOrdenEntrega(int codMaquina, int tipoAlarma, String descripcion, com.zeroc.Ice.Current current) {
        OrdenBodega orden = new OrdenBodega(contadorOrdenes.getAndIncrement(), codMaquina, tipoAlarma, descripcion);
        ordenes.put(orden.getId(), orden);
        
        System.out.println("[Bodega] Orden generada: " + orden.getId());
        return orden.getId() + "#" + orden.toResumen();
    }

    @Override
    public String separarExistencias(int idOrden, int tipoAlarma, com.zeroc.Ice.Current current) {
        OrdenBodega orden = ordenes.get(idOrden);
        if (orden == null) 
            return "ERROR#- Orden no encontrada: " + idOrden;

        orden.setEstado(EstadoOrdenBodega.EN_PREPARACION);
        System.out.println("[Bodega] Orden " + idOrden + " en preparación");
        
        return "EN_PREPARACION#" + orden.toResumen() + "#tipoAlarma=" + tipoAlarma;
    }

    @Override
    public String entregarMateriales(int idOrden, int codMaquina, int tipoAlarma, com.zeroc.Ice.Current current) {
        OrdenBodega orden = ordenes.get(idOrden);
        if (orden == null)
            return "ERROR#- Orden de entrega no existe: " + idOrden;
        if (orden.getCodMaquina() != codMaquina)
            return "ERROR#- La orden no pertenece a la máquina " + codMaquina;
        if (orden.getEstado() != EstadoOrdenBodega.EN_PREPARACION)
            return "ERROR#- La orden debe estar en EN_PREPARACION (actual: " + orden.getEstado() + ")";

        String retiro = inventario.retirarParaAlarma(tipoAlarma);
        orden.setEstado(EstadoOrdenBodega.DESPACHADA);

        System.out.println("[Bodega] Materiales despachados para orden " + idOrden);
        return "DESPACHADA#" + orden.toResumen() + "#" + retiro;
    }

    @Override
    public void registrarRecepcionMateriales(int idOrden, String evidencia, com.zeroc.Ice.Current current) {
        OrdenBodega orden = ordenes.get(idOrden);
        if (orden != null) {
            orden.setEstado(EstadoOrdenBodega.RECIBIDA);
            orden.registrarEvidenciaRecepcion(evidencia);
            System.out.println("[Bodega] Recepción confirmada de orden " + idOrden);
        }
    }

    // === Métodos de consulta ===
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

    // === Otros métodos requeridos ===
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

    // === Métodos locales (interfaz Bodega) ===
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
        return separarExistencias(idOrden, tipoAlarma, null);
    }
}