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
  	    while(NodeDetails.traderNorth!=null && NodeDetails.traderSouth!=null)
  	    {	TraderDetails.isOtherTraderUp = false;
  	    	try {
				obj = (BazaarInterface)Naming.lookup(l);
				obj.heartbeatRequest(NodeDetails.getCurrentNode());
				System.out.println(NodeDetails.getNode()+":[Trader Heartbeat] Heartbeat request to "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port);
	  	    }
	  	    catch (Exception e) {
				System.err.println(NodeDetails.getNode()+":[Trader Heartbeat] Heartbeat request failed to "+l);
				e.printStackTrace();
	  	    }
  	    	//wait for a response
  	    	try{
  	  	    	Thread.sleep(Bazaar.TIMEOUT/10);
  	    	}
  	    	catch(Exception e) {
  	  	    	e.printStackTrace();
  	    	}
  	    	if(!TraderDetails.isOtherTraderUp){
  	    	    System.out.println(NodeDetails.getNode()+":[Trader Takeover] Heartbeat response not received from "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port);
  	    		//if I'm northtrader set southtrader to null and vice versa
  	    		boolean isNorth = false;
  	    		if(!NodeDetails.isTraderNorth)
  	    		    	isNorth=true;
  	    		System.out.println(NodeDetails.getNode()+":[Trader Takeover] Broadcasting to all nodes that "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port+" is down");
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
  	  	    			System.err.println(NodeDetails.getNode()+":[Trader Takeover] set trader down failed to "+l);
  	  		    		e.printStackTrace();
  	  		      	}
  	      		}//for
  	    		lookupName = new StringBuilder("//");
  	    		Neighbor n;
  	    		if (NodeDetails.isTraderNorth){
  	    		    n = NodeDetails.traderSouth;
  	    		}
  	    		else{
  	    		    n= NodeDetails.traderNorth;
  	    		}
  	    		l = lookupName.append(n.ip).append(":").append(n.port).append("/Node").toString();
  	    		System.out.println(NodeDetails.getNode()+":[Trader Takeover] Taking over from "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port);
  	    		try {
  	    		    obj = (BazaarInterface)Naming.lookup(l);
  	    		    obj.getTraderDetails(NodeDetails.getCurrentNode());
  	    		}
  	    		catch (Exception e) {
  	    		    System.err.println(NodeDetails.getNode()+":[Trader Takeover] Taking over failed to "+l);
  	    		    e.printStackTrace();
  	    		}
  	    		if(NodeDetails.isTraderNorth)
	    			NodeDetails.traderSouth=null;
	    		else{
	    			NodeDetails.traderNorth=null;
	    			isNorth=true;
	    		}
  	    		break; //stop the heartbeats since other trader is down
  	  		}//if
  	    	else{
  	    	System.out.println(NodeDetails.getNode()+":[Trader Heartbeat] Heartbeat response received from "+otherTrader.id+"@"+otherTrader.ip+":"+otherTrader.port+"\n");
  	    	}
  	    }//while
	}//run

}//class
