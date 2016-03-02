/**
 * 
 */
package bazaar;
import java.util.ArrayList;
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
	
	public static void main(String[] args) {
        	// run a loop where we create buyers and sellers
        	//in the creation - include node prop + neighbors
        	// call lookup
        	ReadConfiguration();
        	NodeDetails.Display();
        	System.setProperty("java.rmi.server.hostname",NodeDetails.ip);
                try {
                        	//LocateRegistry.createRegistry(1099);
                        	LocateRegistry.getRegistry(NodeDetails.port);
                
                } catch (RemoteException e) {
                    System.err.println("BazaarNode exception: RMI registry already exists");
                    e.printStackTrace();
                }
                String name = "//localhost/Node";
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
                
        		//Node p1 = new Node(1,"127.0.0.1",1099,true,Product.BOAR,-1,neighbors);
        		NodeDetails p2 = new NodeDetails();
        		NodeDetails.assignValues(2,"10.0.0.7",1099,false,Product.BOAR,3,neighbors);
        		
        		//p1.lookUp(Product.BOAR, 1);
        		//p2.reply(p2);
        		//p2.buy(p1);
        		
                try {
		    BazaarInterface obj = (BazaarInterface)Naming.lookup("//10.0.0.7/Node");
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
                //obj.lookUp(Product.SALT,1);
                //System.exit(0);
						
	}

}
