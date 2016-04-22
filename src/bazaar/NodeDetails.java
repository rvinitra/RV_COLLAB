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
		static ArrayList<Neighbor> next;
		static long runningTime = 0;
		static float money;
		
		//Get Node Details for logging
		public static String getNode(){
		    return id + "@" + ip+":"+port;
		}
		
		//Get current node details as a Neighbor object
		public static Neighbor getCurrentNode(){
			return(new Neighbor(NodeDetails.id,NodeDetails.ip,NodeDetails.port));
		}
		
		//Update new trader details
		public static void updateTrader(Neighbor newTrader){
			NodeDetails.trader=newTrader;
			NodeDetails.isTrader=false;
		}
		
		//Take over as new trader
		public static void takeOverAsTrader(){
		    	//get ex-Trader details
			Neighbor exTrader = NodeDetails.trader;
			BazaarInterface obj = null;
			
			//build lookup name for RMI object based on exTraders's ip & port
    		    	Log.l.log(Log.finer, NodeDetails.getNode()+": Asking :"+exTrader.id+"@"+exTrader.ip+":"+exTrader.port+" for trader files");
    		    	StringBuilder lookupName = new StringBuilder("//");
    		    	String l = lookupName.append(exTrader.ip).append(":").append(exTrader.port).append("/Node").toString();
    		    	System.out.println(NodeDetails.getNode()+":[Trader Election] Calling to get Trader Details from ex-Trader "+exTrader.id+"@"+exTrader.ip+":"+exTrader.port);
    		    	try {
    		    	    obj = (BazaarInterface)Naming.lookup(l);
    		    	    obj.getTraderDetails(NodeDetails.getCurrentNode());
    		    	}
    		    	catch (Exception e) {
    		    	    System.out.println(NodeDetails.getNode()+":[Trader Election] Failed to get Trader Details from ex-Trader "+exTrader.id+"@"+exTrader.ip+":"+exTrader.port);
    		    	    e.printStackTrace();
    		    	}
		}
		
		//Select random neighbor node
		public static Neighbor selectRandomNeighbor() {
		    int pick = Bazaar.RANDOM.nextInt(100)%next.size();
		    return next.get(pick);
		}
		
		//Get money to be credited to seller
		public static double getCreditAmount(Product prod) {
		    switch(prod){
        		    case BOAR: return 10;
        		    case FISH: return 20;
        		    case SALT: return 30;
        		    default: return 0;
		    }
		}
		
		//Process the buy queue if you are the trader
		public static void processBuyQueue(){
			while (NodeDetails.isTrader){
			    RequestMsg reqBoar=null, reqFish=null, reqSalt=null;
			    synchronized (TraderDetails.boarBuyerRequestsLock){
		    	    	reqBoar=NodeDetails.traderDetails.boarBuyerRequests.poll();
			    }
			    synchronized (TraderDetails.saltBuyerRequestsLock){
		    		reqSalt=NodeDetails.traderDetails.saltBuyerRequests.poll();
		    	    }
			    synchronized (TraderDetails.fishBuyerRequestsLock){
		    	    	reqFish=NodeDetails.traderDetails.fishBuyerRequests.poll();
		    	    }
			    if (reqBoar!=null)
				(new Thread(new Trader(reqBoar))).start();
			    if (reqSalt!=null)
				(new Thread(new Trader(reqSalt))).start();
			    if (reqFish!=null)
				(new Thread(new Trader(reqFish))).start();
			}
		}
}


