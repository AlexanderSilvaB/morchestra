package orchestra;

public enum Instruments{
    Piano(0), Percussion(9);
    
    private int value;
    Instruments(int value){
        this.value = value;
    }
    
    public int getValue(){
        return value;
    }
}