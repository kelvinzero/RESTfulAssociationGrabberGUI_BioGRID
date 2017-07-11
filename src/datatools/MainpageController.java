package datatools;

import datatools.RESTaccess.AssociationIterator;
import datatools.datahandling.Dataset;
import datatools.datahandling.FileTools;
import datatools.datahandling.SetJoiner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainpageController {

    public Label                        creatingassociationslabel;
    public ProgressBar                  progressbar;
    public RadioButton                  joinbyassociationbutton;
    public RadioButton                  joinallrecordsbutton;
    public ListView                     _associationNamesListObject;
    public Button                       _removeAssociationBtn;
    public Button                       _createdatafilebutton;
    public Label                        iterationsLabel;
    private ArrayList<TableColumn>      _columnHeaders;
    private ObservableList<String[]>    _tableRows;
    private ObservableList<String>      _filenamesList;
    private ObservableList<String>      _associationSetsList;
    private List<Dataset>               _loadedSets;
    private Dataset _associationSet;

    public  Button                      buildassociationsbutton;
    public  Button                      removefilebutton;
    public  HBox                        optionsbox;
    public  TableView                   datatableObject;


    public  ListView                    _filenameListObject;
    static  Stage                       _stage;

    public MainpageController(){
        _filenamesList = FXCollections.observableArrayList();
        _associationSetsList = FXCollections.observableArrayList();
        _loadedSets = new ArrayList<>();
    }

    public void onQuit(){
        Platform.exit();
    }

    public void onSaveFile(){

        FileChooser fileChooser = new FileChooser();
        int idx = _filenameListObject.getSelectionModel().getSelectedIndex();
        File loadFile;

        if(idx >= 0){
            fileChooser.setTitle("Save Data File");
            loadFile = fileChooser.showSaveDialog(_stage);
            if(loadFile == null)
                return;
            FileTools.writeSet(loadFile, _loadedSets.get(idx));
            return;
        }
        idx = _associationNamesListObject.getSelectionModel().getSelectedIndex();
        if(idx >= 0){
            fileChooser.setTitle("Save Association File");
            loadFile = fileChooser.showSaveDialog(_stage);
            if(loadFile == null)
                return;
            FileTools.writeSet(loadFile, _associationSet);
        }
    }




    public void onLoadFile(){

        TableviewController tcc;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DataFile");
        File loadFile = fileChooser.showOpenDialog(_stage);

        if(loadFile == null)
            return;

        Dataset<String> dset = FileTools.loadSet(loadFile);
        ObservableList<String[]> records = FXCollections.observableArrayList(dset.getRecords());

        _loadedSets.add(dset);
        _filenamesList.add(loadFile.getName());
        refreshFileList(_filenameListObject, _filenamesList);
        buildassociationsbutton.setDisable(false);
        removefilebutton.setDisable(false);

        _filenameListObject.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(_filenameListObject.getSelectionModel().getSelectedIndex() < 0)
                return;

            _associationNamesListObject.getSelectionModel().clearSelection();
            clearTable();
            int idx = _filenameListObject.getSelectionModel().getSelectedIndex();
            if(idx >= 0 && idx < _loadedSets.size())
                createTable(_loadedSets.get(idx));

        });
        _filenameListObject.getSelectionModel().clearSelection();
        _filenameListObject.getSelectionModel().selectLast();
    }

    public void onJoinFile(){


        Dataset ds1 = _loadedSets.get(0);
        SetJoiner sj = new SetJoiner();
        Dataset<String> joinedSet = new Dataset<>();
        Task joiner = sj.joinSets(ds1, _associationSet, joinedSet);

        progressbar.progressProperty().unbind();
        progressbar.progressProperty().bind(joiner.progressProperty());
        joiner.setOnSucceeded(event -> {
            _loadedSets.add(joinedSet);
            _filenamesList.add("JoinedSet");
            refreshFileList(_filenameListObject, _filenamesList);
        });

        new Thread(joiner).start();
    }

    public Task associationsTask(Dataset<String> associations){

        ObservableList<Integer> idxs = _filenameListObject.getSelectionModel().getSelectedIndices();
        ArrayList<Dataset<String>> sets = new ArrayList<>();

        for(Integer idx : idxs)
            sets.add(_loadedSets.get(idx));
        AssociationIterator ast = new AssociationIterator(sets);


        return new Task() {
            @Override
            protected Object call() throws Exception {
                int i = 0;
                while(ast.hasNext()){
                    ArrayList<String[]> result = ast.next();
                    if(result != null)
                        associations.addAll(result);
                   updateProgress(i++, ast.recordsCount());
                    Thread.sleep(5);
                }
                return true;
            }
        };
    }

    private void refreshFileList(ListView list, ObservableList listItems){
        list.getItems().clear();
        list.getItems().addAll(listItems);
        list.refresh();
        list.getSelectionModel().selectLast();
    }

    public void onRemoveAssociation(){
        if(_associationNamesListObject.getItems().size() == 0)
            return;

        int idx = _associationNamesListObject.getSelectionModel().getSelectedIndex();
        if(idx >= 0) {
            _associationSetsList.clear();
            refreshFileList(_associationNamesListObject, _associationSetsList);
        }

        if(_associationNamesListObject.getItems().size() == 0){
            _removeAssociationBtn.setDisable(true);
            _createdatafilebutton.setDisable(true);
            return;
        }
    }

    public void onBuildAssociations(){
        creatingassociationslabel.setText("Creating Associations");
        creatingassociationslabel.setDisable(false);
        Dataset<String> associations = new Dataset<>();
        Task grabber = associationsTask(associations);
        List<String> names = _filenameListObject.getSelectionModel().getSelectedItems();
        StringBuilder aName = new StringBuilder();
        for(String n : names){
            aName.append(n.substring(0, 3)).append("_");
        }
        _stage.setOnCloseRequest(event -> {
            if(grabber.isRunning())
                grabber.cancel();
        });

        progressbar.progressProperty().unbind();
        progressbar.progressProperty().bind(grabber.progressProperty());

        _associationNamesListObject.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            clearTable();

            if(_associationSet != null && _associationNamesListObject.getSelectionModel().getSelectedIndex() < 0)
                return;
            _filenameListObject.getSelectionModel().clearSelection();
            createTable(associations);

        });

        grabber.setOnSucceeded(event -> {
            _createdatafilebutton.setDisable(false);
            _removeAssociationBtn.setDisable(false);
            _associationSetsList.clear();
            _associationSet = associations;
            _associationSetsList.add(aName.toString());
            refreshFileList(_associationNamesListObject, _associationSetsList);
            if(_filenameListObject.getItems().size() >= 2){
                joinbyassociationbutton.setDisable(false);
            }
            creatingassociationslabel.setText("Complete");
            creatingassociationslabel.setDisable(true);
            progressbar.progressProperty().unbind();
            progressbar.progressProperty().setValue(0);
        });

        new Thread(grabber).start();



    }


    public void onDeleteFile(){

        ObservableList<Integer> selectedIndices = _filenameListObject.getSelectionModel().getSelectedIndices();

        for(Integer index : selectedIndices){

            // delete the list item
            _filenamesList.remove(index.intValue());
            refreshFileList(_filenameListObject, _filenamesList);
            //_filenameListObject.getItems().remove(index.intValue());
            //_filenameListObject.getSelectionModel().clearSelection();
            _loadedSets.remove(index.intValue());
            _filenameListObject.refresh();
            clearTable();
        }
    }

    void clearTable(){
        // clear the table
        datatableObject.getItems().clear();
        datatableObject.getColumns().clear();
        optionsbox.getChildren().clear();
    }

    // fills the table with a dataset
    public void createTable(Dataset<String> dataset){
        if(dataset == null || dataset.count() == 0)
            return;
        _tableRows = FXCollections.observableArrayList(dataset.getRecords());
        addColumnsToTable(dataset.getRecord(0));
        datatableObject.setItems(_tableRows);
        addItemsToHeaderBox(dataset);
    }

    private void addItemsToHeaderBox(Dataset<String> dataset){

        ObservableList<RadioButton> rbtns = FXCollections.observableArrayList();
        ToggleGroup idgroup = new ToggleGroup();

        for(int i = 0; i < _tableRows.get(0).length; i++){
            RadioButton rbtn = new RadioButton("ID");
            rbtn.setPrefWidth(90);
            rbtn.setPadding(new Insets(0, 0, 0, 20));
            rbtn.setToggleGroup(idgroup);
            rbtn.setTextFill(Paint.valueOf("YELLOW"));
            if(i==dataset.getIdCol())
                rbtn.setSelected(true);
            rbtns.addAll(rbtn);
        }

        // calback to change the id column of the dataset
        idgroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            dataset.setIdCol(rbtns.indexOf(newValue));
        });
        optionsbox.getChildren().addAll(rbtns);
    }


    private void addColumnsToTable(String atts[]){

        _columnHeaders = new ArrayList<>();
        int i = 0;
        for (String at : atts) {
            TableColumn tc = new TableColumn(at);
            final int colNo = i++;

            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
                    return new SimpleStringProperty((p.getValue()[colNo]));
                }
            });

            tc.setPrefWidth(90);
            datatableObject.getColumns().addAll(tc);
            _columnHeaders.add(tc);
        }
    }

}
