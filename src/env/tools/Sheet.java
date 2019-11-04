package tools;

import java.util.ArrayList;

import cartago.*;
import orchestra.*;

public class Sheet extends Artifact {

	Synth synth;
	Midi midi = null;
	ArrayList<Note> notes = null;
	long startTime = 0, endTime = 0, elapsedTime = 0;

	void init() {
		synth = new Synth();
		synth.open();

		defineObsProperty("sheetName", "");
		defineObsProperty("tickDuration", 0);
		defineObsProperty("maxTicks", 0);
		defineObsProperty("tracks", 0);
		defineObsProperty("currentTick", 0);
		defineObsProperty("hasTicks", false);
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

		ObsProperty tickDuration = getObsProperty("tickDuration");
		tickDuration.updateValue(midi.tickDuration);

		ObsProperty maxTicks = getObsProperty("maxTicks");
		maxTicks.updateValue(midi.maxTick);

		ObsProperty tracks = getObsProperty("tracks");
		tracks.updateValue(midi.tracks.size());

		ObsProperty currentTick = getObsProperty("currentTick");
		currentTick.updateValue(0);

		ObsProperty hasTicks = getObsProperty("hasTicks");
		hasTicks.updateValue(midi.maxTick > 0);

		success.set(true);
	}

	@OPERATION
	void nextTick()
	{
		if(midi == null)
			return;

		ObsProperty currentTick = getObsProperty("currentTick");
		long cTick = currentTick.longValue();
		
		notes = midi.getNotes(cTick);

		cTick++;
		currentTick.updateValue(cTick);

		ObsProperty hasTicks = getObsProperty("hasTicks");
		hasTicks.updateValue(cTick < midi.maxTick);
	}

	@OPERATION
	void playTick()
	{
		if(midi == null || notes == null)
		{
			return;
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
	void tickNotes( OpFeedbackParam types, OpFeedbackParam channels, OpFeedbackParam notes, OpFeedbackParam volume) {
		if(midi == null || this.notes == null)
		{
			String[] def = new String[0];
			types.set(def);
			channels.set(def);
			notes.set(def);
			volume.set(def);
			return;
		}

		String[] typesData = new String[this.notes.size()];
		int[] channelsData = new int[this.notes.size()];
		String[] notesData = new String[this.notes.size()];
		int[] volumeData = new int[this.notes.size()];

		int i = 0;
		for(Note note : this.notes)
		{
			//typesData[i] = note.type == NoteType.ON ? "ON" : "OFF";
			typesData[i] = note.type.name();
			channelsData[i] = note.channel;
			notesData[i] = note.printable();
			volumeData[i] = note.velocity;
			i++;
		}

		types.set(typesData);
		channels.set(channelsData);
		notes.set(notesData);
		volume.set(volumeData);
	}
}

