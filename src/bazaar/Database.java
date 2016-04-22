package bazaar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

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
    public void increament(RequestMsg req) throws RemoteException {
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
	    	File db = new File(filename);
	    	StringBuilder writeToFile = new StringBuilder();
	    	writeToFile.append(req.count+","+req.requestingNode.ip+":"+req.requestingNode.port+"\n");
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
    }

    @Override
    public void decreament(RequestMsg req) throws RemoteException {
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
	    	File db = new File(filename);
	    	int num = req.count;
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
        	    		    num-=Integer.parseInt(seller[0]);
        	    		}
        	    		else if (!sold && sellerCount > num){
        	    		    sold=true;
        	    		    writeToFile.append(sellerCount-num+","+seller[1]+"\n");
        	    		    num=0;
        	    		}
        	    		else if (!sold && sellerCount == num){
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
    public List<SellerDetails> lookup(Product prod) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
    }

}
