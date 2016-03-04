/**
 * 
 */
package bazaar;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;

/**
 * @author rvinitra, rshenoy
 *
 */
public class Node extends UnicastRemoteObject implements BazaarInterface{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	public Node() throws RemoteException {
		 super(0);
	}
	
	public void lookUp(LookupMsg incoming){
		System.out.println("Looking up Peer " + NodeDetails.id + " @ "+ NodeDetails.ip+":"+NodeDetails.port);
		if(!NodeDetails.isBuyer && NodeDetails.prod==incoming.prod && NodeDetails.count>0){
			System.out.println("I am a seller. I have the product you asked for. Quantity:" + NodeDetails.count);
			System.out.println("Hop Count I see:"+ incoming.hopcount);
			Neighbor n = incoming.path.pop();
			Neighbor seller = new Neighbor();
			seller.CurrentNode();
			ReplyMsg reply = new ReplyMsg(seller,incoming.path);
			try {
			    BazaarInterface obj = (BazaarInterface)Naming.lookup("//"+n.ip+":"+n.port+"/Node");
			    obj.reply(reply);
			} catch (MalformedURLException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (RemoteException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (NotBoundException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		        //
			//Rmi reply with seller details
		}
		else if(incoming.hopcount>0){
				//create a new stack to read incoming path so far
				Stack<Neighbor> newPathStack= incoming.path;
				//add current nodes details into reverse path
				Neighbor thisnode= new Neighbor();
				thisnode.CurrentNode();
				newPathStack.push(thisnode);
				//construct outgoing message down to my neighbors
				LookupMsg outgoingLookupMsg=new LookupMsg(incoming.prod,incoming.hopcount-1,newPathStack);
			
        		System.out.println("I dont have"+ incoming.prod);
        		System.out.println("Passing it on to my neighbour.My neighbors are:");
        		
        		//send the outgoing message to each neighbor I have
        		for(Neighbor n : NodeDetails.next ){
        			//build lookup name for RMI object based on neighbor's ip & port
        			System.out.println("Neighbor id:"+n.id);
        			StringBuilder lookupName= new StringBuilder("//");
        			String l= lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
        			System.out.println("Lookup string:" + l);
        			//create RMI object
        			BazaarInterface obj;
        			try {
        				obj = (BazaarInterface)Naming.lookup(l);
        				//create a proper lookupmsg & send 
        				obj.lookUp(outgoingLookupMsg);
        			} catch (Exception e) {
        				System.out.println("lookup failed to "+l);
        				e.printStackTrace();
        			}
        		}
		}
		//hopcount down to 0
		else{
			System.out.println("Dropping msg-hopcount to 0");
		}			
	}
	
	public void reply(ReplyMsg sellerReply){
	    if (!sellerReply.path.isEmpty()){
		Neighbor n = sellerReply.path.pop();
		System.out.println("Reply from "+ sellerReply.seller.ip);
		System.out.println("Forwarding reply to "+ n.ip);
		try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup("//"+n.ip+":"+n.port+"/Node");
		    obj.reply(sellerReply);
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (NotBoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    else{
	    	System.out.println("Reply from "+ sellerReply.seller.ip + " Adding this to my buyer reply queue");
	    	//I am the original buyer so I add this into my queue of seller responses
	    	try{
	    		NodeDetails.addSellerReply(sellerReply.seller);
	    	}catch (Exception e){
	    		System.out.println("Error while adding reply from seller!");
	    	}
		
	    }   
	}
	//Method for buyer to initiate the transaction
	public boolean buy(Product prodToBuy){
		//check if I have this product and decrement my counter
		if(NodeDetails.prod==prodToBuy && NodeDetails.count>0){
			NodeDetails.decrementProductCount();
			return true;
		}
		else
			return false;	
		
		}
			
		
}
