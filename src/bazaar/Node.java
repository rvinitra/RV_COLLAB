/**
 * 
 */
package bazaar;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
    ////		Stack<Node> newStack=incoming.path;
    ////		newStack.push(NodeDetails);
    ////		LookupMsg outgoing=new LookupMsg(incoming.prod,incoming.hopcount-1,newStack);
			
        		System.out.println("I dont have"+ incoming.prod);
        		System.out.println("Passing it on to my neighbour.My neighbors are:");
        		for(Neighbor n : NodeDetails.next ){
        			System.out.println("Neighbor id:"+n.id);
        			StringBuilder lookupName= new StringBuilder("//");
        			String l= lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
        			System.out.println("Lookup string:" + l);
        			BazaarInterface obj;
        			try {
        				obj = (BazaarInterface)Naming.lookup(l);
        				//create a proper lookupmsg & send 
        //					obj.lookUp(ADDOUTGOINGMSG!!!);
        			} catch (Exception e) {
        				System.out.println("lookup failed to "+l);
        				e.printStackTrace();
        			}
        		}
		}
		//hopcount down to 0
		else{
			System.out.println("Dropping msg- hopcount to 0");
		}			
	}
	
	public void reply(ReplyMsg seller){
	    if (!seller.path.isEmpty()){
		Neighbor n = seller.path.pop();
		try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup("//"+n.ip+":"+n.port+"/Node");
		    obj.reply(seller);
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
		//buy
	    }   
	}
	
	public boolean buy(Node buyer){return false;}
		
}
