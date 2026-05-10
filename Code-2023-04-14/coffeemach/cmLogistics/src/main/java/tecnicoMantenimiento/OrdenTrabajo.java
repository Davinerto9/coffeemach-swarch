package tecnicoMantenimiento;

import controlAlarma.AlarmaPendiente;

public class OrdenTrabajo {

    private int consecutivo;
    private AlarmaPendiente alarma;
    private OrdenMantenimiento estado;
    private String comprobanteBodega;

    public OrdenTrabajo(int consecutivo, AlarmaPendiente alarma) {
        this.consecutivo = consecutivo;
        this.alarma = alarma;
        this.estado = OrdenMantenimiento.PENDIENTE;
    }

    public int getConsecutivo() {
        return consecutivo;
    }

    public AlarmaPendiente getAlarma() {
        return alarma;
    }

    public OrdenMantenimiento getEstado() {
        return estado;
    }

    public String getComprobanteBodega() {
        return comprobanteBodega;
    }

    public void setEstado(OrdenMantenimiento estado) {
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
                        ? " | Comp: " + comprobanteBodega
                        : "");
    }
}