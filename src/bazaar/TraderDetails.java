package bazaar;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.PriorityQueue;

public class TraderDetails implements Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = -8717834062370042840L;
	PriorityQueue<RequestMsg> boarSellerStock;
	PriorityQueue<RequestMsg> fishSellerStock;
	PriorityQueue<RequestMsg> saltSellerStock;
	PriorityQueue<RequestMsg> boarBuyerRequests;
	PriorityQueue<RequestMsg> fishBuyerRequests;
	PriorityQueue<RequestMsg> saltBuyerRequests;
	int transactionsCount;
	
	public TraderDetails() throws RemoteException{
	    	boarSellerStock = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
	    	fishSellerStock = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
        	saltSellerStock = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
    		boarBuyerRequests = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
    		fishBuyerRequests = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
    		saltBuyerRequests = new PriorityQueue<RequestMsg>(10, new TimestampComparator());
    		transactionsCount=0;
	}
}

