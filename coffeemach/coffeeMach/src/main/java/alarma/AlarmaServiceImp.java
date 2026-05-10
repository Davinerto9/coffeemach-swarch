package alarma;

import servicios.AlarmaServicePrx;
import java.util.Date;

public class AlarmaServiceImp implements AlarmaService {

    private AlarmaServicePrx alarmaServicePrx;
    private AlarmaRepositorio localRepo = AlarmaRepositorio.getInstance();
    private int codMaquina;

    public void setAlarmaService(AlarmaServicePrx a) {
        alarmaServicePrx = a;
    }
    
    public void setCodMaquina(int codMaquina) {
        this.codMaquina = codMaquina;
    }

    @Override
    public void notificarAbastecimiento() {
        registrarAlarmaLocal("REFILL", "Abastecimiento realizado");
    }

    @Override
    public void notificarReparacion() {
        registrarAlarmaLocal("REPAIR", "Reparación realizada");
    }

    @Override
    public void notificarEscasezSuministros() {
        registrarAlarmaLocal("LOW_SUPPLY", "Escasez de suministros");
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionEscasezSuministro("Insumos varios", codMaquina);
        }
    }

    @Override
    public void notificarError() {
        registrarAlarmaLocal("ERROR", "Error general en la máquina");
    }

    @Override
    public void notificarAusenciaMoneda() {
        registrarAlarmaLocal("NO_CASH", "Insuficiencia de moneda");
    }

    @Override
    public void notificarEscazesIngredientes() {
        registrarAlarmaLocal("LOW_INGREDIENTS", "Escasez de ingredientes");
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionEscasezIngredientes("Ingredientes", codMaquina);
        }
    }

    @Override
    public void notificarMalFuncionamiento() {
        registrarAlarmaLocal("MALFUNCTION", "Mal funcionamiento detectado");
        if (alarmaServicePrx != null) {
            alarmaServicePrx.recibirNotificacionMalFuncionamiento(codMaquina, "Se requiere mantenimiento técnico");
        }
    }

    private void registrarAlarmaLocal(String id, String mensaje) {
        Alarma a = new Alarma(id, mensaje, new Date());
        localRepo.addElement(id, a);
    }
}
