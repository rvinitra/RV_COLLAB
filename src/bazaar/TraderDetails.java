package bazaar;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class TraderDetails implements Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = -8717834062370042840L;
	PriorityQueue<Request> boarSellerStock;
	PriorityQueue<Request> fishSellerStock;
	PriorityQueue<Request> saltSellerStock;
	PriorityQueue<Request> boarBuyerRequests;
	PriorityQueue<Request> fishBuyerRequests;
	PriorityQueue<Request> saltBuyerRequests;
	int transactionsCount;
	
	public TraderDetails() throws RemoteException{
	    	boarSellerStock = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
	    	fishSellerStock = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
        	saltSellerStock = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
    		boarBuyerRequests = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
    		fishBuyerRequests = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
    		saltBuyerRequests = new PriorityQueue<Request>(10, new Comparator<Request>() {
	        
        	    @Override
        	    public int compare(Request o1, Request o2) {
        		// TODO Auto-generated method stub
        		if (o1.timestamp < o2.timestamp)
        		    return -1;
        		else if (o1.timestamp > o2.timestamp)
        		    return 1;
        		return 0;
        	    }
    	    
    		});
    		transactionsCount=0;
	}
	public TraderDetails(TraderDetails traderDetails) {
	    this.boarBuyerRequests = traderDetails.boarBuyerRequests;
	    this.saltBuyerRequests = traderDetails.saltBuyerRequests;
	    this.fishBuyerRequests = traderDetails.fishBuyerRequests;
	    this.boarSellerStock = traderDetails.boarSellerStock;
	    this.saltSellerStock = traderDetails.saltSellerStock;
	    this.fishSellerStock = traderDetails.fishSellerStock;
	    // TODO Auto-generated constructor stub
	}
	public static void processBuyQueue(){
		while (NodeDetails.isTrader){
		    Request reqBoar=null, reqFish=null, reqSalt=null;
		    synchronized (NodeDetails.boarBuyerRequestsLock){
	    	    	reqBoar=NodeDetails.traderDetails.boarBuyerRequests.poll();
		    }
		    synchronized (NodeDetails.saltBuyerRequestsLock){
	    		reqSalt=NodeDetails.traderDetails.saltBuyerRequests.poll();
	    	    }
		    synchronized (NodeDetails.fishBuyerRequestsLock){
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

