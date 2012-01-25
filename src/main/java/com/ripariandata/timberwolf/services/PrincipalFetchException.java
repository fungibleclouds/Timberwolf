package com.ripariandata.timberwolf.services;

import org.slf4j.Logger;

/**
 * This or a derivative of this is thrown should an error occur fetching
 * a complete list of principals from whatever derives PrincipalFetcher.
 */
public class PrincipalFetchException extends Exception
{
    public PrincipalFetchException(final Exception innerException)
    {
        super(innerException);
    }

    public PrincipalFetchException(final String message,
                                   final Exception innerException)
    {
        super(message, innerException);
    }

    /**
     * Logs a PrincipalFetchException to the appropriate logs.
     * @param logger The logger to use for logging.
     * @param e The PrincipalFetchException to log.
     * @return The PrincipalFetchException logged.
     */
    public static PrincipalFetchException log(final Logger logger, final PrincipalFetchException e)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(e.getMessage(), e);
        }
        else
        {
            logger.error(e.getMessage());
        }
        return e;
    }
}
