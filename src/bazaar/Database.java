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
    public void updateDB(Product prod, ArrayList<RequestMsg> sellers, Boolean isTraderNorth) throws RemoteException {
		// TODO Auto-generated method stub
		StringBuilder dbwritetofile = new StringBuilder();
		long startTime=System.nanoTime();
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
	    	System.out.println(NodeDetails.getNode()+":[Database Update] Updating cache for "+prod);
	    	File db = new File(filename);
	    	StringBuilder writeToFile = new StringBuilder();
	    	for (int iter=0; iter<sellers.size(); iter++){
	    	    	System.out.println(NodeDetails.getNode()+":[Database Update] "+sellers.get(iter).toString());
		    	writeToFile.append(sellers.get(iter).count+","+sellers.get(iter).requestingNode.ip+":"+sellers.get(iter).requestingNode.port+"\n");   
	    	}
	    	synchronized (this) {
        	    	try {
        	    	    BufferedWriter bw = new BufferedWriter(new FileWriter(db, false));
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
	    	StringBuilder lookupName = new StringBuilder("//");
	    	Neighbor n;
	    	if (isTraderNorth){
	    	    n = NodeDetails.traderSouth;
	    	}
	    	else{
	    	    n = NodeDetails.traderNorth;
	    	}
	    	String l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
	    	try {
	    	    obj = (BazaarInterface)Naming.lookup(l);
	    	    System.out.println(NodeDetails.getNode()+":[Database Update] Invalidating cache of Trader "+Bazaar.GenerateID(n.ip+n.port)+"@"+n.ip+":"+n.port);
	    	    obj.invalidateCache(prod);
	      	}
	    	catch (Exception e) {
	    	    System.err.println(NodeDetails.getNode()+":[Database Update] Invalidate Cache failed to "+l);
	    	    e.printStackTrace();
	      	}
	    	long endTime=System.nanoTime();
        	double duration = (double)((endTime - startTime)/1000000.0);
        	NodeDetails.runningTime+=duration;
        	Log.l.log(Log.finer, NodeDetails.getNode()+":[Database Update] This transaction took "+duration+"ms.");
        	System.out.println(NodeDetails.getNode()+":[Database Update] This update request processing took "+duration+"ms.\n");
        	dbwritetofile.append(duration).append(",");
        	File f = new File(NodeDetails.getNode()+"_db_update.txt");
        	try {
        	    BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
        	    bw.write(dbwritetofile.toString());
        	    bw.flush();
        	    bw.close();            	    
        	} catch (IOException e) {
        	    // TODO Auto-generated catch block
        	    e.printStackTrace();
        	}
    }

    @Override
    public ArrayList<RequestMsg> lookUp(Product prod) throws RemoteException {
	// TODO Auto-generated method stub
	StringBuilder dbwritetofile = new StringBuilder();
	long startTime=System.nanoTime();
	ArrayList<RequestMsg> sellers = new ArrayList<RequestMsg>();
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
    	System.out.println(NodeDetails.getNode()+":[Database Lookup] Looking up for "+prod);
    	synchronized (this) {
	    	try {
	    	    if (db.exists()){
        	    	    BufferedReader fr = new BufferedReader(new FileReader(db));
        	    	    String line;
        	    	    while((line=fr.readLine())!=null){
        	    		String[] seller = line.split(",");
        	    		String[] ipport = seller[1].split(":");
        	    		int sellerCount = Integer.parseInt(seller[0]);
        	    		String ip = ipport[0];
        	    		int port = Integer.parseInt(ipport[1]);
        	    		Neighbor n = new Neighbor(Bazaar.GenerateID(seller[1]),ip,port);
        	    		RequestMsg sellerDet = new RequestMsg();
        	    		sellerDet.prod=prod;
        	    		sellerDet.count=sellerCount;
        	    		sellerDet.requestingNode=n;
        	    		sellers.add(sellerDet);
        	    		System.out.println(NodeDetails.getNode()+":[Database Lookup] "+sellerDet.toString());
        	    	    }
        	    	    fr.close();
	    	    }
	    	} catch (FileNotFoundException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	} catch (IOException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	}
    	}
    	long endTime=System.nanoTime();
	double duration = (double)((endTime - startTime)/1000000.0);
	NodeDetails.runningTime+=duration;
	Log.l.log(Log.finer, NodeDetails.getNode()+":[Database Lookup] This transaction took "+duration+"ms.");
	System.out.println(NodeDetails.getNode()+":[Database Lookup] This buy lookup processing took "+duration+"ms.\n");
	dbwritetofile.append(duration).append(",");
	File f = new File(NodeDetails.getNode()+"_db_lookup.txt");
	try {
	    BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
	    bw.write(dbwritetofile.toString());
	    bw.flush();
	    bw.close();            	    
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return sellers;
    }

}
