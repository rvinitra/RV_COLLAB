package bazaar;

public class SellerDetails {
	Neighbor seller;
	int count;
	public SellerDetails() {
		// TODO Auto-generated constructor stub
	    seller = null;
	    count=0;
	}
	public SellerDetails(Neighbor seller, int count){
	    this.seller=seller;
	    this.count = count;
	}

}
