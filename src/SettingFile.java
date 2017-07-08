import java.io.*;
import java.util.ArrayList;

/**
 * Created by amiiir on 7/8/17.
 */
class SettingFile {
    private BufferedReader reader;
    private PrintWriter writer;


    ArrayList<Integer> getSetting(String path) {

        ArrayList<Integer> res = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(path));
            for (int i = 0; i < 4; i++) {
                String[] temp = reader.readLine().split(":");
                res.add(Integer.valueOf(temp[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    boolean setSetting(ArrayList<Integer> setting, String path) {
        try {
            writer = new PrintWriter(new FileWriter(path, false), true);
            writer.println("N_CLIENTS:" + setting.get(0));
            writer.println("N_RESOURCES:" + setting.get(1));
            writer.println("DEADLOCK_MODE:" + setting.get(2));
            writer.println("PORT_NUMBER:" + setting.get(3));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            writer.close();
        }
        return true;
    }
}
