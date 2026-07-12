package view;

import model.economy.Empire;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Unit;

public interface GameRenderer {

    void render(GameMap map, Empire empire);

    void renderHex(Hex hex);

    void renderUnit(Unit unit);

    void renderHUD(Empire empire, int currentTurn);

    void highlight(java.util.List<model.enums.Position> positions, HighlightType type);

    void clearHighlights();

    void showNotification(String message, NotificationType type);

    enum HighlightType {
        MOVEMENT_RANGE,
        BUILD_TARGET,
        BORDER_EXPAND_PREVIEW,
        SELECTED
    }

    enum NotificationType {
        INFO, WARNING, CRISIS, SUCCESS
    }
}
