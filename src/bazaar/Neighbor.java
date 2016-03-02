package bazaar;

import java.util.ArrayList;

public class Neighbor {
	  int id;
	  String ip;
	  int port;
	  public Neighbor(){
	      
	  }
	  	  	  
	  public Neighbor(int id, String ip,int port) {
		this.id=id;
		this.ip=ip;
		this.port=port;
		}
	  public void CurrentNode(){
	      id = NodeDetails.id;
	      ip = NodeDetails.ip;
	      port = NodeDetails.port;
	  }

}
