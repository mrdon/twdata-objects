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
    private final LexingOutputStream lexer;

    private final EventPublisher eventPublisher;

    public LexerManager(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        lexer = new LexingOutputStream(eventPublisher);
    }

    public OutputStream getLexingOutputStream()
    {
        return lexer;
    }
    private static class LexingOutputStream extends OutputStream
    {
        private final ScreenLexer lexer;
        private int lastState;
        private final EventPublisher eventPublisher;

        public LexingOutputStream(EventPublisher eventPublisher)
        {
            this.eventPublisher = eventPublisher;
            lexer = new ScreenLexer(new StringReader(""));
            lastState = lexer.yystate();
        }

        public void write(int i) throws IOException
        {
            lex(new ByteArrayInputStream(new byte[i]));

        }

        @Override
        public void write(byte[] bytes, int pos, int len) throws IOException
        {
            lex(new ByteArrayInputStream(bytes, pos, len));
        }

        private void lex(ByteArrayInputStream in) throws IOException
        {
            lexer.append(new InputStreamReader(in), lastState);
            Object token = null;
            while((token = lexer.yylex()) != null) {
                System.out.println("token: " + token);
                eventPublisher.publish(token);
            }
            lastState = lexer.yystate();
        }


    }
}
