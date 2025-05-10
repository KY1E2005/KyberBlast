package game2D;

import java.io.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;


public class Sound extends Thread {

	String filename;	// The name of the file to play
	boolean finished;	// A flag showing that the thread has finished
	private static Sequencer sequencer;
	private static String currentTrack = "";

	
	public Sound(String fname) {
		filename = fname;
		finished = false;
	}

	/**
	 * run will play the actual sound but you should not call it directly.
	 * You need to call the 'start' method of your sound object (inherited
	 * from Thread, you do not need to declare your own). 'run' will
	 * eventually be called by 'start' when it has been scheduled by
	 * the process scheduler.
	 */
	public static void run(String filepath) {
	    new Thread(() -> {
	        try {
	            File file = new File(filepath);
	            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
	            AudioFormat format = stream.getFormat();
	            DataLine.Info info = new DataLine.Info(Clip.class, format);
	            Clip clip = (Clip) AudioSystem.getLine(info);
	            clip.open(stream);
	            clip.start();
	        } catch (Exception e) {
	            System.err.println("Error playing sound effect: " + e.getMessage());
	        }
	    }).start();
	}
	
	/**
	 * Plays a MIDI music file for the main menu with fade-in
	 */
	public static void playMainMenuMusic() {
	    playExclusiveMidi("sounds/Castlevania.mid", true, 0);
	}
	
	/**
	 * Plays a MIDI music file for the gameplay state with fade-in and seek.
	 */
	public static void playGameMusic() {
		playExclusiveMidi("sounds/BattleTheme(SoulHackers).mid", true, 3000); // start at 2 seconds
	}
	
	/**
	 * Plays a MIDI file exclusively, stopping any currently playing MIDI track.	
	 * If the given file is already playing, this method does nothing.
	 * Otherwise, it stops any current track, loads the specified file,
	 * seeks to the specified start time, and begins playback.
	 * @param filePath the path to the MIDI file to play.
	 * @param loop whether the MIDI file should loop continuously.
	 * @param startMs the position to start playback from, in milliseconds.
	 */
	public static void playExclusiveMidi(String filePath, boolean loop, int startMs) {
	    try {
	        if (currentTrack.equals(filePath)) return; // Already playing this track

	        stopMidi(); // Stop any current track
	        currentTrack = filePath;

	        sequencer = MidiSystem.getSequencer();
	        sequencer.open();
	        Sequence sequence = MidiSystem.getSequence(new File(filePath));
	        sequencer.setSequence(sequence);

	        if (startMs > 0) {
	            sequencer.setMicrosecondPosition(startMs * 1000);
	        }

	        if (loop) {
	            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
	        }

	        sequencer.start();

	    } catch (Exception e) {
	        System.err.println("MIDI Error: " + e.getMessage());
	    }
	}
	
	/**
	 * Stops any currently playing MIDI music.
	 */
	public static void stopMidi() {
	    if (sequencer != null && sequencer.isRunning()) {
	        sequencer.stop();
	        sequencer.close();
	    }
	}
	
	/**
	 * Preloads the system's MIDI synthesizer to reduce latency when playing MIDI files later.
	 * This method simply opens and closes the synthesizer to ensure that MIDI instruments are loaded
	 * into memory. This can help prevent playback delays the first time a MIDI is played.
	 */
	public static void preloadSynth() {
	    try {
	        Synthesizer synth = MidiSystem.getSynthesizer();
	        if (!synth.isOpen()) synth.open();
	        synth.close(); // Just open/close to trigger loading
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Sets the volume of the MIDI music.
	 * @param volume Value between 0.0f (mute) to 1.0f (full volume).
	 */
	public static void setVolume(float volume) {
	    try {
	        if (sequencer != null && sequencer.isOpen()) {
	            Synthesizer synth = MidiSystem.getSynthesizer();
	            if (!synth.isOpen()) synth.open();

	            MidiChannel[] channels = synth.getChannels();
	            for (MidiChannel channel : channels) {
	                if (channel != null) {
	                    channel.controlChange(7, (int)(volume * 127));  // MIDI volume controller 7
	                }
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Failed to set MIDI volume: " + e.getMessage());
	    }
	}
}
