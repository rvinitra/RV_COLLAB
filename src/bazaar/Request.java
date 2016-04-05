package bazaar;

import java.io.Serializable;

public class Request implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 3018364016008724818L;
    Neighbor requestingNode;
    Product prod;
    int count;
    double money;
    int timestamp;
}
