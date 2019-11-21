package tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

import cartago.*;
import orchestra.*;

public class Sheet extends Artifact {

	Synth synth;
	Midi midi = null;
	ArrayList<Note> notes = null;
	LinkedHashMap<Integer, LinkedHashMap<Long, Note> > individualNotes = null;
	long startTime = 0, endTime = 0, elapsedTime = 0;
	boolean opened = false;
	long cTick = 0;										//current tick
	long maxTick = Long.MAX_VALUE;
	int neededIndex;

	void init() {
		synth = new Synth();
		opened = false;

		defineObsProperty("sheetName", "");
		defineObsProperty("hasTicks", false);
	}

	@Override
	protected void dispose() {
		if(opened)
			synth.close();
		super.dispose();
	}


	/**
	 * Agent request for a Midi object for the Music
	 */
	@OPERATION
	void readSheet(String fileName, OpFeedbackParam success) {
		midi = synth.parse(fileName);
		if(midi == null)
		{
			success.set(false);
			return;
		}

		ObsProperty sheetName = getObsProperty("sheetName");
		sheetName.updateValue(midi.name);

		ObsProperty hasTicks = getObsProperty("hasTicks");
		hasTicks.updateValue(midi.maxTick > 0);

		cTick = 0;
		
		individualNotes = new LinkedHashMap<Integer, LinkedHashMap<Long, Note> >();
		for(long i = 0; i < midi.maxTick; i++)
		{
			ArrayList<Note> notes = midi.getNotes(i);
			for(Note note : notes)
			{
				int index = synth.getInstrument(note.instrument);
				if(individualNotes.containsKey(index) == false)
					individualNotes.put(index, new LinkedHashMap<Long, Note>());
				individualNotes.get(index).put(i, note);
				//System.out.println("["+note.instrument+"]["+i+"] = "+note.toString());
			}
		}

		neededIndex = 0;

		success.set(true);
	}

	@OPERATION
	void getNeededInstrument(OpFeedbackParam name, OpFeedbackParam isValid)
	{
		name.set("");
		isValid.set(false);

		Set<Entry<Integer, LinkedHashMap<Long, Note>>> entries = individualNotes.entrySet();
		int i = 0;
		for(Entry<Integer, LinkedHashMap<Long, Note>> entry : entries)
		{
			if(i == neededIndex)
			{
				String instrumentName = synth.getInstrument(i);
				name.set(instrumentName);
				isValid.set(true);
				neededIndex = i + 1;
				break;
			}
			i++;
		}
	}	

	@OPERATION
	void nextTick()
	{
		if(midi == null)
			return;
		
		notes = midi.getNotes(cTick);

		cTick++;

		ObsProperty hasTicks = getObsProperty("hasTicks");
		hasTicks.updateValue(cTick < midi.maxTick && cTick < maxTick);
	}

	@OPERATION
	void setMaxTick(long maxTick)
	{
		this.maxTick = maxTick;
	}

	@OPERATION
	void playTick()
	{
		if(midi == null || notes == null)
		{
			return;
		}
		if(!opened)
		{
			synth.open();
			opened = true;
		}
		synth.play(notes);
		startTime = System.nanoTime();
	}

	@OPERATION
	void waitTick()
	{
		if(midi == null || notes == null)
		{
			return;
		}
		endTime = System.nanoTime();
		elapsedTime = (endTime - startTime) / 1000;
		Synth.busyWaitMicros(midi.tickDuration - elapsedTime);
	}

	@OPERATION
	void firstTick(String instrumentName, OpFeedbackParam tick)
	{
		int index = synth.getInstrument(instrumentName);
		if(individualNotes.containsKey(index) == false)
		{
			tick.set(Long.MAX_VALUE);
			return;
		}

		Set<Long> keys = individualNotes.get(index).keySet();
		for(Long key : keys)
		{
			tick.set(key);
			break;
		}
	}

	@OPERATION
	void proccessTick(String instrumentName, Long tick, OpFeedbackParam nextTick)
	{
		int index = synth.getInstrument(instrumentName);
		if(!individualNotes.containsKey(index))
		{
			nextTick.set(Long.MAX_VALUE);
			return;
		}

		Note note = individualNotes.get(index).get(tick);
		if(note != null)
		{
			if(!opened)
			{
				synth.open();
				opened = true;
			}
			synth.play(note);
		}

		Set<Entry<Long,Note>> entries = individualNotes.get(index).entrySet();
		nextTick.set(Long.MAX_VALUE);
		for(Entry<Long,Note> entry : entries)
		{
			if(entry.getKey() > tick)
			{
				nextTick.set(entry.getKey());
				break;
			}
			
		}
	}

	@OPERATION
	void startTick(OpFeedbackParam tick)
	{
		tick.set(cTick);
		startTime = System.nanoTime();
	}

	@OPERATION
	void tickNotes( OpFeedbackParam length, OpFeedbackParam types, OpFeedbackParam instruments, OpFeedbackParam notes, OpFeedbackParam volume) {
		if(midi == null || this.notes == null)
		{
			String[] def = new String[0];
			length.set(0);
			types.set(def);
			instruments.set(def);
			notes.set(def);
			volume.set(def);
			startTime = System.nanoTime();
			return;
		}

		String[] typesData = new String[this.notes.size()];
		String[] instrumentsData = new String[this.notes.size()];
		String[] notesData = new String[this.notes.size()];
		int[] volumeData = new int[this.notes.size()];

		int i = 0;
		for(Note note : this.notes)
		{
			//typesData[i] = note.type == NoteType.ON ? "ON" : "OFF";
			typesData[i] = note.type.name();
			instrumentsData[i] = note.instrument;
			notesData[i] = note.printable();
			volumeData[i] = note.velocity;
			i++;
		}

		length.set(this.notes.size());
		types.set(typesData);
		instruments.set(instrumentsData);
		notes.set(notesData);
		volume.set(volumeData);
		startTime = System.nanoTime();
	}
}

