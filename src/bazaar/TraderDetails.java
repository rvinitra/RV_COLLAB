package bazaar;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;

public class TraderDetails implements Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = -8717834062370042840L;
	Queue<RequestMsg> boarSellerStock;
	Queue<RequestMsg> fishSellerStock;
	Queue<RequestMsg> saltSellerStock;
	Queue<RequestMsg> boarBuyerRequests;
	Queue<RequestMsg> fishBuyerRequests;
	Queue<RequestMsg> saltBuyerRequests;
	int transactionsCount;
	static final Object boarSellerStockLock = new Object();
	static final Object boarBuyerRequestsLock = new Object();
	static final Object fishSellerStockLock = new Object();
	static final Object fishBuyerRequestsLock = new Object();
	static final Object saltSellerStockLock = new Object();
	static final Object saltBuyerRequestsLock = new Object();
	static int heartbeatCount = 0;
	static boolean isOtherTraderUp = false;
	
	public TraderDetails() throws RemoteException{
	    	boarSellerStock = new LinkedList<RequestMsg>();
	    	fishSellerStock = new LinkedList<RequestMsg>();
        	saltSellerStock = new LinkedList<RequestMsg>();
    		boarBuyerRequests = new LinkedList<RequestMsg>();
    		fishBuyerRequests = new LinkedList<RequestMsg>();
    		saltBuyerRequests = new LinkedList<RequestMsg>();
    		transactionsCount=0;
	}
	
	public String toString(){
	    StringBuilder st = new StringBuilder();
	    st.append("TraderDetails: { boarSellerStock: ").append(boarSellerStock.toString()).append(", fishSellerStock: ").append(fishSellerStock.toString()).append(", saltSellerStock: ").append(saltSellerStock.toString());
	    st.append(", boarBuyerRequests: ").append(boarBuyerRequests.toString()).append(", fishBuyerRequests: ").append(fishBuyerRequests.toString()).append(", saltBuyerRequests: ").append(saltBuyerRequests.toString()).append("}");
	    return st.toString();
	}
}

