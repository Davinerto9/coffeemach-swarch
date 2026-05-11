package gui;

import controlAlarma.AlarmaPendiente;
import controlAlarma.ControladorAlarmas;
import tecnicoMantenimiento.OrdenTrabajo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AlarmasPanel extends JPanel {

    private final ControladorAlarmas controlador;
    private final DashboardFrame dashboard;

    private JPanel listaPanel;
    private JLabel lblConteo;
    private JButton btnRecargar;

    public AlarmasPanel(ControladorAlarmas controlador, DashboardFrame dashboard) {
        this.controlador = controlador;
        this.dashboard   = dashboard;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel topBar = DashboardFrame.topBar("Alarmas pendientes");
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        lblConteo = new JLabel("…");
        lblConteo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConteo.setForeground(new Color(0xA32D2D));
        btnRecargar = new JButton("↻ Actualizar");
        btnRecargar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRecargar.setFocusPainted(false);
        btnRecargar.addActionListener(e -> recargar());
        topRight.add(lblConteo);
        topRight.add(btnRecargar);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Color.WHITE);
        listaPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
    }

    public void recargar() {
        btnRecargar.setEnabled(false);
        btnRecargar.setText("Cargando…");
        listaPanel.removeAll();
        listaPanel.add(loadingLabel());
        listaPanel.revalidate();

        SwingWorker<List<AlarmaPendiente>, Void> worker = new SwingWorker<List<AlarmaPendiente>, Void>() {
            @Override protected List<AlarmaPendiente> doInBackground() {
                return controlador.consultarAlarmasPendientes();
            }
            @Override protected void done() {
                listaPanel.removeAll();
                try {
                    List<AlarmaPendiente> alarmas = get();
                    lblConteo.setText(alarmas.size() + " activa(s)");
                    if (alarmas.isEmpty()) {
                        listaPanel.add(emptyLabel("No hay alarmas pendientes ✔"));
                    } else {
                        for (AlarmaPendiente a : alarmas) {
                            listaPanel.add(buildAlarmCard(a));
                            listaPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                } catch (Exception ex) {
                    listaPanel.add(emptyLabel("Error al cargar: " + ex.getMessage()));
                }
                listaPanel.revalidate();
                listaPanel.repaint();
                btnRecargar.setEnabled(true);
                btnRecargar.setText("↻ Actualizar");
            }
        };
        worker.execute();
    }

    private JPanel buildAlarmCard(AlarmaPendiente alarma) {
        boolean critica = alarma.getTipoAlarma() == 1;
        Color borderAccent = critica ? new Color(0xE24B4A) : new Color(0xEF9F27);
        Color bgLight      = critica ? new Color(0xFFF5F5) : new Color(0xFFFAF0);
        Color iconBg       = critica ? new Color(0xFCEBEB) : new Color(0xFAEEDA);
        Color iconFg       = critica ? new Color(0xA32D2D) : new Color(0x854F0B);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(bgLight);
        card.setBorder(new CompoundBorder(
            new LineBorder(borderAccent, 1) {
                @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                    g.setColor(borderAccent);
                    g.fillRect(x, y, 3, h);
                    g.setColor(new Color(0xE8E6E0));
                    g.drawRect(x, y, w - 1, h - 1);
                }
            },
            new EmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 200));

        JPanel rowTop = new JPanel(new BorderLayout(12, 0));
        rowTop.setOpaque(false);

        JLabel icoLbl = new JLabel(critica ? "⚠" : "📦");
        icoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icoLbl.setOpaque(true);
        icoLbl.setBackground(iconBg);
        icoLbl.setForeground(iconFg);
        icoLbl.setPreferredSize(new Dimension(36, 36));
        icoLbl.setHorizontalAlignment(SwingConstants.CENTER);
        icoLbl.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titulo = new JLabel("Máquina " + alarma.getCodMaquina() + "  —  " + alarma.getUbicacion());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(new Color(0x1A1A18));

        JLabel detalle = new JLabel("Desde: " + alarma.getFechaInicial() + "  ·  " + alarma.getDescripcion());
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detalle.setForeground(new Color(0x888880));

        info.add(titulo);
        info.add(Box.createVerticalStrut(3));
        info.add(detalle);

        JLabel tipoBadge = badgeTipo(alarma.getTipoAlarma());

        rowTop.add(icoLbl,    BorderLayout.WEST);
        rowTop.add(info,      BorderLayout.CENTER);
        rowTop.add(tipoBadge, BorderLayout.EAST);

        JPanel rowBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowBtns.setOpaque(false);

        JButton btnOT   = accentButton("Generar OT", new Color(0x185FA5), new Color(0xE6F1FB));
        JButton btnRuta = new JButton("Ver ruta");
        btnRuta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRuta.setFocusPainted(false);

        JPanel otPanel = buildOtPanel(alarma, btnOT);
        otPanel.setVisible(false);

        btnOT.addActionListener(e -> {
            if (!otPanel.isVisible()) {
                otPanel.setVisible(true);
                card.revalidate();
            }
        });
        btnRuta.addActionListener(e -> mostrarRuta(alarma));

        rowBtns.add(btnOT);
        rowBtns.add(btnRuta);

        card.add(rowTop,  BorderLayout.NORTH);
        card.add(rowBtns, BorderLayout.CENTER);
        card.add(otPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildOtPanel(AlarmaPendiente alarma, JButton btnOT) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(0xE0DDD6)),
            new EmptyBorder(10, 0, 0, 0)));

        JLabel otTitulo = new JLabel("Orden de trabajo");
        otTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        otTitulo.setForeground(new Color(0x444441));

        String[] pasos = {
            "Calcular ruta",
            "Solicitar materiales a bodega",
            "Preparar existencias en bodega",
            "Despachar materiales",
            "Notificar máquina (abastecer)",
            "Confirmar recepción en bodega"
        };
        JLabel[] stepLabels = new JLabel[pasos.length];
        JLabel[] stepIcos   = new JLabel[pasos.length];
        JPanel stepsPanel = new JPanel();
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        stepsPanel.setOpaque(false);
        stepsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        for (int i = 0; i < pasos.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);
            stepIcos[i] = new JLabel("○");
            stepIcos[i].setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            stepIcos[i].setForeground(new Color(0xB4B2A9));
            stepIcos[i].setPreferredSize(new Dimension(18, 16));
            stepLabels[i] = new JLabel(pasos[i]);
            stepLabels[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            stepLabels[i].setForeground(new Color(0x888880));
            row.add(stepIcos[i]);
            row.add(stepLabels[i]);
            stepsPanel.add(row);
        }

        JLabel lblComprobante = new JLabel(" ");
        lblComprobante.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblComprobante.setForeground(new Color(0x3B6D11));
        final OrdenTrabajo[] ordenTrabajo = new OrdenTrabajo[1];

        JButton btnResolver = LoginFrame.primaryButton("▶  Resolver alarma");
        btnResolver.setMaximumSize(new Dimension(200, 34));
        btnResolver.setPreferredSize(new Dimension(200, 34));
        btnResolver.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnResolver.addActionListener(e -> {
            btnResolver.setEnabled(false);
            btnResolver.setText("Procesando…");
            btnOT.setEnabled(false);

            if (ordenTrabajo[0] == null) {
                ordenTrabajo[0] = controlador.generarOrdenTrabajo(alarma);
                dashboard.registrarOrdenTrabajo(ordenTrabajo[0]);
            }
            OrdenTrabajo ot = ordenTrabajo[0];
            otTitulo.setText("OT #" + ot.getConsecutivo());

            SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {
                @Override protected String doInBackground() throws Exception {
                    for (int i = 0; i < pasos.length; i++) {
                        publish(i);
                        Thread.sleep(600);
                    }
                    return controlador.resolverAlarma(ot);
                }

                @Override protected void process(java.util.List<Integer> chunks) {
                    int paso = chunks.get(chunks.size() - 1);
                    for (int i = 0; i < pasos.length; i++) {
                        if (i < paso) {
                            stepIcos[i].setText("✔");
                            stepIcos[i].setForeground(new Color(0x3B6D11));
                            stepLabels[i].setForeground(new Color(0x3B6D11));
                            stepLabels[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        } else if (i == paso) {
                            stepIcos[i].setText("●");
                            stepIcos[i].setForeground(new Color(0x185FA5));
                            stepLabels[i].setForeground(new Color(0x185FA5));
                            stepLabels[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
                        }
                    }
                    panel.revalidate();
                }

                @Override protected void done() {
                    try {
                        String resultado = get();
                        for (int i = 0; i < pasos.length; i++) {
                            stepIcos[i].setText("✔");
                            stepIcos[i].setForeground(new Color(0x3B6D11));
                            stepLabels[i].setForeground(new Color(0x3B6D11));
                            stepLabels[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                        lblComprobante.setText("✔ Resuelta — " + ot.getComprobanteBodega());
                        btnResolver.setText("✔ Resuelta");
                        btnResolver.setBackground(new Color(0x3B6D11));
                        JOptionPane.showMessageDialog(AlarmasPanel.this,
                            resultado, "Resultado OT #" + ot.getConsecutivo(),
                            JOptionPane.INFORMATION_MESSAGE);
                        recargar();
                    } catch (Exception ex) {
                        String mensaje = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        lblComprobante.setText("✗ No resuelta — " + mensaje);
                        lblComprobante.setForeground(new Color(0xA32D2D));
                        btnResolver.setText("Reintentar");
                        btnResolver.setBackground(new Color(0xA32D2D));
                        btnResolver.setEnabled(true);
                        btnOT.setEnabled(true);
                        JOptionPane.showMessageDialog(AlarmasPanel.this,
                            "No se pudo resolver la alarma.\n\nDetalle: " + mensaje,
                            "Error al resolver OT #" + ot.getConsecutivo(),
                            JOptionPane.ERROR_MESSAGE);
                    }
                    panel.revalidate();
                }
            };
            worker.execute();
        });

        panel.add(otTitulo);
        panel.add(stepsPanel);
        panel.add(lblComprobante);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnResolver);

        return panel;
    }

    private void mostrarRuta(AlarmaPendiente alarma) {
        JOptionPane.showMessageDialog(this,
            "Ubicación: " + alarma.getUbicacion()
            + "\nMáquina: " + alarma.getCodMaquina()
            + "\n\nDiríjase a: " + alarma.getUbicacion(),
            "Ruta hacia máquina " + alarma.getCodMaquina(),
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static JLabel badgeTipo(int tipo) {
        String texto; Color bg; Color fg;
        switch (tipo) {
            case 1:  texto = "Escasez ingrediente";   bg = new Color(0xFCEBEB); fg = new Color(0xA32D2D); break;
            case 2:  texto = "Falta moneda 100";      bg = new Color(0xFAEEDA); fg = new Color(0x854F0B); break;
            case 3:  texto = "Falta moneda 200";      bg = new Color(0xFAEEDA); fg = new Color(0x854F0B); break;
            case 4:  texto = "Falta moneda 500";      bg = new Color(0xFAEEDA); fg = new Color(0x854F0B); break;
            case 5:  texto = "Escasez suministro";    bg = new Color(0xE6F1FB); fg = new Color(0x185FA5); break;
            case 6:  texto = "Mal funcionamiento";    bg = new Color(0xFCEBEB); fg = new Color(0xA32D2D); break;
            default: texto = "Tipo " + tipo;          bg = new Color(0xF1EFE8); fg = new Color(0x5F5E5A); break;
        }
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(3, 7, 3, 7));
        return l;
    }

    private static JButton accentButton(String text, Color fg, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorder(BorderFactory.createLineBorder(fg.brighter(), 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 30));
        return b;
    }

    private static JLabel loadingLabel() {
        JLabel l = new JLabel("Cargando alarmas…");
        l.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        l.setForeground(new Color(0x888880));
        l.setBorder(new EmptyBorder(24, 0, 0, 0));
        return l;
    }

    private static JLabel emptyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(0x888880));
        l.setBorder(new EmptyBorder(24, 0, 0, 0));
        return l;
    }
}
