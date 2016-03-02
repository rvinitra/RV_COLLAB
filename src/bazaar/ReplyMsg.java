package bazaar;

import java.util.Stack;

public class ReplyMsg {
    Neighbor seller;
    public Stack<Neighbor> path;
    public ReplyMsg(){
	
    }
	
    public ReplyMsg(Neighbor seller,Stack<Neighbor> path)
    {
	this.seller=seller;
	this.path=path;
    }

}
