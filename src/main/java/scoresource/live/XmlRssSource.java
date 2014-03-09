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
    private String requiredString = "";

    public XmlRssSource() {
    }

    public XmlRssSource(Properties config) {
        this.requiredString = config.getProperty("requiredString");
    }


    @Override
    public List<LiveGame> getGames(String s) throws UnsupportedEncodingException {
        ArrayList<LiveGame> liveGames = new ArrayList<LiveGame>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        try {
            Document document = new Builder().build(s, null);
            Element rootElement = document.getRootElement();
            if(rootElement != null)  {
                Element channelElement = rootElement.getFirstChildElement("channel");
                if(channelElement != null) {
                    Elements childElements = channelElement.getChildElements("item");
                    for(int i=0; i<childElements.size(); i++) {
                        Element child = childElements.get(i);

                        String title = getChildValue(child, "title");
                        if(title.contains(requiredString)) {
                            final Map<String, Integer> playerScores = parsePlayerScores(title);
                            if(playerScores != null) {
                                final String status = getChildValue(child, "description");

                                final Date pubDate = parseDate(dateFormat, child);

                                liveGames.add(new MyLiveGame(playerScores, status, pubDate));
                            }
                        }
                    }
                }
            }

            return liveGames;
        } catch(Exception e) {
            throw new UnsupportedEncodingException(e.getLocalizedMessage());
        }
    }

    private Map<String, Integer> parsePlayerScores(String title) {
        int start = title.indexOf('#')+1;
        if(start == 0) {
            return null;
        }
        int endTeam1 = title.indexOf(" vs", start);
        if(endTeam1 == -1) {
            return null;
        }
        String team1 = title.substring(start, endTeam1).trim();
        int startTeam2 = endTeam1 + 5;
        int endTeam2 = title.indexOf(":", startTeam2);
        if(endTeam2 == -1) {
            return null;
        }
        String team2 = title.substring(startTeam2, endTeam2).trim();
        int startScore = endTeam2 + 2;
        int endTeam1Score = title.indexOf('-', startScore);
        if(endTeam1Score == -1) {
            return null;
        }

        try {
            int team1Score = Integer.parseInt(title.substring(startScore, endTeam1Score).trim());
            int team2Score = Integer.parseInt(title.substring(endTeam1Score+1).trim());

            final Map<String, Integer> playerScores = new LinkedHashMap<String, Integer>();
            playerScores.put(team1, team1Score);
            playerScores.put(team2, team2Score);
            return playerScores;
        } catch(NumberFormatException e){
            return null;
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

    private String getChildValue(Element parent, String name) {
        Element firstChildElement = parent.getFirstChildElement(name);
        if(firstChildElement != null)  {
            return firstChildElement.getValue();
        }
        return "";
    }

    private static class MyLiveGame implements LiveGame {
        private final Map<String, Integer> playerScores;
        private final String status;
        private final Date pubDate;

        public MyLiveGame(Map<String, Integer> playerScores, String status, Date pubDate) {
            this.playerScores = playerScores;
            this.status = status;
            this.pubDate = pubDate;
        }

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
            return "Game Finished".equals(status);
        }

        @Override
        public String getWinner() {
            if(isFinal()) {
                Map.Entry<String, Integer> leader = null;
                for(Map.Entry<String, Integer> entry: playerScores.entrySet()) {
                    if(leader == null || entry.getValue() > leader.getValue()) {
                        leader = entry;
                    }
                }
                if(leader != null) {
                    return leader.getKey();
                }
            }
            return null;
        }

        @Override
        public String getStatus() {
            return isFinal() ? "Final" : status;
        }

        @Override
        public Date getStartDate() {
            return pubDate;
        }
    }
}
