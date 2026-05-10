package alarma;

import servicios.AlarmaServicePrx;

public class AlarmaServiceImp implements AlarmaService {

    private AlarmaServicePrx alarmaServicePrx;

    public void setAlarmaService(AlarmaServicePrx a) {
        alarmaServicePrx = a;
    }

    /*
     * Capa local secundaria. El flujo real de notificacion remota ocurre desde
     * McControlador.ControladorMQ usando servicios.AlarmaServicePrx con el
     * contexto completo de maquina, tipo de alarma e insumo.
     */
    @Override
    public void notificarAbastecimiento() {
        logNoOp("notificarAbastecimiento",
                "falta contexto de maquina, tipo de alarma y cantidad");
    }

    @Override
    public void notificarReparacion() {
        logNoOp("notificarReparacion",
                "falta contexto de maquina y descripcion de reparacion");
    }

    @Override
    public void notificarEscasezSuministros() {
        logNoOp("notificarEscasezSuministros",
                "falta contexto de maquina e identificador de suministro");
    }

    @Override
    public void notificarError() {
        logNoOp("notificarError",
                "falta contexto de maquina y detalle del error");
    }

    @Override
    public void notificarAusenciaMoneda() {
        logNoOp("notificarAusenciaMoneda",
                "falta contexto de maquina y denominacion de moneda");
    }

    @Override
    public void notificarEscazesIngredientes() {
        logNoOp("notificarEscazesIngredientes",
                "falta contexto de maquina e identificador de ingrediente");
    }

    @Override
    public void notificarMalFuncionamiento() {
        logNoOp("notificarMalFuncionamiento",
                "falta contexto de maquina y descripcion del mal funcionamiento");
    }

    private void logNoOp(String operacion, String motivo) {
        System.out.println("[CoffeeMach] AlarmaServiceImp." + operacion
                + " no ejecuta notificacion remota: " + motivo + ".");
    }

}
