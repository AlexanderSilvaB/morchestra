package orchestra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Track
{
    public int number;                          //Number of the track
    public Map<Long, Note> notes;               // <tick duration, Note>
    
    public Track(int number)
    {
        this.number = number;
        notes = new HashMap<Long, Note>();
    }

    /**
     * Gets the maximum tick from the track
     */
    public long getMaxTick()
    {
        long maxTick = 0;
        Set<Long> ticks = notes.keySet();
        for(Long tick : ticks)
        {
            if(tick > maxTick)
                maxTick = tick;
        }
        return maxTick;
    }

    //returns the note related to the tick
    public Note getNote(long tick)
    {
        return notes.get(tick);
    }

    @Override
    public String toString()
    {
        String str = "Track " + number + ": size = " + notes.size();
        Set<Entry<Long, Note>> set = notes.entrySet();
        Iterator it = set.iterator();
        
        while(it.hasNext())
        {
            Entry<Long, Note> entry = (Entry)it.next();
            str += "\n@" + entry.getKey() + ": " + entry.getValue().toString();
        }
        return str;
    }
}