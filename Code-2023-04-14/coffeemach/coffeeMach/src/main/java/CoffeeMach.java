import com.zeroc.Ice.*;

import McControlador.ControladorMQ;

import java.util.*;
import servicios.*;

public class CoffeeMach {
  public static void main(String[] args) {
    List<String> extPar = new ArrayList<>();
    try (Communicator communicator = Util.initialize(args, "coffeMach.cfg", extPar)) {

      AlarmaServicePrx alarmaS = AlarmaServicePrx.checkedCast(
          communicator.propertyToProxy("alarmas")).ice_twoway();
      VentaServicePrx ventas = VentaServicePrx.checkedCast(
          communicator.propertyToProxy("ventas")).ice_twoway();
      RecetaServicePrx recetaServicePrx = RecetaServicePrx.checkedCast(
          communicator.propertyToProxy("recetas")).ice_twoway();

      ObjectAdapter adapter = communicator.createObjectAdapter("CoffeMach");
      ControladorMQ service = new ControladorMQ();
      service.setAlarmaService(alarmaS);
      service.setVentas(ventas);
      service.setRecetaServicePrx(recetaServicePrx);
      service.setCodMaquinaConfigurado(leerCodMaquinaConfigurado(communicator));
      service.inicializarCodMaquina();

      service.run();
      adapter.add((ServicioAbastecimiento) service, Util.stringToIdentity("abastecer"));
      adapter.activate();
      communicator.waitForShutdown();
    }
  }

  private static Integer leerCodMaquinaConfigurado(Communicator communicator) {
    String valor = communicator.getProperties().getProperty("CoffeeMach.CodMaquina");
    if (valor == null || valor.trim().isEmpty()) {
      return null;
    }

    try {
      int codMaquina = Integer.parseInt(valor.trim());
      if (codMaquina > 0) {
        System.out.println("[CoffeeMach] Código de máquina cargado desde configuración: " + codMaquina);
        return codMaquina;
      }
      System.out.println("[CoffeeMach] CoffeeMach.CodMaquina debe ser mayor que 0: " + valor);
    } catch (NumberFormatException e) {
      System.out.println("[CoffeeMach] CoffeeMach.CodMaquina no es numérico: " + valor);
    }
    return null;
  }
}
