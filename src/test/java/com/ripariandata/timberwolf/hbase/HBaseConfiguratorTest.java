package com.ripariandata.timberwolf.hbase;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the HBaseConfigurator.
 */
public class HBaseConfiguratorTest
{
    /**
     * Simple test to actually make a non-default Hadoop configuration.
     */
    @Test
    public void testCreate()
    {
        String quorum = "someserver.somewhere.test";
        String clientPort = "2181";
        Configuration configuration = HBaseConfigurator.createConfiguration(quorum, clientPort);
        Assert.assertEquals(quorum, configuration.get("hbase.zookeeper.quorum"));
        Assert.assertEquals(clientPort, configuration.get("hbase.zookeeper.property.clientPort"));
    }
}
