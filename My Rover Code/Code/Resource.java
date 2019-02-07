package rover;

//Resource Object used to store each resources infomation
public class Resource {
	
	//Id of the resource in the resource List
	private int id = -1;
	
	//Global Postions
	private double xLocation;
	private double yLocation;
	
	//Resource Type
	private int type;
	
	//state of the stack of resources (empty/not empty)
	private boolean isEmpty;
	
	//Rover Assigned to this resource
	private String assignedRover; 

    public Resource(int id, double x, double y, int type) {
        this.id = id;
        this.xLocation = x;
        this.yLocation = y;
        this.type = type;
        isEmpty = false;
        assignedRover ="";
    }
    
    public Resource(double x, double y , int type){
    	this.xLocation = x;
    	this.yLocation = y;
    	this.type = type;
    	isEmpty = false;
    	assignedRover ="";
    }
	
	public int getId(){
		return id;
	}
	public double getXLocation(){
		return xLocation;
	}
	public double getYLocation(){
		return yLocation;
	}
	public int getType(){
		return type;
	}
	public boolean getIsEmpty(){
		return isEmpty;
	}
	public void setIsEmpty(){
		isEmpty = true;
	}
	public void setId(int i){
		id = i;
	}
	public String getAssignedRover(){
		return assignedRover;
	}
	public void setAssignedRover(String s){
		assignedRover = s;
	}
}