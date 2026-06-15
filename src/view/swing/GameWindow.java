package view.swing;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 800;


    public static final Color BG_DARK = new Color(0x1a1a2e);
    public static final Color BG_PANEL = new Color(0x16213e);
    public static final Color BG_PANEL2 = new Color(0x0f3460);
    public static final Color GOLD = new Color(0xffd700);
    public static final Color TEAL = new Color(0x00cfff);
    public static final Color RED_ALERT = new Color(0xff4444);
    public static final Color GREEN_OK = new Color(0x4cff72);
    public static final Color TEXT_PRIMARY = new Color(0xe0e0e0);
    public static final Color TEXT_DIM = new Color(0x8899aa);
    public static final Color BORDER_COLOR = new Color(0x2a4a6a);


    public GameWindow(MapPanel mapPanel,
                      HUDPanel hudPanel,
                      UnitInfoPanel unitInfoPanel,
                      MinimapPanel minimapPanel) {

        super("Civilization — Advanced Programming Project");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        buildLayout(mapPanel, hudPanel, unitInfoPanel, minimapPanel);
    }


    private void buildLayout(MapPanel mapPanel,
                             HUDPanel hudPanel,
                             UnitInfoPanel unitInfoPanel,
                             MinimapPanel minimapPanel) {

        setLayout(new BorderLayout(0, 0));

        add(hudPanel, BorderLayout.NORTH);

        add(unitInfoPanel, BorderLayout.SOUTH);

        JPanel centerArea = new JPanel(new BorderLayout(0, 0));
        centerArea.setBackground(BG_DARK);
        centerArea.add(mapPanel, BorderLayout.CENTER);

        JPanel mapOverlay = new JPanel(null);
        mapOverlay.setOpaque(false);
        mapOverlay.add(minimapPanel);
        minimapPanel.setBounds(
                DEFAULT_WIDTH - MinimapPanel.WIDTH - 12,
                DEFAULT_HEIGHT - MinimapPanel.HEIGHT - 120,
                MinimapPanel.WIDTH,
                MinimapPanel.HEIGHT
        );

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT - 130));
        mapPanel.setBounds(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT - 130);
        minimapPanel.setBounds(
                DEFAULT_WIDTH - MinimapPanel.WIDTH - 12,
                DEFAULT_HEIGHT - MinimapPanel.HEIGHT - 250,
                MinimapPanel.WIDTH,
                MinimapPanel.HEIGHT
        );
        layered.add(mapPanel, JLayeredPane.DEFAULT_LAYER);
        layered.add(minimapPanel, JLayeredPane.PALETTE_LAYER);

        add(layered, BorderLayout.CENTER);
    }


    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            pack();
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocationRelativeTo(null);
            setVisible(true);
        });
    }
}