package alarma;

import java.util.Date;

import com.zeroc.Ice.Current;

import servicios.AlarmaService;
import servicios.Moneda;

public class Alarma implements AlarmaService {

    public static final int ALARMA_INGREDIENTE = 1;
    public static final int ALARMA_MONEDA_CIEN = 2;
    public static final int ALARMA_MONEDA_DOS = 3;
    public static final int ALARMA_MONEDA_QUI = 4;
    public static final int ALARMA_SUMINISTRO = 5;
    public static final int ALARMA_MAL_FUNCIONAMIENTO = 6;

    private AlarmasManager manager;

    public Alarma(AlarmasManager manager) {
        this.manager = manager;
    }

    @Override
    public void recibirNotificacionEscasezIngredientes(String iDing, int idMaq, Current current) {
        manager.alarmaMaquina(ALARMA_INGREDIENTE, idMaq, new Date());
    }

    @Override
    public void recibirNotificacionInsuficienciaMoneda(Moneda moneda, int idMaq, Current current) {
        switch (moneda) {
            case CIEN:
                manager.alarmaMaquina(ALARMA_MONEDA_CIEN, idMaq, new Date());
                break;
            case DOCIENTOS:
                manager.alarmaMaquina(ALARMA_MONEDA_DOS, idMaq, new Date());
                break;
            case QUINIENTOS:
                manager.alarmaMaquina(ALARMA_MONEDA_QUI, idMaq, new Date());
                break;
            default:
                break;
        }
    }

    @Override
    public void recibirNotificacionEscasezSuministro(String idSumin, int idMaq, Current current) {
        // suministro
        manager.alarmaMaquina(ALARMA_SUMINISTRO, idMaq, new Date());
    }

    @Override
    public void recibirNotificacionAbastesimiento(int idMaq, String idInsumo, int cantidad, Current current) {
        Integer tipoRecibido = parsearTipoAlarma(idInsumo);

        if (tipoRecibido == null) {
            System.out.println("[ServidorCentral] Abastecimiento recibido con idInsumo no numerico: "
                    + idInsumo + ", maquina " + idMaq);
            return;
        }

        System.out.println("[ServidorCentral] Abastecimiento recibido para maquina "
                + idMaq + ", tipo recibido " + tipoRecibido
                + ", cantidad " + cantidad);

        Date fechaFinal = new Date();
        int filasActualizadas = manager.desactivarAlarma(tipoRecibido, idMaq, fechaFinal);

        if (filasActualizadas > 0) {
            System.out.println("[ServidorCentral] Alarma cerrada con tipo exacto "
                    + tipoRecibido + " para maquina " + idMaq);
            return;
        }

        int tipoNormalizado = normalizarTipoAlarma(tipoRecibido);
        if (tipoNormalizado == tipoRecibido) {
            System.out.println("[ServidorCentral] No habia alarma abierta para tipo "
                    + tipoRecibido + " en maquina " + idMaq);
            return;
        }

        filasActualizadas = manager.desactivarAlarma(tipoNormalizado, idMaq, fechaFinal);
        System.out.println("[ServidorCentral] Fallback de cierre: tipo recibido "
                + tipoRecibido + ", tipo normalizado " + tipoNormalizado
                + ", filas cerradas " + filasActualizadas);
    }

    @Override
    public void recibirNotificacionMalFuncionamiento(int idMaq, String descri, Current current) {
        manager.alarmaMaquina(ALARMA_MAL_FUNCIONAMIENTO, idMaq, new Date());
    }

    private Integer parsearTipoAlarma(String idInsumo) {
        if (idInsumo == null) {
            return null;
        }

        try {
            return Integer.parseInt(idInsumo.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int normalizarTipoAlarma(int tipoAlarma) {
        switch (tipoAlarma) {
            case 1:
                return ALARMA_INGREDIENTE;
            case 2:
                return ALARMA_MONEDA_CIEN;
            case 3:
                return ALARMA_MONEDA_DOS;
            case 4:
                return ALARMA_MONEDA_QUI;
            case 5:
                return ALARMA_SUMINISTRO;
            case 6:
                return ALARMA_MAL_FUNCIONAMIENTO;
            case 7:
                return ALARMA_MONEDA_QUI;
            case 8:
            case 9:
            case 10:
            case 12:
            case 13:
            case 14:
                return ALARMA_INGREDIENTE;
            case 11:
            case 15:
                return ALARMA_SUMINISTRO;
            default:
                return tipoAlarma;
        }
    }

}
