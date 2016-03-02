package bazaar;

import java.util.Stack;

public class LookupMsg{
	public Product prod;
	public int hopcount;
	public Stack<Neighbor> path;
	public LookupMsg() {
		
	}
	
	public LookupMsg(Product prod,int hopcount,Stack<Neighbor> path)
	{
		this.prod=prod;
		this.hopcount=hopcount;
		this.path=path;
	}
}
