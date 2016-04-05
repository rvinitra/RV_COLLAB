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
import java.io.Writer;
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
	public static final Random RANDOM = new Random();
	private static int ITER_COUNT = 10;
	public static int TIMEOUT=1500;
			
	public static int GenerateID(String ipport){
	    return ipport.hashCode() & Integer.MAX_VALUE;
	}
	public static void ReadConfiguration(String configFileName){
	    //Reading the XML configuration file
	    Log.l.log(Log.finer, "Reading from configuration file");
	    File fXmlFile = new File(configFileName+".xml");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
	    try {
    		dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(fXmlFile);
    		//Parsing the XML document using the DOM parser
    		NodeList nList = doc.getElementsByTagName("node");
    		Node nNode = nList.item(0);
    		Element eElement = (Element) nNode;
    		NodeDetails.ip=eElement.getElementsByTagName("ip").item(0).getTextContent();
    		NodeDetails.id=GenerateID(NodeDetails.ip+eElement.getElementsByTagName("port").item(0).getTextContent());
    		NodeDetails.port=Integer.parseInt(eElement.getElementsByTagName("port").item(0).getTextContent());
    		NodeDetails.isBuyer=Boolean.parseBoolean(eElement.getElementsByTagName("buyer").item(0).getTextContent());
    		NodeDetails.isSeller=Boolean.parseBoolean(eElement.getElementsByTagName("seller").item(0).getTextContent());
    		NodeDetails.isTrader=Boolean.parseBoolean(eElement.getElementsByTagName("trader").item(0).getTextContent());
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
	
	public static void buy(String config){
	    	StringBuilder writetofile = null;
        	//construct outgoing message down to my neighbors
		NodeDetails.buyProd=pickRandomProduct();
		NodeDetails.buyCount = pickRandomCount();
		Request req = new Request();
		req.requestingNode = NodeDetails.getCurrentNode();
		req.prod = NodeDetails.buyProd;
		req.count=NodeDetails.buyCount;
		req.money=pickRandomCount();
		//increment clock before using the timestamp
		NodeDetails.incrementClock(1);
		req.timestamp=NodeDetails.lamportClock;
		NodeDetails.broadcastClock();
		Log.l.log(Log.finest, NodeDetails.getNode()+": Creating buy request");
		System.out.println(NodeDetails.getNode()+": Buying "+NodeDetails.buyProd+"X"+NodeDetails.buyCount);
		//start timer
		Neighbor trader = NodeDetails.trader;
		//build lookup name for RMI object based on trader's ip & port
		Log.l.log(Log.finer, NodeDetails.getNode()+": Looking up for "+trader.id+"@"+trader.ip+":"+trader.port);
		StringBuilder lookupName= new StringBuilder("//");
		String lookupstring = lookupName.append(trader.ip).append(":").append(trader.port).append("/Node").toString();
		Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + lookupstring);
		long startTime = System.nanoTime();
		try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup(lookupstring);
		    //	create a proper lookupmsg & send 
		    obj.buy(req);
		}
		catch (Exception e) {
		    System.err.println(NodeDetails.getNode()+": Lookup failed to "+lookupstring);
		    e.printStackTrace();
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
		long endTime=System.nanoTime();
		double duration = (double)((endTime - startTime)/1000000.0);
		NodeDetails.runningTime+=duration;
		Log.l.log(Log.finer, NodeDetails.getNode()+": This trasaction took "+duration+"ms.");
		System.out.println(NodeDetails.getNode()+": This trasaction took "+duration+"ms.\n");
//		writetofile.append(duration-TIMEOUT).append(",");
//		File f = new File(config+"_buytransaction.txt");
//		try {
//		    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
//		    bw.write(writetofile.toString());
//		    bw.flush();
//		    bw.close();
//		    
//		} catch (IOException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
	    
	}
	
	public static void sell(String config){
	  //	Pick a product to sell if you are a seller
    		NodeDetails.sellProd=pickRandomProduct();
    		//Pick a count of products that you're selling
    		NodeDetails.sellCount=pickRandomCount();
    		//print seller details
    		System.out.println(NodeDetails.getNode()+": Current Stock:"+ NodeDetails.sellProd +" X "+NodeDetails.sellCount);
    		Request req = new Request();
    		req.requestingNode= NodeDetails.getCurrentNode();
    		req.prod=NodeDetails.sellProd;
    		req.count = NodeDetails.sellCount;
    		req.money=0;
		//increment clock before using the timestamp
		NodeDetails.incrementClock(1);
		NodeDetails.broadcastClock();
		req.timestamp=NodeDetails.lamportClock;
    		BazaarInterface obj = null;
    		Neighbor t = NodeDetails.trader;
		StringBuilder str = new StringBuilder("//");
		String lstr = str.append(t.ip).append(":").append(t.port).append("/Node").toString();
		long startTime = System.nanoTime();
    		try{
    			obj = (BazaarInterface)Naming.lookup(lstr); 
        		obj.deposit(req);
    		 }
    		 catch(Exception e){
    			System.err.println(NodeDetails.getNode()+": Lookup failed to "+lstr);
    			e.printStackTrace();
    		}
    		long endTime=System.nanoTime();
		double duration = (double)((endTime - startTime)/1000000.0);
		NodeDetails.runningTime+=duration;
		Log.l.log(Log.finer, NodeDetails.getNode()+": This trasaction took "+duration+"ms.");
		System.out.println(NodeDetails.getNode()+": This trasaction took "+duration+"ms.\n");
//		StringBuilder writetofile = null;
//		writetofile.append(duration-TIMEOUT).append(",");
//		Log.l.log(Log.finer, NodeDetails.getNode()+": Moving on to next product\n\n");
//		Log.l.log(Log.finer, NodeDetails.getNode()+": Average transaction time:"+ (NodeDetails.runningTime/ITER_COUNT) +"ms");
//		System.out.println(NodeDetails.getNode()+": Average transaction time:"+ (NodeDetails.runningTime/ITER_COUNT) +"ms");
//		File f = new File(config+"_selltransaction.txt");
//		try {
//		    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
//		    bw.write(writetofile.toString());
//		    bw.flush();
//		    bw.close();
//		    
//		} catch (IOException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
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
	

	public static void main(String[] configFile) {
		
	    // run a loop where we create buyers and sellers
	    ReadConfiguration(configFile[0]);
	    System.setProperty("java.rmi.server.hostname",NodeDetails.ip);
	    try {
		LocateRegistry.createRegistry(NodeDetails.port);
		//create registry
	    } catch (ExportException e) {
		try {
		    LocateRegistry.getRegistry(NodeDetails.port);
		    //if registry already exists, locate it
		} catch (RemoteException e1) {
		    // TODO Auto-generated catch block
		    System.err.println(NodeDetails.getNode()+": BazaarNode exception: RMI registry already exists");
		    e1.printStackTrace();
		}
            } catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    
            String name = "//localhost:"+NodeDetails.port+"/Node";
            bazaar.Node engine = null;
    	    try {
    		engine = new bazaar.Node();
    		//create RMI class objec
    	    } catch (RemoteException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    }
    	    try {
    		Naming.rebind(name, engine);
    		//bind RMI class object to listen at the specifief URI
    	    } catch (RemoteException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    } catch (MalformedURLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    }
    	    Log.l.log(Log.finest, NodeDetails.getNode()+": BazaarNode bound");
    	    if (NodeDetails.isTrader)
    	    {
    	    	BazaarInterface obj = null;
    	    	System.out.println(NodeDetails.getNode()+": Won the Election");
    	    	NodeDetails.trader=NodeDetails.getCurrentNode();
    	    	NodeDetails.traderDetails = new TraderDetails();
    	    	ElectionMsg victoryMsg = new ElectionMsg(ElectionMsgType.VICTORY,NodeDetails.getCurrentNode());
    		for(Neighbor n : NodeDetails.next ){
      		    //build lookup name for RMI object based on neighbor's ip & port
      		    Log.l.log(Log.finer, NodeDetails.getNode()+": Victory msg to "+n.id+"@"+n.ip+":"+n.port);
    	      	    StringBuilder lookupName = new StringBuilder("//");
      		    String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
      		    try {
    	    			obj = (BazaarInterface)Naming.lookup(l);
    	    			obj.election(victoryMsg);
      		    }
      		    catch (Exception e) {
    	    			System.err.println(NodeDetails.getNode()+":Election failed to "+l);
    	    			e.printStackTrace();
      		    }
    		}
    	    }
    	    else
    	    {
    		while(NodeDetails.trader==null){
    		    try {
    			Thread.sleep(TIMEOUT/3);
    		    }
    		    catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		    }
    		}
    	    }
    	    for(int iter=1; iter<=ITER_COUNT; iter++){
    			//If the node is a buyer
    		    if (NodeDetails.isBuyer && NodeDetails.isSeller && !NodeDetails.isTrader)
    		    {
    			if (NodeDetails.trader!=null){
        		    	buy(configFile[0]);
        		}
        		else{
        		    System.out.println(NodeDetails.getNode()+": No trader available");
        		}
    			if (NodeDetails.trader!=null){
	    			sell(configFile[0]);
	    		}
        		else{
        		    System.out.println(NodeDetails.getNode()+": No trader available");
        		}
    		    }
    		    else if (NodeDetails.isBuyer && !NodeDetails.isTrader){
        		if (NodeDetails.trader!=null){
        		    	buy(configFile[0]);
        		}
        		else{
        		    System.out.println(NodeDetails.getNode()+": No trader available");
        		}
        		
        	    }
    		    else if (NodeDetails.isSeller && !NodeDetails.isTrader){
        		if (NodeDetails.trader!=null){
    	    			sell(configFile[0]);
    	    		}
        		else{
        		    System.out.println(NodeDetails.getNode()+": No trader available");
        		}
        	    }
    		}
    	    }
	}
