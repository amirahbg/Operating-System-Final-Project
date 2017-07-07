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
        for (int i = 0; i < ConstantValue.N_RESOURCES; i++) {
            builder.append('0');
        }
        for(Integer i: allocatedResources) {
            builder.replace(i, i + 1, "1");
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
