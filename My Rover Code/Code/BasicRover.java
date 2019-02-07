package rover;

import java.util.Iterator;
import java.util.Random;
import com.google.gson.Gson;
import java.util.ArrayList;

// Basic Rover , Used as a parent for all other rovers

public class BasicRover extends Rover {
	
	//Rover Stats
	protected int maxSpeed = 1;
	protected int maxScan = 3;
	protected int maxCarry = 5;
	protected int resourceType = 1;
	protected int roverNumber = -1;
	protected int nScanners = 0;
	protected int targetId = -1;
	
	//Rover Location Info
	protected double xLocation = 0.0;
	protected double yLocation = 0.0;
	protected double xLocationTemp = 0.0;
	protected double yLocationTemp = 0.0;
	protected double resourceX = 0.0;
	protected double resourceY = 0.0;
	
	
	//Map Info
	public int mapWidth;
	public int mapHeight;
	
	//List to Store the maps resources
	protected ArrayList<Resource> resourceList = new ArrayList<Resource>();
	//Json object used to convert Objects to Strings (visa versa)
	Gson g = new Gson();

	
	public BasicRover() {
        super();

		//use your username for team name
		setTeam("lk383");
		
		try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			//Fourth attribute is the collector type
			setAttributes(maxSpeed, maxScan, maxCarry, resourceType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public BasicRover(int speed, int scan, int carry , int resource) {
        super();
        
		//use your username for team name
		setTeam("lk383");
		
		try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			//Fourth attribute is the collector type
			setAttributes(speed, scan, carry, resource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		maxSpeed = speed;
        maxScan = scan;
        maxCarry = carry;
        resourceType = resource;
		
	}

	@Override
	void begin() {
		//called when the world is started
        getLog().info("BEGIN!");
		
		try {
			//Set Inital Values
			mapWidth = getWorldWidth();
			mapHeight = getWorldHeight();
			xLocationTemp = 0.0;
			yLocationTemp = 0.0;
			resourceX = 0.0;
			resourceY = 0.0;
			resourceList.removeAll(resourceList);
			xLocation = 0.0;
			yLocation = 0.0;
			//Start With Scan
			scan(maxScan);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
	}
	
	//Finds Best route to Base, then move to it
	void moveToBase(int speed){
		
		double dx = 0.0;
		double dy = 0.0;
		
		//Calculate Shortest Distance (make Function)
		if(xLocation>mapWidth/2){
			dx = mapWidth-xLocation;
		}
		else{
			dx = -xLocation;
		}
		
		if(yLocation>mapHeight/2){
			dy = mapHeight-yLocation;
		}
		else{
			dy = -yLocation;
		}
		
		xLocationTemp = calcWorldLocation(xLocation + dx,mapWidth);
		yLocationTemp = calcWorldLocation(yLocation + dy,mapHeight);
		
		try{
			if(dx == 0 & dy==0){
				scan(0.0000001);
			}
			else{
				move(dx,dy,speed);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//Calculates the World Location 
	double calcWorldLocation(double location , int range){
		//double location = 0;
		double rem = location % range;
		if(rem < 0) {
			location = range + rem;
		}
		else{
			location = rem;
		}
		
		return (double) location;
		
	}
	
	//Returns the Shortest Route to a Location 
	double shortestRoute(double offset, int size){
		
		if(Math.abs(offset)<=Math.abs(offset-size)){
			return offset;
		}
		else{
			return offset-size;
		}
		
	}
	
	//Compares to double to see if they are close
	boolean isAlmostEqual(double a, double b, double eps){
	    return Math.abs(a-b)<eps;
	}
	
	//Method to check if a resource is already stored based on location
	int isNewResource(double resourceLocationX, double resourceLocationY){
		for(int i=0; i < resourceList.size(); i++){
			Resource r = resourceList.get(i);
			if(isAlmostEqual(r.getXLocation(),resourceLocationX,0.1) && isAlmostEqual(r.getYLocation(),resourceLocationY,0.1)){
				return r.getId();
			}
		}
		
		return -1;
		
	}
	
	//Finds the Resources Index in the List given its id
	int getResourceIndex(int id){
		for(int i=0;i<resourceList.size();i++){
			Resource r = resourceList.get(i);
			if(r.getId() == id){
				return i;
			}
		}
		return resourceList.size();
		
	}
	
	//Prints all the resources in the Resource Lise
	void printResources(){
		
		Resource r ;
		System.out.println("/n" + getID() + " Resources:" );
		for(int i = 0; i < resourceList.size();i++){
			r =  resourceList.get(i);
			System.out.println(g.toJson(r));
		}
		System.out.println();
	}
	
	//Message handing function that reads all the messages in the Message Set
	void interpretMessages(){
		String msg;
		
		Iterator iter = messages.iterator();
		while( iter.hasNext()){
			msg = (String) iter.next();
			Message m = g.fromJson(msg, Message.class);
			//Do relevant computation for each type of message
			if(m.getType() == 'r'){
				Resource r = g.fromJson(m.getMessage(), Resource.class);
				//int index = getResourceIndex(r.getId());
				int index = r.getId();

				if(index >= 0 ){	
					if(resourceList.size() > index ){
						resourceList.set(index, r);
					}
					else{
						while (resourceList.size() <= index){
							resourceList.add(null);
						}
						resourceList.set(index, r);
					}
				}
			}
			else if(m.getType() == 'a'){
				//System.out.println(m.getMessage());
				RoverType rt =  g.fromJson(m.getMessage(), RoverType.class);
				if(getID().equals(rt.getKey())){
					roverNumber =  rt.getNumber();
					//System.out.println("Rover number set");
				}
			}
			else if(m.getType() == 's'){
				Integer num = g.fromJson(m.getMessage(), Integer.class);
				if(nScanners < num){
					nScanners = num;
				}
			 
			}
			else if(m.getType() == 'g'){
				Allocation allo = g.fromJson(m.getMessage(), Allocation.class);
				if(getID().equals(allo.getRoverId())){
					targetId = allo.getAllocation();
				}
			}
			iter.remove();
		}
	}
	
	//Calls move and saves new location data
	void moveAgent(double xOffset, double yOffset,int speed){
		xLocationTemp = calcWorldLocation(xLocation + xOffset,mapWidth);
		yLocationTemp = calcWorldLocation(yLocation + yOffset,mapHeight);
		xOffset = shortestRoute(xOffset,mapWidth);
		yOffset = shortestRoute(yOffset,mapHeight);
		try{
			move(xOffset,yOffset,speed);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());
		
		if(pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
			return;
		}
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			
			//move finished
            getLog().info("Move complete.");
            getLog().info("Updating Position.");
            xLocation = xLocationTemp;
            yLocation = yLocationTemp;
            getLog().info("I'm at x:"+ xLocation+" y:"+yLocation);
			
			//now scan
			try {
                getLog().info("Scanning...");
				scan(maxScan);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case PollResult.SCAN:
            getLog().info("Scan complete");
			
            //Boolean to stop concuurant task errors
            boolean noJob = true;
            
            
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset() + " Type: "+item.getResourceType());
                    //Check if you can carry a resource and if you are close enough the pick it up
                    if(getCurrentLoad() < maxCarry){
	                    if (Math.abs(item.getxOffset()) < 0.1 && Math.abs(item.getyOffset()) < 0.1) { 
	                    	try {
	                    		getLog().info("Attempting a collect!");
	                    		noJob = false;
	                    		collect();
	                    	} catch (Exception e) {
	                    		e.printStackTrace();
	                    	}
	                    	break;
	                    } 
	                    else{
	                    	try {
	                    		getLog().info("Moving to resource.");
	                    		noJob = false;
	                    		moveAgent(item.getxOffset(), item.getyOffset(), maxSpeed);
	                    	} catch (Exception e) {
	                    		e.printStackTrace();
	                    	}
	                    	break;
	                    }
                    }
				} 
				else if(item.getItemType() == ScanItem.BASE) {
                    getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
                    //Found Base, Check if you are carrying resources if so deposit them
                    if(getCurrentLoad() > 0){
		                if (Math.abs(item.getxOffset()) < 0.1 && Math.abs(item.getyOffset()) < 0.1) { 
		                	try {
		                		getLog().info("Attempting to deposit!");
		                		noJob = false;
		                		deposit();
		                	} catch (Exception e) {
		                		e.printStackTrace();
		                	}
		                	break;
		                } 
		                else {
		                	try {
		                	    getLog().info("Moving to Base.");
		                	    noJob = false;
		                		moveAgent(item.getxOffset(), item.getyOffset(), maxSpeed);
		                	} 
		                	catch (Exception e) {
		                			e.printStackTrace();
		                	}
		                	break;
		                }
                    } 
				}
				else {
                    getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
                }
			}
			
			// now move again
			if(noJob){
				getLog().info("Nothing Important Found, Moving along search path");
				try {
	                //getLog().info("Moving...");
					moveAgent(1.73 * (1.5*maxScan), 1.5 * (1.5*maxScan), maxSpeed);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case PollResult.COLLECT:
            getLog().info("Collect complete.");
            //After Collecting, Check if you can carry more otherwise go to base and deposit the resources
			try {
				if(getCurrentLoad() < maxCarry){
					getLog().info("Scanning...");
					scan(0.25);
				}
				else{
					resourceX = xLocation;
					resourceY = yLocation;
					getLog().info("Rover Full, Moving to Base");
					moveToBase(maxSpeed);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PollResult.DEPOSIT:
            getLog().info("Deposit complete.");
            //After Depositing check if you are still carry, if you are deposit the next resource, otherwise return to the last resources location
            if(getCurrentLoad() > 0){
            	try{
            		getLog().info("Trying to Deposit");
            		deposit();
            	}
            	catch(Exception e){
            		e.printStackTrace();
            	}
            }
            else{
            	moveAgent(resourceX,resourceY,maxSpeed);
            }
            //moveAgent(1.73 * 4, 1.5 * 4, 3);
			break;
		}
		
	}

}
