package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BazaarInterface extends Remote {
    public void buy(RequestMsg req) throws RemoteException;
    public void deposit(RequestMsg req) throws RemoteException;
    public void credit(double creditAmount) throws RemoteException;
    /*public void startElection(ElectionMsg exclude) throws RemoteException;
    public void election(ElectionMsg incomingElectionMsg) throws RemoteException;*/
    public void takeTraderDetails(TraderDetails t) throws RemoteException;
    public void getTraderDetails(Neighbor n) throws RemoteException;
}
