/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author root
 *
 */
public class Bazaar {
	ArrayList<NodeDetails> peers;
	static ArrayList<ArrayList<Boolean>> neighbors;
	private static final Random RANDOM = new Random();
			
	public static int GenerateID(String ipport){
	    return ipport.hashCode() & Integer.MAX_VALUE;
	}
	public static void ReadConfiguration(){
		File fXmlFile = new File("./config.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			NodeList nList = doc.getElementsByTagName("node");
			Node nNode = nList.item(0);
			Element eElement = (Element) nNode;
			NodeDetails.ip=eElement.getElementsByTagName("ip").item(0).getTextContent();
			NodeDetails.id=GenerateID(NodeDetails.ip+eElement.getElementsByTagName("port").item(0).getTextContent());
			NodeDetails.port=Integer.parseInt(eElement.getElementsByTagName("port").item(0).getTextContent());
			NodeDetails.isBuyer=Boolean.parseBoolean(eElement.getElementsByTagName("buyer").item(0).getTextContent());
			NodeDetails.next = new ArrayList<Neighbor>();
			NodeList neList = eElement.getElementsByTagName("neighbor");
			for (int temp1 = 0; temp1 < neList.getLength(); temp1++) {
			    Element neElement = (Element) neList.item(temp1);
			    int nid = GenerateID(neElement.getAttribute("ip")+neElement.getAttribute("port"));
			    String nip = neElement.getAttribute("ip");
			    int nport = Integer.parseInt(neElement.getAttribute("port"));
			    Neighbor n = new Neighbor(nid,nip,nport);
			    NodeDetails.next.add(n);
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//This function randomly assigns a product to the seller
	public static Product pickRandomProduct()
	{
		int pick = RANDOM.nextInt(100);
	    return Product.values()[pick%3];
		
	}
	//This function randomly assigns a count<10 of the product that the seller has
	public static int pickRandomCount()
	{
		int pick = RANDOM.nextInt(1234);
		return (pick%10);
	}
	public static void main(String[] args) {
        	// run a loop where we create buyers and sellers
        	//in the creation - include node prop + neighbors
        	// call lookup
        	ReadConfiguration();
        	NodeDetails.Display();
        	System.setProperty("java.rmi.server.hostname",NodeDetails.ip);
                try {
                	//LocateRegistry.createRegistry(NodeDetails.port);
                	LocateRegistry.getRegistry(NodeDetails.port);
                
                } catch (RemoteException e) {
                    System.err.println("BazaarNode exception: RMI registry already exists");
                    e.printStackTrace();
                }
                String name = "//"+NodeDetails.ip+":"+NodeDetails.port+"/Node";
                bazaar.Node engine = null;
		try {
		    engine = new bazaar.Node();
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
                try {
		    Naming.rebind(name, engine);
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
                System.out.println("BazaarNode bound");
                if (NodeDetails.isBuyer){
                    BazaarInterface obj = null;
                    try {
                    	obj = (BazaarInterface)Naming.lookup("//10.0.0.7/Node");
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
                    //Pick a product to buy
                    NodeDetails.prod=pickRandomProduct();
                    //Create a stack which holds return path, buyer is always last to be popped 
                    Stack<Neighbor> returnPath = new Stack<Neighbor>();
                    //Fill in original buyers details
                    Neighbor originalBuyer = new Neighbor();
                    //get details of the current node
                    originalBuyer.CurrentNode();
                    //push buyer first into the returnPath stack
                    returnPath.push(originalBuyer);
                    //Create a lookup message with the product you want to buy
                    LookupMsg msg = new LookupMsg(NodeDetails.prod,10,returnPath);
                    try {
    		    	obj.lookUp(msg);
    		    } catch (RemoteException e) {
    		    // TODO Auto-generated catch block
    		    e.printStackTrace();
    		    }
                }
                else{
                	//Pick a product to sell if you are a seller
                    NodeDetails.prod=pickRandomProduct();
                    //Pick a count of products that you're selling
                    NodeDetails.count=pickRandomCount();
                }
                //System.exit(0);
						
	}

}
