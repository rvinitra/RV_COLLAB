package bazaar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Database extends UnicastRemoteObject implements DatabaseInterface, Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected Database() throws RemoteException {
	super();
	// TODO Auto-generated constructor stub
    }

    @Override
    public void increament(RequestMsg req, Boolean isTraderNorth) throws RemoteException {
		// TODO Auto-generated method stub
	    	String filename;
	    	if (req.prod == Product.BOAR){
	    	    filename="db_BOAR.txt";
	    	}
	    	else if (req.prod == Product.FISH){
	    	    filename="db_FISH.txt";
	    	}
	    	else{
	    	    filename="db_SALT.txt";
	    	}
	    	System.out.println(NodeDetails.getNode()+":[Database] increamenting "+req.prod+"X"+req.count+" from seller "+req.requestingNode.toString());
	    	File db = new File(filename);
	    	StringBuilder writeToFile = new StringBuilder();
	    	writeToFile.append(req.count+","+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
	    	System.out.println(NodeDetails.getNode()+":[Database] DB File "+filename);
	    	synchronized (this) {
        	    	try {
        	    	    BufferedWriter bw = new BufferedWriter(new FileWriter(db, true));
        	    	    bw.write(writeToFile.toString());
        	    	    bw.flush();
        	    	    bw.close();
        	    	} catch (FileNotFoundException e) {
        	    	    // TODO Auto-generated catch block
        	    	    e.printStackTrace();
        	    	} catch (IOException e) {
        	    	    // TODO Auto-generated catch block
        	    	    e.printStackTrace();
        	    	}
	    	}
	    	BazaarInterface obj = null;
	    	ArrayList<SellerDetails> latestCache = new ArrayList<SellerDetails>();
	    	StringBuilder lookupName = new StringBuilder("//");
	    	Neighbor n;
	    	if (isTraderNorth){
	    	    n = NodeDetails.traderNorth;
	    	}
	    	else{
	    	    n = NodeDetails.traderSouth;
	    	}
	    	String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
	    	try {
	    	    obj = (BazaarInterface)Naming.lookup(l);
	    	    //obj.updateCache(latestCache);
	      	}
	    	catch (Exception e) {
	    	    System.err.println(NodeDetails.getNode()+":Election failed to "+l);
	    	    e.printStackTrace();
	      	}
    }

    @Override
    public void decreament(RequestMsg req, Boolean isTraderNorth) throws RemoteException {
		// TODO Auto-generated method stub
	    	String filename;
	    	if (req.prod == Product.BOAR){
	    	    filename="db_BOAR.txt";
	    	}
	    	else if (req.prod == Product.FISH){
	    	    filename="db_FISH.txt";
	    	}
	    	else{
	    	    filename="db_SALT.txt";
	    	}
	    	System.out.println(NodeDetails.getNode()+":[Database] decreamenting "+req.prod+"X"+req.count);
	    	File db = new File(filename);
	    	int num = req.count;
	    	System.out.println(NodeDetails.getNode()+":[Database] DB File "+filename);
	    	synchronized (this) {
        	    	try {
        	    	    BufferedReader fr = new BufferedReader(new FileReader(db));
        	    	    boolean sold=false;
        	    	    String line;
        	    	    StringBuilder writeToFile = new StringBuilder();
        	    	    while((line=fr.readLine())!=null){
        	    		String[] seller = line.split(",");
        	    		int sellerCount = Integer.parseInt(seller[0]);
        	    		if (!sold && sellerCount < num){
        	    		    System.out.println(NodeDetails.getNode()+":[Database] decreamenting "+req.prod+"X"+seller[0]+" from seller "+seller[1]);
        	    		    num-=Integer.parseInt(seller[0]);
        	    		}
        	    		else if (!sold && sellerCount > num){
        	    		    sold=true;
        	    		    System.out.println(NodeDetails.getNode()+":[Database] decreamenting "+req.prod+"X"+num+" out of "+sellerCount+" from seller "+seller[1]);
        	    		    writeToFile.append(sellerCount-num+","+seller[1]+"\n");
        	    		    num=0;
        	    		}
        	    		else if (!sold && sellerCount == num){
        	    		    System.out.println(NodeDetails.getNode()+":[Database] decreamenting "+req.prod+"X"+sellerCount+" from seller "+seller[1]);
        	    		    num=0;
        	    		    sold=true;
        	    		}
        	    		else if (sold){
        	    		    writeToFile.append(line).append("\n");
        	    		}
        	    	    }
        	    	    fr.close();
        	    	    BufferedWriter bw = new BufferedWriter(new FileWriter(db));
        	    	    if (sold){	    
        	    		bw.write(writeToFile.toString());
        	    	    }
                	    else{
                		bw.write("");	
                	    }
        	    	    bw.flush();
                	    bw.close();
        	    	} catch (FileNotFoundException e) {
        	    	    // TODO Auto-generated catch block
        	    	    e.printStackTrace();
        	    	} catch (IOException e) {
        	    	    // TODO Auto-generated catch block
        	    	    e.printStackTrace();
        	    	}
	    	}
    }

    @Override
    public ArrayList<SellerDetails> lookup(Product prod) throws RemoteException {
	// TODO Auto-generated method stub
	ArrayList<SellerDetails> sellers = new ArrayList<SellerDetails>();
	String filename;
    	if (prod == Product.BOAR){
    	    filename="db_BOAR.txt";
    	}
    	else if (prod == Product.FISH){
    	    filename="db_FISH.txt";
    	}
    	else{
    	    filename="db_SALT.txt";
    	}
    	File db = new File(filename);
    	System.out.println(NodeDetails.getNode()+":[Database] DB File "+filename);
    	synchronized (this) {
	    	try {
	    	    BufferedReader fr = new BufferedReader(new FileReader(db));
	    	    String line;
	    	    while((line=fr.readLine())!=null){
	    		String[] seller = line.split(",");
	    		String[] ipport = seller[1].split(":");
	    		int sellerCount = Integer.parseInt(seller[0]);
	    		String ip = ipport[0];
	    		int port = Integer.parseInt(ipport[1]);
	    		Neighbor n = new Neighbor(Bazaar.GenerateID(seller[1]),ip,port);
	    		SellerDetails sellerDet = new SellerDetails(n,sellerCount);
	    		sellers.add(sellerDet);
	    		System.out.println(NodeDetails.getNode()+":[Database] For "+prod+" seller "+sellerDet.toString());
	    	    }
	    	    fr.close();
	    	} catch (FileNotFoundException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	} catch (IOException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	}
    	}
	return sellers;
    }

}
