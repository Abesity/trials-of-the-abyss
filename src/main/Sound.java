package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException; // Import specific exceptions
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException; // Import IOException

public class Sound {

    Clip clip; // The audio clip player
    URL soundURL[] = new URL[30]; // Array to store sound file URLs

    public Sound() {
        // Load sound URLs - wrap in try-catch for better error handling at init
        try {
            // Original chiptune written for the 2026 restoration (tools/chiptune.py)
            soundURL[0] = getClass().getResource("/sound/overworld.wav");
            soundURL[1] = getClass().getResource("/sound/coin.wav");
            soundURL[2] = getClass().getResource("/sound/heal.wav");
            soundURL[3] = getClass().getResource("/sound/unlock.wav");
            soundURL[4] = getClass().getResource("/sound/fanfare.wav");
            soundURL[5] = getClass().getResource("/sound/hit.wav");
            soundURL[6] = getClass().getResource("/sound/teleport.wav");
            soundURL[7] = getClass().getResource("/sound/battle.wav");
            // Add more sounds here...

            // Check if any essential sounds failed to load
            if (soundURL[0] == null) {
                System.err.println("Error: Background music '/sound/BlueBoyAdventure.wav' not found!");
            }
            // Add checks for other critical sounds if needed

        } catch (Exception e) {
            System.err.println("Error loading sound URLs in Sound constructor:");
            e.printStackTrace();
        }
    }

    // Loads the specified sound file into the clip
    public void setFile(int i) {
        // Check if index is valid and URL was loaded
        if (i < 0 || i >= soundURL.length || soundURL[i] == null) {
            System.err.println("Error: Sound URL for index " + i + " is invalid or null.");
            clip = null; // Ensure clip is null if URL is bad
            return;
        }

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            // Close previous clip if open, to prevent resource leaks
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            clip = AudioSystem.getClip(); // Get a new clip resource
            clip.open(ais); // Open the audio stream with the clip

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error: Unsupported audio file format for sound index " + i + ": " + soundURL[i]);
            e.printStackTrace();
            clip = null; // Set clip to null on error
        } catch (IOException e) {
            System.err.println("Error: I/O exception reading sound index " + i + ": " + soundURL[i]);
            e.printStackTrace();
            clip = null;
        } catch (LineUnavailableException e) {
            System.err.println("Error: Audio line unavailable for sound index " + i + ": " + soundURL[i]);
            e.printStackTrace();
            clip = null;
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("Unexpected error setting sound file for index " + i + ": " + soundURL[i]);
            e.printStackTrace();
            clip = null;
        }
    }

    // Plays the clip from the beginning
    public void play() {
        // ****** CORRECTION IS HERE ******
        // Check if clip is not null before using it
        if (clip != null) {
            // Ensure clip is stopped and reset to beginning before playing
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        } else {
            // System.err.println("Warning: Tried to play a null clip.");
        }
        // ****** END OF CORRECTION AREA ******
    }

    // Plays the clip continuously in a loop
    public void loop() {
        // ****** CORRECTION IS HERE ******
        // Check if clip is not null
        if (clip != null) {
            // Ensure clip is stopped and reset to beginning before looping
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0); // Rewind
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            // System.err.println("Warning: Tried to loop a null clip.");
        }
        // ****** END OF CORRECTION AREA ******
    }

    // Stops the clip if it's playing
    public void stop() {
        // ****** CORRECTION IS HERE ******
        // Check if clip is not null AND is running before stopping
        if (clip != null && clip.isRunning()) {
            // Check if clip is open - avoid stopping a closed clip which might cause issues
            if (clip.isOpen()) {
                clip.stop();
            }
        }
        // No warning needed here, often called defensively
        // ****** END OF CORRECTION AREA ******
    }

    // Optional: Method to close the clip and release resources explicitly
    public void close() {
        if (clip != null && clip.isOpen()) {
            clip.stop(); // Stop first
            clip.close(); // Then close
        }
        clip = null; // Release reference
    }
}