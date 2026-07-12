package view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_WIDTH  = 1280;
    public static final int DEFAULT_HEIGHT = 800;


    public static final Color BG_DARK      = new Color(0x1a1a2e);
    public static final Color BG_PANEL     = new Color(0x16213e);
    public static final Color BG_PANEL2    = new Color(0x0f3460);
    public static final Color GOLD         = new Color(0xffd700);
    public static final Color TEAL         = new Color(0x00cfff);
    public static final Color RED_ALERT    = new Color(0xff4444);
    public static final Color GREEN_OK     = new Color(0x4cff72);
    public static final Color TEXT_PRIMARY = new Color(0xe0e0e0);
    public static final Color TEXT_DIM     = new Color(0x8899aa);
    public static final Color BORDER_COLOR = new Color(0x2a4a6a);


    private final MapPanel      mapPanel;
    private final MinimapPanel  minimapPanel;


    public GameWindow(MapPanel mapPanel,
                      HUDPanel hudPanel,
                      UnitInfoPanel unitInfoPanel,
                      MinimapPanel minimapPanel) {

        super("Civilization — Advanced Programming Project");

        this.mapPanel     = mapPanel;
        this.minimapPanel = minimapPanel;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        buildLayout(hudPanel, unitInfoPanel);
        attachResizeListener();
    }


    private void buildLayout(HUDPanel hudPanel, UnitInfoPanel unitInfoPanel) {
        setLayout(new BorderLayout(0, 0));

        add(hudPanel, BorderLayout.NORTH);

        add(unitInfoPanel, BorderLayout.SOUTH);

        JLayeredPane layered = new JLayeredPane() {
            @Override
            public void doLayout() {
                mapPanel.setBounds(0, 0, getWidth(), getHeight());

                int mx = getWidth()  - MinimapPanel.WIDTH  - 10;
                int my = getHeight() - MinimapPanel.HEIGHT - 10;
                minimapPanel.setBounds(mx, my, MinimapPanel.WIDTH, MinimapPanel.HEIGHT);
            }
        };
        layered.setBackground(BG_DARK);
        layered.setOpaque(true);

        layered.add(mapPanel,     JLayeredPane.DEFAULT_LAYER);
        layered.add(minimapPanel, JLayeredPane.PALETTE_LAYER);

        add(layered, BorderLayout.CENTER);
    }


    private void attachResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

                revalidate();
            }
        });
    }


    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocationRelativeTo(null);
            setVisible(true);
            mapPanel.requestFocusInWindow();
        });
    }

    public void showMainMenu(MainMenuPanel menuPanel) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        dialog.setLocationRelativeTo(this);
        dialog.setContentPane(menuPanel);
        dialog.setVisible(true);
    }
}
