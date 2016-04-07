package bazaar;

import java.io.Serializable;
import java.util.Comparator;

public class TimestampComparator implements Comparator<RequestMsg>,Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3838543079091584298L;

    public TimestampComparator() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public int compare(RequestMsg o1, RequestMsg o2) {
	if (o1.timestamp < o2.timestamp)
	    return -1;
	else if (o1.timestamp > o2.timestamp)
	    return 1;
	return 0;
	// TODO Auto-generated method stub
    }

}
