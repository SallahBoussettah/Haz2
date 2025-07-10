import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, Clip> soundCache = new HashMap<>();
    private boolean soundEnabled = true;
    
    // Sound keys
    public static final String SOUND_SELECT_CARD = "select_carte_audio.wav";
    public static final String SOUND_PLAY_CARD = "play_carte_audio.wav";
    public static final String SOUND_DRAW_CARD = "init_carte_audio.wav";
    public static final String SOUND_HAZZ = "hazz.wav";
    public static final String SOUND_ERROR = "error_audio.wav";
    public static final String SOUND_TYPE_CHANGE = "type_audio.wav";
    public static final String SOUND_WIN = "win_audio.wav";
    public static final String SOUND_LOSE = "loose_audio.wav";
    public static final String SOUND_START = "start_audio.wav";
    
    private SoundManager() {
        // Private constructor for singleton
        preloadSounds();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * Preload all game sounds into memory
     */
    private void preloadSounds() {
        String[] soundFiles = {
            SOUND_SELECT_CARD,
            SOUND_PLAY_CARD,
            SOUND_DRAW_CARD,
            SOUND_HAZZ,
            SOUND_ERROR,
            SOUND_TYPE_CHANGE,
            SOUND_WIN,
            SOUND_LOSE,
            SOUND_START
        };
        
        for (String soundFile : soundFiles) {
            try {
                // Use the exact path to the sounds folder
                File file = new File("Hez/sounds/" + soundFile);
                if (file.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    soundCache.put(soundFile, clip);
                    System.out.println("Loaded sound: " + file.getAbsolutePath());
                } else {
                    System.err.println("Sound file not found: " + file.getAbsolutePath());
                    
                    // Try with absolute path as fallback
                    String currentDir = System.getProperty("user.dir");
                    File absoluteFile = new File(currentDir + "/Hez/sounds/" + soundFile);
                    if (absoluteFile.exists()) {
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(absoluteFile);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioStream);
                        soundCache.put(soundFile, clip);
                        System.out.println("Loaded sound with absolute path: " + absoluteFile.getAbsolutePath());
                    } else {
                        System.err.println("Sound file not found with absolute path: " + absoluteFile.getAbsolutePath());
                    }
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error loading sound: " + soundFile);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Play a sound by its key
     * @param soundKey The key of the sound to play
     */
    public void playSound(String soundKey) {
        if (!soundEnabled) return;
        
        Clip clip = soundCache.get(soundKey);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("Sound not found in cache: " + soundKey);
        }
    }
    
    /**
     * Enable or disable all sounds
     * @param enabled True to enable sounds, false to disable
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        
        // Stop all currently playing sounds if disabled
        if (!enabled) {
            for (Clip clip : soundCache.values()) {
                if (clip.isRunning()) {
                    clip.stop();
                }
            }
        }
    }
    
    /**
     * Check if sound is currently enabled
     * @return True if sound is enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
} 