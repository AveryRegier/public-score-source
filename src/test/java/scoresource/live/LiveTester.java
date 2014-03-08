package scoresource.live;

import com.tournamentpool.controller.autoupdate.LiveGame;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * Created by avery on 3/7/14.
 */
public class LiveTester {

    public static void main(String... args) throws IOException {
        URI uri = URI.create(args[0]);
        String s = readFully(uri.toURL().openStream());
        for(LiveGame game: new XmlRssSource().getGames(s)) {
            System.out.println(presentResults(game));
        }
    }

    private static String presentResults(LiveGame game) {
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, Integer> side: game.getPlayerScores().entrySet()) {
            String team = side.getKey();
            if(team.equals(game.getWinner())) {
                sb.append('*');
            }
            sb.append(team);
            sb.append(" (");
            sb.append(side.getValue());
            sb.append(") ");
        }
        sb.append(game.getStatus());
        return sb.toString();
    }

    public static String readFully(InputStream in) {
        try (java.util.Scanner s = new java.util.Scanner(in)) {
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }
}
