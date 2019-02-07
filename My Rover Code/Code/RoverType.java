package rover;

//RoverType Object, used to tell the Master Rover what type of Rover you are

public class RoverType {
	//Type of Rover
	private char type;
	//Rover Number: Assigned by Master Rover
	private int n = 0;
	//Rover ClientKey
	private String key;
	
	 public RoverType(char type, String key) {
	        this.type = type;
	        this.key = key;
	 }
	 
	 public char getType(){
		 return type;
	 }
	 
	 public String getKey(){
		 return key;
	 }
	 
	 public void setNumber(int i){
		 n = i;
	 }
	 
	 public int getNumber(){
		 return n;
	 }
}