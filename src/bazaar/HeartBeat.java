package bazaar;

import java.rmi.Naming;

public class HeartBeat implements Runnable{
	Neighbor otherTrader;
	
	public HeartBeat(Neighbor n){
		otherTrader=n;
	}
	@Override
	public void run() {
		BazaarInterface obj = null;
  	    StringBuilder lookupName = new StringBuilder("//");
  	    String l = lookupName.append(otherTrader.ip).append(":").append(otherTrader.port).append("/Node").toString();
  	    while(true)
  	    {	TraderDetails.isOtherTraderUp = false;
  	    	try {
				obj = (BazaarInterface)Naming.lookup(l);
				obj.heartbeatRequest(NodeDetails.getCurrentNode());
				System.out.println(NodeDetails.getNode()+": Heartbeat request sent to "+otherTrader.port+"\n");
	  	    }
	  	    catch (Exception e) {
				System.err.println(NodeDetails.getNode()+": Heartbeat failed to "+l);
				e.printStackTrace();
	  	    }
  	    	//wait for a response
  	    	try{
  	  	    	Thread.sleep(Bazaar.TIMEOUT);
  	    	}
  	    	catch(Exception e) {
  	  	    	e.printStackTrace();
  	    	}
  	    	if(!TraderDetails.isOtherTraderUp){
  	    		//if I'm northtrader set southtrader to null and vice versa
  	    		boolean isNorth = false;
  	    		if(NodeDetails.isTraderNorth)
  	    			NodeDetails.traderSouth=null;
  	    		else{
  	    			NodeDetails.traderNorth=null;
  	    			isNorth=true;
  	    		}
  	    		for(Neighbor n : NodeDetails.next ){
  	    			//build lookup name for RMI object based on neighbor's ip & port
  	  	    		Log.l.log(Log.finer, NodeDetails.getNode()+": take other trader down "+n.id+"@"+n.ip+":"+n.port);
  	  		      	lookupName = new StringBuilder("//");
  	  		      	l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
  	  		      	try {
  	  		    		obj = (BazaarInterface)Naming.lookup(l);
  	  		    		obj.setTraderDown(isNorth);
  	  		      	}
  	  		      	catch (Exception e) {
  	  	    			System.err.println(NodeDetails.getNode()+": set trader down failed to "+l);
  	  		    		e.printStackTrace();
  	  		      	}
  	      		}//for
  	    		break; //stop the heartbeats since other trader is down
  	  		}//if
  	    }//while
	}//run

}//class
