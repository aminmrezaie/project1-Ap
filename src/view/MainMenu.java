package view;

public class MainMenu {


    public enum MenuItem {
        START, SETTINGS, EXIT
    }



    public enum Screen {
        MAIN,
        SETTINGS,
        EXIT_CONFIRM
    }


    private Screen currentScreen;
    private MenuItem hoveredItem;
    private float musicVolume;
    private boolean visible;


    public interface MenuListener {
        void onStartGame();
        void onExitConfirmed();
        void onMusicVolumeChanged(float volume);
    }

    private MenuListener listener;


    public MainMenu() {
        this.currentScreen = Screen.MAIN;
        this.musicVolume   = 0.5f;
        this.visible       = true;
    }

    public void onItemSelected(MenuItem item) {
        switch (item) {
            case START    -> handleStart();
            case SETTINGS -> currentScreen = Screen.SETTINGS;
            case EXIT     -> currentScreen = Screen.EXIT_CONFIRM;
        }
    }

    private void handleStart() {
        this.visible = false;
        if (listener != null) listener.onStartGame();
    }

    public void onExitConfirmed() {
        if (listener != null) listener.onExitConfirmed();
    }

    public void onExitCancelled() {
        currentScreen = Screen.MAIN;
    }

    public void onSettingsClosed() {
        currentScreen = Screen.MAIN;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (listener != null) listener.onMusicVolumeChanged(this.musicVolume);
    }


    public Screen getCurrentScreen()  { return currentScreen; }
    public float getMusicVolume()     { return musicVolume; }
    public boolean isVisible()        { return visible; }
    public MenuItem getHoveredItem()  { return hoveredItem; }
    public void setHoveredItem(MenuItem item) { this.hoveredItem = item; }
    public void setListener(MenuListener l)   { this.listener = l; }


    public boolean isOnMainScreen()    { return currentScreen == Screen.MAIN; }
    public boolean isOnSettings()      { return currentScreen == Screen.SETTINGS; }
    public boolean isExitDialogOpen()  { return currentScreen == Screen.EXIT_CONFIRM; }
}
