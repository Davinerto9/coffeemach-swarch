package alarma;

import servicios.AlarmaServicePrx;

public class AlarmaServiceImp implements AlarmaService {

    private AlarmaServicePrx alarmaServicePrx;

    public void setAlarmaService(AlarmaServicePrx a) {
        alarmaServicePrx = a;
    }

    @Override
    public void notificarAbastecimiento() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarAbastecimiento'");
    }

    @Override
    public void notificarReparacion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarReparacion'");
    }

    @Override
    public void notificarEscasezSuministros() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarEscasezSuministros'");
    }

    @Override
    public void notificarError() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarError'");
    }

    @Override
    public void notificarAusenciaMoneda() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarAusenciaMoneda'");
    }

    @Override
    public void notificarEscazesIngredientes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarEscazesIngredientes'");
    }

    @Override
    public void notificarMalFuncionamiento() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarMalFuncionamiento'");
    }

}
