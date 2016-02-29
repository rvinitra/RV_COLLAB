/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author root
 *
 */
public class Bazaar {
	ArrayList<Node> peers;
	static ArrayList<ArrayList<Boolean>> neighbors;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// run a loop where we create buyers and sellers
		//in the creation - include node prop + neighbours
		// call lookup
        try {
            String name = "BazaarInterface";
            BazaarInterface engine = new Node();
            BazaarInterface stub =
                (BazaarInterface) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind(name, stub);
            System.out.println("BazaarNode bound");
        } catch (Exception e) {
            System.err.println("BazaarNode exception:");
            e.printStackTrace();
        }
		neighbors=new ArrayList<ArrayList<Boolean>>();
		neighbors.add(new ArrayList<Boolean>(Arrays.asList(true,true)));
		neighbors.add(new ArrayList<Boolean>(Arrays.asList(true,true)));
		Node p1 = new Node(1,"127.0.0.1",9999,true,Product.BOAR,-1,neighbors);
		Node p2 = new Node(2,"127.0.0.1",8888,true,Product.BOAR,3,neighbors);
		
		//p1.lookUp(Product.BOAR, 1);
		p2.reply(p2);
		p2.buy(p1);
						
	}

}
