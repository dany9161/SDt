import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlacesListInterface extends Remote {
    void addPlace(Place p) throws RemoteException;
    ArrayList<Place> allPlaces() throws RemoteException;
}
