/**
 * 
 */
package bazaar;

import java.rmi.Naming;
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
	
	public void lookUp(Product incoming, int hopcount){
		System.out.println("Entering Roopas lookup");
		System.out.println("Am i a buyer?"+ NodeDetails.isBuyer);
		System.out.println("My id:"+ NodeDetails.id);
		if(!NodeDetails.isBuyer && NodeDetails.prod==incoming && NodeDetails.count>0){
				System.out.println("I am a seller. I have the product you asked for. Quantity:" + NodeDetails.count);
			
			}
		else{
////			Stack<Node> newStack=incoming.path;
////			newStack.push(NodeDetails);
////			LookupMsg outgoing=new LookupMsg(incoming.prod,incoming.hopcount-1,newStack);
//			System.out.println("I dont have"+ incoming);
//			BazaarInterface obj = (BazaarInterface)Naming.lookup("//10.0.0.5/Node");
//			
//	        obj.lookUp();
			
			
		}
						
	}
	public void reply(Node seller){}
	public boolean buy(Node buyer){return false;}
		
}
