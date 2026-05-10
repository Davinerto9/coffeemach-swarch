import com.zeroc.Ice.*;

import McControlador.ControladorMQ;
import McControlador.VentaService;
import alarma.AlarmaServiceImp;
import ingrediente.IngredienteServiceImp;
import suministro.SuministroServiceImp;

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
      
      // Service Layer Initialization
      AlarmaServiceImp alarmaService = new AlarmaServiceImp();
      alarmaService.setAlarmaService(alarmaS);
      
      IngredienteServiceImp ingredienteService = new IngredienteServiceImp();
      ingredienteService.setAlarmaService(alarmaService);
      
      VentaService ventaService = new VentaService();
      ventaService.setIngredienteService(ingredienteService);
      
      SuministroServiceImp suministroService = new SuministroServiceImp();

      ControladorMQ service = new ControladorMQ();
      service.setAlarmaService(alarmaS);
      service.setVentas(ventas);
      service.setRecetaServicePrx(recetaServicePrx);
      
      // Injecting new services into the controller
      service.setVentaService(ventaService);
      service.setIngredienteService(ingredienteService);
      service.setAlarmaServiceLocal(alarmaService);
      service.setSuministroService(suministroService);

      service.run();
      adapter.add((ServicioAbastecimiento) service, Util.stringToIdentity("abastecer"));
      adapter.activate();
      communicator.waitForShutdown();
    }
  }
}
