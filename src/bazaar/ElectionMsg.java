package bazaar;

import java.io.Serializable;

public class ElectionMsg implements Serializable {
	ElectionMsgType type;
	Neighbor detail; 
	/*
	 * if type=Victory, detail=winner
	 * if type=Enquiry, detail=Initiator, 
	 * If type=ALIVE, detail=sender
	 * if type=EXCLUDE, detail=who to be excluded from election
	 */
	
	public ElectionMsg(ElectionMsgType type, Neighbor detail) {
		this.type=type;
		this.detail=detail;
	}

}
