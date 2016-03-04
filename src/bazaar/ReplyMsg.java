package bazaar;

import java.io.Serializable;
import java.util.Stack;

public class ReplyMsg implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
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
