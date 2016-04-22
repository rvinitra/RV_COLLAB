package bazaar;

import java.io.Serializable;

public class ElectionMsg implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1621059963545535744L;
	Neighbor traderInfo; 
	//Neighbor excludedNode;
	boolean traderPost; //true-North, false-South
	public ElectionMsg(Neighbor traderInfo, boolean post) {
		
		this.traderInfo=traderInfo;
		this.traderPost=post;
	}
}
