/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
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
public class Bazaar extends Thread{
	ArrayList<NodeDetails> peers;
	static ArrayList<ArrayList<Boolean>> neighbors;
	private static final Random RANDOM = new Random();
	private static int ITER_COUNT = 3;
			
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
	    return Product.values()[pick%Product.values().length];
		
	}
	
	//This function randomly assigns a count<10 of the product that the seller has
	public static int pickRandomCount()
	{
	    int pick = RANDOM.nextInt(1234);
	    return (1+pick%10);
	}
	
	public void run(){
	    
	}
	
	public static void main(String[] args) {
	    // run a loop where we create buyers and sellers
	    //in the creation - include node prop + neighbors
	    // call lookup
	    ReadConfiguration();
	    System.setProperty("java.rmi.server.hostname",NodeDetails.ip);
	    try {
		LocateRegistry.createRegistry(NodeDetails.port);
		//LocateRegistry.getRegistry(NodeDetails.port);
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
    		NodeDetails.sellerReplies = (Queue<Neighbor>) new ArrayList<Neighbor>();
    		BazaarInterface obj = null;
    		Stack<Neighbor> newPathStack = new Stack<Neighbor>();
    		//add current nodes details into reverse path
    		Neighbor thisnode= new Neighbor();
    		thisnode.CurrentNode();
    		newPathStack.push(thisnode);
    		for(int iter=1; iter<=ITER_COUNT; iter++){
    			//construct outgoing message down to my neighbors
        		NodeDetails.prod=pickRandomProduct();
        		NodeDetails.Display();
        		LookupMsg outgoingLookupMsg=new LookupMsg(NodeDetails.prod,10,newPathStack);
        		System.out.println("Looking up my neighbours:");
        		//send the outgoing message to each neighbor I have
        		for(Neighbor n : NodeDetails.next ){
        		    //build lookup name for RMI object based on neighbor's ip & port
        		    System.out.println("Neighbor id:"+n.id);
        		    StringBuilder lookupName= new StringBuilder("//");
        		    String l= lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
        		    System.out.println("Lookup string:" + l);
        		    try {
        			obj = (BazaarInterface)Naming.lookup(l);
        			//	create a proper lookupmsg & send 
        			obj.lookUp(outgoingLookupMsg);
        			//wait for timeout
        			Thread.sleep(3000);
        			while (!NodeDetails.sellerReplies.isEmpty()){
        			    Neighbor chosenSeller = NodeDetails.removeSellerReply();
        			    System.out.println("Seller "+chosenSeller.id+"@"+chosenSeller.ip+" chosen");
        			    BazaarInterface sellerobj = (BazaarInterface)Naming.lookup("//"+chosenSeller.ip+":"+chosenSeller.port+"/Node");
        			    if (sellerobj.buy(NodeDetails.prod)){
        				System.out.println("Bought "+NodeDetails.prod+" from "+chosenSeller.id+"@"+chosenSeller.ip);
        				break;
        			    }
        			}
        		    }
        		    catch (Exception e) {
        			System.out.println("lookup failed to "+l);
        			e.printStackTrace();
        		    }
        		}
    		}
    	    }
    	    else{
    		//Pick a product to sell if you are a seller
    		NodeDetails.prod=pickRandomProduct();
    		//Pick a count of products that you're selling
    		NodeDetails.setProductCount(pickRandomCount());
    		//print seller details
    		NodeDetails.Display();
    	    }
    	    //System.exit(0);
	}

}
