package rover;

import java.util.ArrayList;
import java.util.Collections;

//Rover which collects the resources on the map

public class CamelRover extends BasicRover {
	
	private int resourceId = -1;
	
	
	public CamelRover() {
        super(5,1,3,1);
        
		//use your username for team name
		//setTeam("lk383");
		
		//try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			//Fourth attribute is the collector type
			//setAttributes(4, 5, 1, 1);
		//} catch (Exception e) {
			//e.printStackTrace();
		//}
		
	}
	
	public CamelRover(int speed, int collect, int type){
		super(speed,1,collect,type);
	}

	@Override
	void begin() {
		//called when the world is started
        getLog().info("BEGIN!");
		
		try {
			//Set Inital Values
			mapWidth = getWorldWidth();
			mapHeight = getWorldHeight();
			xLocation = 0.0;
			yLocation = 0.0;
			resourceId = -1;
			targetId = -1;
			xLocationTemp = 0.0;
			yLocationTemp = 0.0;
			resourceX = 0.0;
			resourceY = 0.0;
			resourceList.removeAll(resourceList);
			xLocation = 0.0;
			yLocation = 0.0;
			
			//RoverType rt =  new RoverType('c',getID());
			//Message m = new Message('a',g.toJson(rt));
			//broadCastToTeam(g.toJson(m));
			//Idle Until message
			while(resourceList.isEmpty()){
				retrieveMessages();
				interpretMessages();
			}
			calcMoveAction();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//Builds a list of the Resources the rover can collect with increasing energy
	Allocation getAllocation(){
		
		Allocation allo = new Allocation(getID()) ;
		ArrayList<Double> energies = new ArrayList<Double>();
		double bestX = mapWidth , bestY = mapHeight;
		double bestEnergy = getEnergy();
		Resource r;
		double energy;
		for(int i=0;i<resourceList.size(); i++){
			r =  resourceList.get(i);
			if(!r.getIsEmpty() && r.getType()==resourceType  && (r.getAssignedRover().equals("") || r.getAssignedRover().equals(getID()))){
				energy = energyToCollect(r);
				if(energy < bestEnergy){
					//System.out.println(energy);
					//bestEnergy = energy;
					energies.add(energy);
					double xOffset = r.getXLocation() - xLocation;
					double yOffset = r.getYLocation() - yLocation;
					xOffset = shortestRoute(xOffset,mapWidth);
					yOffset = shortestRoute(yOffset,mapHeight);
					bestX = xOffset;
					bestY = yOffset;
					Collections.sort(energies);
					allo.addId(energies.indexOf(energy),r.getId());
				}	
			}
		}
		
	
		//System.out.println(all.get
		return allo;
	}
	
	//Resets the Assingment of rovers on a resource
	void unassignRover(){
		
		Allocation allo = new Allocation(resourceId);
		
		Message m = new Message('g', g.toJson(allo));
		broadCastToTeam(g.toJson(m));
		//broadCastToTeam(g.toJson(m));
		resourceId = -1;
	}
	
	//Calculates where the rover Should move next
	void calcMoveAction(){
		
		retrieveMessages();
		interpretMessages();
		
		Resource r;
		if(getCurrentLoad() == maxCarry){
			//Full so move to base
			moveToBase(maxSpeed);
		}
		else if(resourceId != -1 && energyToCollect(resourceList.get(resourceId))< getEnergy() && !resourceList.get(resourceId).getIsEmpty()){
			//Allocated resource is not yet empty
			r = resourceList.get(resourceId);
			moveAgent(r.getXLocation()-xLocation,r.getYLocation()-yLocation,maxSpeed);
		}
		else{
			//No allocated resource, find new one and move to it
			
			Allocation allo = getAllocation();
			resourceId = -1;
			//targetId = -1;
			
			if(allo.isAllocationEmpty()){
				//Cant Pick anything up from current Location
				moveToBase(maxSpeed);
			}
			else{
				//Send Message to Master Scanner requesting the allocations
				Message m =  new Message('g', g.toJson(allo));
				//System.out.println(g.toJson(m));
				broadCastToTeam(g.toJson(m));
				retrieveMessages();
				interpretMessages();
				resourceId = targetId;
				if(resourceId == -1){
					try{
						
						scan(0.00000001);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				else{
					targetId = -1;
					//System.out.println(getID() + ": Assigned To " +resourceId);
					r = resourceList.get(resourceId);
					if(r.getIsEmpty()){
						//System.out.println("THis one is empty, so retry");
						resourceId = -1;
						calcMoveAction();
					}
					else{
						//System.out.println("Moving to new r");
						moveAgent(r.getXLocation()-xLocation,r.getYLocation()-yLocation,maxSpeed);
					}
				}
				
			}
		}
		
	}
	
	//Calculates the energy to move to a resource, pick it up and return back to base to deposit them
	double energyToCollect(Resource r){
		
		double xOffset = r.getXLocation() - xLocation;
		double yOffset = r.getYLocation() - yLocation;
		xOffset = shortestRoute(xOffset,mapWidth);
		yOffset = shortestRoute(yOffset,mapHeight);
		
		double moveToEnergy = 2*Math.sqrt(xOffset*xOffset + yOffset*yOffset)/maxSpeed;
		
		double dx, dy;
		if(r.getXLocation()>mapWidth/2){
			dx = mapWidth-r.getXLocation();
		}
		else{
			dx = -r.getXLocation();
		}
		
		if(r.getYLocation()>mapHeight/2){
			dy = mapHeight-r.getYLocation();
		}
		else{
			dy = -r.getYLocation();
		}
		
		double moveToBase = 2*Math.sqrt(dx*dx + dy*dy)/maxSpeed;
		
		double depositEnergy = 5 * maxCarry;
		
		double pickUpEnergy = 5 * (maxCarry-getCurrentLoad());
				
		return (moveToEnergy + moveToBase + depositEnergy + pickUpEnergy);
		
	}
	
	//Sets the allocated resource to Empty
	void setEmptyResource(int Id){
		
		Resource r = resourceList.get(Id);
		r.setIsEmpty();
		resourceList.set(Id,r);
		//System.out.println("Esend:"+g.toJson(r));
		String json = g.toJson(r);
        Message m  = new Message('r',json);
        broadCastToTeam(g.toJson(m));
		//broadCastToTeam(g.toJson(r));
		
	}

	@Override
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());
		
		if(pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
			return;
		}
		
		//Update Resource List 
		retrieveMessages();
		interpretMessages();
		
		//printResources();
		
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
				scan(0.2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case PollResult.SCAN:
            getLog().info("Scan complete");
			
            boolean noJob = true;
            boolean foundResource = false;
            
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
					foundResource = true;
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset() + " Type: "+item.getResourceType());
                    if(getCurrentLoad() < maxCarry){
	                    if (Math.abs(item.getxOffset()) < 0.1 && Math.abs(item.getyOffset()) < 0.1) { 
	                    	try {
	                    		getLog().info("Attempting a collect!");
	                    		noJob = false;
	                    		collect();
	                    	} catch (Exception e) {
	                    		e.printStackTrace();
	                    		try{
	                    			scan(0.1);
	                    		}
	                    		catch(Exception e1){
	                    			e1.printStackTrace();
	                    		}
	                    	}
	                    	break;
	                    }
                    }
                    else{
                		noJob = false;
                    	unassignRover();
                    	moveToBase(maxSpeed);
                    	break;
                    }
				} 
				else if(item.getItemType() == ScanItem.BASE) {
                    getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
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
                    }
				}
				else {
                    getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
                }
			}
			if(!foundResource && resourceId > -1){
				setEmptyResource(resourceId);
				unassignRover();
				resourceId = -1;
			}
			// now move again
			if(noJob){
				getLog().info("Finding which Resource to Move to");
				try {
	                //getLog().info("Moving...");
					calcMoveAction();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case PollResult.COLLECT:
            getLog().info("Collect complete.");
			try {
				//Allways scan after collecting to check if there are anymore
				if(getCurrentLoad() < maxCarry){
					getLog().info("Scanning...");
					scan(0.2);
				}
				else{
					//resourceX = xLocation;
					//resourceY = yLocation;
					getLog().info("Rover Full, Moving to Base");
					scan(0.2);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PollResult.DEPOSIT:
            getLog().info("Deposit complete.");
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
            	calcMoveAction();
            }
            //moveAgent(1.73 * 4, 1.5 * 4, 3);
			break;
		}
		
	}

}
