package view.swing;

import view.MainMenu;
import view.MainMenu.MenuItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

public class MainMenuPanel extends JPanel {

    private final MainMenu model;

    private static final Color BG_TOP = new Color(0x0d0d1a);
    private static final Color BG_BOTTOM = new Color(0x16213e);


    public MainMenuPanel(MainMenu model, MainMenu.MenuListener listener) {
        this.model = model;
        model.setListener(listener);

        setLayout(new GridBagLayout());
        setBackground(BG_TOP);

        buildContent();
    }


    private void buildContent() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("CIVILIZATION", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 52));
        title.setForeground(GameWindow.GOLD);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        add(title, gbc);

        JLabel subtitle = new JLabel("Advanced Programming Project", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subtitle.setForeground(GameWindow.TEXT_DIM);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 48, 0);
        add(subtitle, gbc);

        gbc.insets = new Insets(6, 80, 6, 80);

        gbc.gridy = 2;
        add(menuButton("▶  Start Game", GameWindow.GOLD, e -> model.onItemSelected(MenuItem.START)), gbc);

        gbc.gridy = 3;
        add(menuButton("⚙  Settings", GameWindow.TEAL, e -> showSettings()), gbc);

        gbc.gridy = 4;
        add(menuButton("✕  Exit", GameWindow.RED_ALERT, e -> showExitDialog()), gbc);
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }


    private void showSettings() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Settings",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(320, 160);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(GameWindow.BG_PANEL);

        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lbl = new JLabel("Music Volume");
        lbl.setForeground(GameWindow.TEXT_PRIMARY);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        JSlider slider = new JSlider(0, 100, (int) (model.getMusicVolume() * 100));
        slider.setOpaque(false);
        slider.setForeground(GameWindow.GOLD);
        slider.addChangeListener(e -> model.setMusicVolume(slider.getValue() / 100f));

        JButton close = new JButton("Close");
        close.addActionListener(e -> {
            model.onSettingsClosed();
            dialog.dispose();
        });
        styleDialogButton(close);

        panel.add(lbl);
        panel.add(slider);
        panel.add(close);
        dialog.add(panel);
        dialog.setVisible(true);
    }


    private void showExitDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Exit Game",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(300, 140);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(GameWindow.BG_PANEL);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel msg = new JLabel("Are you sure you want to exit?", SwingConstants.CENTER);
        msg.setForeground(GameWindow.TEXT_PRIMARY);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btns.setOpaque(false);

        JButton yes = new JButton("Exit");
        JButton no = new JButton("Cancel");
        yes.setForeground(GameWindow.RED_ALERT);
        no.setForeground(GameWindow.TEXT_PRIMARY);
        styleDialogButton(yes);
        styleDialogButton(no);

        yes.addActionListener(e -> {
            model.onExitConfirmed();
            dialog.dispose();
        });
        no.addActionListener(e -> {
            model.onExitCancelled();
            dialog.dispose();
        });

        btns.add(yes);
        btns.add(no);
        panel.add(msg, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }


    private static JButton menuButton(String text, Color accent, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(0x0f3460)
                        : new Color(0x16213e);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(accent);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 1),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private static void styleDialogButton(JButton btn) {
        btn.setBackground(GameWindow.BG_PANEL2);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameWindow.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
    }
}