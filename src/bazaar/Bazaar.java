/**
 * 
 */
package bazaar;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
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
	
	//construct and send a buy request message for trader
	public static void buyRequest(){
	    	//Pick a product to buy
		NodeDetails.buyProd=pickRandomProduct();
		//Pick a count of product to buy
		NodeDetails.buyCount = pickRandomCount();
		RequestMsg req = new RequestMsg();
		req.requestingNode = NodeDetails.getCurrentNode();
		req.prod = NodeDetails.buyProd;
		req.count=NodeDetails.buyCount;
		//increment clock before using the timestamp
		NodeDetails.incrementClock(1);
		req.timestamp=NodeDetails.lamportClock;
		NodeDetails.broadcastClock();
		Log.l.log(Log.finest, NodeDetails.getNode()+": Creating buy request");
		Neighbor trader = NodeDetails.trader;
		//build lookup name for RMI object based on trader's ip & port
		Log.l.log(Log.finer, NodeDetails.getNode()+": Looking up for "+trader.id+"@"+trader.ip+":"+trader.port);
		System.out.println(NodeDetails.getNode()+": Buying "+NodeDetails.buyProd+"X"+NodeDetails.buyCount+" from "+trader.id+"@"+trader.ip+":"+trader.port);
		StringBuilder lookupName= new StringBuilder("//");
		String lookupstring = lookupName.append(trader.ip).append(":").append(trader.port).append("/Node").toString();
		Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + lookupstring);
		try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup(lookupstring);
		    obj.buy(req);
		    System.out.println(NodeDetails.getNode()+": Buy transaction queued at trader.");
		}
		catch (Exception e) {
		    System.err.println(NodeDetails.getNode()+": Lookup failed to "+lookupstring);
		    e.printStackTrace();
		}
	}
	
	//construct and send a sell request message to deposit at the trader
	public static void depositRequest(){
	    	//Pick a product to sell
		NodeDetails.sellProd=pickRandomProduct();
		//Pick a count of product to sell
		NodeDetails.sellCount=pickRandomCount();
		System.out.println(NodeDetails.getNode()+": Depositing "+ NodeDetails.sellProd +" X "+NodeDetails.sellCount);
		RequestMsg req = new RequestMsg();
		req.requestingNode= NodeDetails.getCurrentNode();
		req.prod=NodeDetails.sellProd;
		req.count = NodeDetails.sellCount;
		//increment clock before using the timestamp
		NodeDetails.incrementClock(1);
		NodeDetails.broadcastClock();
		req.timestamp=NodeDetails.lamportClock;
		BazaarInterface obj = null;
		Neighbor t = NodeDetails.trader;
		StringBuilder str = new StringBuilder("//");
		String lstr = str.append(t.ip).append(":").append(t.port).append("/Node").toString();
		try{
			obj = (BazaarInterface)Naming.lookup(lstr); 
    		obj.deposit(req);
    		System.out.println(NodeDetails.getNode()+": Successfully deposited "+req.prod+"X"+req.count);
		 }
		 catch(Exception e){
			System.err.println(NodeDetails.getNode()+": Lookup failed to "+lstr);
			e.printStackTrace();
		}
	}
	
	//Main function
	public static void main(String[] configFile) {
	    //Read from configuration file
	    ReadConfiguration(configFile[0]);
	    
	    //Create registry
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
	    
	    //Bind the RMI object to listen 
            String name = "//localhost:"+NodeDetails.port+"/Node";
            bazaar.Node engine = null;
    	    try {
    		engine = new bazaar.Node();
    		//create RMI class object
    	    } catch (RemoteException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    }
    	    try {
    		Naming.rebind(name, engine);
    		//bind RMI class object to listen at the specified URI
    	    } catch (RemoteException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    } catch (MalformedURLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    }
    	    Log.l.log(Log.finest, NodeDetails.getNode()+": BazaarNode bound");
    	    
    	    //If the node is a trader, send Victory Message to all other nodes in the network
    	    if (NodeDetails.isTrader)
    	    {
    	    	BazaarInterface obj = null;
    	    	System.out.println(NodeDetails.getNode()+":[Trader] Won the Election");
    	    	NodeDetails.trader=NodeDetails.getCurrentNode();
    	    	try {
		    NodeDetails.traderDetails = new TraderDetails();
		} catch (RemoteException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
    	    	
    	    	//Create a Victory message
    	    	ElectionMsg victoryMsg = new ElectionMsg(ElectionMsgType.VICTORY,NodeDetails.getCurrentNode(),null);
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
    	    //not a trader so wait until a trader is elected
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
    	    
    	    /*
    	    Loop through ITER_COUNT to:
    	    1. If you are a Trader, process buy requests from the queue
    	    2. If you are a Buyer and a Seller (and not a Trader), send buy and deposit requests to the trader
    	    3. If you are a Buyer (and not a Trader), send buy requests to the trader
    	    4. If you are a Seller (and not a Trader), send deposit requests to the trader
    	    */
    	    StringBuilder buywritetofile= new StringBuilder();
    	    StringBuilder sellwritetofile = new StringBuilder();
    	    for(int iter=1; iter<=ITER_COUNT; iter++){
    		    //If you are a Trader, process buy requests from the queue
    		    if (NodeDetails.isTrader){
    			NodeDetails.processBuyQueue();
    		    }
    		    
    		    //If you are a Buyer and a Seller (and not a Trader), send buy and deposit requests to the trader
    		    if (NodeDetails.isBuyer && NodeDetails.isSeller && !NodeDetails.isTrader)
    		    {
    			if (NodeDetails.trader!=null){
    			    	//start timer
    			    	long startTime = System.nanoTime();
        		    	buyRequest();
        		    	//end timer
        		    	long endTime=System.nanoTime();
        			double duration = (double)((endTime - startTime)/1000000.0);
        			NodeDetails.runningTime+=duration;
        			Log.l.log(Log.finer, NodeDetails.getNode()+": This transaction took "+duration+"ms.");
        			System.out.println(NodeDetails.getNode()+": This transaction took "+duration+"ms.\n");
        			buywritetofile.append(duration).append(",");
        			File f = new File("buy_transaction.txt");
        			try {
        			    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        			    bw.write(buywritetofile.toString());
        			    bw.flush();
        			    bw.close();
        			    
        			} catch (IOException e) {
        			    // TODO Auto-generated catch block
        			    e.printStackTrace();
        			}
        			
        			//Sleep for TIMEOUT/3 before sending next request
        		    	try {
        		    	    Thread.sleep(TIMEOUT/3);
        	    		}
        	    		catch (InterruptedException e) {
        	    		    // TODO Auto-generated catch block
        	    		    e.printStackTrace();
        	    		}
    			}
        		else{
        		    	System.out.println(NodeDetails.getNode()+": No trader available");
        		}
    			
    			if (NodeDetails.trader!=null){
    			    	//start timer
    			    	long startTime = System.nanoTime();
	    			depositRequest();
	    			//end timer
	    			long endTime=System.nanoTime();
        			double duration = (double)((endTime - startTime)/1000000.0);
        			NodeDetails.runningTime+=duration;
        			Log.l.log(Log.finer, NodeDetails.getNode()+": This transaction took "+duration+"ms.");
        			System.out.println(NodeDetails.getNode()+": This transaction took "+duration+"ms.\n");
        			sellwritetofile.append(duration).append(",");
        			File f = new File("sell_transaction.txt");
        			try {
        			    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        			    bw.write(sellwritetofile.toString());
        			    bw.flush();
        			    bw.close();
        			    
        			} catch (IOException e) {
        			    // TODO Auto-generated catch block
        			    e.printStackTrace();
        			}
        			
        			//Sleep for TIMEOUT/3 before sending next request
	    			try {
	    			    Thread.sleep(TIMEOUT/3);
	        		}
	        		catch (InterruptedException e) {
	        		    // TODO Auto-generated catch block
	        		    e.printStackTrace();
	        		}
	    		}
        		else{
        		    	System.out.println(NodeDetails.getNode()+": No trader available");
        		}
    		    }
    		    //If you are a Buyer (and not a Trader), send buy requests to the trader
    		    else if (NodeDetails.isBuyer && !NodeDetails.isTrader){
        		if (NodeDetails.trader!=null){
        		    	//start timer
			    	long startTime = System.nanoTime();
        		    	buyRequest();
        		    	//end timer
        		    	long endTime=System.nanoTime();
        			double duration = (double)((endTime - startTime)/1000000.0);
        			NodeDetails.runningTime+=duration;
        			Log.l.log(Log.finer, NodeDetails.getNode()+": This transaction took "+duration+"ms.");
        			System.out.println(NodeDetails.getNode()+": This transaction took "+duration+"ms.\n");
        			buywritetofile.append(duration).append(",");
        			File f = new File("buy_transaction.txt");
        			try {
        			    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        			    bw.write(buywritetofile.toString());
        			    bw.flush();
        			    bw.close();
        			    
        			} catch (IOException e) {
        			    // TODO Auto-generated catch block
        			    e.printStackTrace();
        			}
        			
        			//Sleep for TIMEOUT/3 before sending next request
        		    	try {
        		    	    Thread.sleep(TIMEOUT/3);
        	    		}
        	    		catch (InterruptedException e) {
        	    		    // TODO Auto-generated catch block
        	    		    e.printStackTrace();
        	    		}
        		}
        		else{
        		    	System.out.println(NodeDetails.getNode()+": No trader available");
        		}
        		
        	    }
    		    //If you are a Seller (and not a Trader), send deposit requests to the trader
    		    else if (NodeDetails.isSeller && !NodeDetails.isTrader){
        		if (NodeDetails.trader!=null){
        		    	//start timer
			    	long startTime = System.nanoTime();
	    			depositRequest();
	    			//end timer
	    			long endTime=System.nanoTime();
        			double duration = (double)((endTime - startTime)/1000000.0);
        			NodeDetails.runningTime+=duration;
        			Log.l.log(Log.finer, NodeDetails.getNode()+": This transaction took "+duration+"ms.");
        			System.out.println(NodeDetails.getNode()+": This transaction took "+duration+"ms.\n");
        			sellwritetofile.append(duration).append(",");
        			File f = new File("sell_transaction.txt");
        			try {
        			    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        			    bw.write(sellwritetofile.toString());
        			    bw.flush();
        			    bw.close();
        			    
        			} catch (IOException e) {
        			    // TODO Auto-generated catch block
        			    e.printStackTrace();
        			}
        			
        			//Sleep for TIMEOUT/3 before sending next request
	    			try {
	    			    Thread.sleep(TIMEOUT/3);
	        		}
	        		catch (InterruptedException e) {
	        		    // TODO Auto-generated catch block
	        		    e.printStackTrace();
	        		}
    	    		}
        		else{
        		    	System.out.println(NodeDetails.getNode()+": No trader available");
        		}
        	    }
    		}
    	    }
	}
