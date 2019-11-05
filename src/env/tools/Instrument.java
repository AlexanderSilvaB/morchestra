package tools;

import cartago.*;
import orchestra.*;

public class Instrument extends Artifact {

    Synth synth;

	void init() {
		synth = new Synth();
        synth.open();
	}

	@Override
	protected void dispose() {
		synth.close();
		super.dispose();
    }
    
    @OPERATION
    void play(String type, String instrumentName, String note, int volume)
    {
        int instrument = synth.getInstrument(instrumentName);

        if(type.equals(NoteType.ON.name()))
            synth.on(instrument, note, volume);
        else
            synth.off(instrument, note, volume);
    }
}