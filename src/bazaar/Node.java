/**
 * 
 */
package bazaar;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author rvinitra, rshenoy
 *
 */
public class Node extends UnicastRemoteObject implements BazaarInterface{
    private static final long serialVersionUID = 1L;
    public Node() throws RemoteException {
	super(0);
    }
    
    //Method for trader to sell product to buyer and credit money to corresponding sellers
    public void buy(Request req){
        System.out.println(NodeDetails.getNode()+":[Trader] Queueing buy request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
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
    public void deposit(Request req) throws RemoteException {
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
	System.out.println(NodeDetails.getNode()+":[Trader] Deposited "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
	NodeDetails.traderDetails.transactionsCount++;
	if (NodeDetails.traderDetails.transactionsCount >= 5 && NodeDetails.isTrader){
	    //start election
	    StringBuilder lookupName= new StringBuilder("//");
	    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
	    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
	    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
	    NodeDetails.isTrader=false;
	    System.out.println(NodeDetails.getNode()+":[Trader] Transaction limit of 5 reached. Resigning as trader and triggering "+randomNode.id+"@"+randomNode.ip+":"+randomNode.port+" to start election");
	    try {
		BazaarInterface obj = null;
		obj = (BazaarInterface)Naming.lookup(l);
		ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode());
		obj.startElection(exclude);
	    }
	    catch (Exception e) {
		System.err.println(NodeDetails.getNode()+":[Trader] Triggering "+l+" to start election failed");
		e.printStackTrace();
	    }
	}
    }
    
    public void credit(double amount){
	System.out.println(NodeDetails.getNode()+": Received $"+amount+" from trader");
	NodeDetails.money+=amount;
    }
  //trigger an election
  	public void startElection(ElectionMsg exclude){
  		NodeDetails.isInElection=true;
  		//Create an enquiry Msg
  		ElectionMsg enquiryElectionMsg=new ElectionMsg(ElectionMsgType.ENQUIRY,NodeDetails.getCurrentNode());
  		BazaarInterface obj = null;
  		
  		//Send enquiry to all nodes with pid>my pid other than initiator/ex-trader
  		for(Neighbor n : NodeDetails.next){
  			if(n.id != exclude.detail.id && n.id > NodeDetails.id){
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
  			
  			//get all details required to be the trader
  			NodeDetails.takeOverAsTrader();			
  				
  			ElectionMsg victoryMsg = new ElectionMsg(ElectionMsgType.VICTORY,NodeDetails.getCurrentNode());
  			System.out.println(NodeDetails.getNode()+":[Trader] Won the Election");
  			//send victory message to all nodes except self
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
  	    				System.err.println(NodeDetails.getNode()+":[Trader] Failed to send Victory Message to "+l);
  		    			e.printStackTrace();
  	    		    }
      			}
  			}
  		}
  	}
  	
  	public void election(ElectionMsg incomingElectionMsg){
  		BazaarInterface obj = null;
  		if(incomingElectionMsg.type == ElectionMsgType.VICTORY){
  			System.out.println(NodeDetails.getNode()+": New Trader is "+ incomingElectionMsg.detail.id+"@"+incomingElectionMsg.detail.ip+":"+incomingElectionMsg.detail.port);
  			NodeDetails.updateTrader(incomingElectionMsg.detail);
  		}
  		else if(incomingElectionMsg.type == ElectionMsgType.ENQUIRY){
  			Neighbor n = incomingElectionMsg.detail; 
  			ElectionMsg aliveMsg = new ElectionMsg(ElectionMsgType.ALIVE,NodeDetails.getCurrentNode());
  			
  		    //build lookup name for RMI object based on sender's ip & port
  		    Log.l.log(Log.finer, NodeDetails.getNode()+": Alive msg to "+n.id+"@"+n.ip+":"+n.port);
        		StringBuilder lookupName = new StringBuilder("//");
  		    String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
  		    try {
      			obj = (BazaarInterface)Naming.lookup(l);
      			obj.election(aliveMsg);
  		    }
  		    catch (Exception e) {
  			System.err.println(NodeDetails.getNode()+": Failed to send Alive msg to "+l);
      			e.printStackTrace();
  		    }
  			//start a new election
  		    startElection(new ElectionMsg(ElectionMsgType.EXCLUDE,NodeDetails.getCurrentNode()));
  		}
  		else if(incomingElectionMsg.type == ElectionMsgType.ALIVE){
  			//If received alive means an higher pid process exists
  			NodeDetails.isInElection=false;
  		}
  	}
  	//If a node gets a call for this - it means it is the ex-Trader
  	public TraderDetails getTraderDetails(){
  	    	System.out.println(NodeDetails.getNode()+": Handing over as trader");
  		return(NodeDetails.traderDetails);
  	}
  	
  	public void clockSync(int remoteLamportClock){
		int diff=(remoteLamportClock - NodeDetails.lamportClock);
		//if incoming broadcast time strictly greater than local time, adjust to incoming+1
		if(diff > 0){
			NodeDetails.incrementClock(diff+1);  			
		}
		
	}
}


