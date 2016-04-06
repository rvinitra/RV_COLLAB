package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BazaarInterface extends Remote {
    public void buy(Request req) throws RemoteException;
    public void deposit(Request req) throws RemoteException;
    public void credit(double creditAmount) throws RemoteException;
    public void startElection(ElectionMsg exclude) throws RemoteException;
    public void election(ElectionMsg incomingElectionMsg) throws RemoteException;
    public TraderDetails getTraderDetails() throws RemoteException;
    public void clockSync(int lamportClock) throws RemoteException;
}
