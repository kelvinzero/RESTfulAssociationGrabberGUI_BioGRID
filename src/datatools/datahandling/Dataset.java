package datatools.datahandling;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Josh Cotes on 6/30/2017.
 */
public class Dataset<T> {

    int idCol = 0;
    List<T[]> records;

    public Dataset(){
        records = new ArrayList<>();
    }

    public Dataset(List<T[]> records){
        this.records = records;
    }

    public int getIdCol(){
        return idCol;
    }

    public void setIdCol(int type){
        this.idCol = type;
    }
    public List<T[]> getRecords(){
        return records;
    }


    public void addAll(Collection<T[]> records){
        this.records.addAll(records);
    }

    public void addRecord(T[] record){
        records.add(record);
    }

    public T[] getRecord(int at){
        return records.get(at);
    }

    public int count(){
        return records.size();
    }

    public boolean contains(T[] obj){
        return records.contains(obj);
    }

    public boolean contains(T obj, int idx){
        for(T[] rec : records)
            if(rec[idx].equals(obj))
                return true;
        return false;
    }
}
