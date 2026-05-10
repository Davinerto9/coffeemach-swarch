package bodega;

import java.util.Date;

public class OrdenEntrega {

    private int    id;
    private int    codMaquina;
    private int    tipoAlarma;
    private String descripcion;
    private EstadoOrdenEntrega estado;
    private Date   fechaCreacion;
    private Date   fechaDespacho;
    private String soporteRecepcion;   

    public OrdenEntrega(int id, int codMaquina, int tipoAlarma, String descripcion) {
        this.id            = id;
        this.codMaquina    = codMaquina;
        this.tipoAlarma    = tipoAlarma;
        this.descripcion   = descripcion;
        this.estado        = EstadoOrdenEntrega.REGISTRADA;
        this.fechaCreacion = new Date();
    }

    public int    getId()           { return id; }
    public int    getCodMaquina()   { return codMaquina; }
    public int    getTipoAlarma()   { return tipoAlarma; }
    public String getDescripcion()  { return descripcion; }
    public EstadoOrdenEntrega getEstado()  { return estado; }
    public Date   getFechaCreacion()       { return fechaCreacion; }
    public Date   getFechaDespacho()       { return fechaDespacho; }
    public String getSoporteRecepcion()    { return soporteRecepcion; }

    public void setEstado(EstadoOrdenEntrega estado) {
        this.estado = estado;
        if (estado == EstadoOrdenEntrega.DESPACHADA) {
            this.fechaDespacho = new Date();
        }
    }

    public void setSoporteRecepcion(String soporteRecepcion) {
        this.soporteRecepcion = soporteRecepcion;
    }

    /**
     * Resumen en una sola línea sin saltos de línea ni '#'.
     * Usado dentro de los retornos Ice para que split("#") funcione correctamente.
     */
    public String toResumen() {
        return "maq=" + codMaquina
             + " tipo=" + tipoAlarma
             + " estado=" + estado
             + " creacion=" + fechaCreacion
             + (fechaDespacho != null ? " despacho=" + fechaDespacho : "")
             + " desc=" + descripcion.replace("#", "-");
    }

    /** toString() legible para la consola del operador de bodega. */
    @Override
    public String toString() {
        return "Id Orden    : " + id            + "\n"
             + "Cod Maquina : " + codMaquina     + "\n"
             + "Tipo Alarma : " + tipoAlarma     + "\n"
             + "Descripcion : " + descripcion    + "\n"
             + "Estado      : " + estado         + "\n"
             + "Creacion    : " + fechaCreacion  + "\n"
             + "Despacho    : " + (fechaDespacho != null ? fechaDespacho : "pendiente");
    }
}