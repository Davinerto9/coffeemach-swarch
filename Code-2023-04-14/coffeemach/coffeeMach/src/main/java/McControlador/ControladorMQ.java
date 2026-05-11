package McControlador;

import servicios.*;
import monedero.DepositoMonedas;
import monedero.MonedasRepositorio;
import productoReceta.Receta;
import productoReceta.RecetaRepositorio;

import java.util.*;
import java.io.*;
import java.util.Map.Entry;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.*;
import interfazUsuario.Interfaz;
import com.zeroc.Ice.Current;

import alarma.Alarma;
import alarma.AlarmaRepositorio;
import ingrediente.Ingrediente;
import ingrediente.IngredienteRepositorio;

public class ControladorMQ implements Runnable, ServicioAbastecimiento {

	private AlarmaServicePrx alarmaServicePrx;
	private VentaServicePrx ventasService;

	// @Reference
	private AlarmaRepositorio alarmas = AlarmaRepositorio.getInstance();
	// @Reference
	private IngredienteRepositorio ingredientes = IngredienteRepositorio.getInstance();
	// @Reference
	private MonedasRepositorio monedas = MonedasRepositorio.getInstance();
	// @Reference
	private RecetaRepositorio recetas = RecetaRepositorio.getInstance();
	// @Referenc
	private VentaRepositorio ventas = VentaRepositorio.getInstance();

	/**
	 * @param ventas the ventas to set
	 */
	public void setVentas(VentaServicePrx ventasS) {
		this.ventasService = ventasS;
	}

	public void setAlarmaService(AlarmaServicePrx a) {
		alarmaServicePrx = a;
	}

	private RecetaServicePrx recetaServicePrx;

	/**
	 * @param recetaServicePrx the recetaServicePrx to set
	 */
	public void setRecetaServicePrx(RecetaServicePrx recetaServicePrx) {
		this.recetaServicePrx = recetaServicePrx;
	}

	private Interfaz frame;
	private int codMaquina;
	private Integer codMaquinaConfigurado;
	private double suma;

	public void run() {

		try {
			frame = new Interfaz();
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		arrancarMaquina();
		eventos();
	}

	public void setCodMaquinaConfigurado(Integer codMaquinaConfigurado) {
		this.codMaquinaConfigurado = codMaquinaConfigurado;
	}

	public void inicializarCodMaquina() {
		codMaquina = cargarCodMaquina();
		if (codMaquina <= 0) {
			String mensaje = "[CoffeeMach] No se pudo determinar codMaquina. Configure CoffeeMach.CodMaquina o cree codMaquina.cafe.";
			System.out.println(mensaje);
			throw new IllegalStateException(mensaje);
		}
	}

	private void ejecutarEnEDT(Runnable accion) {
		if (SwingUtilities.isEventDispatchThread()) {
			accion.run();
		} else {
			SwingUtilities.invokeLater(accion);
		}
	}

	private void ejecutarRemotoAsync(final String nombreOperacion, final Runnable operacion) {
		System.out.println("[CoffeeMach] Notificación remota programada: " + nombreOperacion);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					operacion.run();
					System.out.println("[CoffeeMach] Notificación remota completada: " + nombreOperacion);
				} catch (Exception e) {
					System.err.println("[CoffeeMach] Error en notificación remota "
							+ nombreOperacion + ": " + e);
					ejecutarEnEDT(new Runnable() {
						public void run() {
							if (frame != null && frame.getTextAreaAlarmas() != null) {
								frame.getTextAreaAlarmas().append("[CoffeeMach] Error notificando al servidor: "
										+ nombreOperacion + "\n");
							}
						}
					});
				}
			}
		}, "CoffeeMach-RemoteNotify-" + nombreOperacion);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void abastecer(int codMaquina, int idAlarma, Current current) {
		int cantidad = 0;
		System.out.println("[CoffeeMach] Solicitud de abastecimiento recibida. maquina="
				+ codMaquina + ", tipoAlarma=" + idAlarma
				+ ", maquinaLocal=" + this.codMaquina);

		if (codMaquina != this.codMaquina) {
			String mensaje = "[CoffeeMach] Solicitud rechazada: maquina destino "
					+ codMaquina + " no coincide con maquina local "
					+ this.codMaquina;
			System.out.println(mensaje);
			throw new RuntimeException(mensaje);
		}

		if (alarmaServicePrx == null) {
			String mensaje = "[CoffeeMach] No se puede resolver la alarma: alarmaServicePrx es null.";
			System.out.println(mensaje);
			throw new RuntimeException(mensaje);
		}

		switch (idAlarma) {
			case 1:
				cantidad += recargarIngredienteConCantidad("Agua");
				cantidad += recargarIngredienteConCantidad("Cafe");
				cantidad += recargarIngredienteConCantidad("Azucar");
				System.out.println("[CoffeeMach] Tipo 1 recibido: recarga conservadora de ingredientes principales.");
				break;
			case 2:
				cantidad = recargarMoneda("100", 20);
				break;
			case 3:
				cantidad = recargarMoneda("200", 20);
				break;
			case 4:
				cantidad = recargarMoneda("500", 20);
				break;
			case 5:
				cantidad = recargarIngredienteConCantidad("Vaso");
				break;
			case 6:
				System.out.println("[CoffeeMach] Tipo 6 recibido: mantenimiento/mal funcionamiento atendido.");
				break;
			case 7:
				cantidad = recargarMoneda("500", 20);
				break;
			case 8:
			case 12:
				cantidad = recargarIngredienteConCantidad("Agua");
				break;
			case 9:
			case 13:
				cantidad = recargarIngredienteConCantidad("Cafe");
				break;
			case 10:
			case 14:
				cantidad = recargarIngredienteConCantidad("Azucar");
				break;
			case 11:
			case 15:
				cantidad = recargarIngredienteConCantidad("Vaso");
				break;
			default:
				String mensaje = "[CoffeeMach] Tipo de alarma no reconocido: "
						+ idAlarma + ". No se recargo ningun recurso.";
				System.out.println(mensaje);
				throw new RuntimeException(mensaje);
		}

		System.out.println("[CoffeeMach] Tipo central recibido: " + idAlarma);
		quitarAlarmasLocalesRelacionadas(idAlarma);

		boolean habilitarInterfaz = alarmas.getValues().isEmpty() && frame != null;
		if (habilitarInterfaz) {
			System.out.println("[CoffeeMach] UI se habilita porque no quedan alarmas locales.");
		} else {
			System.out.println("[CoffeeMach] UI permanece en alarma porque aun existen alarmas locales: "
					+ alarmas.getkeys());
		}

		// Respaldo
		respaldarMaq();
		ejecutarEnEDT(new Runnable() {
			public void run() {
				System.out.println("[CoffeeMach] Refrescando interfaz desde EDT. enEDT="
						+ SwingUtilities.isEventDispatchThread());

				if (habilitarInterfaz) {
					frame.setEnabled(true);
					frame.interfazHabilitada();

					System.out.println("[CoffeeMach] Interfaz habilitada: no quedan alarmas locales.");
				}

				actualizarRecetasGraf();
				actualizarInsumosGraf();
				actualizarAlarmasGraf();
			}
		});

		// ResetAlarmas

		// Envio a Servidor
		alarmaServicePrx.recibirNotificacionAbastesimiento(codMaquina, idAlarma + "", cantidad);
		System.out.println("[CoffeeMach] Notificacion de abastecimiento enviada. maquina="
				+ codMaquina + ", tipoAlarma=" + idAlarma
				+ ", cantidad=" + cantidad);
	}

	public void quitarAlarma(String tipo) {
		alarmas.removeElement(tipo);
	}

	private void quitarAlarmasLocalesRelacionadas(int tipoAlarmaCentralOlocal) {
		List<String> alarmasRemover = new ArrayList<String>();

		switch (tipoAlarmaCentralOlocal) {
			case 1:
				if (existeAlgunaAlarma("8", "9", "10", "12", "13", "14")) {
					agregarAlarmas(alarmasRemover, "8", "9", "10", "12", "13", "14");
				} else {
					agregarAlarmas(alarmasRemover, "1");
				}
				break;
			case 2:
				agregarAlarmas(alarmasRemover, "2", "3");
				break;
			case 3:
				agregarAlarmas(alarmasRemover, "4", "5");
				break;
			case 4:
				agregarAlarmas(alarmasRemover, "6", "7");
				break;
			case 5:
				agregarAlarmas(alarmasRemover, "11", "15");
				break;
			case 6:
				agregarAlarmas(alarmasRemover, "1");
				break;
			case 7:
				agregarAlarmas(alarmasRemover, "6", "7");
				break;
			case 8:
			case 12:
				agregarAlarmas(alarmasRemover, "8", "12");
				break;
			case 9:
			case 13:
				agregarAlarmas(alarmasRemover, "9", "13");
				break;
			case 10:
			case 14:
				agregarAlarmas(alarmasRemover, "10", "14");
				break;
			case 11:
			case 15:
				agregarAlarmas(alarmasRemover, "11", "15");
				break;
			default:
				agregarAlarmas(alarmasRemover, tipoAlarmaCentralOlocal + "");
				break;
		}

		List<String> removidas = new ArrayList<String>();
		for (String tipo : alarmasRemover) {
			if (alarmas.findByKey(tipo) != null) {
				quitarAlarma(tipo);
				removidas.add(tipo);
			}
		}

		System.out.println("[CoffeeMach] Alarmas locales removidas: " + removidas);
		System.out.println("[CoffeeMach] Alarmas locales restantes: " + alarmas.getValues().size());
	}

	private boolean existeAlgunaAlarma(String... tipos) {
		for (String tipo : tipos) {
			if (alarmas.findByKey(tipo) != null) {
				return true;
			}
		}
		return false;
	}

	private void agregarAlarmas(List<String> alarmasRemover, String... tipos) {
		for (String tipo : tipos) {
			if (!alarmasRemover.contains(tipo)) {
				alarmasRemover.add(tipo);
			}
		}
	}

	public void recargarIngredienteEspecifico(String ingrediente) {
		recargarIngredienteConCantidad(ingrediente);
	}

	private int recargarMoneda(String denominacion, int cantidadObjetivo) {
		DepositoMonedas moneda = monedas.findByKey(denominacion);
		if (moneda == null) {
			System.out.println("[CoffeeMach] No se recargo moneda " + denominacion
					+ ": deposito no existe.");
			return 0;
		}

		int cantidadAnterior = moneda.getCantidad();
		moneda.setCantidad(cantidadObjetivo);
		monedas.addElement(denominacion, moneda);
		int cantidadRecargada = Math.max(0, moneda.getCantidad() - cantidadAnterior);
		System.out.println("[CoffeeMach] Moneda " + denominacion + " recargada. anterior="
				+ cantidadAnterior + ", actual=" + moneda.getCantidad()
				+ ", recargada=" + cantidadRecargada);
		return cantidadRecargada;
	}

	private int recargarIngredienteConCantidad(String ingrediente) {
		Ingrediente ing = ingredientes.findByKey(ingrediente);
		if (ing == null) {
			System.out.println("[CoffeeMach] No se recargo insumo " + ingrediente
					+ ": no existe en repositorio local.");
			return 0;
		}

		double cantidadAnterior = ing.getCantidad();
		ing.setCantidad(ing.getMaximo());
		ingredientes.addElement(ingrediente, ing);
		int cantidadRecargada = (int) Math.max(0, ing.getCantidad() - cantidadAnterior);
		System.out.println("[CoffeeMach] Insumo " + ingrediente + " recargado. anterior="
				+ cantidadAnterior + ", actual=" + ing.getCantidad()
				+ ", recargada=" + cantidadRecargada);
		return cantidadRecargada;
	}

	public void eventos() {

		frame.getBtnIngresar100().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int saldo = Integer.parseInt(frame.getTextAreaSaldo().getText());
				frame.getTextAreaSaldo().setText((saldo + 100) + "");
				suma += 100;
				DepositoMonedas moneda = monedas.findByKey("100");
				moneda.setCantidad(moneda.getCantidad() + 1);
				monedas.addElement("100", moneda);
				actualizarInsumosGraf();

			}
		});

		frame.getBtnIngresar200().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int saldo = Integer.parseInt(frame.getTextAreaSaldo().getText());
				frame.getTextAreaSaldo().setText((saldo + 200) + "");
				suma += 200;
				DepositoMonedas moneda = monedas.findByKey("200");
				moneda.setCantidad(moneda.getCantidad() + 1);
				monedas.addElement("200", moneda);
				actualizarInsumosGraf();

			}
		});

		frame.getBtnIngresar500().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int saldo = Integer.parseInt(frame.getTextAreaSaldo().getText());
				frame.getTextAreaSaldo().setText((saldo + 500) + "");
				suma += 500;
				DepositoMonedas moneda = monedas.findByKey("500");
				moneda.setCantidad(moneda.getCantidad() + 1);
				monedas.addElement("500", moneda);
				actualizarInsumosGraf();

			}
		});

		frame.getBtnCancelar().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.getTextAreaSaldo().setText("0");

				if (suma > 0) {

					frame.getTextAreaDevuelta().setText(
							frame.getTextAreaDevuelta().getText()
									+ "Se devolvio: " + suma + "\n");

					devolverMonedas();

				}

			}
		});

		frame.getBtnVerificar().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int precio = 0;
				List<Receta> rec = recetas.getValues();
				for (int i = 0; i < rec.size(); i++) {

					if (frame
							.getComboBoxProducto()
							.getSelectedItem()
							.equals(rec.get(i)
									.getDescripcion())) {
						precio = rec.get(i).getValor();
					}

				}

				frame.getTextAreaInfo().setText(
						frame.getTextAreaInfo().getText()
								+ "El producto cuesta: " + precio + "\n");
				frame.getTextAreaInfo().repaint();
			}
		});

		frame.getBtnOrdenar().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int precio = 0;
				Receta temp = null;
				List<Receta> rec = recetas.getValues();
				for (int i = 0; i < rec.size(); i++) {

					temp = rec.get(i);

					if (frame.getComboBoxProducto().getSelectedItem()
							.equals(temp.getDescripcion())) {
						precio = rec.get(i).getValor();

						if (Integer.valueOf(frame.getTextAreaSaldo().getText()) >= precio) {

							frame.getTextAreaInfo().setText(
									frame.getTextAreaInfo().getText()
											+ "Se ordeno: "
											+ frame.getComboBoxProducto()
													.getSelectedItem()
											+ "\n");

							frame.getTextAreaSaldo().setText(
									Integer.valueOf(frame.getTextAreaSaldo()
											.getText()) - precio + "");

							suma -= precio;

							disminuirInsumos(temp);

							devolverMonedas();
							verificarProductos();
							// TODO: corregir el idVenta
							String idV = rec.get(i).getId();
							ventas.addElement(idV, new Venta(frame.getComboBoxProducto()
									.getSelectedItem().toString(), idV,
									precio, new Date()));

							respaldarMaq();

							frame.getTextAreaSaldo().setText("0");

						} else {
							frame.getTextAreaInfo().setText(
									frame.getTextAreaInfo().getText()
											+ "Saldo insuficiente \n");

						}

					}

				}

			}

		});

		frame.getBtnMantenimiento().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Enviar Alarma por SCA

				Alarma temp = new Alarma("1", "Se requiere mantenimiento",
						new Date());

				frame.getTextAreaAlarmas().setText(
						frame.getTextAreaAlarmas().getText()
								+ "Se genero una alarma de: Mantenimiento"
								+ "\n");

				ejecutarRemotoAsync("recibirNotificacionMalFuncionamiento", new Runnable() {
					public void run() {
						alarmaServicePrx.recibirNotificacionMalFuncionamiento(codMaquina,
								"Se requiere mantenimiento");
					}
				});

				alarmas.addElement("1", temp);

				frame.interfazDeshabilitada();

			}
		});

		frame.getBtnEnviarReporte().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				List<Venta> vents = ventas.getValues();
				String[] arregloVentas = new String[vents.size()];
				for (int i = 0; i < arregloVentas.length; i++) {
					arregloVentas[i] = vents.get(i).getId() + "#"
							+ vents.get(i).getValor();
					System.out.println(arregloVentas[i]);
				}

				ejecutarRemotoAsync("registrarVenta", new Runnable() {
					public void run() {
						ventasService.registrarVenta(codMaquina, arregloVentas);
					}
				});

			}
		});

		frame.getBtnActualizar().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ejecutarRemotoAsync("consultarProductos", new Runnable() {
					public void run() {
						cargarRecetaMaquinas();
					}
				});

			}
		});
	}

	public void cargarRecetaMaquinas() {

		recetas.setElements(new HashMap<String, Receta>());

		String[] recetasServer = recetaServicePrx.consultarProductos();

		for (int i = 0; i < recetasServer.length; i++) {

			String[] splitInicial = recetasServer[i].split("#");

			String[] receta = splitInicial[0].split("-");

			HashMap<Ingrediente, Double> listaIngredientes = new HashMap<Ingrediente, Double>();

			for (int i2 = 1; i2 < splitInicial.length; i2++) {

				String[] splitdeIng = splitInicial[i2].split("-");

				Ingrediente ingred = ingredientes.findByKey(splitdeIng[1]);
				if (ingred == null) {
					ingred = new Ingrediente(splitdeIng[1], splitdeIng[2], 500, 50, 1000, 1000);
				}
				listaIngredientes.put(ingred, Double.parseDouble(splitdeIng[4]));

			}

			Receta r = new Receta(receta[1], receta[0],
					Integer.parseInt(receta[2]), listaIngredientes);

			recetas.addElement(receta[0], r);
		}

		// Actualizar Archivo Plano
		recetas.saveData();
		ejecutarEnEDT(new Runnable() {
			public void run() {
				actualizarInsumosGraf();
				actualizarRecetasGraf();
				actualizarRecetasCombo();
			}
		});
	}

	public void respaldarMaq() {
		alarmas.saveData();
		ingredientes.saveData();
		monedas.saveData();
		recetas.saveData();
		ventas.saveData();
	}

	public void verificarProductos() {

		Iterator<Ingrediente> itIng = ingredientes.getValues().iterator();

		while (itIng.hasNext()) {
			Ingrediente ing = itIng.next();

			if (ing.getCantidad() <= ing.getMinimo()
					&& ing.getCantidad() > ing.getCritico()) {

				Alarma alIng = new Alarma(ing.getCodAlarma(),
						ing.getNombre(), new Date());

				if (alarmas.findByKey(ing.getCodAlarma()) == null) {

					alarmas.addElement(ing.getCodAlarma(), alIng);

					// Enviar SCA

					final String nombreIngrediente = ing.getNombre();
					ejecutarRemotoAsync("recibirNotificacionEscasezIngredientes", new Runnable() {
						public void run() {
							alarmaServicePrx.recibirNotificacionEscasezIngredientes(nombreIngrediente, codMaquina);
						}
					});

					frame.getTextAreaAlarmas().setText(
							frame.getTextAreaAlarmas().getText()
									+ "Se genero una alarma de Ingrediente: "
									+ alIng.getMensaje() + "\n");

				}
			}

			if (ing.getCantidad() <= ing.getCritico()) {

				int codAlarma = Integer.parseInt(ing.getCodAlarma()) + 4;

				Alarma alIng = new Alarma(codAlarma + "", ing.getNombre(), new Date());

				alarmas.addElement(codAlarma + "", alIng);

				// Enviar SCA

				final String nombreIngredienteCritico = ing.getNombre();
				ejecutarRemotoAsync("recibirNotificacionEscasezIngredientes", new Runnable() {
					public void run() {
						alarmaServicePrx.recibirNotificacionEscasezIngredientes(nombreIngredienteCritico, codMaquina);
					}
				});

				frame.getTextAreaAlarmas().setText(
						frame.getTextAreaAlarmas().getText()
								+ "Se genero una alarma de: Critico de "
								+ alIng.getMensaje() + "\n");

				frame.interfazDeshabilitada();
			}

		}
	}

	public void disminuirInsumos(Receta r) {
		Iterator<Entry<Ingrediente, Double>> receta = r.getListaIngredientes()
				.entrySet().iterator();
		while (receta.hasNext()) {
			Map.Entry<Ingrediente, Double> ingRec = (Map.Entry<Ingrediente, Double>) receta.next();
			Ingrediente ingrediente = ingredientes.findByKey(ingRec.getKey().getNombre());
			ingrediente.setCantidad(ingrediente.getCantidad() - ingRec.getValue());
			ingredientes.addElement(ingrediente.getNombre(), ingrediente);
		}
		// Modificar XML
		actualizarInsumosGraf();
	}

	public void arrancarMaquina() {
		if (codMaquina <= 0) {
			inicializarCodMaquina();
		}
		// Interfaz
		actualizarRecetasCombo();
		actualizarRecetasGraf();
		actualizarInsumosGraf();
		actualizarAlarmasGraf();

	}

	public void actualizarAlarmasGraf() {

		frame.getTextAreaAlarmas().setText("");

	}

	public void actualizarInsumosGraf() {

		frame.getTextAreaInsumos().setText("");

		// LLenar Insumos
		Iterator<Ingrediente> it = ingredientes.getValues().iterator();
		while (it.hasNext()) {
			Ingrediente ing = it.next();

			frame.getTextAreaInsumos().setText(
					frame.getTextAreaInsumos().getText()
							+ ing.getNombre() + ": "
							+ ing.getCantidad() + "\n");

		}
		DepositoMonedas dep = monedas.findByKey("100");
		frame.getTextAreaInsumos().setText(
				frame.getTextAreaInsumos().getText() + "Deposito "
						+ dep.getTipo() + ": "
						+ dep.getCantidad() + "\n");
		dep = monedas.findByKey("200");
		frame.getTextAreaInsumos().setText(
				frame.getTextAreaInsumos().getText() + "Deposito "
						+ dep.getTipo() + ": "
						+ dep.getCantidad() + "\n");
		dep = monedas.findByKey("500");
		frame.getTextAreaInsumos().setText(
				frame.getTextAreaInsumos().getText() + "Deposito "
						+ dep.getTipo() + ": "
						+ dep.getCantidad() + "\n");

	}

	public void actualizarRecetasGraf() {

		frame.getTextAreaRecetas().setText("");

		// Llenar Recetas

		Iterator<Receta> it2 = recetas.getValues().iterator();
		while (it2.hasNext()) {

			Receta temp = it2.next();
			frame.getTextAreaRecetas().setText(
					frame.getTextAreaRecetas().getText()
							+ temp.getDescripcion() + "\n");

		}

	}

	public void actualizarRecetasCombo() {

		// Reestablece Combobox
		frame.getComboBoxProducto().removeAllItems();

		// LLenar Combo
		List<Receta> rec = recetas.getValues();
		for (int i = 0; i < rec.size(); i++) {

			frame.getComboBoxProducto().addItem(
					rec.get(i).getDescripcion());
		}
	}

	private int cargarCodMaquina() {
		if (codMaquinaConfigurado != null && codMaquinaConfigurado > 0) {
			return codMaquinaConfigurado;
		}

		return cargarCodMaquinaDesdeArchivo();
	}

	private int cargarCodMaquinaDesdeArchivo() {
		String[] rutas = {
				"codMaquina.cafe",
				"coffeeMach/codMaquina.cafe",
				"../codMaquina.cafe",
				"src/main/resources/codMaquina.cafe"
		};

		for (String ruta : rutas) {
			File archivo = new File(ruta);
			if (!archivo.isFile()) {
				continue;
			}

			try (BufferedReader buffer = new BufferedReader(new FileReader(archivo))) {
				String linea = buffer.readLine();
				int cod = Integer.parseInt(linea.trim());
				if (cod > 0) {
					System.out.println("[CoffeeMach] Código de máquina cargado desde archivo "
							+ archivo.getPath() + ": " + cod);
					return cod;
				}
				System.out.println("[CoffeeMach] Código de máquina inválido en "
						+ archivo.getPath() + ": " + linea);
			} catch (IOException | NumberFormatException e) {
				System.out.println("[CoffeeMach] No se pudo leer código de máquina desde "
						+ archivo.getPath() + ": " + e.getMessage());
			}
		}

		System.out.println("[CoffeeMach] No se pudo cargar código de máquina.");
		return -1;
	}

	public void devolverMonedas() {
		// Metodo para devolver monedas
		int monedas100 = 0;
		int monedas200 = 0;
		int monedas500 = 0;
		if (suma / 500 > 0) {
			monedas500 += (int) suma / 500;
			DepositoMonedas moneda = monedas.findByKey("500");
			moneda.setCantidad(moneda.getCantidad() - monedas500);
			monedas.addElement("500", moneda);
			suma -= 500 * ((int) suma / 500);

		}

		if (suma / 200 > 0) {

			monedas200 += (int) suma / 200;
			DepositoMonedas moneda = monedas.findByKey("200");
			moneda.setCantidad(moneda.getCantidad() - monedas200);
			monedas.addElement("200", moneda);
			suma -= 200 * ((int) suma / 200);

		}
		if (suma / 100 > 0) {
			monedas100 += (int) suma / 100;
			DepositoMonedas moneda = monedas.findByKey("100");
			moneda.setCantidad(moneda.getCantidad() - monedas100);
			monedas.addElement("100", moneda);
			suma -= 100 * ((int) suma / 100);
		}
		if (suma != 0) {
			System.out.println("Ocurrio un error en dar devueltas: " + suma);
		}

		frame.getTextAreaDevuelta().setText(
				frame.getTextAreaDevuelta().getText() + "Se devolvieron: "
						+ monedas500 + " monedas de 500, " + monedas200
						+ " monedas de 200 y " + monedas100
						+ " monedas de 100 \n");

		actualizarInsumosGraf();
		verificarMonedas();

	}

	public void verificarMonedas() {

		// Alarma (Generada por Uso)
		DepositoMonedas moneda = monedas.findByKey("100");
		if (moneda.getCantidad() <= moneda.getMinimo()
				&& moneda.getCantidad() > moneda.getCritico()) {

			Alarma alMon = new Alarma("2", "Faltan monedas de 100", new Date());

			if (alarmas.findByKey("2") == null) {
				alarmas.addElement("2", alMon);

				ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-CIEN", new Runnable() {
					public void run() {
						alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.CIEN, codMaquina);
					}
				});
				frame.getTextAreaAlarmas().setText(
						frame.getTextAreaAlarmas().getText()
								+ "Se genero una alarma de: Monedas de 100"
								+ "\n");

			}
		}

		if (moneda.getCantidad() <= moneda.getCritico()) {

			Alarma alMon = new Alarma("3",
					"ESTADO CRITICO: Faltan monedas de 100", new Date());
			alarmas.addElement("3", alMon);

			// Enviar SCA
			ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-CIEN", new Runnable() {
				public void run() {
					alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.CIEN, codMaquina);
				}
			});

			frame.getTextAreaAlarmas().setText(
					frame.getTextAreaAlarmas().getText()
							+ "Se genero una alarma de: Critica Monedas de 100"
							+ "\n");

			frame.interfazDeshabilitada();

		}
		moneda = monedas.findByKey("200");
		if (moneda.getCantidad() <= moneda.getMinimo()
				&& moneda.getCantidad() > moneda.getCritico()) {

			Alarma alMon = new Alarma("4", "Faltan monedas de 200", new Date());

			if (alarmas.findByKey("4") == null) {
				alarmas.addElement("4", alMon);

				// Enviar SCA

				ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-DOCIENTOS", new Runnable() {
					public void run() {
						alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.DOCIENTOS, codMaquina);
					}
				});

				frame.getTextAreaAlarmas().setText(
						frame.getTextAreaAlarmas().getText()
								+ "Se genero una alarma de: Mondedas de 200"
								+ "\n");

			}
		}

		if (moneda.getCantidad() <= moneda.getCritico()) {

			Alarma alMon = new Alarma("5",
					"ESTADO CRITICO: Faltan monedas de 200", new Date());
			alarmas.addElement("5", alMon);

			// Enviar SCA

			ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-DOCIENTOS", new Runnable() {
				public void run() {
					alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.DOCIENTOS, codMaquina);
				}
			});

			frame.getTextAreaAlarmas()
					.setText(
							frame.getTextAreaAlarmas().getText()
									+ "Se genero una alarma de: Critica de Monedas de 200"
									+ "\n");

			frame.interfazDeshabilitada();

		}
		moneda = monedas.findByKey("500");

		if (moneda.getCantidad() <= moneda.getMinimo()
				&& moneda.getCantidad() > moneda.getCritico()) {

			Alarma alMon = new Alarma("6", "Faltan monedas de 500", new Date());
			if (alarmas.findByKey("6") == null) {
				alarmas.addElement("6", alMon);

				// Enviar SCA

				ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-QUINIENTOS", new Runnable() {
					public void run() {
						alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.QUINIENTOS, codMaquina);
					}
				});

				frame.getTextAreaAlarmas().setText(
						frame.getTextAreaAlarmas().getText()
								+ "Se genero una alarma de: Monedas de 500"
								+ "\n");

			}
		}
		if (moneda.getCantidad() <= moneda.getCritico()) {

			Alarma alMon = new Alarma("7",
					"ESTADO CRITICO: Faltan monedas de 500", new Date());
			alarmas.addElement("7", alMon);

			ejecutarRemotoAsync("recibirNotificacionInsuficienciaMoneda-QUINIENTOS", new Runnable() {
				public void run() {
					alarmaServicePrx.recibirNotificacionInsuficienciaMoneda(Moneda.QUINIENTOS, codMaquina);
				}
			});

			frame.getTextAreaAlarmas().setText(
					frame.getTextAreaAlarmas().getText()
							+ "Se genero una alarma de: Critica Monedas de 500"
							+ "\n");

			frame.interfazDeshabilitada();

		}

	}

}
