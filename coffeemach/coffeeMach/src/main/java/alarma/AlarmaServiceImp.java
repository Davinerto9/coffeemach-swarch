package alarma;

import servicios.AlarmaServicePrx;
import servicios.Moneda;

public class AlarmaServiceImp implements AlarmaService {

    private AlarmaServicePrx alarmaServicePrx;
    private int codMaquina;

    public void setAlarmaService(AlarmaServicePrx a) {
        this.alarmaServicePrx = a;
    }

    public void setCodMaquina(int codMaquina) {
        this.codMaquina = codMaquina;
    }

    @Override
    public void notificarAbastecimiento() {
        // En este sistema, el abastecimiento se notifica desde el servidor hacia la maquina,
        // o la maquina confirma el abastecimiento recibido.
        System.out.println("[CoffeeMach] AlarmaServiceImp.notificarAbastecimiento: operacion no mapeada directamente.");
    }

    @Override
    public void notificarReparacion() {
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionMalFuncionamiento(codMaquina, "Se requiere reparacion");
        }
    }

    @Override
    public void notificarEscasezSuministros() {
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionEscasezSuministro("suministro", codMaquina);
        }
    }

    @Override
    public void notificarError() {
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionMalFuncionamiento(codMaquina, "Error general detectado");
        }
    }

    @Override
    public void notificarAusenciaMoneda() {
        if (alarmaServicePrx != null) {
            // Por defecto 100 si no hay contexto, mejorado en ControladorMQ
            alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.CIEN, codMaquina);
        }
    }

    @Override
    public void notificarEscazesIngredientes() {
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionEscasezIngredientes("ingrediente", codMaquina);
        }
    }

    @Override
    public void notificarMalFuncionamiento() {
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionMalFuncionamiento(codMaquina, "Mal funcionamiento detectado");
        }
    }

}
