package bazaar;

import java.util.ArrayList;
import java.util.Queue;

public class NodeDetails {

		static int id;
		static String ip;
		static int port;
		static boolean isBuyer;
		static Product prod;
		static int count;
		static ArrayList<Neighbor> next;
		static Queue<Neighbor> sellerReplies;
		private static final Object countLock = new Object();
		private static final Object sellerRepliesLock = new Object();
				
		public static ArrayList<Neighbor> getMyNeighbors()
		{	
			ArrayList<Neighbor> neighbourList = new ArrayList<Neighbor>();
			Neighbor nd= new Neighbor(1,"10.0.0.5",1099);
			neighbourList.add(nd);
			return(neighbourList);
		}
		//synchronized so that any thread that attempts to modify count first obtains a lock on it
		public static void decrementProductCount(){
			synchronized (countLock){
				NodeDetails.count=NodeDetails.count-1;
			}			
		}
		public static void setProductCount(int newCount){
			synchronized (countLock){
				NodeDetails.count=newCount;
			}
					
		}
		//synchronized so that any thread that attempts to modify sellerReplies first obtains a lock on it
		public static void addSellerReply(Neighbor newReply){
			synchronized(sellerRepliesLock){
				NodeDetails.sellerReplies.add(newReply);
			}
		}
		//synchronized so that any thread that attempts to modify seller Replies first obtains a lock on it
		public static Neighbor removeSellerReply(){
			Neighbor topSeller;
			synchronized(sellerRepliesLock){
				topSeller=NodeDetails.sellerReplies.remove();
			}
			return(topSeller);
		}
		
		public static void Display(){
		    System.out.println("id = "+NodeDetails.id);
		    System.out.println("ip = "+NodeDetails.ip);
		    System.out.println("port = "+NodeDetails.port);
		    System.out.println("isBuyer = "+NodeDetails.isBuyer);
		    System.out.println("Product = "+NodeDetails.prod);
		    if(!NodeDetails.isBuyer)
		    	System.out.println("Count = "+NodeDetails.count);
		    for(int i=0; i<NodeDetails.next.size(); i++){
			System.out.println("Neighbor "+(i+1));
			System.out.println("id = " + NodeDetails.next.get(i).id);
			System.out.println("ip = " + NodeDetails.next.get(i).ip);
			System.out.println("port = " + NodeDetails.next.get(i).port);
		    }
		}
		
		
		
	}


