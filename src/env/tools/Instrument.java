package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;

import cartago.*;
import orchestra.*;

public class Instrument extends Artifact {

    Synth synth;
    ArrayList<String> names;
    Random rand;
    Semaphore semaphore;


	void init() {
        names = new ArrayList<String>();
        rand = new Random();
        synth = new Synth();
        semaphore = new Semaphore(1);

        synth.open();
	}

	@Override
	protected void dispose() {
		synth.close();
		super.dispose();
    }

    @OPERATION
	void instrumentsNames(OpFeedbackParam length, OpFeedbackParam names)
	{
        String[] inames = synth.getInstruments();
        names.set(inames);
        length.set(inames.length);
    }
    
    @OPERATION
    void pickAnInstrument(OpFeedbackParam name)
    {
        try
        {
            semaphore.acquire();

            if(names.size() == 0)
            {
                names.addAll(Arrays.asList(synth.getInstruments()));
            }

            int i = rand.nextInt(names.size());
            name.set(names.get(i));
            names.remove(i);
            
            semaphore.release();
        } 
        catch (InterruptedException e) 
        {
            name.set("Piano 1");
        }
    }
    
    @OPERATION
    void play(String type, String instrumentName, String note, int volume)
    {
        int instrument = synth.getInstrument(instrumentName);

        if(type.equals(NoteType.ON.name()))
            synth.on(instrument, note, volume);             //Play the instrument note
        else
            synth.off(instrument, note, volume);            //Stop playing the note
    }
}