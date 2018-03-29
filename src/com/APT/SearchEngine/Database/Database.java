package com.APT.SearchEngine.Database;

import javafx.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;

import javax.naming.Name;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Database {
    private static Database DatabaseConnection = null;
    private static Configuration configuration;
    private static Connection connection;
    private static Admin admin;

    private Database()
    {

    }

    public static Database GetInstance()
    {
        if (DatabaseConnection == null)
        {
            DatabaseConnection = new Database();
            Init();
        }
        return DatabaseConnection;
    }

    public static void Init()
    {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum","sandbox-hdp.hortonworks.com");
        configuration.set("hbase.zookeeper.property.clientPort","2181");
        configuration.set("zookeeper.znode.parent","/hbase-unsecure");

        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Close()
    {
        try {
            if(admin != null)
                admin.close();
            if(connection != null)
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createTable(String Name,String[] cols) throws IOException
    {
        TableName tableName = TableName.valueOf(Name);
        if(admin.tableExists(tableName)){
            System.out.println("table already exists!");
        }
        else
            {
                HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
                for(String col:cols)
                {
                   HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(col);
                   hTableDescriptor.addFamily(hColumnDescriptor);
                }
                admin.createTable(hTableDescriptor);
            }
    }

    public void deleteTable(String tableName) throws IOException
    {
        TableName tn = TableName.valueOf(tableName);
        if (admin.tableExists(tn))
        {
            admin.disableTable(tn);
            admin.deleteTable(tn);
        }
    }

    public void listTables() throws IOException
    {
        HTableDescriptor hTableDescriptors[] = admin.listTables();
        for(HTableDescriptor hTableDescriptor :hTableDescriptors)
        {
            System.out.println(hTableDescriptor.getNameAsString());
        }
    }

    public void InsertAndUpdateRow(String tableName,String rowkey,String colFamily,String col,String val) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(val));
        table.put(put);

       /* List<Put> putList = new ArrayList<Put>();
        puts.add(put);
        table.put(putList);*/
        table.close();
    }

    public void DeleteRow(String tableName,String rowkey,String colFamily,String col) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowkey));
        //delete.addFamily(Bytes.toBytes(colFamily));
        //delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        table.delete(delete);
       /* List<Delete> deleteList = new ArrayList<Delete>();
        deleteList.add(delete);
        table.delete(deleteList);*/
        table.close();
    }

    public void GetData(String tableName,String rowkey,String colFamily,String col)throws  IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(rowkey));
        //get.addFamily(Bytes.toBytes(colFamily));
        //get.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        Result result = table.get(get);

        ShowCell(result);
        table.close();
    }

    public ArrayList<Pair<String,Long>> getAllUrls(String tableName,String columnFamily ,String columnName) throws IOException
    {

        Table table =connection.getTable(TableName.valueOf(tableName));
        Scan scan =new Scan ();
        scan.addColumn(Bytes.toBytes(columnFamily),Bytes.toBytes(columnName));
        ResultScanner resultScanner = table.getScanner(scan);
        Cell[] cells;
        ArrayList<Pair<String,Long>> output = new ArrayList<>();
        Pair<String,Long>token;
        for (Result result : resultScanner)
        {
            cells = result.rawCells();
            for (Cell cell:cells)
            {
                token = new Pair<String, Long>(new String (CellUtil.cloneRow(cell)),cell.getTimestamp());
                output.add(token);
            }
        }
        return output;
    }

    public ArrayList<ArrayList<String>> getDocumentDetails (String tableName,String columnFamily, String columnName) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                Bytes.toBytes(columnFamily),
              Bytes.toBytes(columnName),
             CompareFilter.CompareOp.EQUAL,
            Bytes.toBytes("false")
        );
        scan.setFilter(filter);
        ResultScanner resultScanner = table.getScanner(scan);
        Cell[] cells;
        ArrayList<String> row ;
        ArrayList<ArrayList<String>> collectionOfRows= new ArrayList<ArrayList<String>>();
        for (Result result : resultScanner)
        {
            cells = result.rawCells();
            row = new ArrayList<>();
            row.add(new String(CellUtil.cloneRow(cells[0])));
            for (Cell cell:cells)
            {
                row.add(new String(CellUtil.cloneValue(cell)));
            }
            collectionOfRows.add(row);
        }
    return collectionOfRows;
    }

    public void ShowCell(Result result)
    {
        Cell[] cells = result.rawCells();
        for(Cell cell:cells)
        {
            System.out.println("RowName:"+new String(CellUtil.cloneRow(cell))+" ");
            System.out.println("Timetamp:"+cell.getTimestamp()+" ");
            System.out.println("column Family:"+new String(CellUtil.cloneFamily(cell))+" ");
            System.out.println("row Name:"+new String(CellUtil.cloneQualifier(cell))+" ");
            System.out.println("value:"+new String(CellUtil.cloneValue(cell))+" ");
        }
    }
    public void ScanData(String tableName,String startRow,String stopRow)throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        //scan.setStartRow(Bytes.toBytes(startRow));
        //scan.setStopRow(Bytes.toBytes(stopRow));
        ResultScanner resultScanner = table.getScanner(scan);
        for(Result result : resultScanner)
        {
            ShowCell(result);
        }
        table.close();
    }

}

