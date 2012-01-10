package com.softartisans.timberwolf.hbase;

import com.softartisans.timberwolf.MockHTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class HBaseUserTimeUpdaterTest
{
    private HBaseManager manager = new HBaseManager();

    private IHBaseTable mockTable(HBaseManager manager, String tableName)
    {
        MockHTable table = MockHTable.create(tableName);
        IHBaseTable hbaseTable = new HBaseTable(table);
        manager.addTable(hbaseTable);
        return hbaseTable;
    }

    @Test
    public void testLastUpdated()
    {
        String tableName = "testLastUpdated";
        IHBaseTable hbaseTable = mockTable(manager, tableName);

        String userName = "Robert the User";
        Put put = new Put(Bytes.toBytes(userName));
        long time = 23488902348L;
        put.add(Bytes.toBytes("t"),Bytes.toBytes("d"),Bytes.toBytes(time));
        hbaseTable.put(put);
        hbaseTable.flush();

        HBaseUserTimeUpdater updates = new HBaseUserTimeUpdater(manager, tableName);
        DateTime date = updates.lastUpdated(userName);
        Assert.assertEquals(time, date.getMillis());
    }

    @Test
    public void testLastUpdatedNoUser()
    {
        String tableName = "testLastUpdatedNoUser";
        IHBaseTable hbaseTable = mockTable(manager, tableName);

        HBaseUserTimeUpdater updates = new HBaseUserTimeUpdater(manager, tableName);
        DateTime date = updates.lastUpdated("not actually a username");
        Assert.assertEquals(0L, date.getMillis());
    }

    @Test
    public void testUpdateUser()
    {
        String tableName = "testUpdateUser";
        IHBaseTable hbaseTable = mockTable(manager, tableName);

        HBaseUserTimeUpdater updates = new HBaseUserTimeUpdater(manager, tableName);
        long time = 1234355L;
        String userName = "A Generic Username";
        updates.updated(userName, new DateTime(time));
        Assert.assertEquals(time, updates.lastUpdated(userName).getMillis());
    }

    @Test
    public void testUpdateExistingUser()
    {
        String tableName = "testUpdateExistingUser";
        IHBaseTable hbaseTable = mockTable(manager, tableName);

        HBaseUserTimeUpdater updates = new HBaseUserTimeUpdater(manager, tableName);
        long time = 3425322L;
        String userName = "Some other username";
        updates.updated(userName, new DateTime(time));
        updates.updated(userName, new DateTime(2 * time));
        Assert.assertEquals(2*time, updates.lastUpdated(userName).getMillis());
    }
}
