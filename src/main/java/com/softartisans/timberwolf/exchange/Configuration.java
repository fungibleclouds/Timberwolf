package com.softartisans.timberwolf.exchange;

import org.joda.time.DateTime;

/**
 * This class contains any configurable settings
 * that will effect the exchange service calls.
 */
public class Configuration
{
    private final int findPageSize;
    private final int getPageSize;

    public Configuration(final int findItemPageSize,
                         final int getItemPageSize)
    {
        this.findPageSize = findItemPageSize;
        this.getPageSize = getItemPageSize;
    }

    public DateTime getStartDate()
    {
        return null;
    }

    public int getFindItemPageSize()
    {
        return findPageSize;
    }

    public int getGetItemPageSize()
    {
        return getPageSize;
    }
}
