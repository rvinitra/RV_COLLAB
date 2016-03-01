package bazaar;

import java.util.ArrayList;

public class NodeDetails {

		static int id;
		static private String ip;
		static private int port;
		static boolean isBuyer;
		static Product prod;
		static int count;
		ArrayList<Neighbor> next;
		
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
		
		public static ArrayList<Neighbor> getMyNeighbors()
		{	
			ArrayList<Neighbor> neighbourList = new ArrayList<Neighbor>();
			Neighbor nd= new Neighbor(1,"10.0.0.5",1099);
			neighbourList.add(nd);
			return(neighbourList);
		}
		
		
		
	}


