package controlAlarma;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import servicios.ServicioAbastecimientoPrx;
import servicios.ServicioBodegaPrx;
import servicios.ServicioComLogisticaPrx;
import tecnicoMantenimiento.EstadoOrdenTrabajo;
import tecnicoMantenimiento.OrdenTrabajo;
import zonaGeografica.ZonaGeografica;

public class ControladorAlarmas {

    private final ServicioComLogisticaPrx servidorCentral;
    private final ServicioAbastecimientoPrx maquinaCafe;
    private final ServicioBodegaPrx bodegaCentral;
    private final ZonaGeografica zonaGeografica = new ZonaGeografica();
    private final AtomicInteger consecutivoOrdenTrabajo = new AtomicInteger(1);

    private int codigoOperadorActivo = -1;

    public ControladorAlarmas(ServicioComLogisticaPrx servidorCentral,
                               ServicioAbastecimientoPrx maquinaCafe,
                               ServicioBodegaPrx bodegaCentral) {
        this.servidorCentral = servidorCentral;
        this.maquinaCafe     = maquinaCafe;
        this.bodegaCentral   = bodegaCentral;
    }

    public boolean iniciarSesion(int codigoOperador, String password) {
        boolean ok = servidorCentral.inicioSesion(codigoOperador, password);
        if (ok) this.codigoOperadorActivo = codigoOperador;
        return ok;
    }

    private void verificarSesion() {
        if (codigoOperadorActivo == -1)
            throw new IllegalStateException("Operacion rechazada: no hay sesion activa.");
    }

    public List<String> consultarMaquinasAsignadas() {
        verificarSesion();
        return servidorCentral.asignacionMaquina(codigoOperadorActivo);
    }

    public List<AlarmaPendiente> consultarAlarmasPendientes() {
        verificarSesion();
        List<String> datos = servidorCentral
                .asignacionMaquinasDesabastecidas(codigoOperadorActivo);
        List<AlarmaPendiente> alarmas = new ArrayList<>();
        if (datos == null) return alarmas;
        for (String dato : datos) {
            try {
                alarmas.add(AlarmaPendiente.desdeCadena(dato));
            } catch (Exception e) {
                System.err.println("Formato de alarma inesperado [" + dato
                        + "]: " + e.getMessage());
            }
        }
        return alarmas;
    }

    public OrdenTrabajo generarOrdenTrabajo(AlarmaPendiente alarma) {
        verificarSesion();
        return new OrdenTrabajo(consecutivoOrdenTrabajo.getAndIncrement(), alarma);
    }

    public String resolverAlarma(OrdenTrabajo ordenTrabajo) {
        verificarSesion();
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.EN_PROCESO);

        try {
            // Paso 1: calcular ruta
            String ruta = calcularRuta(ordenTrabajo);

            // Paso 2: registrar orden en bodega (→ REGISTRADA)
            String ordenEntrega = solicitarMaterialesABodega(ordenTrabajo);
            int idOrden = extraerIdOrdenEntrega(ordenEntrega);

            // Paso 3: separar existencias (→ EN_PICKING)  ← NUEVO
            separarExistenciasEnBodega(idOrden, ordenTrabajo);

            // Paso 4: despachar materiales (→ DESPACHADA)
            String comprobante = despacharMateriales(idOrden, ordenTrabajo);

            // Paso 5: notificar máquina
            notificarMaquina(ordenTrabajo);

            // Paso 6: confirmar recepción en bodega (→ RECIBIDA)
            confirmarRecepcionEnBodega(idOrden, ordenTrabajo);

            ordenTrabajo.setComprobanteBodega(comprobante);
            ordenTrabajo.setEstado(EstadoOrdenTrabajo.RESUELTA);

            return construirReporte(ruta, ordenTrabajo, ordenEntrega, comprobante);

        } catch (Exception e) {
            ordenTrabajo.setEstado(EstadoOrdenTrabajo.CANCELADA);
            throw new RuntimeException(
                "Fallo al resolver OT#" + ordenTrabajo.getConsecutivo()
                + ": " + e.getMessage(), e);
        }
    }

    // ── Pasos privados ───────────────────────────────────────────────────────

    private String calcularRuta(OrdenTrabajo ot) {
        return zonaGeografica.calcularRuta(ot.getAlarma().getUbicacion());
    }

    private String solicitarMaterialesABodega(OrdenTrabajo ot) {
        AlarmaPendiente alarma = ot.getAlarma();
        return bodegaCentral.generarOrdenEntrega(
                alarma.getCodMaquina(),
                alarma.getTipoAlarma(),
                alarma.getDescripcion());
    }

    // NUEVO: llama separarExistencias y valida que bodega haya aceptado el picking
    private void separarExistenciasEnBodega(int idOrden, OrdenTrabajo ot) {
        String respuesta = bodegaCentral.separarExistencias(
                idOrden,
                ot.getAlarma().getTipoAlarma());
        if (respuesta == null || respuesta.startsWith("ERROR")) {
            throw new RuntimeException(
                "Bodega rechazo el picking para OT#"
                + ot.getConsecutivo() + ": " + respuesta);
        }
    }

    private String despacharMateriales(int idOrden, OrdenTrabajo ot) {
        AlarmaPendiente alarma = ot.getAlarma();
        return bodegaCentral.entregarMateriales(
                idOrden,
                alarma.getCodMaquina(),
                alarma.getTipoAlarma());
    }

    private void notificarMaquina(OrdenTrabajo ot) {
        AlarmaPendiente alarma = ot.getAlarma();
        maquinaCafe.abastecer(alarma.getCodMaquina(), alarma.getTipoAlarma());
    }

    private void confirmarRecepcionEnBodega(int idOrden, OrdenTrabajo ot) {
        bodegaCentral.registrarRecepcionMateriales(
                idOrden,
                "Recepcion confirmada por operador "
                + codigoOperadorActivo + " para " + ot);
    }

    private String construirReporte(String ruta, OrdenTrabajo ot,
                                     String ordenEntrega, String comprobante) {
        return ruta
                + "\n" + ot
                + "\nOrden entrega: " + ordenEntrega
                + "\nComprobante: "   + comprobante;
    }

    private int extraerIdOrdenEntrega(String ordenEntrega) {
        if (ordenEntrega == null || ordenEntrega.isBlank())
            throw new IllegalArgumentException(
                "Bodega retorno una orden de entrega vacia.");
        String[] partes = ordenEntrega.split("#");
        try {
            return Integer.parseInt(partes[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "ID de orden no numerico en respuesta: '" + ordenEntrega + "'");
        }
    }

    public List<String> consultarInventarioBodega() {
        verificarSesion();
        return bodegaCentral.consultarInventario();
    }

    public int getCodigoOperadorActivo() {
        return codigoOperadorActivo;
    }
}