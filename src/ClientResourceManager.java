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
        StringBuilder builder = new StringBuilder();
        for (Integer allocatedResource : allocatedResources) {
            if (1 == allocatedResource) {
                builder.append(1);
            } else {
                builder.append(0);
            }
        }
        return builder.toString();
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void addResource(int resNo) {
        allocatedResources.add(resNo);
    }

    public void deallocateResource(int j) {
        int i = allocatedResources.indexOf(j);
        if (i >= 0)
            allocatedResources.remove(i);
    }

    public ArrayList<Integer> getAllocatedResources() {
        return allocatedResources;
    }
}
