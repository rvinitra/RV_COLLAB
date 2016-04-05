package bazaar;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class TraderDetails {
	HashMap<Product,List<SellerDetails>> stock;
	Queue<Request> pendingDeposit;
	Queue<Request> pendingBuy;
	int transactionsCount;
	
	public TraderDetails() {
		
		
	}
	
	

}

