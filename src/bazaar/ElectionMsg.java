package bazaar;

import java.io.Serializable;

public class ElectionMsg implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1621059963545535744L;
	ElectionMsgType type;
	Neighbor detail; 
	Neighbor excludedNode;
	/*
	 * if type=Victory, detail=winner
	 * if type=Enquiry, detail=Initiator, 
	 * If type=ALIVE, detail=sender
	 * if type=EXCLUDE, detail=who to be excluded from election
	 */
	
	public ElectionMsg(ElectionMsgType type, Neighbor detail, Neighbor excluded) {
		this.type=type;
		this.detail=detail;
		excludedNode = excluded;
	}
}
