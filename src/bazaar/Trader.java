package bazaar;

//import java.io.Serializable;
import java.rmi.Naming;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Trader implements Runnable{
    /**
     * 
     */
    //private static final long serialVersionUID = -8583699888820980147L;
    Request req;
    public Trader() {
	// TODO Auto-generated constructor stub
    }
    public Trader(Request req) {
	// TODO Auto-generated constructor stub
	this.req=req;
    }

    @Override
    public void run() {
	// TODO Auto-generated method stub
	System.out.println(NodeDetails.getNode()+":[Trader] Processing buy request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+" from the Queue");
	if (req!=null){
	    Request sellerRequest = null;
	    PriorityQueue<Request> potentialSellers = null;
    	    switch(req.prod){
        	case BOAR: 
        	    synchronized (NodeDetails.boarSellerStockLock){
        		potentialSellers=NodeDetails.traderDetails.boarSellerStock;
    		} break;
        	case SALT: 
        	    synchronized (NodeDetails.saltSellerStockLock){
    		    	potentialSellers=NodeDetails.traderDetails.saltSellerStock;
    		} break;
        	case FISH: 
        	    synchronized (NodeDetails.fishSellerStockLock){
    		    	potentialSellers=NodeDetails.traderDetails.fishSellerStock;
    		} break;
        	default:
        	    break;
    	    }
    	    Queue<SellerDetails> sellers = new LinkedList<SellerDetails>();
    	    SellerDetails seller = null;
    	    int count = req.count;
    	    if (potentialSellers.size()==0){
    		Log.l.log(Log.finer, "No potential sellers");
    		System.out.println(NodeDetails.getNode()+":[Trader] No sellers for "+req.prod+"X"+req.count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
    		return;
    	    }
    	    switch(req.prod){
        	case BOAR: 
        	    synchronized (NodeDetails.boarSellerStockLock){
        		sellerRequest = NodeDetails.traderDetails.boarSellerStock.poll();
        		} break;
        	case SALT: 
        	    synchronized (NodeDetails.saltSellerStockLock){
        		sellerRequest = NodeDetails.traderDetails.saltSellerStock.poll();
        		} break;
        	case FISH: 
        	    synchronized (NodeDetails.fishSellerStockLock){
        		sellerRequest = NodeDetails.traderDetails.fishSellerStock.poll();
        		} break;
        	default:
        	    break;
    	    }
    	    while (sellerRequest!=null){
    		seller = new SellerDetails(sellerRequest.requestingNode,sellerRequest.count);
        	if(req.count >= seller.count){
       			req.count-=seller.count;
       			sellers.add(seller);
           	    	switch(req.prod){
                        	case BOAR: 
                        	    synchronized (NodeDetails.boarSellerStockLock){
                        		sellerRequest = NodeDetails.traderDetails.boarSellerStock.poll();
                        		} break;
                        	case SALT: 
                        	    synchronized (NodeDetails.saltSellerStockLock){
                        		sellerRequest = NodeDetails.traderDetails.saltSellerStock.poll();
                        		} break;
                        	case FISH: 
                        	    synchronized (NodeDetails.fishSellerStockLock){
                        		sellerRequest = NodeDetails.traderDetails.fishSellerStock.poll();
                        		} break;
                        	default:
                        	    break;
                    	}
       		    }
       		    else{
       			break;
       		    }
    	    }
    	    if (sellerRequest==null){
		    Log.l.log(Log.finer, "Partial sale made");
		    System.out.println(NodeDetails.getNode()+":[Trader] Sold "+(count-req.count)+" out of "+count+" "+req.prod+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
		    NodeDetails.traderDetails.transactionsCount++;
		    if (NodeDetails.traderDetails.transactionsCount >= 5 && NodeDetails.isTrader){
			    //start election
			    StringBuilder lookupName= new StringBuilder("//");
			    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
			    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
			    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
			    NodeDetails.traderDetails.transactionsCount=0;
			    System.out.println(NodeDetails.getNode()+":[Trader] Transaction limit of 5 reached. Resigning as trader and triggering "+randomNode.id+"@"+randomNode.ip+":"+randomNode.port+" to start election");
			    try {
				BazaarInterface obj = null;
				obj = (BazaarInterface)Naming.lookup(l);
				//	create a proper lookupmsg & send 
				ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode(), NodeDetails.getCurrentNode());
				obj.startElection(exclude);
			    }
			    catch (Exception ex) {
				System.err.println(NodeDetails.getNode()+":[Trader] Triggering "+l+" to start election failed");
				ex.printStackTrace();
			    }
		    }
		    return;
		}
    	    	if (req.count!=0){
        		SellerDetails partialSeller = new SellerDetails();
        		partialSeller.seller = seller.seller;
        		partialSeller.count = req.count;
        		sellers.add(partialSeller);
        		sellerRequest.count-=req.count;
        		switch(req.prod){
                        	case BOAR: 
                        	    synchronized (NodeDetails.boarSellerStockLock){
                        		NodeDetails.traderDetails.boarSellerStock.add(sellerRequest);
                        		} break;
                        	case SALT: 
                        	    synchronized (NodeDetails.saltSellerStockLock){
                        		NodeDetails.traderDetails.saltSellerStock.add(sellerRequest);
                        		} break;
                        	case FISH: 
                        	    synchronized (NodeDetails.fishSellerStockLock){
                        		NodeDetails.traderDetails.fishSellerStock.add(sellerRequest);
                        		} break;
                        	default:
                        	    break;
        		}
    	    	}
		System.out.println(NodeDetails.getNode()+":[Trader] Sold "+req.prod+"X"+count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
		while(!sellers.isEmpty()){
		    SellerDetails creditseller = sellers.poll();
		    if (seller!=null){
        		    StringBuilder lookupName= new StringBuilder("//");
        		    String l= lookupName.append(creditseller.seller.ip).append(":").append(creditseller.seller.port).append("/Node").toString();
        		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
        		    try {
        			BazaarInterface obj = null;
        			obj = (BazaarInterface)Naming.lookup(l);
        			//	create a proper lookupmsg & send 
        			obj.credit(NodeDetails.getCreditAmount(req.prod));
        			System.out.println(NodeDetails.getNode()+":[Trader] Crediting $"+NodeDetails.getCreditAmount(req.prod)+" for "+req.prod+" to "+creditseller.seller.id+"@"+creditseller.seller.ip+":"+creditseller.seller.port);
        		    }
        		    catch (Exception e) {
        			System.err.println(NodeDetails.getNode()+":[Trader] Crediting to "+l+" failed");
        			e.printStackTrace();
        		    }
		    }
		    //credit money to sellers
		}
		NodeDetails.traderDetails.transactionsCount++;
		if (NodeDetails.traderDetails.transactionsCount >= 5 && NodeDetails.isTrader){
		    //start election
		    StringBuilder lookupName= new StringBuilder("//");
		    Neighbor randomNode = NodeDetails.selectRandomNeighbor();
		    String l= lookupName.append(randomNode.ip).append(":").append(randomNode.port).append("/Node").toString();
		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
		    NodeDetails.traderDetails.transactionsCount=0;
		    System.out.println(NodeDetails.getNode()+":[Trader] Transaction limit of 5 reached. Resigning as trader and triggering "+randomNode.id+"@"+randomNode.ip+":"+randomNode.port+" to start election");
		    try {
			BazaarInterface obj = null;
			obj = (BazaarInterface)Naming.lookup(l);
			//	create a proper lookupmsg & send 
			ElectionMsg exclude=new ElectionMsg(ElectionMsgType.EXCLUDE, NodeDetails.getCurrentNode(),NodeDetails.getCurrentNode());
			obj.startElection(exclude);
		    }
		    catch (Exception e) {
			System.err.println(NodeDetails.getNode()+":[Trader] Triggering "+l+" to start election failed");
			e.printStackTrace();
		    }
		}
		return;
	    }
	    else{
		System.out.println(NodeDetails.getNode()+":[Trader] No sellers for "+req.prod+"X"+req.count);
		return;
	    }
	
    }

}
