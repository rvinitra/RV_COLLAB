package bazaar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DatabaseInterface extends Remote {
        public void increament(RequestMsg req) throws RemoteException;
        public void decreament(RequestMsg req) throws RemoteException;
        public List<SellerDetails> lookup(Product prod) throws RemoteException;
}

