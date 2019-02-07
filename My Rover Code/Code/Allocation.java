package rover;

import java.util.ArrayList;

/*
 Class to be used in message passing, it is used to request/confirm resource Allocations to Collector Rovers 
 */

public class Allocation {
	
	//List to store the resource Ids that the collector can pick up, ordered with increasing energy to collect
	private ArrayList<Integer> IdList = new ArrayList<Integer>();
	//Collectors RoverID used to assign the Resource
	private String roverId;
	//Resource ID the Collector is told to pickup
	private int resourceAllocated = -1;


    public Allocation(String roverId) {
       this.roverId = roverId;
    }
    
    public Allocation(int resourceId){
    	resourceAllocated = resourceId;
    	roverId = "";
    }
	
	public String getRoverId(){
		return roverId;
	}
	
	public int getAllocation(){
		return resourceAllocated;
	}
	
	public void setAllocation(int i){
		resourceAllocated = i;
	}
	
	//Removes the head of the list
	public int getNextId(){
		
		//System.out.println(IdList.toString());
		if(!IdList.isEmpty()){
			int id = (int) IdList.get(0);
			IdList.remove(0);
			return id;
		}
		else{
			return -1;
		}
	}
	
	public boolean isAllocationEmpty(){
		return IdList.isEmpty();
	}
	
	//Adds an Id to the list at a desired index;
	public void addId(int index,int value){
		IdList.add(index,value);
	}
	
	
}