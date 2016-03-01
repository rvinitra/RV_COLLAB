/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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
	 * @throws MalformedURLException 
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 */
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		// run a loop where we create buyers and sellers
		//in the creation - include node prop + neighbours
		// call lookup
		System.setProperty("java.rmi.server.hostname","10.0.0.7");
        try {
                	//LocateRegistry.createRegistry(1099);
                	LocateRegistry.getRegistry(1099);
        
        } catch (RemoteException e) {
            System.err.println("BazaarNode exception: RMI registry already exists");
            e.printStackTrace();
        }
        String name = "//10.0.0.7/Node";
        Node engine = new Node();
        Naming.rebind(name, engine);
        System.out.println("BazaarNode bound");
        
        neighbors=new ArrayList<ArrayList<Boolean>>();
        neighbors.add(new ArrayList<Boolean>(Arrays.asList(true,true)));
        neighbors.add(new ArrayList<Boolean>(Arrays.asList(true,true)));
		//Node p1 = new Node(1,"127.0.0.1",1099,true,Product.BOAR,-1,neighbors);
		NodeDetails p2 = new NodeDetails();
		NodeDetails.assignValues(2,"10.0.0.7",1099,false,Product.BOAR,3,neighbors);
		
		//p1.lookUp(Product.BOAR, 1);
		//p2.reply(p2);
		//p2.buy(p1);
        
		
//        BazaarInterface obj = (BazaarInterface)Naming.lookup("//10.0.0.5/Node");
//        obj.lookUp("Hello from the other side..456");
        //System.exit(0);
						
	}

}
