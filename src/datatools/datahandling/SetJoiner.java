package datatools.datahandling;

import javafx.concurrent.Task;

/**
 * Created by Josh Cotes on 7/4/2017.
 */
public class SetJoiner {


    public Task joinSets(Dataset<String> set, Dataset<String> associations, Dataset<String> joinedSets){
        return new Task() {
            @Override
            protected Object call() throws Exception {

                int progress = 0;
                for(String[] association : associations.getRecords()){

                    for(int i = 0; i < set.count(); i++) {

                             if(set.getRecord(i)[set.idCol].equalsIgnoreCase(association[0]) ||
                                    set.getRecord(i)[set.idCol].equalsIgnoreCase(association[2]))
                                joinedSets.addRecord(set.getRecord(i));

                    }
                    updateProgress(progress, association.length);
                }
                return null;
            }
        };
    }

}
