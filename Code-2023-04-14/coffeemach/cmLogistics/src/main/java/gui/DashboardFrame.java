package gui;

import controlAlarma.AlarmaPendiente;
import controlAlarma.ControladorAlarmas;
import tecnicoMantenimiento.OrdenTrabajo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final ControladorAlarmas controlador;

    private AlarmasPanel   alarmasPanel;
    private OrdenMantenimientoUI ordenesPanel;
    private InventarioPanel inventarioPanel;
    private MaquinasPanel   maquinasPanel;

    private JButton[] navBtns;
    private JLabel    lblOpBadge;
    private JPanel    contentArea;
    private CardLayout cardLayout;

    private static final String CARD_ALARMAS    = "alarmas";
    private static final String CARD_ORDENES    = "ordenes";
    private static final String CARD_INVENTARIO = "inventario";
    private static final String CARD_MAQUINAS   = "maquinas";

    public DashboardFrame(ControladorAlarmas controlador) {
        this.controlador = controlador;
        initUI();
        cargarAlarmas();
    }

    private void initUI() {
        setTitle("cmLogistics");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        add(root);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(new Color(0xF7F7F5));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0xE0DDD6)));
        sb.setPreferredSize(new Dimension(200, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 14, 12, 14));

        JLabel logoIco = new JLabel("☕  cmLogistics");
        logoIco.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        logoIco.setForeground(new Color(0x185FA5));

        lblOpBadge = new JLabel("Operador #" + controlador.getCodigoOperadorActivo());
        lblOpBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblOpBadge.setForeground(new Color(0x888880));

        JLabel sesionBadge = badge("Sesión activa", new Color(0xE6F1FB), new Color(0x185FA5));

        header.add(logoIco);
        header.add(Box.createVerticalStrut(4));
        header.add(lblOpBadge);
        header.add(Box.createVerticalStrut(6));
        header.add(sesionBadge);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(6, 0, 6, 0));

        String[][] navItems = {
            {"🔔", " Alarmas",    CARD_ALARMAS},
            {"📋", " Órdenes",    CARD_ORDENES},
            {"🏭", " Inventario", CARD_INVENTARIO},
            {"🖥",  " Máquinas",   CARD_MAQUINAS},
        };

        navBtns = new JButton[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            final String card = navItems[i][2];
            JButton btn = navButton(navItems[i][0] + navItems[i][1]);
            navBtns[i] = btn;
            btn.addActionListener(e -> showCard(card, idx));
            nav.add(btn);
        }
        setActiveNav(0);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(0xE0DDD6)),
            new EmptyBorder(10, 14, 12, 14)));

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSalir.setForeground(new Color(0x666660));
        btnSalir.setBackground(new Color(0xF7F7F5));
        btnSalir.setBorder(BorderFactory.createLineBorder(new Color(0xD0CEC5), 1));
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> { dispose(); System.exit(0); });

        footer.add(btnSalir, BorderLayout.CENTER);

        sb.add(header, BorderLayout.NORTH);
        sb.add(nav,    BorderLayout.CENTER);
        sb.add(footer, BorderLayout.SOUTH);
        return sb;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(new Color(0xF7F7F5));
        b.setForeground(new Color(0x666660));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setMinimumSize(new Dimension(160, 38));
        b.setPreferredSize(new Dimension(200, 38));
        b.setBorder(new CompoundBorder(
            new MatteBorder(0, 2, 0, 0, new Color(0xF7F7F5)),
            new EmptyBorder(0, 12, 0, 8)));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!b.getFont().isBold()) b.setBackground(new Color(0xEEECE7));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!b.getFont().isBold()) b.setBackground(new Color(0xF7F7F5));
            }
        });
        return b;
    }

    private void setActiveNav(int idx) {
        for (int i = 0; i < navBtns.length; i++) {
            JButton b = navBtns[i];
            if (i == idx) {
                b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
                b.setForeground(new Color(0x185FA5));
                b.setBackground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    new MatteBorder(0, 2, 0, 0, new Color(0x185FA5)),
                    new EmptyBorder(0, 12, 0, 8)));
            } else {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setForeground(new Color(0x666660));
                b.setBackground(new Color(0xF7F7F5));
                b.setBorder(new CompoundBorder(
                    new MatteBorder(0, 2, 0, 0, new Color(0xF7F7F5)),
                    new EmptyBorder(0, 12, 0, 8)));
            }
        }
    }

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Color.WHITE);

        alarmasPanel    = new AlarmasPanel(controlador, this);
        ordenesPanel    = new OrdenMantenimientoUI(controlador);
        inventarioPanel = new InventarioPanel(controlador);
        maquinasPanel   = new MaquinasPanel(controlador);

        contentArea.add(alarmasPanel,    CARD_ALARMAS);
        contentArea.add(ordenesPanel,    CARD_ORDENES);
        contentArea.add(inventarioPanel, CARD_INVENTARIO);
        contentArea.add(maquinasPanel,   CARD_MAQUINAS);

        return contentArea;
    }

    private void showCard(String card, int navIdx) {
        setActiveNav(navIdx);
        cardLayout.show(contentArea, card);
        switch (card) {
            case CARD_ALARMAS:    alarmasPanel.recargar();    break;
            case CARD_ORDENES:    ordenesPanel.recargar();    break;
            case CARD_INVENTARIO: inventarioPanel.recargar(); break;
            case CARD_MAQUINAS:   maquinasPanel.recargar();   break;
        }
    }

    public void registrarOrdenTrabajo(OrdenTrabajo ot) {
        ordenesPanel.agregarOrden(ot);
    }

    private void cargarAlarmas() {
        alarmasPanel.recargar();
    }

    static JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(2, 7, 2, 7));
        return l;
    }

    static JPanel topBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0xE8E6E0)),
            new EmptyBorder(12, 20, 12, 20)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(0x1A1A18));
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }
}