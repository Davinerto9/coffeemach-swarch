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

    private final OrdenObserver ordenLogger = new OrdenLoggerObserver();

    public BodegaServiceImpl(Inventario inventario) {
        this.inventario = inventario;
    }

    @Override
public String generarOrdenEntrega(int codMaquina, int tipoAlarma, String descripcion, com.zeroc.Ice.Current current) {
    OrdenBodega orden = new OrdenBodega(
        contadorOrdenes.getAndIncrement(),
        codMaquina,
        tipoAlarma,
        descripcion
    );

    orden.addObserver(ordenLogger);

    ordenes.put(orden.getId(), orden);

    System.out.println("[Bodega] Orden generada. idOrden=" + orden.getId()
        + ", maquina=" + codMaquina
        + ", tipoAlarma=" + tipoAlarma
        + ", descripcion=" + descripcion);

    return orden.getId() + "#" + orden.toResumen();
}

   @Override
public String separarExistencias(int idOrden, int tipoAlarma, com.zeroc.Ice.Current current) {
    OrdenBodega orden = ordenes.get(idOrden);

    if (orden == null) {
        String error = "Orden no encontrada: " + idOrden;
        System.err.println("[Bodega] ERROR separarExistencias: " + error);
        return "ERROR#- " + error;
    }

    if (orden.getEstado() != EstadoOrdenBodega.REGISTRADA) {
        String error = "La orden debe estar en REGISTRADA para separar existencias. Estado actual: " + orden.getEstado();
        System.err.println("[Bodega] ERROR separarExistencias: idOrden="
            + idOrden + ", " + error);
        return "ERROR#- " + error;
    }

    orden.setEstado(EstadoOrdenBodega.EN_PREPARACION);
    System.out.println("[Bodega] Orden " + idOrden
        + " en EN_PREPARACION para tipoAlarma=" + tipoAlarma);

    orden.setEstado(EstadoOrdenBodega.LISTA_PARA_DESPACHO);
    System.out.println("[Bodega] Orden " + idOrden
        + " en LISTA_PARA_DESPACHO");

    return "LISTA_PARA_DESPACHO#" + orden.toResumen() + "#tipoAlarma=" + tipoAlarma;
}

   @Override
public String entregarMateriales(int idOrden, int codMaquina, int tipoAlarma, com.zeroc.Ice.Current current) {
    OrdenBodega orden = ordenes.get(idOrden);

    if (orden == null) {
        String error = "Orden de entrega no existe: " + idOrden;
        System.err.println("[Bodega] ERROR entregarMateriales: " + error);
        return "ERROR#- " + error;
    }

    if (orden.getCodMaquina() != codMaquina) {
        String error = "La orden " + idOrden + " pertenece a la maquina "
            + orden.getCodMaquina() + ", no a " + codMaquina;
        System.err.println("[Bodega] ERROR entregarMateriales: " + error);
        return "ERROR#- " + error;
    }

    if (orden.getEstado() != EstadoOrdenBodega.LISTA_PARA_DESPACHO) {
        String error = "La orden debe estar en LISTA_PARA_DESPACHO. Estado actual: " + orden.getEstado();
        System.err.println("[Bodega] ERROR entregarMateriales: idOrden="
            + idOrden + ", " + error);
        return "ERROR#- " + error;
    }

    String retiro = inventario.retirarParaAlarma(tipoAlarma);

    if (retiro == null || retiro.isBlank() || retiro.startsWith("ERROR")) {
        String error = "No se pudieron retirar existencias para la alarma "
            + tipoAlarma + ". Detalle: " + retiro;
        System.err.println("[Bodega] ERROR entregarMateriales: idOrden="
            + idOrden + ", " + error);
        return "ERROR#- " + error;
    }

    orden.setEstado(EstadoOrdenBodega.DESPACHADA);

    System.out.println("[Bodega] Materiales despachados. idOrden=" + idOrden
        + ", maquina=" + codMaquina
        + ", tipoAlarma=" + tipoAlarma
        + ", recursoRetirado=" + retiro);
    return "DESPACHADA#" + orden.toResumen() + "#" + retiro;
}

    @Override
    public void registrarRecepcionMateriales(int idOrden, String evidencia, com.zeroc.Ice.Current current) {
        OrdenBodega orden = ordenes.get(idOrden);
        if (orden == null) {
            System.err.println("[Bodega] ERROR registrarRecepcionMateriales: orden no encontrada "
                + idOrden + ", evidencia=" + evidencia);
            return;
        }
        orden.setEstado(EstadoOrdenBodega.RECIBIDA);
        orden.registrarEvidenciaRecepcion(evidencia);
        System.out.println("[Bodega] Recepcion confirmada. idOrden=" + idOrden
            + ", estado=" + orden.getEstado()
            + ", evidencia=" + evidencia);
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
