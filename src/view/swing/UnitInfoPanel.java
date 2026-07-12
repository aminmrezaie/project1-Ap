package view.swing;

import model.unit.Builder;
import model.unit.Explorer;
import model.unit.Unit;
import model.unit.Worker;
import view.UnitPanel;
import view.UnitPanel.Action;
import view.UnitPanel.ActionButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class UnitInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int HEIGHT = 90;


    private final JLabel  unitNameLabel;
    private final JLabel  unitStatusLabel;
    private final JPanel  apBarPanel;
    private final JPanel  actionButtonsPanel;
    private final JLabel  emptyLabel;

    private final Map<JButton, Action> buttonActions = new HashMap<>();

    private UnitPanel.PanelListener listener;


    public UnitInfoPanel() {
        setPreferredSize(new Dimension(0, HEIGHT));
        setBackground(GameWindow.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, GameWindow.BORDER_COLOR));
        setLayout(new BorderLayout(8, 4));

        emptyLabel = new JLabel("  Click a unit on the map to select it", SwingConstants.CENTER);
        emptyLabel.setForeground(GameWindow.TEXT_DIM);
        emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        add(emptyLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 0));
        unitNameLabel   = styledLabel("", GameWindow.GOLD, 14, Font.BOLD);
        unitStatusLabel = styledLabel("", GameWindow.TEXT_DIM, 11, Font.PLAIN);
        infoPanel.add(unitNameLabel);
        infoPanel.add(unitStatusLabel);

        actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 16));
        actionButtonsPanel.setOpaque(false);

        apBarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        apBarPanel.setOpaque(false);
        apBarPanel.setPreferredSize(new Dimension(100, HEIGHT));

        JPanel leftAndCenter = new JPanel(new BorderLayout());
        leftAndCenter.setOpaque(false);
        leftAndCenter.add(infoPanel, BorderLayout.WEST);
        leftAndCenter.add(actionButtonsPanel, BorderLayout.CENTER);

    }


    public void showUnit(Unit unit, java.util.List<ActionButton> buttons) {
        removeAll();

        unitNameLabel.setText(unit.getType().getDisplayName() + "  #" + unit.getId());

        unitStatusLabel.setText(buildStatusText(unit));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 0));
        infoPanel.add(unitNameLabel);
        infoPanel.add(unitStatusLabel);

        actionButtonsPanel.removeAll();
        buttonActions.clear();
        for (ActionButton ab : buttons) {
            JButton btn = buildActionButton(ab, unit);
            actionButtonsPanel.add(btn);
            buttonActions.put(btn, ab.action);
        }

        JPanel apDisplay = buildAPDisplay(unit);

        JPanel leftAndCenter = new JPanel(new BorderLayout());
        leftAndCenter.setOpaque(false);
        leftAndCenter.add(infoPanel, BorderLayout.WEST);
        leftAndCenter.add(actionButtonsPanel, BorderLayout.CENTER);

        add(leftAndCenter, BorderLayout.CENTER);
        add(apDisplay,     BorderLayout.EAST);

        revalidate();
        repaint();
    }

    public void showEmpty() {
        removeAll();
        add(emptyLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }


    private JButton buildActionButton(ActionButton ab, Unit unit) {
        JButton btn = new JButton(ab.label);
        btn.setEnabled(ab.enabled);
        btn.setToolTipText(ab.tooltip);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (ab.enabled) {
            btn.setBackground(GameWindow.BG_PANEL2);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GameWindow.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
        } else {
            btn.setBackground(new Color(0x1a1a2e));
            btn.setForeground(GameWindow.TEXT_DIM);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x2a2a3e)),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
        }

        btn.addActionListener(e -> {
            if (listener != null) listener.onActionClicked(ab.action, unit);
        });

        return btn;
    }


    private JPanel buildAPDisplay(Unit unit) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth() - 20, h = 12;
                int x = 10, y = (getHeight() - h) / 2;

                g2.setColor(new Color(0x333333));
                g2.fillRoundRect(x, y, w, h, 6, 6);

                float ratio = unit.getMaxAP() == 0 ? 0 : (float) unit.getAP() / unit.getMaxAP();
                int fill = (int)(w * ratio);
                Color c = ratio > 0.6f ? new Color(0x44ff88)
                        : ratio > 0.3f ? new Color(0xffdd00)
                        :                new Color(0xff4444);
                g2.setColor(c);
                g2.fillRoundRect(x, y, fill, h, 6, 6);

                g2.setColor(GameWindow.BORDER_COLOR);
                g2.drawRoundRect(x, y, w, h, 6, 6);

                g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                String apText = "AP  " + unit.getAP() + " / " + unit.getMaxAP();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(apText, x + (w - fm.stringWidth(apText)) / 2, y - 4);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(130, HEIGHT));
        return panel;
    }


    private String buildStatusText(Unit unit) {
        return switch (unit.getType()) {
            case BUILDER -> {
                Builder b = (Builder) unit;
                yield "Charges: " + b.getCharges() + " / 3";
            }
            case WORKER -> {
                Worker w = (Worker) unit;
                yield w.isStationed()
                        ? "Stationed in: " + w.getAssignedBuilding().getType().getDisplayName()
                        : "Idle — not assigned";
            }
            case EXPLORER -> {
                Explorer e = (Explorer) unit;
                yield e.isAutoExploreEnabled() ? "Auto-Explore: ON" : "Manual control";
            }
            case BORDER_EXPANDER -> "One-time use — expands 7 hexes";
        };
    }


    private static JLabel styledLabel(String text, Color color, int size, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", style, size));
        return lbl;
    }

    public void setListener(UnitPanel.PanelListener l) { this.listener = l; }
}
