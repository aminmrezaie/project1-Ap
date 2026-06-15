package view.swing;

import model.enums.Position;
import view.Minimap;
import view.Minimap.MinimapSnapshot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;


public class MinimapPanel extends JPanel {

    public static final int WIDTH = Minimap.DEFAULT_WIDTH;
    public static final int HEIGHT = Minimap.DEFAULT_HEIGHT;

    private final Minimap minimap;
    private MinimapSnapshot snapshot;

    public interface MinimapClickListener {
        void onMinimapClicked(Position worldPos);
    }

    private MinimapClickListener clickListener;


    public MinimapPanel(Minimap minimap) {
        this.minimap = minimap;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0x0d0d1a));
        setBorder(BorderFactory.createLineBorder(GameWindow.BORDER_COLOR, 1));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Position pos = minimap.onClick(e.getX(), e.getY());
                if (pos != null && clickListener != null) {
                    clickListener.onMinimapClicked(pos);
                }
            }
        });
    }


    public void updateSnapshot(MinimapSnapshot snap) {
        this.snapshot = snap;
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (snapshot == null) return;

        Graphics2D g2 = (Graphics2D) g.create();

        for (Map.Entry<Position, String> entry : snapshot.hexColors.entrySet()) {
            int[] px = minimap.hexToPixel(entry.getKey());
            if (px[0] < 0 || px[0] >= WIDTH || px[1] < 0 || px[1] >= HEIGHT) continue;

            g2.setColor(Color.decode(entry.getValue()));
            g2.fillRect(px[0], px[1], 2, 2);
        }

        g2.setColor(new Color(0x4a8fc430, true));
        for (Position pos : snapshot.borderPositions) {
            int[] px = minimap.hexToPixel(pos);
            if (px[0] < 0 || px[0] >= WIDTH || px[1] < 0 || px[1] >= HEIGHT) continue;
            g2.fillRect(px[0], px[1], 2, 2);
        }

        g2.setColor(new Color(0x00cfff));
        for (Position pos : snapshot.unitPositions) {
            int[] px = minimap.hexToPixel(pos);
            if (px[0] < 0 || px[0] >= WIDTH || px[1] < 0 || px[1] >= HEIGHT) continue;
            g2.fillOval(px[0] - 1, px[1] - 1, 4, 4);
        }

        if (snapshot.viewportCenter != null) {
            int[] vc = minimap.hexToPixel(snapshot.viewportCenter);
            int vw = WIDTH / 4;
            int vh = HEIGHT / 4;
            g2.setColor(new Color(0xffffff88, true));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(vc[0] - vw / 2, vc[1] - vh / 2, vw, vh);
        }

        g2.setColor(GameWindow.BORDER_COLOR);
        g2.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(GameWindow.TEXT_DIM);
        g2.drawString("MAP", 4, 10);

        g2.dispose();
    }


    public void setClickListener(MinimapClickListener l) {
        this.clickListener = l;
    }
}