package org.twdata.objects.screen;

import org.twdata.objects.Lazy;
import org.twdata.objects.Sourced;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class SectorDisplay implements Sourced
{
    private static final Pattern warpPtn = Pattern.compile("\\(?([0-9]+)");

    @Lazy(matchFirst = ": ([0-9,]+) in")
    public abstract int getSectorId();

    @Lazy(matchFirst = "Ports   : .*, Class [0-9] \\(([SB]+)\\)")
    public abstract PortType getPortType();

    @Lazy
    public List<Integer> getWarps() {
        List<Integer> warps = new ArrayList<Integer>();
        int startPos = getSource().indexOf("Warps to Sector(s) : ");
        Matcher m = warpPtn.matcher(getSource());
        m.region(startPos, getSource().length());
        while (m.find()) {
            warps.add(Integer.parseInt(m.group(1)));
        }
        return warps;
    }
}
