package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BazaarInterface extends Remote {
    public boolean buy(Request req) throws RemoteException;
    public boolean deposit(Request req) throws RemoteException;
    public void credit(double creditAmount);
    public void startElection(ElectionMsg exclude) throws RemoteException;
    public void election(ElectionMsg incomingElectionMsg) throws RemoteException;
    public String getTraderDetails();
}
