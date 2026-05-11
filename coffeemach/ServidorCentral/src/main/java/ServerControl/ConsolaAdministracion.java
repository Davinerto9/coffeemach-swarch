package ServerControl;

import java.util.Scanner;
import java.util.List;

import modelo.Operador;

public class ConsolaAdministracion implements Runnable{

	private ServerControl control;
	
	public ConsolaAdministracion(ServerControl control)
	{
		this.control=control;
	}

	@Override
	public void run() 
	{
		
		while(true)
		{
			Scanner lector=new Scanner(System.in);
			
			System.out.println("****************************");
			System.out.println("Consola de administración");
			System.out.println("****************************");
			
			System.out.println("\nOpciones:" +
					"\n1.Agregar un nuevo operador" +
					"\n2.Agregar una nueva maquina" +
					"\n3.Asignar maquina a operador" +
					"\n4.Estado de alarmas" +
					"\n5.Agregar nueva receta");
			int valor=lector.nextInt();
			
			switch (valor) {
				case 1:
					lector.nextLine();
					System.out.println(">>Digite el código del operador");
					int codOp = lector.nextInt();
					lector.nextLine();
					System.out.println(">>Digite el nombre del operador");
					String nombre = lector.nextLine();
					System.out.println(">>Digite el correo del operador");
					String correo = lector.nextLine();
					System.out.println(">>Digite la contraseña del operador");
					String pass = lector.nextLine();

					if (control.registrarOperador(codOp, nombre, correo, pass)) {
						System.out.println("Operador registrado con éxito");
					} else {
						System.out.println("Error al registrar operador");
					}
					break;
				case 2:
					System.out.println(">>Digite el código de la maquina");
					int codMaq = lector.nextInt();
					lector.nextLine();
					System.out.println(">>Digite la ubicación de la maquina");
					String ubicacion = lector.nextLine();

					if (control.registrarMaquina(codMaq, ubicacion)) {
						System.out.println("Maquina registrada con éxito");
					} else {
						System.out.println("Error al registrar maquina");
					}
					break;
				case 3:
					System.out.println(">>Digite el código de la maquina");
					int idMaq = lector.nextInt();
					System.out.println(">>Digite el código del operador");
					int idOp = lector.nextInt();

					if (control.asignarOperador(idMaq, idOp)) {
						System.out.println("Asignación realizada con éxito");
					} else {
						System.out.println("Error al realizar asignación");
					}
					break;
				case 4:
					System.out.println("=== Alarmas Activas ===");
					List<String> alarmas = control.darAlarmasActivas();
					if (alarmas.isEmpty()) {
						System.out.println("No hay alarmas activas.");
					} else {
						for (String al : alarmas) {
							System.out.println(al);
						}
					}
					break;
				case 5:
					lector.nextLine();
					System.out.println(">>Digite el nombre de la receta");
					String nomRec = lector.nextLine();
					System.out.println(">>Digite el precio de la receta");
					int precio = lector.nextInt();

					String res = control.registrarReceta(nomRec, precio);
					if (!res.isEmpty()) {
						int idReceta = Integer.parseInt(res.split("-")[0]);
						System.out.println("Receta creada con ID: " + idReceta);

						String[] ingredientes = control.consultarIngredientes();
						System.out.println("Ingredientes disponibles:");
						for (String ing : ingredientes) {
							System.out.println(ing);
						}

						while (true) {
							System.out.println(">>Digite ID de ingrediente a añadir (o -1 para terminar)");
							int idIng = lector.nextInt();
							if (idIng == -1) break;
							System.out.println(">>Digite cantidad de unidades");
							int cant = lector.nextInt();
							control.registrarRecetaIngrediente(idReceta, idIng, cant);
						}
						System.out.println("Receta completada.");
					} else {
						System.out.println("Error al crear receta");
					}
					break;
				default:
					System.out.println("¡¡¡Opción incorrecta seleccionada!!!");
					break;
			}
		}
		
	}
}
