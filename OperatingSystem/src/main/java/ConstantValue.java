import java.util.ArrayList;

class ConstantValue {
    public static final int DEADLOCK_HANDLER_SLEEP = 1000;
    static int N_RESOURCES;
    static int N_CLIENTS;
    static int DEADLOCK_MODE;
    static int PORT_NUMBER;

    static final int MUTEX_LOCK = 1;
    static int NOTIFY_TIME_ELAPSE = 10000;
    static final String SERVER_ADDRESS = "127.0.0.1";
    static final String CONF_PATH = "./OperatingSystem/src/main/java/conf.txt";

    static void init(ArrayList<Integer> setting) {
        N_CLIENTS = setting.get(0);
        N_RESOURCES = setting.get(1);
        DEADLOCK_MODE = setting.get(2);
        PORT_NUMBER = setting.get(3);
    }
}
