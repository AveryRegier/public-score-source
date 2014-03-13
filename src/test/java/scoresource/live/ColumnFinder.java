package scoresource.live;

/**
 * Created by avery on 3/12/14.
 */
public class ColumnFinder {

    public static void main(String... args) {
        int column = Integer.parseInt(args[0]);
        String s = args[1];
        System.out.println(s.substring(column-10));

    }
}
