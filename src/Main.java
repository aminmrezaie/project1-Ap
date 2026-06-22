import controller.GameController;

import javax.swing.*;

public class Main {

    private static final int WINDOW_WIDTH  = 1280;
    private static final int WINDOW_HEIGHT = 800;

    public static void main(String[] args) {

        configureLookAndFeel();

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Thread.setDefaultUncaughtExceptionHandler(Main::handleUncaughtException);

        SwingUtilities.invokeLater(Main::launchGame);
    }


    private static void launchGame() {
        try {
            GameController controller = new GameController(WINDOW_WIDTH, WINDOW_HEIGHT);
            controller.start();
        } catch (Exception e) {
            handleUncaughtException(Thread.currentThread(), e);
        }
    }


    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system Look & Feel: " + e.getMessage());
        }
    }


    private static void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace();

        SwingUtilities.invokeLater(() -> {
            String message = "An unexpected error occurred:\n"
                    + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "\n\nSee console for full stack trace.";

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Civilization — Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}
