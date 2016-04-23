package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface DatabaseInterface extends Remote {
        public void increament(RequestMsg req, Boolean isTraderNorth) throws RemoteException;
        public void decreament(RequestMsg req, Boolean isTraderNorth) throws RemoteException;
        public ArrayList<SellerDetails> lookup(Product prod) throws RemoteException;
}

