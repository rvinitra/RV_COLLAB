/**
 * 
 */
package bazaar;

import java.util.ArrayList;

/**
 * @author root
 *
 */
public class Node {
	private int id;
	private String ip;
	private int port;
	private boolean isBuyer;
	private Product prod;
	private int count;
	ArrayList<Boolean> next;
	
	public Node(int id, String ip, int port, boolean isBuyer, Product prod, int count, ArrayList<ArrayList<Boolean>> neighbors){
	id=id;
	ip=ip;
	port=port;
	isBuyer=isBuyer;
	prod=prod;
	if(!isBuyer)
		count=count;
	else 
		count=-1;
	}
	public void setNeighbors(neighbors)ArrayList<E>etNeighbors(ArrayList<ArrayList<Boolean>> neighbors)
	{	
		next=neighbors.get(id-1);		
	}
	public void lookUp(Product product, int hopcount){
		
	}
	public void reply(Node seller){}
	public boolean buy(Node buyer){return false;}
		
}
