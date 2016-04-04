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
		static Product prod;//seller product
		static Product buyProd;
		static int count;//seller stock of product
		static int buyCount;
		static Neighbor trader;//current trader
		static boolean isInElection;
		private static final Object countLock = new Object();
		static ArrayList<Neighbor> next;
		static long runningTime = 0;
		static float money;
		static String testTraderData;
				
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
		
		public static void updateLeader(Neighbor newLeader){
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
			BazaarInterface obj = null;
			
		    //build lookup name for RMI object based on exTraders's ip & port
		    Log.l.log(Log.finer, NodeDetails.getNode()+": Asking :"+exTrader.id+"@"+exTrader.ip+":"+exTrader.port+" for trader files");
  		StringBuilder lookupName = new StringBuilder("//");
		    String l = lookupName.append(exTrader.ip).append(":").append(exTrader.port).append("/Node").toString();
		    try {
			obj = (BazaarInterface)Naming.lookup(l);
			NodeDetails.testTraderData= obj.getTraderDetails();
		    }
		    catch (Exception e) {
			System.err.println(NodeDetails.getNode()+":Get trader details failed:"+l);
			e.printStackTrace();
			
		    }
			
				
		}
	}


