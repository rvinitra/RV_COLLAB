package bazaar;

import java.util.ArrayList;

public class NodeDetails {

		static int id;
		static String ip;
		static int port;
		static boolean isBuyer;
		static Product prod;
		static int count;
		static ArrayList<Neighbor> next;
		
		
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
		public static void Display(){
		    System.out.println("id = "+NodeDetails.id);
		    System.out.println("ip = "+NodeDetails.ip);
		    System.out.println("port = "+NodeDetails.port);
		    System.out.println("isBuyer = "+NodeDetails.isBuyer);
		    for(int i=0; i<NodeDetails.next.size(); i++){
			System.out.println("Neighbor "+(i+1));
			System.out.println("id = " + NodeDetails.next.get(i).id);
			System.out.println("ip = " + NodeDetails.next.get(i).ip);
			System.out.println("port = " + NodeDetails.next.get(i).port);
		    }
		}
		
		
		
	}


