package scoresource.live;

import com.tournamentpool.controller.autoupdate.LiveGame;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by avery on 3/2/14.
 */
public class XmlRssSourceTest {
    @Test
    public void works() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>Some Text I don't care about</title>" +
                "<description>More text I don't care about.</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:12:01 GMT</pubDate>" +
                "<item>" +
                "<title>Blah Blah Blah: (USA-NBA) #Phoenix Suns vs #Atlanta Hawks: 116-108</title>" +
                "<description>4th Quarter</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:09:40 GMT</pubDate>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource classUnderTest = new XmlRssSource();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertEquals(1, games.size());

        LiveGame liveGame = games.get(0);
        assertNotNull(liveGame);

        assertEquals("4th Quarter", liveGame.getStatus());
        assertNull(liveGame.getWinner());
        Map<String,Integer> playerScores = liveGame.getPlayerScores();
        assertEquals(2, playerScores.size());
        assertEquals(new Integer(116), playerScores.get("Phoenix Suns"));
        assertEquals(new Integer(108), playerScores.get("Atlanta Hawks"));
        assertFalse(liveGame.isFinal());
        assertEquals(2, liveGame.getStartDate().getMonth());
    }

    @Test
    public void gameCompleted() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>Some Text I don't care about</title>" +
                "<description>More text I don't care about.</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:12:01 GMT</pubDate>" +
                "<item>" +
                "<title>Blah Blah Blah: (USA-NBA) #Phoenix Suns vs #Atlanta Hawks: 129-120</title>" +
                "<description>Game Finished</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:28:03 GMT</pubDate>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource classUnderTest = new XmlRssSource();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertEquals(1, games.size());

        LiveGame liveGame = games.get(0);
        assertNotNull(liveGame);

        assertEquals("Final", liveGame.getStatus());
        assertEquals("Phoenix Suns", liveGame.getWinner());
        Map<String,Integer> playerScores = liveGame.getPlayerScores();
        assertEquals(2, playerScores.size());
        assertEquals(new Integer(129), playerScores.get("Phoenix Suns"));
        assertEquals(new Integer(120), playerScores.get("Atlanta Hawks"));
        assertTrue(liveGame.isFinal());
        assertEquals(28, liveGame.getStartDate().getMinutes());
    }

    @Test
    public void badRSS() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<notAChannel>" +
                "</notAChannel>" +
                "</rss>";
        XmlRssSource classUnderTest = new XmlRssSource();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertTrue(games.isEmpty());
    }

    @Test
    public void badData() throws UnsupportedEncodingException {
        assertNoGames("I am a title that just won't parse.");
    }

    @Test
    public void badDataWithHash() throws UnsupportedEncodingException {
        assertNoGames("I am a # title that just won't parse.");
    }

    @Test
    public void badDataWithHashAndVS() throws UnsupportedEncodingException {
        assertNoGames("I am a # title vsthat just won't parse.");
    }

    @Test
    public void badDataWithHashAndVSandColon() throws UnsupportedEncodingException {
        assertNoGames("I am a # title vsthat just: won't parse.");
    }

    @Test
    public void badDataWithHashAndVSandColonAndDashAtEnd() throws UnsupportedEncodingException {
        assertNoGames("I am a # title vsthat just: won't parse.-");
    }

    @Test
    public void badDataWithNumbersThatWontParse() throws UnsupportedEncodingException {
        assertNoGames("I am a # title vsthat just: won't-parse");
    }

    @Test
    public void badDataWithNumbersThatWontParseSecondNumber() throws UnsupportedEncodingException {
        assertNoGames("I am a # title vsthat just: 54-won't parse");
    }

    private void assertNoGames(String title) throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>Some Text I don't care about</title>" +
                "<description>More text I don't care about.</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:12:01 GMT</pubDate>" +
                "<item>" +
                "<title>" + title + "</title>" +
                "<description>Some other description</description>" +
                "<pubDate>Mon, 3 Mar 2014 03:28:03 GMT</pubDate>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource classUnderTest = new XmlRssSource();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertTrue(games.isEmpty());
    }

}
