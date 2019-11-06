package tools;

import java.util.ArrayList;

import cartago.*;
import orchestra.*;

public class Sheet extends Artifact {

	Synth synth;
	Midi midi = null;
	ArrayList<Note> notes = null;
	long startTime = 0, endTime = 0, elapsedTime = 0;
	boolean opened = false;
	long cTick = 0;
	long maxTick = Long.MAX_VALUE;

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

		success.set(true);
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

