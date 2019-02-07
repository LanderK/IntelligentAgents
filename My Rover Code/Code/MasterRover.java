package rover;

import java.util.Iterator;

// Master Scanner that assigns resource Ids and allocations

public class MasterRover extends BasicRover {
	
	
	private int resourceId = 0;
	
	
	
	public MasterRover() {
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
			retrieveMessages();
			interpretMessages();
			scan(maxScan);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//Overriden interpret Message, as Master Scanners do different Things depending on the messages
	void interpretMessages(){
		
		retrieveMessages();
		String msg;
		
		Iterator iter = messages.iterator();
		while( iter.hasNext()){
			msg = (String) iter.next();
			Message m = g.fromJson(msg, Message.class);
			if(m.getType() == 'r'){
				Resource r = g.fromJson(m.getMessage(), Resource.class);
				//int index = getResourceIndex(r.getId());
				int index = r.getId();
				if(index >= 0 ){
					if(resourceList.size() > index){
						resourceList.set(index, r);
					}
					else{
						while (resourceList.size() <= index){
							resourceList.add(null);
						}
						resourceList.set(index, r);
					}
				}
				else{
					int newId = isNewResource(r.getXLocation(),r.getYLocation());
					if(newId == -1 ){
						r.setId(resourceId);
						resourceList.add(r);
						resourceId++;
					}
					else{
						r.setId(newId);
						resourceList.set(newId,r);
					}
					Message m1 = new Message('r', g.toJson(r));
					broadCastToTeam(g.toJson(m1));
				}
				//printResources();
			}
			else if(m.getType() == 'a'){
				RoverType rt =  g.fromJson(m.getMessage(), RoverType.class);
				if(rt.getNumber() == 0  && rt.getType() == 's'){
					nScanners++;
					rt.setNumber(nScanners);
				}
				m.setMessage(g.toJson(rt));
				//System.out.println(g.toJson(m));
				broadCastToTeam(g.toJson(m));
				
				Message updateNS = new Message('s',g.toJson(nScanners));
				broadCastToTeam(g.toJson(updateNS));
			}
			else if(m.getType() ==  'g'){
				continue;
			}
			iter.remove();
		}
		
		sendResourcesList();
		doAllocations();
	}
	
	//Assign Rovers to resources depending on the allocation Messages in Messages Set
	//It check for each resource id in the allocation message if that resource can be allocated
	//if it can set the allocation and send the allocation back to the collector
	void doAllocations(){
		
		String msg;
		
		Iterator iter = messages.iterator();
		while( iter.hasNext()){
			msg = (String) iter.next();
			Message m = g.fromJson(msg, Message.class);
			if(m.getType() ==  'g'){
				Allocation allo = g.fromJson(m.getMessage(), Allocation.class);
				//System.out.println("New Allocation Message");
				if(allo.getRoverId().isEmpty()){
					int id = allo.getAllocation();
					//System.out.println("Unallocating:"+id);
					resourceList.get(id).setAssignedRover("");
					m.setType('r');
					m.setMessage(g.toJson(resourceList.get(id)));
					//System.out.println(g.toJson(m));
					broadCastToTeam(g.toJson(m));
				}
				else{
					//System.out.println("Allocating");
					while(allo.getAllocation() < 0 && !allo.isAllocationEmpty()){
						//System.out.println("loop");
						int newId = allo.getNextId();
						//System.out.println("Trying to Allocate : " + newId);
						if(newId != -1){
							String assignedRover = resourceList.get(newId).getAssignedRover();
							//System.out.println(g.toJson(resourceList.get(newId)));
							if(!resourceList.get(newId).getIsEmpty()){
								if((assignedRover.isEmpty() || assignedRover.equals(allo.getRoverId()))){
									allo.setAllocation(newId);
									resourceList.get(newId).setAssignedRover(allo.getRoverId());
									m.setType('r');
									m.setMessage(g.toJson(resourceList.get(newId)));
									broadCastToTeam(g.toJson(m));
									//System.out.println("Assigning " + newId + " to " +allo.getRoverId() );
									break;
								}
							}
						}
						else{
							//System.out.println("No Allocation");
						}
					}
					
					m.setType('g');
					m.setMessage(g.toJson(allo));
					broadCastToTeam(g.toJson(m));
				}
			}
			else{
				continue;
			}
			iter.remove();
		}
		
		
		
	}
	
	//BroadCasts the Entire Resource List
	void sendResourcesList(){
		
		Message m;
		for(int i=0;i<resourceList.size();i++){
			m = new Message('r', g.toJson(resourceList.get(i)));
			broadCastToTeam(g.toJson(m));
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
		
		//printResources();
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			
			//move finished
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
			
            
			for(ScanItem item : pr.getScanItems()) {
				if(item.getItemType() == ScanItem.RESOURCE) {
					double globalX = calcWorldLocation(xLocation+item.getxOffset(), mapWidth);
					double globalY = calcWorldLocation(yLocation+item.getyOffset(), mapHeight);
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset() + " Type: "+item.getResourceType());
                    getLog().info("global location: "+globalX + ", " +globalY);
                    Resource r ;
                    //Checks if the found resource is new, if it is add it to the List
                    if(isNewResource(globalX,globalY) == -1){
                    	r = new Resource(resourceId,globalX,globalY, item.getResourceType());
                    	resourceId++;
                    	resourceList.add(r.getId(),r);
                    	//printResources();
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
			
			retrieveMessages();
			interpretMessages();
			sendResourcesList();
			
			//Cant do anymore actions without dying so scan a small amount to stop agents from dying (needed for resource Allocation)
			if(getEnergy() < 25){
				try{
					scan(0.000001);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else{
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
