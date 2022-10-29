import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface {
    static ArrayList<Place> list;
    protected PlaceManager() throws RemoteException {
        list = new ArrayList<>();
    }

    @Override
    public void addPlace(Place p) throws RemoteException {
        list.add(p);
    }

    @Override
    public ArrayList<Place> allPlaces() throws RemoteException {
        return list;
    }
}
