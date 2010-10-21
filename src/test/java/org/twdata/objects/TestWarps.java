package org.twdata.objects;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.*;
import com.atlassian.event.spi.ListenerHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.twdata.objects.screen.SectorDisplay;
import org.twdata.objects.screen.SectorPrompt;
import org.twdata.objects.screen.WarpDisplay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestWarps
{
    private OutputStream lexer;
    private LinkedList tokenStream;
    private LexerManager lexerManager;
    private EventPublisher eventPublisher;

    @Test
    public void testWarp() throws IOException, URISyntaxException
    {
        writeStrippedLog("/warp.log");
        assertEquals(4, tokenStream.size());
        assertTrue(tokenStream.get(0) instanceof SectorPrompt);
        assertTrue(tokenStream.get(1) instanceof SectorDisplay);
        assertTrue(tokenStream.get(2) instanceof WarpDisplay);
        assertTrue(tokenStream.get(3) instanceof SectorPrompt);
    }
    @Test
    public void testTokenOverTwoFeeds() throws IOException, URISyntaxException
    {
        String data = "Command [TL=11:38:35]:[4310] (?=Help)? :\r\n";
        lexer.write(data.getBytes());
        assertEquals(1, tokenStream.size());
        assertTrue(tokenStream.get(0) instanceof SectorPrompt);
        tokenStream.clear();
        lexer.write(data.getBytes());
        assertEquals(1, tokenStream.size());
        assertTrue(tokenStream.get(0) instanceof SectorPrompt);
        tokenStream.clear();
        lexer.write(data.substring(0, 10).getBytes());
        lexer.write(data.substring(10).getBytes());
        assertEquals(1, tokenStream.size());
        assertTrue(tokenStream.get(0) instanceof SectorPrompt);
    }

    private void writeStrippedLog(String file) throws IOException
    {
        InputStream in = getClass().getResourceAsStream(file);
        final byte[] rawBytes = IOUtils.toByteArray(in);
        final byte[] strippedBytes = new byte[rawBytes.length];
        int strippedLen = stripAnsi(rawBytes, strippedBytes, rawBytes.length);
        lexer.write(strippedBytes, 0, strippedLen);
    }


    @Before
    public void setUp() throws Exception
    {
        eventPublisher = new LockFreeEventPublisher(new AsynchronousAbleEventDispatcher(
                new EventExecutorFactoryImpl(new EventThreadPoolConfigurationImpl())), new ListenerHandlersConfiguration()
        {
            public List<ListenerHandler> getListenerHandlers()
            {
                return Collections.<ListenerHandler> singletonList(new AnnotatedMethodsListenerHandler());
            }
        });
        eventPublisher.register(this);
        lexerManager = new LexerManager(eventPublisher);
        lexer = lexerManager.getLexingOutputStream();
        tokenStream = new LinkedList();

    }

    @EventListener
    public void onToken(Object token) {
        tokenStream.push(token);
    }

    int NORMAL = 0, ESCAPE = 1, ESCAPE2 = 2, ESCAPE_STRING = 3;
    int ansiState = NORMAL;


    //hand made lexer to strip out pesky ansi escape codes

    //we don't want the overhead of another lexer, and we can't do it in the full lex
    /**
     *  Description of the Method
     *
     *@param  b       Description of the Parameter
     *@param  c       Description of the Parameter
     *@param  amount  Description of the Parameter
     *@return         Description of the Return Value
     */
    public int stripAnsi(byte[] b, byte[] c, int amount) {
        int counter;
        int rCounter;
        int numbytes;

        numbytes = c.length;
        rCounter = 0;
        for (counter = 0; counter < amount; counter++) {
            char current = (char) b[counter];
            switch (ansiState) {
                case 0:
                    //NORMAL:
                    if (current != 27 && current != 0) {
                        //get rid of those pesky nulls

                        c[rCounter] = b[counter];
                        rCounter++;
                    } else if (current == 27) {
                        ansiState = ESCAPE;
                    }
                    break;
                case 1:
                    //ESCAPE:
                    if (current == '[' || Character.isDigit(current)) {
                        ansiState = ESCAPE2;
                    } else if (current == '\"') {
                        ansiState = ESCAPE_STRING;

                    }
                    break;
                case 2:
                    //ESCAPE2
                    if (Character.isLetter(current)) {
                        ansiState = NORMAL;
                    } else if (current == '[' || Character.isDigit(current)) {
                        ansiState = ESCAPE2;
                    } else {
                        ansiState = ESCAPE;
                    }
                    break;
                case 3:
                    //ESCAPE_STRING:
                    if (current == '\"') {
                        ansiState = ESCAPE;
                    }
                    break;
            }
        }

        return rCounter;
    }
}
