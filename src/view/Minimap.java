package view;

import model.economy.Empire;
import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;

public class Minimap {


    public static final int DEFAULT_WIDTH  = 200;  // pixels
    public static final int DEFAULT_HEIGHT = 150;

    public static final String COLOR_UNEXPLORED = "#0d0d1a";
    public static final String COLOR_EXPLORED   = "#2a3a4a";
    public static final String COLOR_OWNED      = "#1a3a5c";
    public static final String COLOR_FOREST     = "#1e3d1e";
    public static final String COLOR_MOUNTAIN   = "#4a3a2a";
    public static final String COLOR_FARMLAND   = "#3a4a1e";
    public static final String COLOR_TOWN_HALL  = "#ffd700";
    public static final String COLOR_UNIT       = "#00cfff";
    public static final String COLOR_BORDER     = "#4a8fc4";


    private final int width;
    private final int height;

    private Position viewportCenter;

    private double scaleX;
    private double scaleY;

    private boolean visible;


    public Minimap() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Minimap(int width, int height) {
        this.width   = width;
        this.height  = height;
        this.visible = true;
    }


    public void calibrate(int mapWidth, int mapHeight) {
        this.scaleX = (double) width  / Math.max(mapWidth,  1);
        this.scaleY = (double) height / Math.max(mapHeight, 1);
    }

    public int[] hexToPixel(Position pos) {
        // Convert axial (q, r) to pixel offset
        int col = pos.getQ() + (pos.getR() - (pos.getR() & 1)) / 2;
        int row = pos.getR();
        int px = (int) (col * scaleX);
        int py = (int) (row * scaleY);
        return new int[]{px, py};
    }

    public Position pixelToHex(int px, int py) {
        int col = (int) (px / scaleX);
        int row = (int) (py / scaleY);
        int q   = col - (row - (row & 1)) / 2;
        return new Position(q, row);
    }


    public Position onClick(int pixelX, int pixelY) {
        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= height) {
            return null;
        }
        return pixelToHex(pixelX, pixelY);
    }

    public MinimapSnapshot snapshot(GameMap map, Empire empire) {
        return new MinimapSnapshot(map, empire, this);
    }



    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void toggleVisibility() { this.visible = !visible; }
    public Position getViewportCenter() { return viewportCenter; }
    public void setViewportCenter(Position center) { this.viewportCenter = center; }

    public static class MinimapSnapshot {

        public final int width;
        public final int height;

        public final java.util.Map<Position, String> hexColors;

        public final java.util.List<Position> unitPositions;

        public final java.util.List<Position> borderPositions;

        public final Position viewportCenter;

        public MinimapSnapshot(GameMap map, Empire empire, Minimap minimap) {
            this.width          = minimap.width;
            this.height         = minimap.height;
            this.viewportCenter = minimap.viewportCenter;

            java.util.Map<Position, String> colors = new java.util.HashMap<>();
            for (Hex hex : map.getAllHexes()) {
                Position pos = hex.getPosition();
                String color;
                if (!hex.isExplored()) {
                    color = COLOR_UNEXPLORED;
                } else if (hex.isOwned()) {
                    color = switch (hex.getTerrain()) {
                        case FOREST   -> COLOR_FOREST;
                        case MOUNTAIN -> COLOR_MOUNTAIN;
                        case FARMLAND -> COLOR_FARMLAND;
                        default       -> COLOR_OWNED;
                    };
                } else {
                    color = COLOR_EXPLORED;
                }
                colors.put(pos, color);
            }
            this.hexColors = java.util.Collections.unmodifiableMap(colors);

            java.util.List<Position> uPos = new java.util.ArrayList<>();
            empire.getUnits().stream()
                    .filter(u -> u.isAlive())
                    .forEach(u -> uPos.add(u.getPosition()));
            this.unitPositions = java.util.Collections.unmodifiableList(uPos);

            this.borderPositions = java.util.List.copyOf(empire.getTerritory());
        }
    }
}
