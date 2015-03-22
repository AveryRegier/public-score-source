package scoresource.live;

import com.tournamentpool.controller.autoupdate.LiveGame;
import com.tournamentpool.controller.autoupdate.ScoreSource;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import javax.xml.parsers.SAXParserFactory;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class XmlRssSource2 implements ScoreSource {
    private static Pattern pattern = Pattern.compile(
            "(?:\\(\\d+\\)\\s+)?+(.+)\\s+(\\d+)(?:\\s+\\(\\d+\\))?+\\s+(.+)\\s+(\\d+)\\s\\((.+)\\)");


    public XmlRssSource2() {
    }

    public XmlRssSource2(Properties config) {
    }


    @Override
    public List<LiveGame> getGames(String s) throws UnsupportedEncodingException {
        ArrayList<LiveGame> liveGames = new ArrayList<LiveGame>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        try {
            s = s.replaceAll("&", "&amp;");
            SAXParserFactory xmlReader = SAXParserFactory.newInstance();
            xmlReader.setValidating(false);
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Builder xomBuilder = new Builder(xmlReader.newSAXParser().getXMLReader());

            Document document = xomBuilder.build(s, null);
            Element rootElement = document.getRootElement();
            if(rootElement != null)  {
                Element channelElement = rootElement.getFirstChildElement("channel");
                if(channelElement != null) {
                    Elements childElements = channelElement.getChildElements("item");
                    for(int i=0; i<childElements.size(); i++) {
                        Element child = childElements.get(i);

                        String title = getChildValue(child, "title");
                        Matcher matcher = pattern.matcher(title);

                        if(matcher.matches()) {
                            try {
                                int team1Score = Integer.parseInt(matcher.group(2).trim());
                                int team2Score = Integer.parseInt(matcher.group(4).trim());

                                Map<String, Integer> playerScores = new LinkedHashMap<String, Integer>();
                                playerScores.put(matcher.group(1).toUpperCase(), team1Score);
                                playerScores.put(matcher.group(3).toUpperCase(), team2Score);
                                final String status = matcher.group(5);

                                final Date pubDate = parseDate(dateFormat, child);

                                liveGames.add(new MyLiveGame(playerScores, status, pubDate));
                            } catch(NumberFormatException e){}
                        }

                    }
                }
            }

            return liveGames;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(s);
            throw new UnsupportedEncodingException(e.getLocalizedMessage());
        }
    }

    private Date parseDate(SimpleDateFormat dateFormat, Element child) {
        Date pubDate;
        try {
            Element element = child.getFirstChildElement("pubDate");
            if(element != null) {
                pubDate = dateFormat.parse(element.getValue());
            } else {
                pubDate = new Date();
            }
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
            return "FINAL".equals(status);
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
