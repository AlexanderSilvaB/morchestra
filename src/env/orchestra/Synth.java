package orchestra;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Synth {
	public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
	private static List<String> NOTE_NAMES = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
	private MidiChannel[] channels;
	private int volume = 80; // between 0 et 127
	private Synthesizer synth;

	public void playSample()
	{
		play(Instruments.Piano, "6D",  1000);
		rest(500);
		
		play(Instruments.Piano, "6D",  300);
		play(Instruments.Piano, "6C#", 300);
		play(Instruments.Piano, "6D",  1000);
		rest(500);
		
		play(Instruments.Piano, "6D",  300);
		play(Instruments.Piano, "6C#", 300);
		play(Instruments.Piano, "6D",  1000);
		play(Instruments.Piano, "6E",  300);
		play(Instruments.Piano, "6E",  600);
		play(Instruments.Piano, "6G",  300);
		play(Instruments.Piano, "6G",  600);
		rest(500);
	}

	public Midi parse(String midiFileName)
	{
		try
		{
			Path path = Paths.get(midiFileName);
			
			Midi m = new Midi(path.getFileName().toString());
			Sequence sequence = MidiSystem.getSequence(new File(midiFileName));

			m.tickDuration = (sequence.getMicrosecondLength() / sequence.getTickLength());

			int trackNumber = 0;
			for (Track track :  sequence.getTracks()) {
				trackNumber++;
				orchestra.Track t = new orchestra.Track(trackNumber);

				for (int i=0; i < track.size(); i++) { 
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();
					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						if (sm.getCommand() == NOTE_ON) {
							int key = sm.getData1();
							int octave = (key / 12)-1;
							int note = key % 12;
							String noteName = NOTE_NAMES.get(note);
							int velocity = sm.getData2();

							Note n = new Note(NoteType.ON, noteName, octave);
							n.velocity = velocity;
							n.key = key;
							n.channel = sm.getChannel();
							t.notes.put(event.getTick(), n);
							
						} else if (sm.getCommand() == NOTE_OFF) {
							int key = sm.getData1();
							int octave = (key / 12)-1;
							int note = key % 12;
							String noteName = NOTE_NAMES.get(note);
							int velocity = sm.getData2();

							Note n = new Note(NoteType.OFF, noteName, octave);
							n.velocity = velocity;
							n.key = key;
							n.channel = sm.getChannel();
							t.notes.put(event.getTick(), n);
						}
					}
				}

				m.tracks.add(t);
			}

			m.normalize();
			return m;	
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return null;
	}

	public void open()
	{
		try {
			// * Open a synthesizer
			synth = MidiSystem.getSynthesizer();
			synth.open();
			channels = synth.getChannels();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void close()
	{
		try {
			// * finish up
			synth.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Turn on a note
	 */
	public void on(int instrument, String note)
	{
		channels[instrument].noteOn(id(note), volume );
	}

	/**
	 * Turn off a note
	 */
	public void off(int instrument, String note)
	{
		channels[instrument].noteOff(id(note), volume );
	}

	/**
	 * Turn on a note
	 */
	public void on(int instrument, String note, int volume)
	{
		channels[instrument].noteOn(id(note), volume );
	}

	/**
	 * Turn off a note
	 */
	public void off(int instrument, String note, int volume)
	{
		channels[instrument].noteOff(id(note), volume );
	}

	/**
	 * Plays the given note for the given duration
	 */
	public void play(int instrument, String note, int duration)
	{
		try
		{
			// * start playing a note
			channels[instrument].noteOn(id(note), volume );
			// * wait
			Thread.sleep( duration );
			// * stop playing a note
			channels[instrument].noteOff(id(note));
		}
		catch(InterruptedException ex)
		{
			System.out.println(ex.getMessage());
		}
	}

	public void play(Instruments instrument, String note, int duration)
	{
		play(instrument.getValue(), note, duration);
	}

	public void play(Midi midi)
	{
		if(midi == null)
			return;
		
		long tick = 0;
		ArrayList<Note> notes = midi.getNotes(tick);
		int p;
		long startTime, endTime, elapsedTime;
		while(notes != null)
		{
			startTime = System.nanoTime();
			p = (int)((tick / (double)midi.maxTick) * 100);
			progress(p, tick == 0, false);
			play(notes);
			notes = midi.getNotes(tick);
			endTime = System.nanoTime();
			elapsedTime = (endTime - startTime) / 1000;
			busyWaitMicros(midi.tickDuration - elapsedTime);
			tick++;
		}
		progress(100, false, true);
	}

	public void play(ArrayList<Note> notes)
	{
		for(int i = 0; i < notes.size(); i++)
		{
			play(notes.get(i));
		}
	}

	public void play(Note note)
	{
		if(note.type == NoteType.ON)
		{
			on(note.channel, note.printable(), note.velocity);
		}
		else
		{
			off(note.channel, note.printable(), note.velocity);
		}
	}
	
	/**
	 * Plays nothing for the given duration
	 */
	private void rest(int duration)
	{
		// try
		// {
		// 	Thread.sleep(duration);
		// }
		// catch(InterruptedException ex)
		// {
		// 	System.out.println(ex.getMessage());
		// }
		busyWaitMicros(duration * 1000);
	}

	public static void busyWaitMicros(long micros){
        long waitUntil = System.nanoTime() + (micros * 1_000);
        while(waitUntil > System.nanoTime()){
            ;
        }
    }
	
	private void progress(int p, boolean first, boolean last)
	{
		String str = "";
		for(int i = 0; i < 100; i++)
		{
			if(i < p)
				str += "=";
			else if(i == p && i != 100)
				str += ">";
			else
				str += " ";
		}
		if(first)
		{
			System.out.print("\n");
		}
		System.out.print("Playing [" + str + "] " + p + "%");
		if(last)
			System.out.print("\n");
		else
		System.out.print("\r");
	}
	
	/**
	 * Returns the MIDI id for a given note: eg. 4C -> 60
	 * @return
	 */
	private int id(String note)
	{
		int octave = Integer.parseInt(note.substring(0, 1));
		return NOTE_NAMES.indexOf(note.substring(1)) + 12 * octave + 12;	
	}
}