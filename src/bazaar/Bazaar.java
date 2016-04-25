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
	public static int MAXHEARTBEAT=500;
			
	public static int GenerateID(String ipport){
	    return ipport.hashCode() & Integer.MAX_VALUE;
	}
	public static void ReadConfiguration(String configFileName){
	    //Reading the XML configuration file
	    Log.l.log(Log.finer, "Reading from configuration file");
	    System.out.println("Reading from configuration file");
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
    		NodeDetails.isDB=Boolean.parseBoolean(eElement.getElementsByTagName("databaseserver").item(0).getTextContent());
    		if (NodeDetails.isDB){
    		    Element neElement=(Element) eElement.getElementsByTagName("northtrader").item(0);
		    int nid = GenerateID(neElement.getAttribute("ip")+neElement.getAttribute("port"));
		    String nip = neElement.getAttribute("ip");
		    int nport = Integer.parseInt(neElement.getAttribute("port"));
		    NodeDetails.traderNorth = new Neighbor(nid,nip,nport);
		    neElement=(Element) eElement.getElementsByTagName("southtrader").item(0);
		    nid = GenerateID(neElement.getAttribute("ip")+neElement.getAttribute("port"));
		    nip = neElement.getAttribute("ip");
		    nport = Integer.parseInt(neElement.getAttribute("port"));
		    NodeDetails.traderSouth = new Neighbor(nid,nip,nport);
    		}
    		else{
        		NodeDetails.isTrader=Boolean.parseBoolean(eElement.getElementsByTagName("trader").item(0).getTextContent());
        		if (NodeDetails.isTrader){
                		NodeDetails.isTraderNorth=Boolean.parseBoolean(eElement.getElementsByTagName("north").item(0).getTextContent());
                		NodeDetails.isWeakTrader=Boolean.parseBoolean(eElement.getElementsByTagName("weak").item(0).getTextContent());
                		NodeList neList = eElement.getElementsByTagName("neighbor");
                		NodeDetails.next = new ArrayList<Neighbor>();
                		for (int temp1 = 0; temp1 < neList.getLength(); temp1++) {
                		    Element neElement = (Element) neList.item(temp1);
                		    int nid = GenerateID(neElement.getAttribute("ip")+neElement.getAttribute("port"));
                		    String nip = neElement.getAttribute("ip");
                		    int nport = Integer.parseInt(neElement.getAttribute("port"));
                		    NodeDetails.next.add(new Neighbor(nid,nip,nport));
            		    	}
                		Element neElement=(Element) eElement.getElementsByTagName("database").item(0);
            		    	int nid = GenerateID(neElement.getAttribute("ip")+neElement.getAttribute("port"));
            		    	String nip = neElement.getAttribute("ip");
            		    	int nport = Integer.parseInt(neElement.getAttribute("port"));
            		    	NodeDetails.db = new Neighbor(nid,nip,nport);
        		}
        		else{
        		    NodeDetails.isBuyer=Boolean.parseBoolean(eElement.getElementsByTagName("buyer").item(0).getTextContent());
        		    NodeDetails.isSeller=Boolean.parseBoolean(eElement.getElementsByTagName("seller").item(0).getTextContent());
        		    NodeDetails.isTraderNorth=false;
        		    NodeDetails.isWeakTrader=false;
        		    NodeDetails.next = new ArrayList<Neighbor>();
        		}
        		TIMEOUT=Integer.parseInt(eElement.getElementsByTagName("timeout").item(0).getTextContent());
        		ITER_COUNT=Integer.parseInt(eElement.getElementsByTagName("iterations").item(0).getTextContent());
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
	public static Product pickRandomProduct(){
	    int pick = RANDOM.nextInt(100);
	    return Product.values()[pick%Product.values().length];
		
	}
	
	//This function randomly assigns a count<10 of the product that the seller has
	public static int pickRandomCount(){
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
		Log.l.log(Log.finest, NodeDetails.getNode()+": Creating buy request");
		Neighbor trader = NodeDetails.randomTrader();
		//build lookup name for RMI object based on trader's ip & port
		Log.l.log(Log.finer, NodeDetails.getNode()+": Looking up for "+trader.id+"@"+trader.ip+":"+trader.port);
		System.out.println(NodeDetails.getNode()+": Buying "+NodeDetails.buyProd+"X"+NodeDetails.buyCount+" from "+trader.id+"@"+trader.ip+":"+trader.port);
		StringBuilder lookupName= new StringBuilder("//");
		String lookupstring = lookupName.append(trader.ip).append(":").append(trader.port).append("/Node").toString();
		Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + lookupstring);
		try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup(lookupstring);
		    obj.buy(req);
		    System.out.println(NodeDetails.getNode()+": Buy request queued at trader.");
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
		BazaarInterface obj = null;
		Neighbor t = NodeDetails.randomTrader();
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
	    } 
	    catch (ExportException e) {
	    	try {
	    		LocateRegistry.getRegistry(NodeDetails.port);
	    		//if registry already exists, locate it
	    	} 
	    	catch (RemoteException e1) {
	    		// TODO Auto-generated catch block
	    		System.err.println(NodeDetails.getNode()+": BazaarNode exception: RMI registry already exists");
	    		e1.printStackTrace();
	    	}
	    } 
	    catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	    //If the node is a database server
	    if (NodeDetails.isDB){
        	    //Bind the RMI object to listen 
                String name = "//localhost:"+NodeDetails.port+"/Database";
                bazaar.Database engine = null;
        	    try {
        			engine = new bazaar.Database();
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
        	    Log.l.log(Log.finest, NodeDetails.getNode()+": DatabaseNode bound");
	    }
	    else{
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

	    }
	    
	    
	    
	    //If the node is a trader, inform all other nodes in the network
	    if (NodeDetails.isTrader){
		try {
		    NodeDetails.traderDetails = new TraderDetails();
		    NodeDetails.traderDetails.isCacheValid.put(Product.BOAR, false);
		    NodeDetails.traderDetails.isCacheValid.put(Product.FISH, false);
		    NodeDetails.traderDetails.isCacheValid.put(Product.SALT, false);
		} catch (RemoteException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
	    	BazaarInterface obj = null;
	    	if (NodeDetails.isTraderNorth){
	    		System.out.println(NodeDetails.getNode()+":[Trader] Informing I am the North Trader to all nodes");
	    		NodeDetails.traderNorth=NodeDetails.getCurrentNode();
	    	}
	    	else{
	    		System.out.println(NodeDetails.getNode()+":[Trader] Informing I am the South Trader to all nodes");
	    		NodeDetails.traderSouth=NodeDetails.getCurrentNode();    	    		
	    	}
	    	while(NodeDetails.isTraderNorth && NodeDetails.traderSouth==null){
	    	    try {
			Thread.sleep(TIMEOUT/3);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
	    	}
	    	//Create a trader info message
	    	ElectionMsg traderMsg = new ElectionMsg(NodeDetails.getCurrentNode(),NodeDetails.isTraderNorth);
	    	
	    	//..and send it to all nodes
	    	for(Neighbor n : NodeDetails.next ){
  		    //build lookup name for RMI object based on neighbor's ip & port
	      	    StringBuilder lookupName = new StringBuilder("//");
	      	    String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
	      	    try {
	    			obj = (BazaarInterface)Naming.lookup(l);
	    			obj.traderInfo(traderMsg);
	      	    }
	      	    catch (Exception e) {
	    			System.err.println(NodeDetails.getNode()+": Election failed to "+l);
	    			e.printStackTrace();
	      	    }
	    	}
	    }
	    //not a trader so wait until a trader is elected
	    else{	
	    	while(NodeDetails.traderNorth==null || NodeDetails.traderSouth==null){
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
			NodeDetails.startHeartbeat();
		    	NodeDetails.processBuyQueue();
		    }
		    
		    //If you are a Buyer and a Seller (and not a Trader), send buy and deposit requests to 
		    //one of the traders
		    if (NodeDetails.isBuyer && NodeDetails.isSeller && !NodeDetails.isTrader){
		 	    if(NodeDetails.isTraderAvailable()){
			    	//start timer
				    long startTime = System.nanoTime();
	    		    buyRequest();
	    		    //end timer
	    		    long endTime=System.nanoTime();
	    			double duration = (double)((endTime - startTime)/1000000.0);
	    			NodeDetails.runningTime+=duration;
	    			Log.l.log(Log.finer, NodeDetails.getNode()+": This buy request took "+duration+"ms.");
	    			System.out.println(NodeDetails.getNode()+": This buy request took "+duration+"ms.\n");
	    			buywritetofile.append(duration).append(",");
	    			File file_buy = new File(NodeDetails.getNode()+"_buy_requests.txt");
	    			try {
	    			    BufferedWriter bw = new BufferedWriter(new FileWriter(file_buy));
	    			    bw.write(buywritetofile.toString());
	    			    bw.flush();
	    			    bw.close();
	    			} 
	    			catch (IOException e) {
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
	    		    //now make a sell request
				  	//start timer
				   	startTime = System.nanoTime();
	    			depositRequest();
	    			//end timer
	    			endTime=System.nanoTime();
	    			duration = (double)((endTime - startTime)/1000000.0);
	    			NodeDetails.runningTime+=duration;
	    			Log.l.log(Log.finer, NodeDetails.getNode()+": This deposit request took "+duration+"ms.");
	    			System.out.println(NodeDetails.getNode()+": This deposit request took "+duration+"ms.\n");
	    			sellwritetofile.append(duration).append(",");
	    			File file_deposit = new File(NodeDetails.getNode()+"_deposit_requests.txt");
	    			try {
	    			    BufferedWriter bw = new BufferedWriter(new FileWriter(file_deposit));
	    			    bw.write(sellwritetofile.toString());
	    			    bw.flush();
	    			    bw.close();    
	    			} 
	    			catch (IOException e) {
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
		 	    	System.out.println(NodeDetails.getNode()+": No trader available\n");
		 	    }
		    }
		    
		    //If you are a Buyer (and not a Trader), send buy requests to the trader
		    else if (NodeDetails.isBuyer && !NodeDetails.isTrader){
		    	if(NodeDetails.isTraderAvailable()){
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
	    			File f = new File(NodeDetails.getNode()+"_buy_requests.txt");
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
    		    	System.out.println(NodeDetails.getNode()+": No trader available\n");
		    	}
    	    }
		    //If you are a Seller (and not a Trader), send deposit requests to the trader
		    else if (NodeDetails.isSeller && !NodeDetails.isTrader){
		    	if (NodeDetails.isTraderAvailable()){
    		    	//start timer
			    	long startTime = System.nanoTime();
	    			depositRequest();
	    			//end timer
	    			long endTime=System.nanoTime();
	    			double duration = (double)((endTime - startTime)/1000000.0);
	    			NodeDetails.runningTime+=duration;
	    			Log.l.log(Log.finer, NodeDetails.getNode()+": This deposit request took "+duration+"ms.");
	    			System.out.println(NodeDetails.getNode()+": This deposit request took "+duration+"ms.\n");
	    			sellwritetofile.append(duration).append(",");
	    			File f = new File(NodeDetails.getNode()+"_deposit_requests.txt");
	    			try {
	    			    BufferedWriter bw = new BufferedWriter(new FileWriter(f,true));
	    			    bw.write(sellwritetofile.toString());
	    			    bw.flush();
	    			    bw.close();	    			    
	    			} 
	    			catch (IOException e) {
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
    		    	System.out.println(NodeDetails.getNode()+": No trader available\n");
		    	}//else
    	    }//if
		}//for iter
	}//main
}//class
