package bazaar;

import java.io.Serializable;

public class RequestMsg implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 3018364016008724818L;
    Neighbor requestingNode;
    Product prod;
    int count;
    int timestamp;
    
    public String toString(){
	return "Request: { requestingNode: "+requestingNode.toString()+", prod: "+prod.toString()+", count: "+String.valueOf(count)+", timestamp: "+String.valueOf(timestamp)+"}";
	
    }
}
