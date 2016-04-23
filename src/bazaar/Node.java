/**
 * 
 */
package bazaar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        }
        catch (Exception e) {
            System.err.println(NodeDetails.getNode()+":[Trader Deposit] Updating Database Server "+l+" failed");
            e.printStackTrace();
        }
	System.out.println(NodeDetails.getNode()+":[Trader Deposit] Deposited "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
	NodeDetails.traderDetails.transactionsCount++;
	
    }
    
    //Credit money received from Trader
    public void credit(double amount){
	System.out.println(NodeDetails.getNode()+": Received $"+amount+" from trader\n");
	NodeDetails.money+=amount;
    }
    
    //Trigger an election
    /*public void startElection(ElectionMsg exclude){
	NodeDetails.isInElection=true;
  	//Create an enquiry Msg
 	ElectionMsg enquiryElectionMsg=new ElectionMsg(ElectionMsgType.ENQUIRY,NodeDetails.getCurrentNode(),exclude.detail);
 	BazaarInterface obj = null;
  		
  	//Send enquiry to all nodes with pid>my pid other than initiator/ex-trader
  	for(Neighbor n : NodeDetails.next){
  		if(n.id != exclude.detail.id && n.id > NodeDetails.id && n.id!= exclude.excludedNode.id){
  			//build lookup name for RMI object based on neighbor's ip & port
  		    	Log.l.log(Log.finer, NodeDetails.getNode()+": Enquiry msg to "+n.id+"@"+n.ip+":"+n.port);
  	      		StringBuilder lookupName= new StringBuilder("//");
  	      		String l= lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
  	      		Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
  	      		try {
  	    			obj = (BazaarInterface)Naming.lookup(l);
  	    			obj.election(enquiryElectionMsg);
  	      		}
  	      		catch (Exception e) {
  	      		    	System.err.println(NodeDetails.getNode()+": Failed to start election on "+l);
  	    			e.printStackTrace();
  	      		}
  		}
  	}
  	
  	//wait for TIMEOUT and collect responses
  	try{
  	    	Thread.sleep(Bazaar.TIMEOUT);
  	}
  	catch(Exception e) {
  	    	e.printStackTrace();
  	}
  	
  	//If I'm still in the election i.e after time out no replies => I'm the highest & so the new leader
 	if(NodeDetails.isInElection){
 	    	System.out.println(NodeDetails.getNode()+":[Trader Election] Won the Election");
  		//get all details required to be the trader
  		NodeDetails.takeOverAsTrader();			
 		
  		//send victory message to all nodes except self
  		ElectionMsg victoryMsg = new ElectionMsg(ElectionMsgType.VICTORY,NodeDetails.getCurrentNode(),null);
  		for(Neighbor n : NodeDetails.next ){
 			if(n.id!=NodeDetails.id){
 			    	//build lookup name for RMI object based on neighbor's ip & port
  	    		    	Log.l.log(Log.finer, NodeDetails.getNode()+": Victory msg to "+n.id+"@"+n.ip+":"+n.port);
  		      		StringBuilder lookupName = new StringBuilder("//");
  		      		String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
  		      		try {
  		    			obj = (BazaarInterface)Naming.lookup(l);
  		    			obj.election(victoryMsg);
  		      		}
  		      		catch (Exception e) {
  	    				System.err.println(NodeDetails.getNode()+":[Trader Election] Failed to send Victory Message to "+l);
  		    			e.printStackTrace();
  		      		}
      			}
  		}
  	}
    }*/
    
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
  	  		String l = lookupName.append(otherTrader.ip).append(":").append(otherTrader.port).append("/Node").toString();
  	  		try {
  	  			BazaarInterface obj = (BazaarInterface)Naming.lookup(l);
  	  			obj.heartbeatResponse();
  	  		}
  	  		catch (Exception e) {
  	  			System.err.println(NodeDetails.getNode()+": Failed to send TraderDetails to "+l);
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
  	    NodeDetails.isTrader=true;
  	    try {
  		NodeDetails.traderDetails= new TraderDetails();
	    } catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
  	    NodeDetails.traderDetails=d;
  	    NodeDetails.traderDetails.transactionsCount=0;
  	    System.out.println(NodeDetails.getNode()+":[Trader Election] Sucessfully took over as trader from ex-Trader\n");
    }
  
    public void setTraderDown(boolean isNorth){
    	if(isNorth)
    		NodeDetails.traderNorth=null;
    	else
    		NodeDetails.traderSouth=null;
    	
    }
    //If a node gets a call for this, it means it is the old trader and hence write to file and send TraderDetails to the new trader.
    public void getTraderDetails(Neighbor n){
		System.out.println(NodeDetails.getNode()+":[Trader Election] Handing over as trader to "+n.id+"@"+n.ip+":"+n.port+"\n");
      	  	File f = new File("TraderDetails.json");
      	  	try {
      	  	    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
      	  	    bw.write(NodeDetails.traderDetails.toString());
      	  	    bw.flush();
      	  	    bw.close();
      	  	} catch (IOException e) {
      	  	    // TODO Auto-generated catch block
      	  	    e.printStackTrace();
      	  	}
      	  	StringBuilder lookupName = new StringBuilder("//");
      	  	String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
      	  	try {
			BazaarInterface obj = (BazaarInterface)Naming.lookup(l);
			obj.takeTraderDetails(NodeDetails.traderDetails);
      	  	}
      	  	catch (Exception e) {
			System.err.println(NodeDetails.getNode()+": Failed to send TraderDetails to "+l);
			e.printStackTrace();
      	  	}
    }

    @Override
    public void invalidateCache(Product prod) {
	// TODO Auto-generated method stub
	NodeDetails.traderDetails.isCacheValid.put(prod, false);
    }
}


