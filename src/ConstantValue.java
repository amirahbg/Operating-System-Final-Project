import java.util.ArrayList;

class ConstantValue {
    static final int MUTEX_LOCK = 1;
    static int N_RESOURCES = 10;
    static int N_CLIENTS = 3;
    static int NOTIFY_TIME_ELAPSE = 10000;
    static int DEADLOCK_MODE = 1;
    static int PORT_NUMBER = 9898;
    static final String CONF_PATH = "./src/conf.txt";

    static void init(ArrayList<Integer> setting) {
        N_CLIENTS = setting.get(0);
        N_RESOURCES = setting.get(1);
        DEADLOCK_MODE = setting.get(2);
        PORT_NUMBER = setting.get(3);
    }
}
