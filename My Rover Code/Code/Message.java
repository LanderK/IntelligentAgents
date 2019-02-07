package rover;

//Message Object, used to add a header to a message, so that message interpreter can differentiate between different type of messages

public class Message {
	//Type of Message
	private char type;
	// Json Message
	private String msg;
	
	 public Message(char type, String msg) {
	        this.type = type;
	        this.msg = msg;
	 }
	 
	 public char getType(){
		 return type;
	 }
	 
	 public String getMessage(){
		 return msg;
	 }
	 
	 public void setMessage(String msg){
		 this.msg = msg;
	 }
	 public void setType(char t){
		 type = t;
	 }
}