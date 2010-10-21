package org.twdata.objects;

import java.io.*;
import org.twdata.objects.*;
import org.twdata.objects.screen.*;
import static org.twdata.objects.VT100Writer.CharacterModifier.*;
import static org.twdata.objects.Color.*;
import org.slf4j.*;


/**
 * This class is a simple example lexer.
 */
%%

%class ScreenLexer
%unicode

%{
    private final StringBuilder screenBuffer = new StringBuilder();
    private final Logger log = LoggerFactory.getLogger(ScreenLexer.class);

    public final void append(java.io.Reader reader, int state) {
        zzReader = reader;
        zzLexicalState = state;
        zzAtEOF  = false;
        zzEOFDone = false;
      }
%}

%init{
%init}

CHAR=[A-Za-z]
WORD=[0-9A-Za-z']+

DIGIT=[0-9]
NUMBER=[0-9]+(","[0-9]+)*
FLOAT={NUMBER}"."{NUMBER}
PORT_TYPE=(((S|B)(S|B)(S|B))|"Special")
TIME=({DIGIT}{DIGIT})":"({DIGIT}{DIGIT})":"({DIGIT}{DIGIT})
CLOCK={TIME}" "{CHAR}{2}
DATE={CHAR}{3}" "{CHAR}{3}" "{DIGIT}{2}", "{DIGIT}{4}
TIMESTAMP={CLOCK}" "{DATE}
FUEL="Fuel Ore"
ORG="Organics"
EQU="Equipment"
COL="Colonists"
EMP="Empty"
ANYPRODUCT=({FUEL}|{ORG}|{EQU}|{COL}|{EMP})
RANK={BAD_RANK}|{GOOD_RANK}
NAME=([A-Za-z0-9"!""-"".""'"" ""*"])+

%state SECTOR_PROMPT
%state SECTOR_DISPLAY
%type java.lang.Object

%%

"Command [TL="{TIME}"]:["{NUMBER}"]"  {
  yybegin(SECTOR_PROMPT);
  return new SectorPrompt(yytext());
}

<SECTOR_PROMPT> "Warping to Sector " {NUMBER} {
    return new WarpDisplay(yytext());
}

<SECTOR_PROMPT> "Sector  : " {NUMBER} {
    log.warn("start sector screen");
    screenBuffer.append(yytext());
    yybegin(SECTOR_DISPLAY);
}

<SECTOR_DISPLAY> ^\r {
    yybegin(SECTOR_PROMPT);
    String data = screenBuffer.toString();
    screenBuffer.setLength(0);
    return new SectorDisplay(data);
}

/* fallback */
.|\n                             {
    if (screenBuffer.length() > 0) {
        screenBuffer.append(yytext());
    }

}
