package bazaar;

import java.util.ArrayList;

public class NodeDetails {

		static int id;
		static private String ip;
		static private int port;
		static boolean isBuyer;
		static Product prod;
		static int count;
		ArrayList<Boolean> next;
		
		public static void assignValues(int id, String ip, int port, boolean isBuyer, Product prod, int count, ArrayList<ArrayList<Boolean>> neighbors){
		NodeDetails.id=id;
		NodeDetails.ip=ip;
		NodeDetails.port=port;
		NodeDetails.isBuyer=isBuyer;
		NodeDetails.prod=prod;
		if(!isBuyer)
			NodeDetails.count=count;
		else 
			count=-1;
		}
		public void setNeighbors( ArrayList<ArrayList<Boolean>> neighbors)
		{	
			next=neighbors.get(id-1);		
		}
		
		
	}


