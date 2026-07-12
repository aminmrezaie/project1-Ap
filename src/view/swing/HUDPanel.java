package view.swing;

import model.economy.Empire;
import model.enums.ResourceType;
import view.GameRenderer.NotificationType;
import view.HUD;
import view.HUD.ResourceDisplay;
import view.HUD.Warning;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class HUDPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int HEIGHT = 64;


    private final JPanel  resourceBar;
    private final JLabel  turnLabel;
    private final JLabel  unitCapLabel;
    private final JLabel  productionLabel;
    private final JButton endTurnButton;
    private final JLabel  warningLabel;

    private final JLabel notifLabel;
    private Timer        notifTimer;


    private ActionListener endTurnListener;


    public HUDPanel() {
        setPreferredSize(new Dimension(0, HEIGHT));
        setBackground(GameWindow.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GameWindow.BORDER_COLOR));
        setLayout(new BorderLayout(8, 0));

        resourceBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        resourceBar.setOpaque(false);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        centerPanel.setOpaque(false);

        turnLabel       = styledLabel("Turn: 1",   GameWindow.GOLD,         13, Font.BOLD);
        unitCapLabel    = styledLabel("Units: 0/5", GameWindow.TEXT_PRIMARY, 12, Font.PLAIN);
        productionLabel = styledLabel("Idle",       GameWindow.TEXT_DIM,     11, Font.ITALIC);
        centerPanel.add(turnLabel);
        centerPanel.add(unitCapLabel);
        centerPanel.add(productionLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        warningLabel = styledLabel("", GameWindow.RED_ALERT, 11, Font.BOLD);
        notifLabel   = styledLabel("", GameWindow.GREEN_OK,  11, Font.BOLD);

        endTurnButton = new JButton("End Turn ▶");
        styleButton(endTurnButton);
        endTurnButton.addActionListener(e -> {
            if (endTurnListener != null) endTurnListener.actionPerformed(e);
        });

        rightPanel.add(notifLabel);
        rightPanel.add(warningLabel);
        rightPanel.add(endTurnButton);

        add(resourceBar,  BorderLayout.WEST);
        add(centerPanel,  BorderLayout.CENTER);
        add(rightPanel,   BorderLayout.EAST);
    }


    public void update(Empire empire, int currentTurn) {
        HUD snap = HUD.buildFrom(empire, currentTurn, null);

        resourceBar.removeAll();
        for (ResourceDisplay rd : snap.getResources()) {
            resourceBar.add(buildResourceWidget(rd));
        }

        turnLabel.setText("Turn: " + snap.getCurrentTurn());

        HUD.UnitCountDisplay uc = snap.getUnitCount();
        unitCapLabel.setText("Units: " + uc.formatTotal());
        unitCapLabel.setForeground(uc.isAtCap() ? GameWindow.RED_ALERT : GameWindow.TEXT_PRIMARY);

        HUD.ProductionQueueDisplay pq = snap.getProductionQueue();
        productionLabel.setText("Producing: " + pq.format());

        if (snap.hasCrisis()) {
            warningLabel.setText("⚠ STARVATION!");
            warningLabel.setForeground(GameWindow.RED_ALERT);
        } else if (snap.hasWarnings()) {
            Warning w = snap.getActiveWarnings().get(0);
            warningLabel.setText("⚠ " + w.message);
            warningLabel.setForeground(GameWindow.GOLD);
        } else {
            warningLabel.setText("");
        }

        endTurnButton.setEnabled(!snap.isEndTurnLocked());
        if (snap.hasIdleUnitWarning()) {
            endTurnButton.setForeground(GameWindow.GOLD);
            endTurnButton.setToolTipText("Some units still have unused AP");
        } else {
            endTurnButton.setForeground(Color.WHITE);
            endTurnButton.setToolTipText(null);
        }

        revalidate();
        repaint();
    }


    private JPanel buildResourceWidget(ResourceDisplay rd) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        String icon = switch (rd.type) {
            case FOOD  -> "🌾";
            case WOOD  -> "🪵";
            case STONE -> "⛏";
            case IRON  -> "⚙";
        };

        JLabel iconLbl = styledLabel(icon, Color.WHITE, 14, Font.PLAIN);

        String sign = rd.netRate >= 0 ? "+" : "";
        String text = rd.current + "/" + rd.capacity + " " + sign + rd.netRate;
        Color  netColor = rd.netRate < 0 ? GameWindow.RED_ALERT
                : rd.netRate > 0 ? GameWindow.GREEN_OK
                : GameWindow.TEXT_DIM;
        JLabel valueLbl = styledLabel(text, netColor, 11, Font.BOLD);

        p.add(iconLbl);
        p.add(valueLbl);
        p.setToolTipText(rd.type.getDisplayName() + ": " + rd.format());
        return p;
    }



    public void showNotification(String message, NotificationType type) {
        Color color = switch (type) {
            case INFO    -> GameWindow.TEAL;
            case SUCCESS -> GameWindow.GREEN_OK;
            case WARNING -> GameWindow.GOLD;
            case CRISIS  -> GameWindow.RED_ALERT;
        };
        notifLabel.setText(message);
        notifLabel.setForeground(color);

        if (notifTimer != null && notifTimer.isRunning()) notifTimer.stop();
        notifTimer = new Timer(3000, e -> { notifLabel.setText(""); });
        notifTimer.setRepeats(false);
        notifTimer.start();
    }


    public void setEndTurnListener(ActionListener l) { this.endTurnListener = l; }


    private static JLabel styledLabel(String text, Color color, int size, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", style, size));
        return lbl;
    }

    private static void styleButton(JButton btn) {
        btn.setBackground(GameWindow.BG_PANEL2);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameWindow.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
