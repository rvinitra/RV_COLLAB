package bazaar;

import java.io.Serializable;

public class SellerDetails implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = -8043887092830481325L;
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
