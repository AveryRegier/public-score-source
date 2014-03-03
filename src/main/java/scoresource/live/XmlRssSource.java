package scoresource.live;

import com.tournamentpool.controller.autoupdate.LiveGame;
import com.tournamentpool.controller.autoupdate.ScoreSource;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Hello world!
 *
 */
public class XmlRssSource implements ScoreSource
{
    @Override
    public List<LiveGame> getGames(String s) throws UnsupportedEncodingException {
        ArrayList<LiveGame> liveGames = new ArrayList<LiveGame>();
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        try {
            Document document = new Builder().build(s, null);
            Elements childElements = document.getRootElement().getFirstChildElement("channel").getChildElements("item");
            for(int i=0; i<childElements.size(); i++) {
                Element child = childElements.get(i);

                String title = child.getFirstChildElement("title").getValue();
                int start = title.indexOf('#')+1;
                int endTeam1 = title.indexOf(" vs", start);
                String team1 = title.substring(start, endTeam1).trim();
                int startTeam2 = endTeam1 + 5;
                int endTeam2 = title.indexOf(":", startTeam2);
                String team2 = title.substring(startTeam2, endTeam2).trim();
                int startScore = endTeam2 + 2;
                int endTeam1Score = title.indexOf('-', startScore);
                int team1Score = Integer.parseInt(title.substring(startScore, endTeam1Score).trim());
                int team2Score = Integer.parseInt(title.substring(endTeam1Score+1).trim());

                final Map<String, Integer> playerScores = new LinkedHashMap<String, Integer>();
                playerScores.put(team1, team1Score);
                playerScores.put(team2, team2Score);


                final String status = child.getFirstChildElement("description").getValue();

                final Date pubDate = parseDate(dateFormat, child);

                LiveGame game = new LiveGame() {
                    @Override
                    public Map<String, Integer> getPlayerScores() {
                        return playerScores;
                    }

                    @Override
                    public String getGameID() {
                        return null;
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }

                    @Override
                    public String getWinner() {
                        return null;
                    }

                    @Override
                    public String getStatus() {
                        return status;
                    }

                    @Override
                    public Date getStartDate() {
                        return pubDate;
                    }
                };

                liveGames.add(game);
            }


            return liveGames;
        } catch(Exception e) {
            throw new UnsupportedEncodingException(e.getLocalizedMessage());
        }
    }

    private Date parseDate(SimpleDateFormat dateFormat, Element child) {
        Date pubDate;
        try {
            pubDate = dateFormat.parse(
                    child.getFirstChildElement("pubDate").getValue());
        } catch(ParseException e) {
            pubDate = new Date();
        }
        return pubDate;
    }
}