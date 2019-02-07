package rover;

//Rover which scans the map, it requires a Master Scanner to Run 

public class ScannerRover extends BasicRover {
	
	//Number used to count the number of resources that have been found
	private int resourceId = 0;
	//Number assigned to the scanner by the Master scan which is used to define the starting offset
	private int scannerNumber  = -1;
	
	
	public ScannerRover() {
        super(2,7,0,1);
        
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

	@Override
	void begin() {
		//called when the world is started
        getLog().info("BEGIN!");
		
		try {
			//Set Inital Values
			mapWidth = getWorldWidth();
			mapHeight = getWorldHeight();
			resourceId = 0;
			xLocationTemp = 0.0;
			yLocationTemp = 0.0;
			resourceX = 0.0;
			resourceY = 0.0;
			resourceList.removeAll(resourceList);
			xLocation = 0.0;
			yLocation = 0.0;
			if(roverNumber < 0){
				RoverType rt =  new RoverType('s',getID());
				Message m = new Message('a',g.toJson(rt));
				broadCastToTeam(g.toJson(m));
				//System.out.println(g.toJson(m));
			}
			//Wait to be assigned a rover number
			while(roverNumber < 0){
				retrieveMessages();
				interpretMessages();
			}
			Thread.sleep(1000);
			retrieveMessages();
			interpretMessages();
			//Move to Starting Offset
			moveAgent(0,mapHeight/(nScanners+1) * roverNumber,maxSpeed);
		} catch (Exception e) {
			e.printStackTrace();
			retrieveMessages();
			interpretMessages();
			moveAgent(0,mapHeight/(nScanners+1) * roverNumber,maxSpeed);
		}
		
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
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			
			//After move save position and then scan
            getLog().info("Move complete.");
            getLog().info("Updating Position.");
            xLocation = xLocationTemp;
            yLocation = yLocationTemp;
            getLog().info("I'm at x:"+ xLocation+" y:"+yLocation);
            if(isAlmostEqual(xLocation,0.0,0.1) && isAlmostEqual(yLocation,0.0,0.1)){
            	getLog().info("Traversed Map");
            }
			
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
			
            boolean noJob = true;
            
            //For each Resource found set its resource id to -1 such that it can be set later by the Master Scanner, then broadCast it
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
					double globalX = calcWorldLocation(xLocation+item.getxOffset(), mapWidth);
					double globalY = calcWorldLocation(yLocation+item.getyOffset(), mapHeight);
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset() + " Type: "+item.getResourceType());
                    getLog().info("global location: "+globalX + ", " +globalY);
                    Resource r ;
                    if(isNewResource(globalX,globalY) == -1){
                    	r = new Resource(globalX,globalY, item.getResourceType());
                    	//resourceId++;
                    	//resourceList.add(r.getId(),r);
                    	//System.out.println(g.toJson(r));
                    	String json = g.toJson(r);
                    	Message m  = new Message('r',json);
                        broadCastToTeam(g.toJson(m));
                    }       
				} 
				else if(item.getItemType() == ScanItem.BASE) {
                    getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
				}
				else {
                    getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
                }
			}
			
			// now move again
			if(noJob){
				getLog().info("Moving along search path");
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
			try {
				if(getCurrentLoad() < 2){
					getLog().info("Scanning...");
					scan(1);
				}
				else{
					resourceX = xLocation;
					resourceY = yLocation;
					getLog().info("Rover Full, Moving to Base");
					moveToBase(3);
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
            moveAgent(resourceX,resourceY,3);
            //moveAgent(1.73 * 4, 1.5 * 4, 3);
			break;
		}
		
	}

}
