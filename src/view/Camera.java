package view;

import model.enums.Position;


public class Camera {


    public static final int[] ZOOM_LEVELS = {24, 36, 48, 64, 80};
    public static final int DEFAULT_ZOOM_INDEX = 2;   // 48px


    private double offsetX;
    private double offsetY;

    private int zoomIndex;

    private int viewportWidth;
    private int viewportHeight;

    private int mapPixelWidth;
    private int mapPixelHeight;


    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.zoomIndex = DEFAULT_ZOOM_INDEX;
        this.offsetX = 0;
        this.offsetY = 0;
    }


    public int getHexSize() {
        return ZOOM_LEVELS[zoomIndex];
    }

    public int getZoomIndex() {
        return zoomIndex;
    }

    public boolean canZoomIn() {
        return zoomIndex < ZOOM_LEVELS.length - 1;
    }

    public boolean canZoomOut() {
        return zoomIndex > 0;
    }

    public void zoomIn() {
        if (canZoomIn()) {
            zoomIndex++;
            clampOffset();
        }
    }

    public void zoomOut() {
        if (canZoomOut()) {
            zoomIndex--;
            clampOffset();
        }
    }


    public void pan(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
        clampOffset();
    }


    public void centerOn(Position hexPos) {
        int[] pixel = hexToWorldPixel(hexPos);
        offsetX = (viewportWidth / 2.0) - pixel[0];
        offsetY = (viewportHeight / 2.0) - pixel[1];
        clampOffset();
    }


    public int[] hexToWorldPixel(Position pos) {
        int size = getHexSize();
        double x = size * 1.5 * pos.getQ();
        double y = size * Math.sqrt(3) * (pos.getR() + pos.getQ() / 2.0);
        return new int[]{(int) x, (int) y};
    }

    public int[] worldToScreen(int worldX, int worldY) {
        return new int[]{(int) (worldX + offsetX), (int) (worldY + offsetY)};
    }


    public Position screenToHex(int screenX, int screenY) {
        int size = getHexSize();
        double worldX = screenX - offsetX;
        double worldY = screenY - offsetY;

        double q = (2.0 / 3.0 * worldX) / size;
        double r = (-1.0 / 3.0 * worldX + Math.sqrt(3) / 3.0 * worldY) / size;

        return axialRound(q, r);
    }

    private Position axialRound(double q, double r) {
        double s = -q - r;
        long rq = Math.round(q);
        long rr = Math.round(r);
        long rs = Math.round(s);

        double dq = Math.abs(rq - q);
        double dr = Math.abs(rr - r);
        double ds = Math.abs(rs - s);

        if (dq > dr && dq > ds) rq = -rr - rs;
        else if (dr > ds) rr = -rq - rs;

        return new Position((int) rq, (int) rr);
    }


    public void setMapPixelSize(int width, int height) {
        this.mapPixelWidth = width;
        this.mapPixelHeight = height;
    }

    public void setViewportSize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        clampOffset();
    }

    private void clampOffset() {
        double minX = viewportWidth - mapPixelWidth;
        double minY = viewportHeight - mapPixelHeight;
        offsetX = Math.max(minX, Math.min(0, offsetX));
        offsetY = Math.max(minY, Math.min(0, offsetY));
    }


    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }
}