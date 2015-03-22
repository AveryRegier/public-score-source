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
public class XmlRssSource2Test {
    @Test
    public void works() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>Some Text I don't care about</title>" +
                "<description>More text I don't care about.</description>" +
                "<item>" +
                "<title>Cincinnati 43   (1) Kentucky 58 (3:21 IN 2ND)</title>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource2 classUnderTest = new XmlRssSource2();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertEquals(1, games.size());

        LiveGame liveGame = games.get(0);
        assertNotNull(liveGame);

        assertEquals("3:21 IN 2ND", liveGame.getStatus());
        assertNull(liveGame.getWinner());
        Map<String,Integer> playerScores = liveGame.getPlayerScores();
        assertEquals(2, playerScores.size());
        assertEquals(new Integer(43), playerScores.get("CINCINNATI"));
        assertEquals(new Integer(58), playerScores.get("KENTUCKY"));
        assertFalse(liveGame.isFinal());
    }

    @Test
    public void gameCompleted() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>Some Text I don't care about</title>" +
                "<description>More text I don't care about.</description>" +
                "<item>" +
                "<title>Alabama 66   Miami (FL) 73 (FINAL)</title>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource2 classUnderTest = new XmlRssSource2();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertEquals(1, games.size());

        LiveGame liveGame = games.get(0);
        assertNotNull(liveGame);

        assertEquals("Final", liveGame.getStatus());
        assertEquals("MIAMI (FL)", liveGame.getWinner());
        Map<String,Integer> playerScores = liveGame.getPlayerScores();
        assertEquals(2, playerScores.size());
        assertEquals(new Integer(66), playerScores.get("ALABAMA"));
        assertEquals(new Integer(73), playerScores.get("MIAMI (FL)"));
        assertTrue(liveGame.isFinal());
    }

    @Test
    public void badRSS() throws UnsupportedEncodingException {
        String xmlFeed = "<?xml version=\"1.0\"?>" +
                "<rss version=\"2.0\">" +
                "<notAChannel>" +
                "</notAChannel>" +
                "</rss>";
        XmlRssSource2 classUnderTest = new XmlRssSource2();
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
                "<item>" +
                "<title>" + title + "</title>" +
                "<description>Some other description</description>" +
                "<link>http://link.i.dont.care.about/</link>" +
                "</item>" +
                "</channel>" +
                "</rss>";
        XmlRssSource2 classUnderTest = new XmlRssSource2();
        List<LiveGame> games = classUnderTest.getGames(xmlFeed);
        assertTrue(games.isEmpty());
    }

}
