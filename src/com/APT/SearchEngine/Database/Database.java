package com.APT.SearchEngine.Database;

import com.kenai.jaffl.byref.ByReference;
import javafx.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Hash;

import javax.naming.Name;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.*;

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
    public void BulkInsertAndUpdate(String tableName,ArrayList<String>rowkey,String colFamily,String col,String val ) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put ;
        ArrayList<Put> putlist = new ArrayList<>();
        for (int i=0;i<rowkey.size();i++)
        {
            put = new Put (Bytes.toBytes(rowkey.get(i)));
            put.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col),Bytes.toBytes(val));
            putlist.add(put);
        }
        table.put(putlist);
        table.close();
    }

    public void DeleteRow(String tableName,String rowkey,String colFamily,String col) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowkey));
        //delete.addFamily(Bytes.toBytes(colFamily));
        //delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        table.delete(delete);
        table.close();

    }

    public void BulkDelete(String tableName,ArrayList<String> rowkey,String colFamily,String col) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete;
        ArrayList<Delete> deleteList = new ArrayList<Delete>();
        for (int i =0; i<rowkey.size();i++)
        {
            delete = new Delete(Bytes.toBytes(rowkey.get(i)));
            delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
            deleteList.add(delete);
        }
        table.delete(deleteList);
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
    public ArrayList<HashMap<String,Integer>> getOriginalWordLinks (String tableName,String rowkey,String columnFamily) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get (Bytes.toBytes(rowkey));
        QualifierFilter filter = new QualifierFilter(
                CompareFilter.CompareOp.NOT_EQUAL,
                new BinaryComparator(Bytes.toBytes("StemmedWord"))
        );
        get.addFamily(Bytes.toBytes(columnFamily));
        get.setFilter(filter);

        Result result = table.get(get);

        NavigableMap<byte[],byte[]> familymap = result.getFamilyMap(columnFamily.getBytes());
        HashMap<String,Integer> QuantifersAndValues = new HashMap<>();

        for(Map.Entry<byte[], byte[]> entry: familymap.entrySet())
        {
            QuantifersAndValues.put(Bytes.toString(entry.getKey()),Bytes.toInt(entry.getValue()));
        }
        table.close();
        ArrayList<HashMap<String,Integer>> output= new ArrayList<>();
        output.add(QuantifersAndValues);
        return output;
     }
     public ArrayList<HashMap<String,Integer>> getStemmedWordsLinks (String tableName,String columnFamily, String stemmedWord) throws IOException
     {
         Table table = connection.getTable(TableName.valueOf(tableName));
         Scan scan = new Scan();
         FilterList filterlist = new FilterList();
         filterlist.addFilter(
                 new SingleColumnValueFilter(
                    Bytes.toBytes(columnFamily),
                    Bytes.toBytes("StemmedWord"),
                    CompareFilter.CompareOp.EQUAL,
                    Bytes.toBytes(stemmedWord)
                                            )
         );
         filterlist.addFilter(
                 new QualifierFilter(
                 CompareFilter.CompareOp.NOT_EQUAL,
                 new BinaryComparator(Bytes.toBytes("StemmedWord"))
                                    )
         );
         scan.setFilter(filterlist);
         ResultScanner resultScanner = table.getScanner(scan);
         NavigableMap<byte[],byte[]> familymap;
         HashMap<String,Integer> QuantifiersAndValues;
         ArrayList<HashMap<String,Integer>> output = new ArrayList<>();
         ArrayList<String> row ;
         Cell[] cells;
         for (Result result : resultScanner)
         {
             cells = result.rawCells();
             familymap = result.getFamilyMap(columnFamily.getBytes());
             QuantifiersAndValues = new HashMap<>();
             row = new ArrayList<>();
             row.add(new String(CellUtil.cloneRow(cells[0])));
             for(Map.Entry<byte[], byte[]> entry: familymap.entrySet())
             {
                 QuantifiersAndValues.put(Bytes.toString(entry.getKey()),Bytes.toInt(entry.getValue()));
             }
             output.add(QuantifiersAndValues);
         }
         table.close();
         return output;

     }
     public ArrayList<HashMap<String,Integer>> getOriginalMultipleWordsLinks(String tableName,ArrayList<String> rowkey, String columnFamily) throws IOException
     {
         Table table = connection.getTable(TableName.valueOf(tableName));
         QualifierFilter filter = new QualifierFilter(
                 CompareFilter.CompareOp.NOT_EQUAL,
                 new BinaryComparator(Bytes.toBytes("StemmedWord"))
         );
         ArrayList<Get> getsList = new ArrayList<>();
         Get get;
         for (int i=0 ; i<rowkey.size();i++)
         {
             get = new Get(Bytes.toBytes(rowkey.get(i)));
             get.setFilter(filter);
             get.addFamily(Bytes.toBytes(columnFamily));
             getsList.add(get);
         }
         Result resultout[] = table.get(getsList);
         NavigableMap<byte[],byte[]> familymap;
         HashMap<String,Integer>QuantifiersAndValues;
         ArrayList<HashMap<String,Integer>> output = new ArrayList<>();
         for (Result result: resultout)
         {
             familymap = result.getFamilyMap(columnFamily.getBytes());
             QuantifiersAndValues = new HashMap<>();
             for (Map.Entry<byte[],byte[]> entry : familymap.entrySet())
             {
                 QuantifiersAndValues.put(Bytes.toString(entry.getKey()),Bytes.toInt((entry.getValue())));
             }
             output.add(QuantifiersAndValues);
         }
         table.close();
         return output;
     }

     public ArrayList<HashMap<String,Integer>> getStemmedMultipleWordsLinks (String tableName,ArrayList<String>stemmedWord,String columnFamily) throws IOException
     {
         Table table = connection.getTable(TableName.valueOf(tableName));
         HashMap<String,Integer> QuantifiersAndValues;
         ArrayList<HashMap<String,Integer>> output = new ArrayList<>();
         ArrayList<String> row ;
         Cell[] cells;
         Scan scan;
         FilterList filterlist;
         ResultScanner resultScanner;
         NavigableMap<byte[],byte[]>familymap;
         Filter filter = new QualifierFilter(CompareFilter.CompareOp.NOT_EQUAL,new BinaryComparator(Bytes.toBytes("StemmedWord")));
         for (int i=0; i<stemmedWord.size();i++)
         {
              scan = new Scan();
              filterlist = new FilterList();
              filterlist.addFilter(filter);
              filterlist.addFilter(
                      new SingleColumnValueFilter(
                              Bytes.toBytes(columnFamily),
                              Bytes.toBytes("StemmedWord"),
                              CompareFilter.CompareOp.EQUAL,
                              Bytes.toBytes(stemmedWord.get(i))
                                                )
              );
              scan.setFilter(filterlist);
              resultScanner = table.getScanner(scan);
             for (Result result : resultScanner)
             {
                 cells = result.rawCells();
                 familymap = result.getFamilyMap(columnFamily.getBytes());
                 QuantifiersAndValues = new HashMap<>();
                 row = new ArrayList<>();
                 row.add(new String(CellUtil.cloneRow(cells[0])));
                 for(Map.Entry<byte[], byte[]> entry: familymap.entrySet())
                 {
                     QuantifiersAndValues.put(Bytes.toString(entry.getKey()),Bytes.toInt(entry.getValue()));
                 }
                 output.add(QuantifiersAndValues);
             }
         }
         table.close();
         return output;
     }

    public ArrayList<HashMap<String,Pair<Integer,ArrayList<String>>>> getPhraseLinks (String tableName,ArrayList<String>rowkey,String rankcolumnFamily,String postioncolumnFamily) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        QualifierFilter filter = new QualifierFilter(
                CompareFilter.CompareOp.NOT_EQUAL,
                new BinaryComparator(Bytes.toBytes("StemmedWord"))
        );
        ArrayList<Get> getsList1 = new ArrayList<>();
        ArrayList<Get> getsList2 = new ArrayList<>();
        Get get1;
        Get get2;
        for (int i=0 ; i<rowkey.size();i++)
        {
            get1 = new Get(Bytes.toBytes(rowkey.get(i)));
            get2 = new Get(Bytes.toBytes(rowkey.get(i)));
            get1.setFilter(filter);
            get2.setFilter(filter);
            get1.addFamily(Bytes.toBytes(rankcolumnFamily));
            get2.addFamily(Bytes.toBytes(postioncolumnFamily));
            getsList1.add(get1);
            getsList2.add(get2);

        }
        Result resultout1[] = table.get(getsList1);
        Result resultout2[] = table.get(getsList1);
        NavigableMap<byte[],byte[]> rankmap;
        NavigableMap<byte[],byte[]> positionmap;

        //Map<String,Integer>QuantifiersAndRanks;
        HashMap<String,Pair<Integer,ArrayList<String>>> QuantifiersAndPositionsAndRanks ;
        Pair<Integer,ArrayList<String>> RanksAndPositions;
        ArrayList<HashMap<String,Pair<Integer,ArrayList<String>>>> output = new ArrayList<>();
        int i,j;
        for (i=0,j=0;j<resultout2.length && i<resultout1.length;i++,j++)
        {
            rankmap = resultout1[i].getFamilyMap(rankcolumnFamily.getBytes());
            positionmap = resultout2[j].getFamilyMap(postioncolumnFamily.getBytes());
            QuantifiersAndPositionsAndRanks = new HashMap<>();
            for (Map.Entry<byte[],byte[]> entry : rankmap.entrySet())
            {
                byte[] key = entry.getKey();
                Integer value1 = Bytes.toInt(entry.getValue());
                String value2 = Bytes.toString(positionmap.get(key));
                ArrayList<String> positions = new ArrayList<String>(Arrays.asList(value2.split(" ")));
                RanksAndPositions=new Pair<Integer, ArrayList<String>>(value1,positions);
                QuantifiersAndPositionsAndRanks.put(Bytes.toString(key),RanksAndPositions);
            }
            output.add(QuantifiersAndPositionsAndRanks);

        }
        table.close();
        return output;
    }

    public ArrayList<String> getAllLinkWords (String tableName,String columnFamily, String col) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes(columnFamily),Bytes.toBytes(col));
        ResultScanner resultScanner = table.getScanner(scan);
        Cell[] cells;
        ArrayList<String> output = new ArrayList<>();
        String word;
        for (Result result : resultScanner)
        {
            cells = result.rawCells();
            for (Cell cell:cells)
            {
                word = new String (CellUtil.cloneRow(cell));
                output.add(word);
            }
        }
        return output;
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

    public  ArrayList<ArrayList<String>> getSortedLinksDocuement (String tableName,ArrayList<String>rowkey, String columnFamily,String columnName) throws IOException
    {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get;
        ArrayList<Get>getsList = new ArrayList<>();
        for(int i =0; i<rowkey.size();i++)
        {
            get = new Get(Bytes.toBytes(rowkey.get(i)));
            get.addColumn(Bytes.toBytes(columnFamily),Bytes.toBytes(columnName));
            getsList.add(get);
        }

        Result result[] = table.get(getsList);
        Cell[] cells;
        ArrayList<String> row = new ArrayList<>(); ;
        ArrayList<ArrayList<String>> collectionOfRows= new ArrayList<ArrayList<String>>();
        for (int i = 0;i<result.length;i++)
        {
            cells = result[i].rawCells();
            row.add(new String(CellUtil.cloneValue(cells[0])));
            row.add(new String(CellUtil.cloneRow(cells[0])));
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

