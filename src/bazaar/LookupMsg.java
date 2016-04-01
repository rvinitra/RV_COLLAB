package bazaar;

import java.io.Serializable;
import java.util.Stack;

public class LookupMsg implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public Product prod;
	public int hopcount;
	public Stack<Neighbor> path;
	public LookupMsg() {
	    hopcount=10;
	}
	
	public LookupMsg(Product prod,int hopcount,Stack<Neighbor> path){
	    this.prod=prod;
	    this.hopcount=hopcount;
	    this.path=path;
	}
}
