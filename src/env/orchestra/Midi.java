package orchestra;

import java.util.ArrayList;

public class Midi
{
    public String name;
    public ArrayList<Track> tracks;
    public long maxTick;
    public long tickDuration;

    public Midi(String name)
    {
        this.name = name;
        tracks = new ArrayList<Track>();
        maxTick = 0;
    }

    public float getSecondsDuration()
    {
        return (tickDuration * maxTick) / (1000000.0f);
    }

    public void normalize()
    {
        maxTick = 0;
        for(int i = 0; i < tracks.size(); i++)
        {
            long mt = tracks.get(i).getMaxTick();
            if(mt > maxTick)
                maxTick = mt;
        }

        if(tickDuration < 1)
            tickDuration = 1;
    }

    public ArrayList<Note> getNotes(long tick)
    {
        if(tick > maxTick)
            return null;

        ArrayList<Note> notes = new ArrayList<Note>();
        for(int i = 0; i < tracks.size(); i++)
        {
            Note note = tracks.get(i).getNote(tick);
            if(note != null)
                notes.add(note);
        }
        return notes;
    }

    @Override
    public String toString()
    {
        String str = toBasicString();
        for(int i = 0; i < tracks.size(); i++)
        {
            str += "\n" + tracks.get(i).toString();
        }
        return str;
    }

    public String toBasicString()
    {
        String str = "Name: " + name;
        str += "\nTracks: " + tracks.size();
        str += "\nTick duration: " + tickDuration + "us";
        str += "\nTicks: " + maxTick;
        str += "\nDuration: " + getSecondsDuration() + "s";
        return str;
    }
}