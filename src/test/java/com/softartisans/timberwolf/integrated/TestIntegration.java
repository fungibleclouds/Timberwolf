package com.softartisans.timberwolf.integrated;

import com.cloudera.alfredo.client.AuthenticationException;
import com.softartisans.timberwolf.MailStore;
import com.softartisans.timberwolf.MailWriter;
import com.softartisans.timberwolf.exchange.ExchangeMailStore;
import com.softartisans.timberwolf.hbase.HBaseMailWriter;
import com.softartisans.timberwolf.hbase.HBaseManager;
import com.softartisans.timberwolf.hbase.IHBaseTable;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Overall integration testing for timberwolf.
 */
public class TestIntegration
{
    private static final String ZOO_KEEPER_QUORUM_PROPERTY_NAME = "ZooKeeperQuorum";
    private static final String ZOO_KEEPER_CLIENT_PORT_PROPERTY_NAME = "ZooKeeperClientPort";

    @Rule
    public IntegrationTestProperties properties = new IntegrationTestProperties(ZOO_KEEPER_QUORUM_PROPERTY_NAME,
                                                                                ZOO_KEEPER_CLIENT_PORT_PROPERTY_NAME);

    @Test
    public void testIntegrationNoCLI()
    {
        String tableName = "testIntegrationNoCLI";
        String columnFamily = "h";
        String keyHeader = "Item ID";

        HBaseManager hbase = new HBaseManager(IntegrationTestProperties.getProperty(ZOO_KEEPER_QUORUM_PROPERTY_NAME),
                                              IntegrationTestProperties.getProperty(ZOO_KEEPER_CLIENT_PORT_PROPERTY_NAME));

        List<String> columnFamilies = new ArrayList<String>();
        columnFamilies.add(columnFamily);
        IHBaseTable table = hbase.createTable(tableName, columnFamilies);

        String exchangeURL = "https://devexch01.int.tartarus.com/ews/exchange.asmx";

        MailStore mailStore = new ExchangeMailStore(exchangeURL);
        MailWriter mailWriter = HBaseMailWriter.create(table, keyHeader, columnFamily);
        try
        {
            mailWriter.write(mailStore.getMail());
        }
        catch (IOException e)
        {
            Assert.fail("Error writing mail!");
        }
        catch (AuthenticationException e)
        {
            Assert.fail("Error authenticating to Exchange");
        }

        hbase.deleteTable(tableName);
    }
}
