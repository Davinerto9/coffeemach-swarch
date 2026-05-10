package tecnicoMantenimiento;

import controlAlarma.AlarmaPendiente;

public class OrdenTrabajo {

    private int consecutivo;
    private AlarmaPendiente alarma;
    private EstadoOrdenTrabajo estado;
    private String comprobanteBodega;

    public OrdenTrabajo(int consecutivo, AlarmaPendiente alarma) {
        this.consecutivo = consecutivo;
        this.alarma      = alarma;
        this.estado      = EstadoOrdenTrabajo.PENDIENTE;
    }

    public int getConsecutivo()           { return consecutivo; }
    public AlarmaPendiente getAlarma()    { return alarma; }
    public EstadoOrdenTrabajo getEstado() { return estado; }
    public String getComprobanteBodega()  { return comprobanteBodega; }

    public void setEstado(EstadoOrdenTrabajo estado) {
        this.estado = estado;
    }
    public void setComprobanteBodega(String comprobante) {
        this.comprobanteBodega = comprobante;
    }

    @Override
    public String toString() {
        return "OT#" + consecutivo
                + " | " + alarma
                + " | Estado: " + estado
                + (comprobanteBodega != null
                        ? " | Comp: " + comprobanteBodega : "");
    }
}