/**
 * Created by amiiir on 7/7/17.
 */
public class Util {
    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder("hello there!");

        stringBuilder.delete(5,6);
        updateResources("0101111000001010010");
    }

    private static void updateResources(String input) {
        int i = input.indexOf('1');
        while (i >= 0) {
            i = input.indexOf('1', i + 1);
        }
    }
}
