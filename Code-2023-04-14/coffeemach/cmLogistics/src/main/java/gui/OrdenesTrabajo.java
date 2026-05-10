package gui;

import controlAlarma.ControladorAlarmas;
import tecnicoMantenimiento.EstadoOrdenTrabajo;
import tecnicoMantenimiento.OrdenTrabajo;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenesTrabajo extends JPanel {

    private final ControladorAlarmas controlador;
    private final List<OrdenTrabajo> ordenes = new ArrayList<>();

    private DefaultTableModel tableModel;
    private JTable tabla;
    private JLabel lblTotales;

    public OrdenesTrabajo(ControladorAlarmas controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel topBar = DashboardFrame.topBar("Órdenes de trabajo — sesión actual");
        lblTotales = new JLabel("0 órdenes");
        lblTotales.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotales.setForeground(new Color(0x888880));
        topBar.add(lblTotales, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"#OT", "Máquina", "Ubicación", "Tipo alarma", "Descripción", "Estado", "Comprobante"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(28);
        tabla.setGridColor(new Color(0xEEECE7));
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.getTableHeader().setBackground(new Color(0xF7F7F5));
        tabla.getTableHeader().setForeground(new Color(0x666660));
        tabla.setSelectionBackground(new Color(0xE6F1FB));
        tabla.setSelectionForeground(new Color(0x185FA5));

        int[] widths = {45, 70, 120, 100, 200, 100, 200};
        for (int i = 0; i < widths.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                                                           boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String estado = val == null ? "" : val.toString();
                switch (estado) {
                    case "RESUELTA":   setForeground(new Color(0x3B6D11)); break;
                    case "EN_PROCESO": setForeground(new Color(0x185FA5)); break;
                    case "CANCELADA":  setForeground(new Color(0xA32D2D)); break;
                    default:           setForeground(new Color(0x888880)); break;
                }
                if (sel) setForeground(new Color(0x185FA5));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new EmptyBorder(14, 18, 14, 18));
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        footer.setBackground(new Color(0xF7F7F5));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0xE0DDD6)));
        footer.add(statLabel("Pendientes", EstadoOrdenTrabajo.PENDIENTE));
        footer.add(statLabel("En proceso", EstadoOrdenTrabajo.EN_PROCESO));
        footer.add(statLabel("Resueltas",  EstadoOrdenTrabajo.RESUELTA));
        footer.add(statLabel("Canceladas", EstadoOrdenTrabajo.CANCELADA));
        add(footer, BorderLayout.SOUTH);
    }

    public void agregarOrden(OrdenTrabajo ot) {
        ordenes.add(ot);
        recargar();
    }

    public void recargar() {
        tableModel.setRowCount(0);
        for (OrdenTrabajo ot : ordenes) {
            tableModel.addRow(new Object[]{
                ot.getConsecutivo(),
                ot.getAlarma().getCodMaquina(),
                ot.getAlarma().getUbicacion(),
                "Tipo " + ot.getAlarma().getTipoAlarma(),
                ot.getAlarma().getDescripcion(),
                ot.getEstado().name(),
                ot.getComprobanteBodega() != null ? ot.getComprobanteBodega() : "—"
            });
        }
        lblTotales.setText(ordenes.size() + " orden(es)");
    }

    private JLabel statLabel(String nombre, EstadoOrdenTrabajo estado) {
        long count = ordenes.stream().filter(o -> o.getEstado() == estado).count();
        JLabel l = new JLabel(nombre + ": " + count);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        switch (estado) {
            case RESUELTA:   l.setForeground(new Color(0x3B6D11)); break;
            case EN_PROCESO: l.setForeground(new Color(0x185FA5)); break;
            case CANCELADA:  l.setForeground(new Color(0xA32D2D)); break;
            default:         l.setForeground(new Color(0x888880)); break;
        }
        return l;
    }
}