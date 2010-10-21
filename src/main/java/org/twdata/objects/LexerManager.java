package org.twdata.objects;


import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.*;
import com.atlassian.event.spi.ListenerHandler;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class LexerManager
{
    private final ScreenLexer lexer;

    private final EventPublisher eventPublisher;

    public LexerManager(EventPublisher eventPublisher, InputStream rawInput)
    {
        this.eventPublisher = eventPublisher;
        lexer = new ScreenLexer(rawInput);
    }

    public void start()
    {
        Object token = null;
        try
        {
            while((token = lexer.yylex()) != null) {
                    System.out.println("token: " + token);
                    eventPublisher.publish(token);
                }
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
