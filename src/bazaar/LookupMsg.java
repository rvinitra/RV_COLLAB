package bazaar;

import java.io.Serializable;
import java.util.Stack;

public class LookupMsg implements Serializable {
	public Product prod;
	public int hopcount;
	public Stack<Node> path;
	public LookupMsg() {
		
	}
	
	public LookupMsg(Product prod,int hopcount,Stack<Node> path)
	{
		this.prod=prod;
		this.hopcount=hopcount;
		this.path=path;
	}
}
