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
        	    synchronized (NodeDetails.boarBuyerRequestsLock){
    		    	NodeDetails.traderDetails.boarBuyerRequests.add(req);
    		} break;
        	case SALT: 
        	    synchronized (NodeDetails.saltBuyerRequestsLock){
    		    	NodeDetails.traderDetails.saltBuyerRequests.add(req);
    		} break;
        	case FISH: 
        	    synchronized (NodeDetails.fishBuyerRequestsLock){
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
        	    synchronized (NodeDetails.boarSellerStockLock){
        		    	NodeDetails.traderDetails.boarSellerStock.add(req);
        		} break;
        	case SALT: 
        	    synchronized (NodeDetails.saltSellerStockLock){
        		    	NodeDetails.traderDetails.saltSellerStock.add(req);
        		} break;
        	case FISH: 
        	    synchronized (NodeDetails.fishSellerStockLock){
        		    	NodeDetails.traderDetails.fishSellerStock.add(req);
        		} break;
        	default:
        	    break;
        }
	System.out.println(NodeDetails.getNode()+":[Trader Deposit] Deposited "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
	NodeDetails.traderDetails.transactionsCount++;
	
	//If Trader has processed more than 5 transactions, reset transaction count, resign as trader and start election on random node 
	if (NodeDetails.traderDetails.transactionsCount >= 20 && NodeDetails.isTrader){
    	    StringBuilder lookupName= new StringBuilder("//");
    	    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
    	    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
    	    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
    	    NodeDetails.traderDetails.transactionsCount=0;
    	    System.out.println(NodeDetails.getNode()+":[Trader Election] Transaction limit of 20 reached. Resigning as trader and triggering "+randomNode.id+"@"+randomNode.ip+":"+randomNode.port+" to start election");
    	    try {
    		BazaarInterface obj = null;
    		obj = (BazaarInterface)Naming.lookup(l);
    		ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode(),NodeDetails.getCurrentNode());
    		obj.startElection(exclude);
    	    }
    	    catch (Exception e) {
    		System.err.println(NodeDetails.getNode()+":[Trader Resign] Triggering "+l+" to start election failed");
    		e.printStackTrace();
    	    }
	}
    }
    
    //Credit money received from Trader
    public void credit(double amount){
	System.out.println(NodeDetails.getNode()+": Received $"+amount+" from trader\n");
	NodeDetails.money+=amount;
    }
    
    //Trigger an election
    public void startElection(ElectionMsg exclude){
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
    }
    
    //Process an election message based on if it's a VICTORY, ENQUIRY or ALIVE message
    public void election(ElectionMsg incomingElectionMsg){
  		BazaarInterface obj = null;
  		if(incomingElectionMsg.type == ElectionMsgType.VICTORY){
  			System.out.println(NodeDetails.getNode()+": New Trader is "+ incomingElectionMsg.detail.id+"@"+incomingElectionMsg.detail.ip+":"+incomingElectionMsg.detail.port);
  			NodeDetails.updateTrader(incomingElectionMsg.detail);
  		}
  		else if(incomingElectionMsg.type == ElectionMsgType.ENQUIRY){
  			Neighbor n = incomingElectionMsg.detail; 
  			ElectionMsg aliveMsg = new ElectionMsg(ElectionMsgType.ALIVE,NodeDetails.getCurrentNode(),null);
  			
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
        		startElection(new ElectionMsg(ElectionMsgType.EXCLUDE,NodeDetails.getCurrentNode(),incomingElectionMsg.excludedNode));
  		}
  		else if(incomingElectionMsg.type == ElectionMsgType.ALIVE){
  			//If received alive means an higher pid process exists
  			NodeDetails.isInElection=false;
  		}
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
  	
    //Synchronize the clock
    public void clockSync(int remoteLamportClock){
	int diff=(remoteLamportClock - NodeDetails.lamportClock);
	//if incoming broadcast time strictly greater than local time, adjust to incoming+1
	if(diff > 0){
		NodeDetails.incrementClock(diff+1);  			
	}
		
    }
}


