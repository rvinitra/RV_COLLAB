package bazaar;

//import java.io.Serializable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Trader implements Runnable{
    /**
     * 
     */
    //private static final long serialVersionUID = -8583699888820980147L;
    RequestMsg req;
    public Trader() {
	// TODO Auto-generated constructor stub
    }
    public Trader(RequestMsg req) {
	// TODO Auto-generated constructor stub
	this.req=req;
    }
    
    //Trader thread for processing each of the BOAR, FISH and SALT Queue
    @Override
    public void run() {
		// TODO Auto-generated method stub
		StringBuilder traderwritetofile = new StringBuilder();
		long startTime=System.nanoTime();
		System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Processing buy request "+req.prod+"X"+req.count+" from "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port+" from the Queue");
		if (!NodeDetails.traderDetails.isCacheValid.get(req.prod)){
		    System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Invalid cache for "+req.prod+". Looking up Database Server");
		    StringBuilder lookupName= new StringBuilder("//");
		    String l= lookupName.append(NodeDetails.db.ip).append(":").append(NodeDetails.db.port).append("/Database").toString();
		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
		    try {
			DatabaseInterface obj = null;
			obj = (DatabaseInterface)Naming.lookup(l);
			switch(req.prod){
				case BOAR: 
	                    	    synchronized (TraderDetails.boarSellerStockLock){
	                    		NodeDetails.traderDetails.boarSellerStock=obj.lookUp(req.prod);
	                    	    } break;
	                    	case SALT: 
	                    	    synchronized (TraderDetails.saltSellerStockLock){
	                    		NodeDetails.traderDetails.saltSellerStock=obj.lookUp(req.prod);
	                    	    } break;
	                    	case FISH: 
	                    	    synchronized (TraderDetails.fishSellerStockLock){
	                    		NodeDetails.traderDetails.fishSellerStock=obj.lookUp(req.prod);
	                    	    } break;
	                    	default:
	                    	    break;
			}
		    }
		    catch (Exception e) {
			System.err.println(NodeDetails.getNode()+":[Trader Process Buy] Looking up Database Server "+l+" failed");
			e.printStackTrace();
		    }
		    NodeDetails.traderDetails.isCacheValid.put(req.prod, true);
		    System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Retreived latest data from Database Server for "+req.prod+". Cache valid.");
		}
		else
		{
		    System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Cache valid for "+req.prod);
		}
		RequestMsg sellerRequest = null;
		ArrayList<RequestMsg> potentialSellers = null;
                switch(req.prod){
                	case BOAR: 
                	    synchronized (TraderDetails.boarSellerStockLock){
                		potentialSellers=NodeDetails.traderDetails.boarSellerStock;
            		} break;
                	case SALT: 
                	    synchronized (TraderDetails.saltSellerStockLock){
            		    	potentialSellers=NodeDetails.traderDetails.saltSellerStock;
            		} break;
                	case FISH: 
                	    synchronized (TraderDetails.fishSellerStockLock){
            		    	potentialSellers=NodeDetails.traderDetails.fishSellerStock;
            		} break;
                	default:
                	    break;
                }
                //To store the list of sellers from whom the product is sold
                Queue<SellerDetails> sellers = new LinkedList<SellerDetails>();
                SellerDetails seller = null;
                int count = req.count;
                if (potentialSellers.size()==0){
        		Log.l.log(Log.finer, "No potential sellers");
        		System.out.println(NodeDetails.getNode()+":[Trader Process Buy] No sellers for "+req.prod+"X"+req.count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
        		long endTime=System.nanoTime();
                	double duration = (double)((endTime - startTime)/1000000.0);
                	NodeDetails.runningTime+=duration;
                	Log.l.log(Log.finer, NodeDetails.getNode()+":[Trader] This transaction took "+duration+"ms.");
                	System.out.println(NodeDetails.getNode()+":[Trader Process Buy] This buy request processing took "+duration+"ms.\n");
                	traderwritetofile.append(duration).append(",");
                	File f = new File(NodeDetails.getNode()+"_trader_"+req.prod+"_buy_requests.txt");
                	try {
                	    BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                	    bw.write(traderwritetofile.toString());
                	    bw.flush();
                	    bw.close();            	    
                	} catch (IOException e) {
                	    // TODO Auto-generated catch block
                	    e.printStackTrace();
                	}
        		return;
                }
                switch(req.prod){
                	case BOAR: 
                	    synchronized (TraderDetails.boarSellerStockLock){
                		if (NodeDetails.traderDetails.boarSellerStock.size()!=0)
                		    sellerRequest = NodeDetails.traderDetails.boarSellerStock.remove(0);
                	    } break;
                	case SALT: 
                	    synchronized (TraderDetails.saltSellerStockLock){
                		if (NodeDetails.traderDetails.saltSellerStock.size()!=0)
                		    sellerRequest = NodeDetails.traderDetails.saltSellerStock.remove(0);
                	    } break;
                	case FISH: 
                	    synchronized (TraderDetails.fishSellerStockLock){
                		if (NodeDetails.traderDetails.fishSellerStock.size()!=0)
                		    sellerRequest = NodeDetails.traderDetails.fishSellerStock.remove(0);
                	    } break;
                	default:
                	    break;
                }
        	    
                //If the count of the potential seller is less than the BuyRequest
                while (sellerRequest!=null){
                    	seller = new SellerDetails(sellerRequest.requestingNode,sellerRequest.count);
                    	sellerRequest=null;
        		if(req.count >= seller.count){
           			req.count-=seller.count;
           			sellers.add(seller);
                   	    	switch(req.prod){
                           	    	case BOAR: 
                                	    synchronized (TraderDetails.boarSellerStockLock){
                                		if (NodeDetails.traderDetails.boarSellerStock.size()!=0)
                                		    sellerRequest = NodeDetails.traderDetails.boarSellerStock.remove(0);
                                	    } break;
                                	case SALT: 
                                	    synchronized (TraderDetails.saltSellerStockLock){
                                		if (NodeDetails.traderDetails.saltSellerStock.size()!=0)
                                		    sellerRequest = NodeDetails.traderDetails.saltSellerStock.remove(0);
                                	    } break;
                                	case FISH: 
                                	    synchronized (TraderDetails.fishSellerStockLock){
                                		if (NodeDetails.traderDetails.fishSellerStock.size()!=0)
                                		    sellerRequest = NodeDetails.traderDetails.fishSellerStock.remove(0);
                                	    } break;
                                	default:
                                	    break;
                            	}
           		    }
           		    else{
           			break;
           		    }
                }
        	    
                //No more potential sellers available for remaining count in BuyRequest
                if (sellerRequest==null){
        		    Log.l.log(Log.finer, "Partial sale made");
        		    System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Sold "+(count-req.count)+" out of "+count+" "+req.prod+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
                }
                else{
                    	//If count of seller is greater than the count in BuyRequest
            		if (req.count!=0){
        			SellerDetails partialSeller = new SellerDetails();
                		partialSeller.seller = seller.seller;
                		partialSeller.count = req.count;
                		sellers.add(partialSeller);
                		sellerRequest.count-=req.count;
                		switch(req.prod){
                                	case BOAR: 
                                	    synchronized (TraderDetails.boarSellerStockLock){
                                		NodeDetails.traderDetails.boarSellerStock.add(0, sellerRequest);
                                		} break;
                                	case SALT: 
                                	    synchronized (TraderDetails.saltSellerStockLock){
                                		NodeDetails.traderDetails.saltSellerStock.add(0, sellerRequest);
                                		} break;
                                	case FISH: 
                                	    synchronized (TraderDetails.fishSellerStockLock){
                                		NodeDetails.traderDetails.fishSellerStock.add(0, sellerRequest);
                                		} break;
                                	default:
                                	    break;
                		}
            		}
            		System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Sold "+req.prod+"X"+count+" requested by "+req.requestingNode.id+"@"+req.requestingNode.ip+":"+req.requestingNode.port);
                }
                StringBuilder lookupName= new StringBuilder("//");
                String l= lookupName.append(NodeDetails.db.ip).append(":").append(NodeDetails.db.port).append("/Database").toString();
                Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
                try {
                    DatabaseInterface obj = null;
                    obj = (DatabaseInterface)Naming.lookup(l);
                    switch(req.prod){
                    	case BOAR: 
                    	    synchronized (TraderDetails.boarSellerStockLock){
                    		obj.updateDB(req.prod,NodeDetails.traderDetails.boarSellerStock,NodeDetails.isTraderNorth);
                    	    } break;
                    	case SALT: 
                    	    synchronized (TraderDetails.saltSellerStockLock){
                    		obj.updateDB(req.prod,NodeDetails.traderDetails.saltSellerStock,NodeDetails.isTraderNorth);
                    	    } break;
                    	case FISH: 
                    	    synchronized (TraderDetails.fishSellerStockLock){
                    		obj.updateDB(req.prod,NodeDetails.traderDetails.fishSellerStock,NodeDetails.isTraderNorth);
                    	    } break;
                    	default:
                    	    break;
                    }
                    System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Updating Database Server for "+req.prod);
                }
                catch (Exception e) {
                    System.err.println(NodeDetails.getNode()+":[Trader Process Buy] Updating Database Server "+l+" failed");
                    e.printStackTrace();
                }
            	    
                //Credit money all sellers from whom product was sold
                while(!sellers.isEmpty()){
        		    SellerDetails creditseller = sellers.poll();
        		    if (seller!=null){
                		    StringBuilder lookupNameCredit= new StringBuilder("//");
                		    String lc= lookupNameCredit.append(creditseller.seller.ip).append(":").append(creditseller.seller.port).append("/Node").toString();
                		    Log.l.log(Log.finest, NodeDetails.getNode()+": Lookup string:" + l);
                		    try {
                			BazaarInterface obj = null;
                			obj = (BazaarInterface)Naming.lookup(lc); 
                			obj.credit(NodeDetails.getCreditAmount(req.prod));
                			System.out.println(NodeDetails.getNode()+":[Trader Process Buy] Crediting $"+NodeDetails.getCreditAmount(req.prod)+" for "+req.prod+" to "+creditseller.seller.id+"@"+creditseller.seller.ip+":"+creditseller.seller.port);
                		    }
                		    catch (Exception e) {
                			System.err.println(NodeDetails.getNode()+":[Trader Process Buy] Crediting to "+l+" failed");
                        			e.printStackTrace();
                		    }
        		    }
                }
                NodeDetails.traderDetails.transactionsCount++;
                long endTime=System.nanoTime();
                double duration = (double)((endTime - startTime)/1000000.0);
                NodeDetails.runningTime+=duration;
                Log.l.log(Log.finer, NodeDetails.getNode()+":[Trader Process Buy] This transaction took "+duration+"ms.");
                System.out.println(NodeDetails.getNode()+":[Trader Process Buy] This buy request processing took "+duration+"ms.\n");
                traderwritetofile.append(duration).append(",");
                File f = new File(NodeDetails.getNode()+"_trader_"+req.prod+"_buy_requests.txt");
                try {
                    	BufferedWriter bw = new BufferedWriter(new FileWriter(f,true));
            		bw.write(traderwritetofile.toString());
            		bw.flush();
            		bw.close();            	    
                } catch (IOException e) {
                    	// TODO Auto-generated catch block
            		e.printStackTrace();
                }
                return;
    }
}
