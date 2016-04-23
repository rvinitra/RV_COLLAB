package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface DatabaseInterface extends Remote {
        public void updateDB(Product prod, ArrayList<RequestMsg> sellers, Boolean isTraderNorth) throws RemoteException;
        public ArrayList<RequestMsg> lookUp(Product prod) throws RemoteException;
}

