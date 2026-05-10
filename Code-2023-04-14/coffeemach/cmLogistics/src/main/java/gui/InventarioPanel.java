package gui;

import controlAlarma.ControladorAlarmas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Panel que consulta y muestra el inventario de la bodega central.
 */
public class InventarioPanel extends JPanel {

    private final ControladorAlarmas controlador;
    private JPanel listaPanel;
    private JButton btnRecargar;

    public InventarioPanel(ControladorAlarmas controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel topBar = DashboardFrame.topBar("Inventario — bodega central");
        btnRecargar = new JButton("↻ Consultar");
        btnRecargar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRecargar.setFocusPainted(false);
        btnRecargar.addActionListener(e -> recargar());
        topBar.add(btnRecargar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Color.WHITE);
        listaPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    public void recargar() {
        btnRecargar.setEnabled(false);
        listaPanel.removeAll();
        JLabel loading = new JLabel("Consultando inventario…");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loading.setForeground(new Color(0x888880));
        listaPanel.add(loading);
        listaPanel.revalidate();

        SwingWorker<List<String>, Void> w = new SwingWorker<>() {
            @Override protected List<String> doInBackground() {
                return controlador.consultarInventarioBodega();
            }
            @Override protected void done() {
                listaPanel.removeAll();
                try {
                    List<String> items = get();
                    if (items.isEmpty()) {
                        listaPanel.add(emptyLabel("Inventario vacío o sin datos."));
                    } else {
                        // Cabecera
                        JLabel header = new JLabel("Artículos en bodega (" + items.size() + ")");
                        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        header.setForeground(new Color(0x444441));
                        header.setBorder(new EmptyBorder(0, 0, 10, 0));
                        listaPanel.add(header);

                        for (String item : items) {
                            listaPanel.add(buildItemRow(item));
                            listaPanel.add(Box.createVerticalStrut(4));
                        }
                    }
                } catch (Exception ex) {
                    listaPanel.add(emptyLabel("Error: " + ex.getMessage()));
                }
                listaPanel.revalidate();
                listaPanel.repaint();
                btnRecargar.setEnabled(true);
            }
        };
        w.execute();
    }

    private JPanel buildItemRow(String item) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(0xF7F7F5));
        row.setBorder(new CompoundBorder(
            new LineBorder(new Color(0xE0DDD6), 1),
            new EmptyBorder(8, 12, 8, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel ico = new JLabel("📦");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel txt = new JLabel(item);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt.setForeground(new Color(0x2C2C2A));

        row.add(ico, BorderLayout.WEST);
        row.add(txt, BorderLayout.CENTER);
        return row;
    }

    private JLabel emptyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(0x888880));
        l.setBorder(new EmptyBorder(20, 0, 0, 0));
        return l;
    }
}