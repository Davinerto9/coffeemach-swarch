package guiInventario;

import bodega.Bodega;
import mantenimientoExistencias.Inventario;

import java.awt.Color;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

public class Interfaz extends JFrame {

    private final Inventario inventario;
    private final Bodega bodega;

    // ── Componentes ──────────────────────────────────────────────────────────
    private JTextArea textAreaInventario;
    private JTextArea textAreaResultado;
    private JComboBox<String> comboCategoria;
    private JTextField txtCodigo;
    private JTextField txtCantidad;
    private JTextField txtIdOrden;
    private JTextField txtCodMaquina;
    private JTextField txtTipoAlarma;

    private JButton btnConsultarTodo;
    private JButton btnConsultarCategoria;
    private JButton btnAbastecerMasivo;
    private JButton btnAbastecerIndividual;
    private JButton btnSepararExistencias;
    private JButton btnRetirarExistencias;
    private JButton btnKitReparacion;

    public Interfaz(Inventario inventario, Bodega bodega) {
        this.inventario = inventario;
        this.bodega = bodega;
        construirVentana();
        registrarEventos();
    }

    // ── Construcción de la ventana ────────────────────────────────────────────

    private void construirVentana() {
        setTitle("Bodega Central - Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 500);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ── Panel inventario ──────────────────────────────────────────────────

        JPanel panelInventario = new JPanel();
        panelInventario.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panelInventario.setBounds(10, 10, 420, 450);
        panelInventario.setLayout(null);
        contentPane.add(panelInventario);

        JLabel lblInventario = new JLabel("Inventario");
        lblInventario.setHorizontalAlignment(SwingConstants.CENTER);
        lblInventario.setBounds(10, 8, 400, 16);
        panelInventario.add(lblInventario);

        JScrollPane scrollInventario = new JScrollPane();
        scrollInventario.setBounds(10, 30, 400, 280);
        panelInventario.add(scrollInventario);

        textAreaInventario = new JTextArea();
        textAreaInventario.setEditable(false);
        scrollInventario.setViewportView(textAreaInventario);

        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setBounds(10, 322, 70, 20);
        panelInventario.add(lblCategoria);

        comboCategoria = new JComboBox<String>(
                new String[]{
                        "Todos",
                        "Ingredientes",
                        "Monedas",
                        "Suministros"
                });

        comboCategoria.setBounds(85, 322, 130, 22);
        panelInventario.add(comboCategoria);

        btnConsultarTodo = new JButton("Ver todo");
        btnConsultarTodo.setBounds(225, 322, 85, 22);
        panelInventario.add(btnConsultarTodo);

        btnConsultarCategoria = new JButton("Por categoría");
        btnConsultarCategoria.setBounds(315, 322, 95, 22);
        panelInventario.add(btnConsultarCategoria);

        btnAbastecerMasivo = new JButton("Abastecer categoría");
        btnAbastecerMasivo.setBounds(10, 356, 160, 24);
        panelInventario.add(btnAbastecerMasivo);

        // ── Abastecer individual ─────────────────────────────────────────────

        JLabel lblCodigo = new JLabel("Código:");
        lblCodigo.setBounds(10, 394, 55, 20);
        panelInventario.add(lblCodigo);

        txtCodigo = new JTextField();
        txtCodigo.setBounds(68, 394, 175, 22);
        panelInventario.add(txtCodigo);

        JLabel lblCantidad = new JLabel("Cant:");
        lblCantidad.setBounds(250, 394, 40, 20);
        panelInventario.add(lblCantidad);

        txtCantidad = new JTextField("0");
        txtCantidad.setBounds(293, 394, 55, 22);
        panelInventario.add(txtCantidad);

        btnAbastecerIndividual = new JButton("+");
        btnAbastecerIndividual.setBounds(355, 394, 55, 22);
        panelInventario.add(btnAbastecerIndividual);

        // ── Panel órdenes ────────────────────────────────────────────────────

        JPanel panelOrdenes = new JPanel();
        panelOrdenes.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panelOrdenes.setBounds(440, 10, 440, 200);
        panelOrdenes.setLayout(null);
        contentPane.add(panelOrdenes);

        JLabel lblOrdenes = new JLabel("Gestión de órdenes de entrega");
        lblOrdenes.setHorizontalAlignment(SwingConstants.CENTER);
        lblOrdenes.setBounds(10, 8, 420, 16);
        panelOrdenes.add(lblOrdenes);

        JLabel lblIdOrden = new JLabel("ID Orden:");
        lblIdOrden.setBounds(10, 38, 65, 20);
        panelOrdenes.add(lblIdOrden);

        txtIdOrden = new JTextField();
        txtIdOrden.setBounds(78, 38, 80, 22);
        panelOrdenes.add(txtIdOrden);

        JLabel lblCodMaq = new JLabel("Cod Máq:");
        lblCodMaq.setBounds(170, 38, 65, 20);
        panelOrdenes.add(lblCodMaq);

        txtCodMaquina = new JTextField();
        txtCodMaquina.setBounds(238, 38, 80, 22);
        panelOrdenes.add(txtCodMaquina);

        JLabel lblTipoAlarma = new JLabel("Tipo alarma:");
        lblTipoAlarma.setBounds(10, 72, 80, 20);
        panelOrdenes.add(lblTipoAlarma);

        txtTipoAlarma = new JTextField();
        txtTipoAlarma.setBounds(95, 72, 55, 22);
        panelOrdenes.add(txtTipoAlarma);

        JLabel lblRef = new JLabel("1=Ingr  2=Mon  3/4=Sum  6=Kit");
        lblRef.setBounds(160, 72, 260, 20);
        lblRef.setForeground(Color.GRAY);
        panelOrdenes.add(lblRef);

        btnSepararExistencias = new JButton("Separar existencias");
        btnSepararExistencias.setBounds(10, 108, 160, 28);
        panelOrdenes.add(btnSepararExistencias);

        btnRetirarExistencias = new JButton("Retirar existencias");
        btnRetirarExistencias.setBounds(180, 108, 155, 28);
        panelOrdenes.add(btnRetirarExistencias);

        btnKitReparacion = new JButton("Entregar kit reparación");
        btnKitReparacion.setBounds(10, 148, 200, 28);
        panelOrdenes.add(btnKitReparacion);

        // ── Panel resultado ──────────────────────────────────────────────────

        JPanel panelResultado = new JPanel();
        panelResultado.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panelResultado.setBounds(440, 220, 440, 240);
        panelResultado.setLayout(null);
        contentPane.add(panelResultado);

        JLabel lblResultado = new JLabel("Resultado");
        lblResultado.setHorizontalAlignment(SwingConstants.CENTER);
        lblResultado.setBounds(10, 8, 420, 16);
        panelResultado.add(lblResultado);

        JScrollPane scrollResultado = new JScrollPane();
        scrollResultado.setBounds(10, 28, 420, 200);
        panelResultado.add(scrollResultado);

        textAreaResultado = new JTextArea();
        textAreaResultado.setEditable(false);
        scrollResultado.setViewportView(textAreaResultado);
    }

    // ── Eventos ─────────────────────────────────────────────────────────────

    private void registrarEventos() {

        btnConsultarTodo.addActionListener(e -> {
            mostrarEnInventario(inventario.consultarInventario());
        });

        btnConsultarCategoria.addActionListener(e -> {

            String cat = (String) comboCategoria.getSelectedItem();

            List<String> items;

            switch (cat) {

                case "Ingredientes":
                    items = bodega.consultarIngredientes();
                    break;

                case "Monedas":
                    items = bodega.consultarMonedas();
                    break;

                case "Suministros":
                    items = bodega.consultarSuministros();
                    break;

                default:
                    items = inventario.consultarInventario();
                    break;
            }

            mostrarEnInventario(items);
        });

        btnAbastecerMasivo.addActionListener(e -> {

            String cat = (String) comboCategoria.getSelectedItem();

            switch (cat) {

                case "Ingredientes":
                    inventario.abastecerIngredientes();
                    break;

                case "Monedas":
                    inventario.abastecerMonedas();
                    break;

                case "Suministros":
                    inventario.abastecerSuministros();
                    break;

                default:
                    mostrarResultado("Seleccione una categoría específica.");
                    return;
            }

            mostrarResultado("Abastecimiento masivo de " + cat + " realizado.");
            mostrarEnInventario(inventario.consultarInventario());
        });

        btnAbastecerIndividual.addActionListener(e -> {

            String codigo = txtCodigo.getText().trim();

            if (codigo.isEmpty()) {
                mostrarResultado("Ingrese un código de existencia.");
                return;
            }

            try {

                int cantidad = Integer.parseInt(txtCantidad.getText().trim());

                bodega.abastecerExistencia(codigo, cantidad);

                mostrarResultado("Abastecido: " + codigo + " +" + cantidad);

                mostrarEnInventario(inventario.consultarInventario());

            } catch (NumberFormatException ex) {

                mostrarResultado("Cantidad no válida.");
            }
        });

        btnSepararExistencias.addActionListener(e -> {

            try {

                int idOrden = Integer.parseInt(txtIdOrden.getText().trim());

                int tipoAlarma =
                        Integer.parseInt(txtTipoAlarma.getText().trim());

                mostrarResultado(
                        bodega.separarExistencias(idOrden, tipoAlarma));

            } catch (NumberFormatException ex) {

                mostrarResultado(
                        "ID orden y tipo alarma deben ser números.");
            }
        });

        btnRetirarExistencias.addActionListener(e -> {

            try {

                int tipoAlarma =
                        Integer.parseInt(txtTipoAlarma.getText().trim());

                String resultado =
                        bodega.retirarExistencias(tipoAlarma);

                mostrarResultado("Retirado:\n" + resultado);

                mostrarEnInventario(inventario.consultarInventario());

            } catch (NumberFormatException ex) {

                mostrarResultado("Tipo alarma debe ser un número.");
            }
        });

        btnKitReparacion.addActionListener(e -> {

            try {

                int idOrden =
                        Integer.parseInt(txtIdOrden.getText().trim());

                int codMaquina =
                        Integer.parseInt(txtCodMaquina.getText().trim());

                mostrarResultado(
                        bodega.entregaKitReparacion(
                                idOrden,
                                codMaquina));

                mostrarEnInventario(inventario.consultarInventario());

            } catch (NumberFormatException ex) {

                mostrarResultado(
                        "ID orden y Cod máquina deben ser números.");
            }
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void mostrarEnInventario(List<String> items) {

        StringBuilder sb = new StringBuilder();

        for (String i : items) {
            sb.append(i).append("\n");
        }

        textAreaInventario.setText(sb.toString());
    }

    private void mostrarResultado(String texto) {
        textAreaResultado.setText(texto);
    }
}