package datatools.RESTaccess;

import datatools.RESTaccess.RestQuery;
import datatools.datahandling.Dataset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Josh Cotes on 7/3/2017.
 */
public class AssociationIterator implements Iterator {

    private RestQuery                   _restQuery;
    private List<Dataset<String>>       _records;
    private ArrayList<String[]>         _associations;
    private int                         _currentRecord;
    private int                         _idColumn;
    private int                         _recordsCount;
    private int                         _currentSet;
    private int                         _thisRecord;

    public AssociationIterator(List<Dataset<String>> records) {

        _records        = records;
        _thisRecord      = 0;
        _restQuery      = new RestQuery();
        _associations   = new ArrayList<>();
        _currentRecord  = 0;
        _recordsCount   = 0;
        _currentSet     = 0;

        for(Dataset ds : records)
            _recordsCount+= ds.count();
    }

    @Override
    public boolean hasNext() {
        if(_currentRecord < _recordsCount)
            return true;
        return false;
    }



    public boolean associationListContains(String geneid, String matchId){
        int i = 0;
        while ( i < _associations.size()) {
            if (_associations.get(i)[0].equalsIgnoreCase(geneid) && _associations.get(i)[1].equalsIgnoreCase(matchId))
                return true;
            if (_associations.get(i)[1].equalsIgnoreCase(geneid) && _associations.get(i)[0].equalsIgnoreCase(matchId))
                return true;
            i += 1;
        }
        return false;
    }

    @Override
    public ArrayList<String[]> next(){

        ArrayList<String[]> newAssoc = new ArrayList<>();

        if(_currentRecord >= _recordsCount)
            throw new IndexOutOfBoundsException("Ran off the end of iterator");

        if(_thisRecord >= _records.get(_currentSet).count()) {
            _currentSet++;
            _thisRecord = 0;
        }


        try{
            Dataset thisDataset = _records.get(_currentSet);
            int idCol = thisDataset.getIdCol();
            String thisId = (String)thisDataset.getRecord(_thisRecord)[idCol];

            ArrayList<String[]> results =
                    _restQuery.filterAssociationsList(thisId, _restQuery.getAssociationsForGeneid(thisId));

                for (String[] result : results) {
                    for (Dataset recordSet : _records) {
                        if (recordSet.contains(result[2], recordSet.getIdCol()) &&
                                !associationListContains(thisId, result[2])) {
                            newAssoc.add(result);
                            _associations.add(result);
                        }
                    }
                }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        _currentRecord++;
        _thisRecord++;
        return newAssoc;
    }

    public int recordsCount(){
        return _recordsCount;
    }

}
