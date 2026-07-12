package controller;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;


public class AudioManager {

    private Clip backgroundClip;
    private FloatControl volumeControl;
    private float currentVolume = 0.5f;
    private boolean loaded = false;


    public void loadAndPlayBackgroundMusic(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Background music not found: " + resourcePath
                        + " — game will continue without audio.");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);

            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(currentVolume);
            }

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            loaded = true;

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + resourcePath + " — " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable (no audio device?): " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Could not read audio file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected audio error: " + e.getMessage());
        }
    }


    public void setVolume(float volume) {
        this.currentVolume = Math.max(0f, Math.min(1f, volume));
        if (volumeControl == null) return;


        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();

        if (currentVolume <= 0.0001f) {
            volumeControl.setValue(min);
            return;
        }

        float db = (float) (Math.log10(currentVolume) * 20.0);
        db = Math.max(min, Math.min(max, db));
        volumeControl.setValue(db);
    }

    public float getVolume() { return currentVolume; }


    public void pause() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
        }
    }

    public void resume() {
        if (backgroundClip != null && !backgroundClip.isRunning()) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    public boolean isLoaded() { return loaded; }
}
