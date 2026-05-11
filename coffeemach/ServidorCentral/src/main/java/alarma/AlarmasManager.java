package alarma;

import java.util.Date;
import java.util.List;

import com.zeroc.Ice.Communicator;

import modelo.AlarmaMaquina;
import modelo.ConexionBD;
import modelo.ManejadorDatos;

public class AlarmasManager {

    private Communicator comunicator;

    public AlarmasManager(Communicator communicator) {
        this.comunicator = communicator;
    }

    public String alarmaMaquina(int idAlarma, int idMaquina, Date fechainicial) {
        ConexionBD cbd = new ConexionBD(comunicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());

        String alarma = md.darNombreAlarma(idAlarma);
        String infoOperador = md.darOperador(idMaquina);

        if (alarma != null && infoOperador != null) {
            String[] parts = infoOperador.split("#");
            String operador = parts[0];
            String correo = parts.length > 1 ? parts[1] : "N/A";

            AlarmaMaquina aM = new AlarmaMaquina(idAlarma, idMaquina,
                    fechainicial);
            md.registrarAlarma(aM);
            cbd.cerrarConexion();

            String msg = "NOTIFICACIÓN: Fallo de máquina " + idMaquina + " - Alarma: " + alarma + " - Asignado a: " + operador + " (" + correo + ")";
            System.out.println(msg);
            return msg;
        }
        cbd.cerrarConexion();
        return "Alarma recibida para máquina " + idMaquina + ", pero no se encontró operador asignado.";
    }

    public List<String> getActiveAlarms() {
        ConexionBD cbd = new ConexionBD(comunicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());
        List<String> alarmas = md.darAlarmasActivas();
        cbd.cerrarConexion();
        return alarmas;
    }

    public void desactivarAlarma(int idAlarma, int idMaquina, Date fechaFinal) {
        ConexionBD cbd = new ConexionBD(comunicator);
        cbd.conectarBaseDatos();
        ManejadorDatos md = new ManejadorDatos();
        md.setConexion(cbd.getConnection());
        md.desactivarAlarma(idMaquina, idAlarma, fechaFinal);
        cbd.cerrarConexion();
    }

}
