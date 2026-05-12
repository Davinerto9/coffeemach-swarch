package gui;

import controlAlarma.ControladorAlarmas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana de inicio de sesión del operador logístico.
 * Al autenticarse exitosamente abre el DashboardFrame.
 */
public class LoginFrame extends JFrame {

    private final ControladorAlarmas controlador;

    private JTextField txtCodigo;
    private JPasswordField txtPass;
    private JButton btnEntrar;
    private JLabel lblError;

    public LoginFrame(ControladorAlarmas controlador) {
        this.controlador = controlador;
        initUI();
    }

    private void initUI() {
        setTitle("cmLogistics — Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(380, 300);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(32, 40, 32, 40));

        // ── Encabezado ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);

        JLabel ico = new JLabel("☕");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel title = new JLabel("  cmLogistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0x185FA5));

        header.add(ico);
        header.add(title);

        JLabel subtitle = new JLabel("Ingrese sus credenciales para continuar");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x666666));
        subtitle.setBorder(new EmptyBorder(4, 0, 16, 0));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(header);
        headerPanel.add(subtitle);

        // ── Formulario ──────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);

        // Código operador
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(label("Código operador"), gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 1;
        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCodigo.setPreferredSize(new Dimension(0, 32));
        form.add(txtCodigo, gc);

        // Contraseña
        gc.gridx = 0; gc.gridy = 2;
        form.add(label("Contraseña"), gc);
        gc.gridx = 0; gc.gridy = 3;
        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPass.setPreferredSize(new Dimension(0, 32));
        form.add(txtPass, gc);

        // Botón
        gc.gridx = 0; gc.gridy = 4;
        gc.insets = new Insets(12, 0, 0, 0);
        btnEntrar = primaryButton("Entrar →");
        form.add(btnEntrar, gc);

        // Error
        gc.gridx = 0; gc.gridy = 5;
        gc.insets = new Insets(6, 0, 0, 0);
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(0xA32D2D));
        form.add(lblError, gc);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        add(root);

        // ── Acciones ────────────────────────────────────────────────────────
        ActionListener login = e -> intentarLogin();
        btnEntrar.addActionListener(login);
        txtPass.addActionListener(login);
    }

    private void intentarLogin() {
        String codStr = txtCodigo.getText().trim();
        String pass   = new String(txtPass.getPassword());

        if (codStr.isEmpty() || pass.isEmpty()) {
            lblError.setText("Complete todos los campos.");
            return;
        }

        int cod;
        try {
            cod = Integer.parseInt(codStr);
        } catch (NumberFormatException ex) {
            lblError.setText("El código debe ser numérico.");
            return;
        }

        btnEntrar.setEnabled(false);
        btnEntrar.setText("Verificando…");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return controlador.iniciarSesion(cod, pass);
            }
            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        dispose();
                        new DashboardFrame(controlador).setVisible(true);
                    } else {
                        lblError.setText("Credenciales incorrectas. Intente de nuevo.");
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("Entrar →");
                        txtPass.setText("");
                    }
                } catch (Exception ex) {
                    lblError.setText("Error de conexión: " + ex.getMessage());
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("Entrar →");
                }
            }
        };
        worker.execute();
    }

    // ── Helpers de UI ────────────────────────────────────────────────────────

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(0x444444));
        return l;
    }

    static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(new Color(0x185FA5));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 36));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0x0C447C)); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(new Color(0x185FA5)); }
        });
        return b;
    }
}