/**
 * 
 */
package bazaar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Stack;

/**
 * @author rvinitra, rshenoy
 *
 */
public class Node extends UnicastRemoteObject implements BazaarInterface{
	private int id;
	private String ip;
	private int port;
	private boolean isBuyer;
	private Product prod;
	private int count;
	ArrayList<Boolean> next;
	
//	public Node(int id, String ip, int port, boolean isBuyer, Product prod, int count, ArrayList<ArrayList<Boolean>> neighbors){
//	this.id=id;
//	this.ip=ip;
//	this.port=port;
//	this.isBuyer=isBuyer;
//	this.prod=prod;
//	if(!isBuyer)
//		this.count=count;
//	else 
//		count=-1;
//	}
	public Node() throws RemoteException {
		 super(0);
	}
	public void setNeighbors( ArrayList<ArrayList<Boolean>> neighbors)
	{	
		next=neighbors.get(id-1);		
	}
	
	public void lookUp(String incoming){
//		if(!this.isBuyer && this.prod==incoming.prod && this.count>0){
//				//reply
//			
//			}
//		else{
//			Stack<Node> newStack=incoming.path;
//			newStack.push(this);
//			LookupMsg outgoing=new LookupMsg(incoming.prod,incoming.hopcount-1,newStack);
//			//fowards this message
//			
//		}
		System.out.println("In Lookup Method" + incoming);
				
	}
	public void reply(Node seller){}
	public boolean buy(Node buyer){return false;}
		
}
