package datatools.datahandling;

import datatools.datahandling.Dataset;

import java.io.*;

/**
 * Created by Josh Cotes on 6/30/2017.
 */

public class FileTools {

    public static void writeSet(File file, Dataset<String> set){
        if(file == null || set == null)
            return;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(String[] record : set.getRecords()){

                for(int i = 0; i < record.length; i ++){
                    writer.write(record[i]);
                    if(i < record.length-1)
                        writer.write('\t');
                }
                writer.write('\n');
            }
            writer.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static Dataset<String> loadSet(File datafile){
        Dataset<String> dataset = new Dataset<String>();
        int attCount = 0;
        int run = 0;

        try {
            String linebuffer;
            BufferedReader reader = new BufferedReader(new FileReader(datafile));
            while((linebuffer = reader.readLine()) != null){
                if(!linebuffer.startsWith("#") && !linebuffer.startsWith("/")) {
                    String split[] = linebuffer.split("\t|[ ]+|,");
                    if(run++ == 0)
                        attCount = split.length;
                    if(attCount == split.length)
                        dataset.addRecord(split);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return dataset;
    }
}
