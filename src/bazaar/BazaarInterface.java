package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BazaarInterface extends Remote {
	public void lookUp(LookupMsg msg) throws RemoteException;

}
