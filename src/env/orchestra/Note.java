package orchestra;

public class Note
{
    public NoteType type;
    public String name;
    public int octave;
    public int velocity;
    public int key;
    public int channel;
    public String instrument;

    public Note(NoteType type, String name, int octave)
    {
        this.type = type;
        this.name = name;
        this.octave = octave;
        this.channel = 0;
        this.velocity = 0;
        this.key = 0;
        this.instrument = "";
    }   

    public String printable()
    {
        return octave + name;
    }

    @Override
    public String toString()
    {
        String str = "Channel " + channel + ", " + (type == NoteType.ON ? "ON, " : "OFF, ") + octave + name + ", " + velocity;
        return str;
    }
}