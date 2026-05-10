package gui;

import controlAlarma.ControladorAlarmas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra las máquinas asignadas al operador logístico activo.
 */
public class MaquinasPanel extends JPanel {

    private final ControladorAlarmas controlador;
    private JPanel gridPanel;
    private JButton btnRecargar;
    private JLabel lblConteo;

    public MaquinasPanel(ControladorAlarmas controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel topBar = DashboardFrame.topBar("Mis máquinas asignadas");
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        lblConteo = new JLabel("…");
        lblConteo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConteo.setForeground(new Color(0x888880));
        btnRecargar = new JButton("↻ Actualizar");
        btnRecargar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRecargar.setFocusPainted(false);
        btnRecargar.addActionListener(e -> recargar());
        topRight.add(lblConteo);
        topRight.add(btnRecargar);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
    }

    public void recargar() {
        btnRecargar.setEnabled(false);
        gridPanel.removeAll();
        JLabel loading = new JLabel("Consultando máquinas asignadas…");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        loading.setForeground(new Color(0x888880));
        gridPanel.add(loading);
        gridPanel.revalidate();

        SwingWorker<List<String>, Void> w = new SwingWorker<>() {
            @Override protected List<String> doInBackground() {
                return controlador.consultarMaquinasAsignadas();
            }
            @Override protected void done() {
                gridPanel.removeAll();
                try {
                    List<String> maquinas = get();
                    lblConteo.setText(maquinas.size() + " máquina(s)");
                    if (maquinas.isEmpty()) {
                        JLabel l = new JLabel("No tiene máquinas asignadas.");
                        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        l.setForeground(new Color(0x888880));
                        gridPanel.add(l);
                    } else {
                        for (String m : maquinas) {
                            gridPanel.add(buildMachineCard(m));
                        }
                    }
                } catch (Exception ex) {
                    JLabel err = new JLabel("Error: " + ex.getMessage());
                    err.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    err.setForeground(new Color(0xA32D2D));
                    gridPanel.add(err);
                }
                gridPanel.revalidate();
                gridPanel.repaint();
                btnRecargar.setEnabled(true);
            }
        };
        w.execute();
    }

    private JPanel buildMachineCard(String maquina) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(0xE0DDD6), 1),
            new EmptyBorder(14, 16, 14, 16)));
        card.setPreferredSize(new Dimension(180, 100));

        JLabel ico = new JLabel("🖥");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        // El servidor retorna strings con info de la máquina
        JLabel info = new JLabel("<html><center>" + maquina.replace("#", "<br>") + "</center></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(new Color(0x444441));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(ico);
        card.add(info);
        return card;
    }

    /**
     * FlowLayout que hace wrap correctamente en un JPanel con BoxLayout padre.
     * Implementación mínima suficiente para este uso.
     */
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets ins = target.getInsets();
                int maxWidth = targetWidth - ins.left - ins.right - hgap * 2;
                int x = 0, y = ins.top + vgap, rowH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x != 0 && x + d.width > maxWidth) {
                        y += rowH + vgap;
                        x = 0;
                        rowH = 0;
                    }
                    x += d.width + hgap;
                    rowH = Math.max(rowH, d.height);
                }
                y += rowH + vgap + ins.bottom;
                return new Dimension(targetWidth, y);
            }
        }
    }
}