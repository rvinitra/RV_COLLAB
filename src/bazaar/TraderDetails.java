package bazaar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TraderDetails {
	HashMap<Product,List<SellerDetails>> stock;
	Queue<Request> pendingDeposit;
	Queue<Request> pendingBuy;
	int transactionsCount;
	
	public TraderDetails() {
	    stock = new HashMap<Product, List<SellerDetails>>();
	    pendingDeposit = new LinkedList<Request>();
	    pendingBuy = new LinkedList<Request>();
	}
	
	

}

