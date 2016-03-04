package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BazaarInterface extends Remote {
    public void lookUp(LookupMsg lm) throws RemoteException;
    public void reply(ReplyMsg seller) throws RemoteException;

}
