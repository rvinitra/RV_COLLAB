package bazaar;

import java.util.ArrayList;
import java.util.Queue;

public class NodeDetails {

		static int id;
		static String ip;
		static int port;
		static boolean isBuyer;
		static boolean isSeller;
		static boolean isTrader;
		static Trader traderDetails;
		static Product prod;//seller product
		static Product buyProd;
		static int count;//seller stock of product
		static int buyCount;
		static ArrayList<Neighbor> next;
		static Queue<Neighbor> sellerReplies;
		private static final Object countLock = new Object();
		private static final Object sellerRepliesLock = new Object();
		static long runningTime = 0;
		static float money;
				
		//synchronized so that any thread that attempts to modify count first obtains a lock on it
		public static void decrementProductCount(){
			synchronized (countLock){
				NodeDetails.count=NodeDetails.count-1;
			}
			if (NodeDetails.count!=0)
			    System.out.println(NodeDetails.getNode()+": Current Stock: "+ NodeDetails.prod +" X "+NodeDetails.count);
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
		public static void checkAndUpdateSeller(){
		    	Log.l.log(Log.finer, NodeDetails.getNode()+": Run out of "+NodeDetails.prod);
			if(NodeDetails.count==0){
				NodeDetails.prod=Bazaar.pickRandomProduct();
				NodeDetails.setProductCount(Bazaar.pickRandomCount());
				Log.l.log(Log.finer, NodeDetails.getNode()+": Picked new product to sell "+NodeDetails.prod);
				System.out.println(NodeDetails.getNode()+": Current Stock: "+ NodeDetails.prod +" X "+NodeDetails.count);
			}
				
		}
		public static void Display(){
		    Log.l.log(Log.finest, NodeDetails.getNode()+":\n id = "+NodeDetails.id);
		    Log.l.log(Log.finest, "ip = "+NodeDetails.ip);
		    Log.l.log(Log.finest, "port = "+NodeDetails.port);
		    Log.l.log(Log.finest, "isBuyer = "+NodeDetails.isBuyer);
		    Log.l.log(Log.finest, "Product = "+NodeDetails.prod);
		    if(!NodeDetails.isBuyer)
			Log.l.log(Log.finest, "Count = "+NodeDetails.count);
		    for(int i=0; i<NodeDetails.next.size(); i++){
			Log.l.log(Log.finest, "Neighbor "+(i+1));
			Log.l.log(Log.finest, "id = " + NodeDetails.next.get(i).id);
			Log.l.log(Log.finest, "ip = " + NodeDetails.next.get(i).ip);
			Log.l.log(Log.finest, "port = " + NodeDetails.next.get(i).port);
		    }
		}
		
		public static String getNode(){
		    return id + "@" + ip+":"+port;
		}
		
		
		
	}


