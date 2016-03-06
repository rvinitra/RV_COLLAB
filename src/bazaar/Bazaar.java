/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

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
public class Bazaar{
	ArrayList<NodeDetails> peers;
	static ArrayList<ArrayList<Boolean>> neighbors;
	private static final Random RANDOM = new Random();
	private static int ITER_COUNT = 10;
	private static int TIMEOUT=1500;
			
	public static int GenerateID(String ipport){
	    return ipport.hashCode() & Integer.MAX_VALUE;
	}
	public static void ReadConfiguration(){
	    Log.l.log(Log.finer, "Reading from configuration file");
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
    		TIMEOUT=Integer.parseInt(eElement.getElementsByTagName("timeout").item(0).getTextContent());
    		ITER_COUNT=Integer.parseInt(eElement.getElementsByTagName("iterations").item(0).getTextContent());
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
	    } catch (ExportException e) {
		try {
		    LocateRegistry.getRegistry(NodeDetails.port);
		} catch (RemoteException e1) {
		    // TODO Auto-generated catch block
		    System.err.println(NodeDetails.getNode()+": BazaarNode exception: RMI registry already exists");
		    e1.printStackTrace();
		}
            } catch (RemoteException e) {
		// TODO Auto-generated catch block
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
    	    Log.l.log(Log.finest, NodeDetails.getNode()+": BazaarNode bound");
    	    if (NodeDetails.isBuyer){
    		NodeDetails.sellerReplies = new LinkedList<Neighbor>();
    		BazaarInterface obj = null;
    		Stack<Neighbor> newPathStack = new Stack<Neighbor>();
    		//add current nodes details into reverse path
    		Neighbor thisnode= new Neighbor();
    		thisnode.CurrentNode();
    		newPathStack.push(thisnode);
    		StringBuilder writetofile = new StringBuilder();
    		for(int iter=1; iter<=ITER_COUNT; iter++){
    			//construct outgoing message down to my neighbors
        		NodeDetails.prod=pickRandomProduct();
        		NodeDetails.Display();
        		LookupMsg outgoingLookupMsg=new LookupMsg(NodeDetails.prod,10,newPathStack);
        		Log.l.log(Log.finest, NodeDetails.getNode()+": Looking up my neighbours:");
        		System.out.println(NodeDetails.getNode()+": Looking up for "+NodeDetails.prod);
        		//start timer
        		long startTime = System.nanoTime();
        		//send the outgoing message to each neighbor I have
        		for(Neighbor n : NodeDetails.next ){
        		    //build lookup name for RMI object based on neighbor's ip & port
        		    Log.l.log(Log.finer, NodeDetails.getNode()+": Looking up for "+NodeDetails.prod+" on "+n.id+"@"+n.ip+":"+n.port);
        		    Log.l.log(Log.finest, NodeDetails.getNode()+": Neighbor: "+n.id+"@"+n.ip);
        		    StringBuilder lookupName= new StringBuilder("//");
        		    String l= lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
        		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
        		    try {
        			obj = (BazaarInterface)Naming.lookup(l);
        			//	create a proper lookupmsg & send 
        			obj.lookUp(outgoingLookupMsg);
        		    }
        		    catch (Exception e) {
        			System.err.println(NodeDetails.getNode()+": Lookup failed to "+l);
        			e.printStackTrace();
        		    }
        		}
        		//wait for timeout
			try {
			    Log.l.log(Log.finest, NodeDetails.getNode()+": Waiting for "+TIMEOUT+"ms");
			    Thread.sleep(TIMEOUT);
			    Log.l.log(Log.finest, NodeDetails.getNode()+": Timeout complete for "+TIMEOUT+"ms");
			} catch (InterruptedException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			if (!NodeDetails.sellerReplies.isEmpty()){
			    Iterator<Neighbor> it = NodeDetails.sellerReplies.iterator();
			    StringBuilder logstring = new StringBuilder();
			    while(it.hasNext()){
				Neighbor seller = it.next();
				logstring.append(String.valueOf(seller.id)).append("@").append(seller.ip).append(":").append(String.valueOf(seller.port)).append(" ");
				
			    }
			    Log.l.log(Log.finer, NodeDetails.getNode()+": Available Sellers: "+logstring.toString());
			    System.out.println(NodeDetails.getNode()+": Available Sellers: "+logstring.toString());
			}
			else{
			    Log.l.log(Log.finer, NodeDetails.getNode()+": No replies for product "+NodeDetails.prod);
			    System.out.println(NodeDetails.getNode()+": No replies for product "+NodeDetails.prod);
			}
			while (!NodeDetails.sellerReplies.isEmpty()){
			    Neighbor chosenSeller = NodeDetails.removeSellerReply();
			    Log.l.log(Log.finer, NodeDetails.getNode()+": Seller "+chosenSeller.id+"@"+chosenSeller.ip+" chosen");
			    System.out.println(NodeDetails.getNode()+": Seller "+chosenSeller.id+"@"+chosenSeller.ip+" chosen");
			    BazaarInterface sellerobj = null;
			    try {
				sellerobj = (BazaarInterface)Naming.lookup("//"+chosenSeller.ip+":"+chosenSeller.port+"/Node");
			    } catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			    } catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			    } catch (NotBoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			    }
			    try {
				if (sellerobj.buy(NodeDetails.prod)){
				    	Log.l.log(Log.finer, NodeDetails.getNode()+": Bought "+NodeDetails.prod+" from "+chosenSeller.id+"@"+chosenSeller.ip+":"+chosenSeller.port);
				    	System.out.println(NodeDetails.getNode()+": Bought "+NodeDetails.prod+" from "+chosenSeller.id+"@"+chosenSeller.ip+":"+chosenSeller.port);
    					break;
				}else{
				    Log.l.log(Log.finer, NodeDetails.getNode()+": Buy from "+chosenSeller.id+"@"+chosenSeller.ip+":"+chosenSeller.port+" failed");
				    System.out.println(NodeDetails.getNode()+": Buy from "+chosenSeller.id+"@"+chosenSeller.ip+":"+chosenSeller.port+" failed");
				}
			    } catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
			long endTime=System.nanoTime();
			double duration = (double)((endTime - startTime)/1000000.0);
			NodeDetails.runningTime+=duration;
			Log.l.log(Log.finer, NodeDetails.getNode()+": This trasaction took "+duration+"ms.");
			System.out.println(NodeDetails.getNode()+": This trasaction took "+duration+"ms.\n");
			writetofile.append(duration-TIMEOUT).append(",");
			Log.l.log(Log.finer, NodeDetails.getNode()+": Moving on to next product\n\n");
    		}
    		Log.l.log(Log.finer, NodeDetails.getNode()+": Average transaction time:"+ (NodeDetails.runningTime/ITER_COUNT) +"ms");
    		System.out.println(NodeDetails.getNode()+": Average transaction time:"+ (NodeDetails.runningTime/ITER_COUNT) +"ms");
    		File f = new File("transaction.txt");
    		try {
    		    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
    		    bw.write(writetofile.toString());
    		    bw.flush();
    		    bw.close();
    		    
    		} catch (IOException e) {
    		    // TODO Auto-generated catch block
    		    e.printStackTrace();
    		}
    	    }
    	    else{
    		//Pick a product to sell if you are a seller
    		NodeDetails.prod=pickRandomProduct();
    		//Pick a count of products that you're selling
    		NodeDetails.setProductCount(pickRandomCount());
    		//print seller details
    		System.out.println(NodeDetails.getNode()+": Current Stock:"+ NodeDetails.prod +" X "+NodeDetails.count);
    	    }
	}
}
