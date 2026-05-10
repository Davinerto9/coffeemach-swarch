package bodega;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representa una orden de entrega de materiales desde bodega.
 * Implementa el rol de Subject en el Observer Pattern.
 */
public class OrdenBodega {

    private final int id;
    private final int codMaquina;
    private final int tipoAlarma;
    private final String descripcion;
    private EstadoOrdenBodega estado;
    private final Date fechaCreacion;
    private Date fechaDespacho;
    private String evidenciaRecepcion;

    // === Observer Pattern ===
    private final List<OrdenObserver> observers = new ArrayList<>();

    public OrdenBodega(int id, int codMaquina, int tipoAlarma, String descripcion) {
        this.id = id;
        this.codMaquina = codMaquina;
        this.tipoAlarma = tipoAlarma;
        this.descripcion = descripcion;
        this.estado = EstadoOrdenBodega.REGISTRADA;
        this.fechaCreacion = new Date();
    }

    // ==================== Getters ====================
    public int getId() { return id; }
    public int getCodMaquina() { return codMaquina; }
    public int getTipoAlarma() { return tipoAlarma; }
    public String getDescripcion() { return descripcion; }
    public EstadoOrdenBodega getEstado() { return estado; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public Date getFechaDespacho() { return fechaDespacho; }
    public String getEvidenciaRecepcion() { return evidenciaRecepcion; }

    // ==================== Setters con notificación ====================
    public void setEstado(EstadoOrdenBodega nuevoEstado) {
        this.estado = nuevoEstado;
        if (nuevoEstado == EstadoOrdenBodega.DESPACHADA) {
            this.fechaDespacho = new Date();
        }
        notifyObservers();   // Notifica a todos los observers
    }

    public void registrarEvidenciaRecepcion(String evidencia) {
        this.evidenciaRecepcion = evidencia;
        notifyObservers();
    }

    // ==================== Observer Pattern Methods ====================
    public void addObserver(OrdenObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("[Bodega] Observer registrado en orden " + id + ": " 
                             + observer.getObserverName());
        }
    }

    public void removeObserver(OrdenObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        System.out.println("[Bodega] Notificando cambio → Orden " + id 
                         + " | Estado: " + estado);
        
        // Usamos copia para evitar problemas de modificación concurrente
        for (OrdenObserver observer : new ArrayList<>(observers)) {
            try {
                observer.onOrdenActualizada(this);
            } catch (Exception e) {
                System.err.println("[Bodega] Error notificando a " 
                                 + observer.getObserverName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Versión compacta para comunicación por Ice (una sola línea)
     */
    public String toResumen() {
        return "maq=" + codMaquina +
               "|tipo=" + tipoAlarma +
               "|estado=" + estado +
               "|creacion=" + fechaCreacion.getTime() +
               (fechaDespacho != null ? "|despacho=" + fechaDespacho.getTime() : "") +
               "|desc=" + descripcion.replace("#", "-").replace("|", "-");
    }

    @Override
    public String toString() {
        return "=== Orden de Bodega ===\n" +
               "ID Orden      : " + id + "\n" +
               "Máquina       : " + codMaquina + "\n" +
               "Tipo Alarma   : " + tipoAlarma + "\n" +
               "Descripción   : " + descripcion + "\n" +
               "Estado        : " + estado + "\n" +
               "Fecha Creación: " + fechaCreacion + "\n" +
               "Fecha Despacho: " + (fechaDespacho != null ? fechaDespacho : "Pendiente") + "\n" +
               "Evidencia     : " + (evidenciaRecepcion != null ? evidenciaRecepcion : "Sin registrar");
    }
}