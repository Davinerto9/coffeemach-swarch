package controlAlarma;

public class AlarmaPendiente {

    private int    codMaquina;
    private String ubicacion;
    private String fechaInicial;
    private int    tipoAlarma;
    private String descripcion;

    public AlarmaPendiente(int codMaquina, String ubicacion,
                           String fechaInicial, int tipoAlarma,
                           String descripcion) {
        this.codMaquina   = codMaquina;
        this.ubicacion    = ubicacion;
        this.fechaInicial = fechaInicial;
        this.tipoAlarma   = tipoAlarma;
        this.descripcion  = descripcion;
    }

    /**
     * Formato que retorna el servidor:
     * "idMaquina#ubicacion#fechaInicial#idAlarma#descripcion"
     */
    public static AlarmaPendiente desdeCadena(String cadena) {
        String[] p = cadena.split("#");
        if (p.length < 5) {
            throw new IllegalArgumentException(
                    "Formato invalido, se esperaban 5 campos: " + cadena);
        }
        return new AlarmaPendiente(
                Integer.parseInt(p[0].trim()),
                p[1].trim(),
                p[2].trim(),
                Integer.parseInt(p[3].trim()),
                p[4].trim()
        );
    }

    public int    getCodMaquina()    { return codMaquina; }
    public String getUbicacion()     { return ubicacion; }
    public String getFechaInicial()  { return fechaInicial; }
    public int    getTipoAlarma()    { return tipoAlarma; }
    public String getDescripcion()   { return descripcion; }

    @Override
    public String toString() {
        return "Maquina " + codMaquina
                + " (" + ubicacion + ")"
                + " | Alarma " + tipoAlarma + ": " + descripcion
                + " | Desde: " + fechaInicial;
    }
}