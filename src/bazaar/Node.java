/**
 * 
 */
package bazaar;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    public boolean buy(Request req){
	if (NodeDetails.isTrader){
	    List<SellerDetails> potentialSellers;
	    if (NodeDetails.traderDetails.stock.containsKey(req.prod)){
		potentialSellers = NodeDetails.traderDetails.stock.get(req.prod);
		Queue<SellerDetails> sellers = new LinkedList<SellerDetails>();
		SellerDetails seller;
		int count = req.count;
		try{
		    seller = potentialSellers.get(0);
		}
		catch(IndexOutOfBoundsException e){
		    Log.l.log(Log.finer, "No potential sellers");
		    System.out.println(NodeDetails.getNode()+": No sellers for "+req.prod+"X"+req.count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
		    return false;
		}
		try{
		    while(req.count >= seller.count){
			req.count-=seller.count;
			sellers.add(seller);
			potentialSellers.remove(0);
			seller = potentialSellers.get(0);
		    }
		}
		catch(IndexOutOfBoundsException e){
		    Log.l.log(Log.finer, "Partial sale made");
		    System.out.println(NodeDetails.getNode()+": Sold "+(count-req.count)+" out of "+count+" "+req.prod+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
		    NodeDetails.traderDetails.transactionsCount++;
		    if (NodeDetails.traderDetails.transactionsCount >= 5){
			    //start election
			    StringBuilder lookupName= new StringBuilder("//");
			    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
			    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
			    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
			    try {
				BazaarInterface obj = null;
				obj = (BazaarInterface)Naming.lookup(l);
				//	create a proper lookupmsg & send 
				ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode());
				obj.startElection(exclude);
			    }
			    catch (Exception ex) {
				System.err.println(NodeDetails.getNode()+": Lookup failed to "+l);
				ex.printStackTrace();
			    }
		    }
		    return true;
		}
		SellerDetails partialSeller = new SellerDetails();
		partialSeller.seller = seller.seller;
		partialSeller.count = req.count;
		sellers.add(partialSeller);
		seller.count-=req.count;
		potentialSellers.remove(0);
		potentialSellers.add(0, seller);
		System.out.println(NodeDetails.getNode()+": Sold "+req.prod+"X"+req.count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
		while(!sellers.isEmpty()){
		    SellerDetails creditseller = sellers.peek();
		    if (seller!=null){
        		    StringBuilder lookupName= new StringBuilder("//");
        		    String l= lookupName.append(creditseller.seller.ip).append(":").append(creditseller.seller.port).append("/Node").toString();
        		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
        		    try {
        			BazaarInterface obj = null;
        			obj = (BazaarInterface)Naming.lookup(l);
        			//	create a proper lookupmsg & send 
        			obj.credit(NodeDetails.getCreditAmount(req.prod));
        		    }
        		    catch (Exception e) {
        			System.err.println(NodeDetails.getNode()+": Lookup failed to "+l);
        			e.printStackTrace();
        		    }
		    }
		    //credit money to sellers
		}
		NodeDetails.traderDetails.transactionsCount++;
		if (NodeDetails.traderDetails.transactionsCount >= 5){
		    //start election
		    StringBuilder lookupName= new StringBuilder("//");
		    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
		    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
		    try {
			BazaarInterface obj = null;
			obj = (BazaarInterface)Naming.lookup(l);
			//	create a proper lookupmsg & send 
			ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode());
			obj.startElection(exclude);
		    }
		    catch (Exception e) {
			System.err.println(NodeDetails.getNode()+": Lookup failed to "+l);
			e.printStackTrace();
		    }
		}
		return true;
	    }
	    else{
		System.out.println(NodeDetails.getNode()+": No sellers for "+req.prod+"X"+req.count);
		return false;
	    }   
	}
	else{
	    System.out.println(NodeDetails.getNode()+": Queueing buy request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+" as trader is not available");
	    if (NodeDetails.traderDetails.pendingBuy.isEmpty()){
		NodeDetails.traderDetails.pendingBuy = new LinkedList<Request>();
	    }
	    NodeDetails.traderDetails.pendingBuy.add(req);
	    return false;
	    
	}
    }
    
    //Method for trader to update his inventory for goods deposited
    public boolean deposit(Request req)
	    throws RemoteException {
	//check if current node is still the trader
	if (NodeDetails.isTrader){
	    List<SellerDetails> sellers;
	    if (NodeDetails.traderDetails.stock.containsKey(req.prod)){
		sellers = NodeDetails.traderDetails.stock.get(req.prod);
	    }
	    else{
		sellers = new LinkedList<SellerDetails>();
	    }
	    SellerDetails seller = new SellerDetails(req.requestingNode,req.count);
	  //add in order here!!! 
	    sellers.add(seller);
	    NodeDetails.traderDetails.stock.put(req.prod, sellers);
	    System.out.println(NodeDetails.getNode()+": Deposited "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
	    NodeDetails.traderDetails.transactionsCount++;
	    if (NodeDetails.traderDetails.transactionsCount >= 5){
		    //start election
		    StringBuilder lookupName= new StringBuilder("//");
		    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
		    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
		    try {
			BazaarInterface obj = null;
			obj = (BazaarInterface)Naming.lookup(l);
			//	create a proper lookupmsg & send 
			ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode());
			obj.startElection(exclude);
		    }
		    catch (Exception e) {
			System.err.println(NodeDetails.getNode()+": Lookup failed to "+l);
			e.printStackTrace();
		    }
	    }
	    return true;
	}
	else{
	    System.out.println(NodeDetails.getNode()+": Queueing deposit request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+" as trader is not available");
	    if (NodeDetails.traderDetails.pendingDeposit.isEmpty()){
		NodeDetails.traderDetails.pendingDeposit = new LinkedList<Request>();
	    }
	    NodeDetails.traderDetails.pendingDeposit.add(req);
	    return false;
	}
    }
    
    public void credit(double amount){
	System.out.println(NodeDetails.getNode()+": Received $"+amount+" from "+NodeDetails.trader.id+"@"+NodeDetails.trader.ip+":"+NodeDetails.port);
	NodeDetails.money+=amount;
    }
  //trigger an election
  	public void startElection(ElectionMsg exclude){
  	    	System.out.println(NodeDetails.getNode()+": Starting election");
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
  	    			System.err.println(NodeDetails.getNode()+":Election failed to "+l);
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
  		    			System.err.println(NodeDetails.getNode()+":Election failed to "+l);
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
      			System.err.println(NodeDetails.getNode()+":Election failed to "+l);
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
  	public String getTraderDetails(){
  		return("MsgFromEx:"+NodeDetails.id);
  	}
  	
  	public void clockSync(int remoteLamportClock){
		int diff=(remoteLamportClock - NodeDetails.lamportClock);
		//if incoming broadcast time strictly greater than local time, adjust to incoming+1
		if(diff > 0){
			NodeDetails.incrementClock(diff+1);  			
		}
		
	}
}


