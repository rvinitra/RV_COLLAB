package bazaar;

import java.io.Serializable;


public class Neighbor implements Serializable{
    
	private static final long serialVersionUID = 1L;
	int id;
	String ip;
	int port;
	
	public Neighbor(){}
	  	  	  
	public Neighbor(int id, String ip,int port) {
	    this.id=id;
	    this.ip=ip;
	    this.port=port;
	}
	
	public String toString(){
	    return "Neighbor: {id: "+String.valueOf(id)+", ip: "+ip+", port: "+String.valueOf(port)+"}";
	    
	}
}
