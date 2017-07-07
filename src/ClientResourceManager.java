import java.util.ArrayList;


public class ClientResourceManager {
    private int clientNumber;
    private ArrayList<Integer> allocatedResources;

    ClientResourceManager(int clientNumber, ArrayList<Integer> allocatedResources) {
        this.clientNumber = clientNumber;
        this.allocatedResources = allocatedResources;
    }

    @Override
    public String toString() {
        return (new StringBuilder("clientNumber: "))
                .append(clientNumber)
                .append(", allocatedResources: ")
                .append(allocatedResources.toString())
                .toString();
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void addResource(int resNo) {
        allocatedResources.add(resNo);
    }

    public void deallocateResource(int j) {
        allocatedResources.remove(allocatedResources.indexOf(j));
    }

    public ArrayList<Integer> getAllocatedResources() {
        return allocatedResources;
    }
}
