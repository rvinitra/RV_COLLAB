/**
 * 
 */
package bazaar;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Stack;

/**
 * @author rvinitra, rshenoy
 *
 */
public class Node extends UnicastRemoteObject implements BazaarInterface{

	public Node() throws RemoteException {
		 super(0);
	}
	
	public void lookUp(LookupMsg incoming){
		System.out.println("Entering Roopas lookup");
		System.out.println("Am i a buyer?"+ NodeDetails.isBuyer);
		System.out.println("My id:"+ NodeDetails.id);
		if(!NodeDetails.isBuyer && NodeDetails.prod==incoming.prod && NodeDetails.count>0){
				System.out.println("I am a seller. I have the product you asked for. Quantity:" + NodeDetails.count);
				System.out.println("Hop Count I see:"+ incoming.hopcount);
				//Rmi reply with seller details
			}
		else if(incoming.hopcount>0){
////		Stack<Node> newStack=incoming.path;
////		newStack.push(NodeDetails);
////		LookupMsg outgoing=new LookupMsg(incoming.prod,incoming.hopcount-1,newStack);
			
			System.out.println("I dont have"+ incoming.prod);
			System.out.println("Passing it on to my neighbour.My neighbors are:");
			ArrayList<Neighbor> myNeighborsList = NodeDetails.getMyNeighbors();
			for(Neighbor n : myNeighborsList ){
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
	public void reply(Node seller){}
	public boolean buy(Node buyer){return false;}
		
}
