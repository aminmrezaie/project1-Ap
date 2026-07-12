package view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class NotificationOverlay {


    public static class Notification {

        public final String message;
        public final GameRenderer.NotificationType type;
        public final long createdAtMs;
        public final long ttlMs;        // how long to display (milliseconds)

        private float fadeProgress;

        public Notification(String message, GameRenderer.NotificationType type, long ttlMs) {
            this.message     = message;
            this.type        = type;
            this.ttlMs       = ttlMs;
            this.createdAtMs = System.currentTimeMillis();
            this.fadeProgress = 0f;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAtMs > ttlMs;
        }

        public float getFadeProgress() {
            long elapsed = System.currentTimeMillis() - createdAtMs;
            float ratio  = (float) elapsed / ttlMs;
            return ratio < 0.8f ? 0f : (ratio - 0.8f) / 0.2f;
        }

        public String getColor() {
            return switch (type) {
                case INFO    -> "#00cfff";
                case SUCCESS -> "#4cff72";
                case WARNING -> "#ffd700";
                case CRISIS  -> "#ff4444";
            };
        }
    }


    private final int maxVisible;
    private final List<Notification> active;


    public NotificationOverlay() {
        this(5);
    }

    public NotificationOverlay(int maxVisible) {
        this.maxVisible = maxVisible;
        this.active     = new ArrayList<>();
    }


    public void show(String message, GameRenderer.NotificationType type) {
        show(message, type, defaultTtl(type));
    }

    public void show(String message, GameRenderer.NotificationType type, long ttlMs) {
        active.add(0, new Notification(message, type, ttlMs));
        if (active.size() > maxVisible) {
            active.remove(active.size() - 1);
        }
    }


    public void info(String message)    { show(message, GameRenderer.NotificationType.INFO); }
    public void success(String message) { show(message, GameRenderer.NotificationType.SUCCESS); }
    public void warning(String message) { show(message, GameRenderer.NotificationType.WARNING); }
    public void crisis(String message)  { show(message, GameRenderer.NotificationType.CRISIS); }

    public void tick() {
        active.removeIf(Notification::isExpired);
    }



    public List<Notification> getActive() {
        return Collections.unmodifiableList(active);
    }

    public boolean isEmpty() { return active.isEmpty(); }


    private long defaultTtl(GameRenderer.NotificationType type) {
        return switch (type) {
            case CRISIS  -> 5000L;
            case WARNING -> 3500L;
            case SUCCESS -> 2500L;
            case INFO    -> 2000L;
        };
    }
}
