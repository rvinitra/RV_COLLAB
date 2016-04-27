/**
 * 
 */
package bazaar;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author rvinitra, rshenoy
 *
 */
public class Node extends UnicastRemoteObject implements BazaarInterface, Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 2489778917567778819L;

    public Node() throws RemoteException {
	super();
    }
    
    //Method for trader to update the Buy Queue for buy requests
    public void buy(RequestMsg req){
        System.out.println(NodeDetails.getNode()+":[Trader Queue Buy] Queueing buy request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
        switch(req.prod){
        	case BOAR: 
        	    synchronized (TraderDetails.boarBuyerRequestsLock){
    		    	NodeDetails.traderDetails.boarBuyerRequests.add(req);
    		} break;
        	case SALT: 
        	    synchronized (TraderDetails.saltBuyerRequestsLock){
    		    	NodeDetails.traderDetails.saltBuyerRequests.add(req);
    		} break;
        	case FISH: 
        	    synchronized (TraderDetails.fishBuyerRequestsLock){
    		    	NodeDetails.traderDetails.fishBuyerRequests.add(req);
    		} break;
        	default:
        	    break;
        }
    }
    
    //Method for trader to update his inventory for goods deposited
    public void deposit(RequestMsg req) throws RemoteException {
	if (!NodeDetails.traderDetails.isCacheValid.get(req.prod)){
	    System.out.println(NodeDetails.getNode()+":[Trader Deposit] Invalid cache for "+req.prod+". Looking up Database Server");
	    StringBuilder lookupName= new StringBuilder("//");
	    String l= lookupName.append(NodeDetails.db.ip).append(":").append(NodeDetails.db.port).append("/Database").toString();
	    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
	    try {
		DatabaseInterface obj = null;
		obj = (DatabaseInterface)Naming.lookup(l);
		switch(req.prod){
			case BOAR: 
                    	    synchronized (TraderDetails.boarSellerStockLock){
                    		NodeDetails.traderDetails.boarSellerStock=obj.lookUp(req.prod);
                    	    } break;
                    	case SALT: 
                    	    synchronized (TraderDetails.saltSellerStockLock){
                    		NodeDetails.traderDetails.saltSellerStock=obj.lookUp(req.prod);
                    	    } break;
                    	case FISH: 
                    	    synchronized (TraderDetails.fishSellerStockLock){
                    		NodeDetails.traderDetails.fishSellerStock=obj.lookUp(req.prod);
                    	    } break;
                    	default:
                    	    break;
		}
	    }
	    catch (Exception e) {
		System.err.println(NodeDetails.getNode()+":[Trader Deposit] Looking up Database Server "+l+" failed");
		e.printStackTrace();
	    }
	    NodeDetails.traderDetails.isCacheValid.put(req.prod, true);
	    System.out.println(NodeDetails.getNode()+":[Trader Deposit] Retrieved latest data from Database Server for "+req.prod+". Cache valid.");
	}
	else
	{
	    NodeDetails.traderDetails.hitCount++;
	    System.out.println(NodeDetails.getNode()+":[Trader Deposit] Cache valid for "+req.prod);
	}
	NodeDetails.traderDetails.transactionsCount++;
	switch(req.prod){
        	case BOAR: 
        	    synchronized (TraderDetails.boarSellerStockLock){
        		    	NodeDetails.traderDetails.boarSellerStock.add(req);
        		} break;
        	case SALT: 
        	    synchronized (TraderDetails.saltSellerStockLock){
        		    	NodeDetails.traderDetails.saltSellerStock.add(req);
        		} break;
        	case FISH: 
        	    synchronized (TraderDetails.fishSellerStockLock){
        		    	NodeDetails.traderDetails.fishSellerStock.add(req);
        		} break;
        	default:
        	    break;
        }
	StringBuilder lookupName= new StringBuilder("//");
        String l= lookupName.append(NodeDetails.db.ip).append(":").append(NodeDetails.db.port).append("/Database").toString();
        Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
        try {
            DatabaseInterface obj = null;
            obj = (DatabaseInterface)Naming.lookup(l);
            switch(req.prod){
            	case BOAR: 
            	    synchronized (TraderDetails.boarSellerStockLock){
            		obj.updateDB(req.prod,NodeDetails.traderDetails.boarSellerStock,NodeDetails.isTraderNorth);
            	    } break;
            	case SALT: 
            	    synchronized (TraderDetails.saltSellerStockLock){
            		obj.updateDB(req.prod,NodeDetails.traderDetails.saltSellerStock,NodeDetails.isTraderNorth);
            	    } break;
            	case FISH: 
            	    synchronized (TraderDetails.fishSellerStockLock){
            		obj.updateDB(req.prod,NodeDetails.traderDetails.fishSellerStock,NodeDetails.isTraderNorth);
            	    } break;
            	default:
            	    break;
            }
            System.out.println(NodeDetails.getNode()+":[Trader Deposit] Updating Database Server for Deposit of "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
            NodeDetails.traderDetails.isCacheValid.put(req.prod, true);
        }
        catch (Exception e) {
            System.err.println(NodeDetails.getNode()+":[Trader Deposit] Updating Database Server "+l+" failed");
            e.printStackTrace();
        }
        System.out.println(NodeDetails.getNode()+":[Trader Deposit] Cache Hit Rate = "+(NodeDetails.traderDetails.hitCount/(double)NodeDetails.traderDetails.transactionsCount)*100);
	System.out.println(NodeDetails.getNode()+":[Trader Deposit] Deposited "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
    }
    
    //Credit money received from Trader
    public void credit(double amount){
	System.out.println(NodeDetails.getNode()+": Received $"+amount+" from trader\n");
	NodeDetails.money+=amount;
    }
    
    //Information about the trader
    public void traderInfo(ElectionMsg incomingMsg){
  		//set my traderdetails based on the message
  		if (incomingMsg.traderPost)//true=north post
  			NodeDetails.traderNorth = incomingMsg.traderInfo;
  		else
  			NodeDetails.traderSouth = incomingMsg.traderInfo;		
    }
    
    public void heartbeatRequest(Neighbor otherTrader){
    	TraderDetails.heartbeatCount ++;
    	if(!NodeDetails.isWeakTrader || (NodeDetails.isWeakTrader && TraderDetails.heartbeatCount<=Bazaar.MAXHEARTBEAT)){   	    		   	    		
    		StringBuilder lookupName = new StringBuilder("//");
    	    	System.out.println(NodeDetails.getNode()+":[Trader Heartbeat] Heartbeat response to "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port+"\n");
  	  		String l = lookupName.append(otherTrader.ip).append(":").append(otherTrader.port).append("/Node").toString();
  	  		try {
  	  			BazaarInterface obj = (BazaarInterface)Naming.lookup(l);
  	  			obj.heartbeatResponse();
  	  		}
  	  		catch (Exception e) {
  	  			System.err.println(NodeDetails.getNode()+":[Trader Heartbeat] Failed to send heartbeat request to "+l);
  	  			e.printStackTrace();
  	  		}
    	}
    	//else don't respond this is equivalent to no response to heartbeat
    }
    
    public void heartbeatResponse(){
    	TraderDetails.isOtherTraderUp=true;
    }
    
    //If a node gets a call for this, it means it is the new trader and hence save TraderDetails received.
    public void takeTraderDetails(TraderDetails d){
  	    synchronized(this){
  		NodeDetails.traderDetails.boarBuyerRequests.addAll(d.boarBuyerRequests);
  		NodeDetails.traderDetails.fishBuyerRequests.addAll(d.fishBuyerRequests);
  		NodeDetails.traderDetails.saltBuyerRequests.addAll(d.saltBuyerRequests);
  		NodeDetails.traderDetails.hitCount+=d.hitCount;
  		NodeDetails.traderDetails.transactionsCount+=d.transactionsCount;
  	    }
  	    System.out.println(NodeDetails.getNode()+":[Trader Handover] Successfully took over as trader\n");
    }
  
    public void setTraderDown(boolean isNorth){
    	if(isNorth)
    		NodeDetails.traderNorth=null;
    	else
    		NodeDetails.traderSouth=null;
    	
    }
    //If a node gets a call for this, it means it is the old trader and hence send TraderDetails to the new trader.
    public void getTraderDetails(Neighbor n){
		System.out.println(NodeDetails.getNode()+":[Trader Takeover] Handing over as trader to "+n.id+"@"+n.ip+":"+n.port+"\n");
      	  	StringBuilder lookupName = new StringBuilder("//");
      	  	String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
      	  	try {
			BazaarInterface obj = (BazaarInterface)Naming.lookup(l);
			obj.takeTraderDetails(NodeDetails.traderDetails);
      	  	}
      	  	catch (Exception e) {
			System.err.println(NodeDetails.getNode()+":[Trader Takeover]  Failed to send TraderDetails to "+l);
			e.printStackTrace();
      	  	}
    }
    
    //Invalidate cache for a product. Called by database server
    @Override
    public void invalidateCache(Product prod) {
	// TODO Auto-generated method stub
	System.out.println(NodeDetails.getNode()+":[Trader] Cache for "+prod+" invalidated \n");
	NodeDetails.traderDetails.isCacheValid.put(prod, false);
    }
}


