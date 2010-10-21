package org.twdata.objects.screen;

import java.util.List;

/**
 *
 */
public class AutoWarpScreen extends AbstractScreen
{
    public AutoWarpScreen nextWarp(){
        return null;
    }

    public SectorScreen finalWarp(){
        return null;
    }

    public SectorScreen stop()
    {
        return null;
    }

    public boolean hasNextWarp()
    {
        return false;
    }

    public List<Integer> getWarpsLeft()
    {
        return null;
    }
}
