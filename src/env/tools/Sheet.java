package tools;

import cartago.*;
import orchestra.*;

public class Sheet extends Artifact {

	Midi midi = null;

	void init() {
		defineObsProperty("tickDuration", 0);
		defineObsProperty("maxTicks", 0);
		defineObsProperty("songName", "");
		defineObsProperty("tracks", 0);
	}

	@OPERATION
	void readSheet(String fileName, OpFeedbackParam success) {

		Synth synth = new Synth();
		midi = synth.parse(fileName);
		if(midi == null)
		{
			success.set(false);
			return;
		}

		ObsProperty songName = getObsProperty("songName");
		songName.updateValue(midi.name);

		ObsProperty tickDuration = getObsProperty("tickDuration");
		tickDuration.updateValue(midi.tickDuration);

		ObsProperty maxTicks = getObsProperty("maxTicks");
		maxTicks.updateValue(midi.maxTick);

		ObsProperty tracks = getObsProperty("tracks");
		tracks.updateValue(midi.tracks.size());

		success.set(true);
	}
}

