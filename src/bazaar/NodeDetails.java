package bazaar;

import java.rmi.Naming;
import java.util.ArrayList;


public class NodeDetails {

		static int id;
		static String ip;
		static int port;
		static boolean isBuyer;
		static boolean isSeller;
		static boolean isTrader;
		static TraderDetails traderDetails;//trader details if I'm the trader
		static Product sellProd;//seller product
		static Product buyProd;
		static int sellCount;//seller stock of product
		static int buyCount;
		static Neighbor trader;//current trader
		static boolean isInElection;
		static ArrayList<Neighbor> next;
		static long runningTime = 0;
		static float money;
		static int lamportClock = 0;
				
		//synchronized so that any thread that attempts to modify count first obtains a lock on it
//		public static void decrementProductCount(){
//			synchronized (countLock){
//				NodeDetails.count=NodeDetails.count-1;
//			}
//			if (NodeDetails.count!=0)
//			    System.out.println(NodeDetails.getNode()+": Current Stock: "+ NodeDetails.prod +" X "+NodeDetails.count);
//		}
//		public static void setProductCount(int newCount){
//			synchronized (countLock){
//				NodeDetails.count=newCount;
//			}
//					
//		}
//		public static void checkAndUpdateSeller(){
//		    	Log.l.log(Log.finer, NodeDetails.getNode()+": Run out of "+NodeDetails.prod);
//			if(NodeDetails.count==0){
//				NodeDetails.prod=Bazaar.pickRandomProduct();
//				NodeDetails.setProductCount(Bazaar.pickRandomCount());
//				Log.l.log(Log.finer, NodeDetails.getNode()+": Picked new product to sell "+NodeDetails.prod);
//				System.out.println(NodeDetails.getNode()+": Current Stock: "+ NodeDetails.prod +" X "+NodeDetails.count);
//			}
//				
//		}
//		public static void Display(){
//		    Log.l.log(Log.finest, NodeDetails.getNode()+":\n id = "+NodeDetails.id);
//		    Log.l.log(Log.finest, "ip = "+NodeDetails.ip);
//		    Log.l.log(Log.finest, "port = "+NodeDetails.port);
//		    Log.l.log(Log.finest, "isBuyer = "+NodeDetails.isBuyer);
//		    Log.l.log(Log.finest, "Product = "+NodeDetails.prod);
//		    if(!NodeDetails.isBuyer)
//			Log.l.log(Log.finest, "Count = "+NodeDetails.count);
//		    for(int i=0; i<NodeDetails.next.size(); i++){
//			Log.l.log(Log.finest, "Neighbor "+(i+1));
//			Log.l.log(Log.finest, "id = " + NodeDetails.next.get(i).id);
//			Log.l.log(Log.finest, "ip = " + NodeDetails.next.get(i).ip);
//			Log.l.log(Log.finest, "port = " + NodeDetails.next.get(i).port);
//		    }
//		}
		
		public static String getNode(){
		    return id + "@" + ip+":"+port;
		}
		
		public static void updateTrader(Neighbor newLeader){
			NodeDetails.trader=newLeader;
			NodeDetails.isTrader=false;
		}

		public static Neighbor getCurrentNode(){
			return(new Neighbor(NodeDetails.id,NodeDetails.ip,NodeDetails.port));
		}
		
		public static void takeOverAsTrader(){
			//set myself to trader
			NodeDetails.isTrader=true;
			Neighbor exTrader = NodeDetails.trader;
			NodeDetails.trader=NodeDetails.getCurrentNode();
			BazaarInterface obj = null;
			
		    //build lookup name for RMI object based on exTraders's ip & port
		    Log.l.log(Log.finer, NodeDetails.getNode()+": Asking :"+exTrader.id+"@"+exTrader.ip+":"+exTrader.port+" for trader files");
  		StringBuilder lookupName = new StringBuilder("//");
		    String l = lookupName.append(exTrader.ip).append(":").append(exTrader.port).append("/Node").toString();
		    try {
			obj = (BazaarInterface)Naming.lookup(l);
			NodeDetails.traderDetails= obj.getTraderDetails();
			NodeDetails.traderDetails.transactionsCount=0;
			System.out.println(NodeDetails.getNode()+":[Trader] Sucessfully took over as trader from "+exTrader.id+"@"+exTrader.ip+":"+exTrader.port);
		    }
		    catch (Exception e) {
			System.out.println(NodeDetails.getNode()+":[Trader] Failed to take over as trader from "+exTrader.id+"@"+exTrader.ip+":"+exTrader.port);
			e.printStackTrace();
			
		    }
			
				
		}
		public static Neighbor selectRandomNeighbor() {
		    int pick = Bazaar.RANDOM.nextInt(100)%next.size();
		    return next.get(pick);
		}
		public static double getCreditAmount(Product prod) {
		    switch(prod){
        		    case BOAR: return 10;
        		    case FISH: return 20;
        		    case SALT: return 30;
        		    default: return 0;
		    }
		}
		public static void incrementClock(int ticks){
			NodeDetails.lamportClock+=ticks;
		}
		
		//send my clock to all nodes
		public static void broadcastClock(){
			BazaarInterface obj = null;
			for(Neighbor n : NodeDetails.next ){
				if(n.id != NodeDetails.trader.id){
	  			    //build lookup name for RMI object based on neighbor's ip & port
	    		    Log.l.log(Log.finer, NodeDetails.getNode()+": sending my timestamp to "+n.id+"@"+n.ip+":"+n.port);
		      		StringBuilder lookupName = new StringBuilder("//");
	    		    String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
	    		    try {
		    			obj = (BazaarInterface)Naming.lookup(l);
		    			obj.clockSync(NodeDetails.lamportClock);
	    		    }
	    		    catch (Exception e) {
		    			System.err.println(NodeDetails.getNode()+":Election failed to "+l);
		    			e.printStackTrace();
	    		    }
				}
			}
		}
	}


