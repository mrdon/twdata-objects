package org.twdata.objects.screen;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 *
 */
public enum PortType
{
    BBS(1, Commodity.FUEL_ORE, Commodity.ORGANICS),
    BSB(2, Commodity.FUEL_ORE, Commodity.EQUIPMENT),
    SBB(3, Commodity.EQUIPMENT, Commodity.ORGANICS),
    SSB(4, Commodity.EQUIPMENT),
    SBS(5, Commodity.ORGANICS),
    BSS(6, Commodity.FUEL_ORE),
    SSS(7),
    BBB(8, Commodity.FUEL_ORE, Commodity.ORGANICS, Commodity.EQUIPMENT);

    private final int classType;
    private final Set<Commodity> commoditiesToBuy;

    private PortType(int classType, Commodity... commoditiesToBuy)
    {
        this.classType = classType;
        this.commoditiesToBuy = unmodifiableSet(new HashSet<Commodity>(asList(commoditiesToBuy)));
    }

    public boolean buys(Commodity commodity)
    {
        return commoditiesToBuy.contains(commodity);
    }

    public boolean sells(Commodity commodity)
    {
        return !commoditiesToBuy.contains(commodity);
    }

    public int getClassType()
    {
        return classType;
    }
}
