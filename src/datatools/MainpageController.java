package datatools;

import datatools.RESTaccess.AssociationIterator;
import datatools.datahandling.Dataset;
import datatools.datahandling.FileTools;
import datatools.datahandling.SetJoiner;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainpageController {

    // FXML Objects
    public ProgressBar pbar_progressbar;
    public ListView    lview_dataFiles;
    public ListView    lview_associationFiles;
    public Button      btn_removeAssociationFile;
    public Button      btn_createDataFile;
    public Button      btn_buildAssociationList;
    public Button      btn_removeDatafile;
    public Label       lbl_creatingAssociations;
    public HBox        hbox_optionsBox;
    public TableView   tbl_dataTable;

    // member variables
    static  Stage                       _stage;
    private ArrayList<TableColumn>      _columnHeaders;
    private ObservableList<String[]>    _tableRows;
    private ObservableList<String>      _filenamesList;
    private ObservableList<String>      _associationSetsList;
    private List<Dataset>               _loadedSets;
    private Dataset                     _associationSet;


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
        int idx = lview_dataFiles.getSelectionModel().getSelectedIndex();
        File loadFile;

        if(idx >= 0){
            fileChooser.setTitle("Save Data File");
            loadFile = fileChooser.showSaveDialog(_stage);
            if(loadFile == null)
                return;
            FileTools.writeSet(loadFile, _loadedSets.get(idx));
            return;
        }
        idx = lview_associationFiles.getSelectionModel().getSelectedIndex();
        if(idx >= 0){
            fileChooser.setTitle("Save Association File");
            loadFile = fileChooser.showSaveDialog(_stage);
            if(loadFile == null)
                return;
            FileTools.writeSet(loadFile, _associationSet);
        }
    }




    public void onLoadFile(){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DataFile");
        File loadFile = fileChooser.showOpenDialog(_stage);

        if(loadFile == null)
            return;

        Dataset<String> dset = FileTools.loadSet(loadFile);
        ObservableList<String[]> records = FXCollections.observableArrayList(dset.getRecords());

        _loadedSets.add(dset);
        _filenamesList.add(loadFile.getName());
        refreshFileList(lview_dataFiles, _filenamesList);
        btn_buildAssociationList.setDisable(false);
        btn_removeDatafile.setDisable(false);

        lview_dataFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(lview_dataFiles.getSelectionModel().getSelectedIndex() < 0)
                return;

            lview_associationFiles.getSelectionModel().clearSelection();
            clearTable();
            int idx = lview_dataFiles.getSelectionModel().getSelectedIndex();
            if(idx >= 0 && idx < _loadedSets.size())
                createTable(_loadedSets.get(idx));

        });
        lview_dataFiles.getSelectionModel().clearSelection();
        lview_dataFiles.getSelectionModel().selectLast();
    }

    public void onJoinFile(){


        Dataset ds1 = _loadedSets.get(0);
        SetJoiner sj = new SetJoiner();
        Dataset<String> joinedSet = new Dataset<>();
        Task joiner = sj.joinSets(ds1, _associationSet, joinedSet);

        pbar_progressbar.progressProperty().unbind();
        pbar_progressbar.progressProperty().bind(joiner.progressProperty());
        joiner.setOnSucceeded(event -> {
            _loadedSets.add(joinedSet);
            _filenamesList.add("JoinedSet");
            refreshFileList(lview_dataFiles, _filenamesList);
        });

        new Thread(joiner).start();
    }

    public Task associationsTask(Dataset<String> associations){

        ObservableList<Integer> idxs = lview_dataFiles.getSelectionModel().getSelectedIndices();
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
        if(lview_associationFiles.getItems().size() == 0)
            return;

        int idx = lview_associationFiles.getSelectionModel().getSelectedIndex();
        if(idx >= 0) {
            _associationSetsList.clear();
            refreshFileList(lview_associationFiles, _associationSetsList);
        }

        if(lview_associationFiles.getItems().size() == 0){
            btn_removeAssociationFile.setDisable(true);
            btn_createDataFile.setDisable(true);
            return;
        }
    }

    public void onLoadAssociationFile(){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DataFile");
        File loadFile = fileChooser.showOpenDialog(_stage);

        if(loadFile == null)
            return;

        Dataset<String> associations = FileTools.loadSet(loadFile);
        ObservableList<String[]> associationsList = FXCollections.observableArrayList(associations.getRecords());

        setAssociationListener(associations);

        _associationSetsList.clear();
        _associationSet = associations;
        _associationSetsList.add(loadFile.getName());
        refreshFileList(lview_associationFiles, _associationSetsList);

        btn_createDataFile.setDisable(false);
        btn_removeAssociationFile.setDisable(false);
    }


    void setAssociationListener(Dataset<String> associations){
        lview_associationFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            clearTable();

            if(_associationSet != null && lview_associationFiles.getSelectionModel().getSelectedIndex() < 0)
                return;
            lview_dataFiles.getSelectionModel().clearSelection();
            createTable(associations);

        });
    }

    public void onBuildAssociations(){
        lbl_creatingAssociations.setText("Creating Associations");
        lbl_creatingAssociations.setDisable(false);
        Dataset<String> associations = new Dataset<>();
        Task grabber = associationsTask(associations);
        List<String> names = lview_dataFiles.getSelectionModel().getSelectedItems();
        StringBuilder aName = new StringBuilder();
        for(String n : names){
            aName.append(n.substring(0, 3)).append("_");
        }
        _stage.setOnCloseRequest(event -> {
            if(grabber.isRunning())
                grabber.cancel();
        });

        pbar_progressbar.progressProperty().unbind();
        pbar_progressbar.progressProperty().bind(grabber.progressProperty());

        setAssociationListener(associations);

        grabber.setOnSucceeded(event -> {
            btn_createDataFile.setDisable(false);
            btn_removeAssociationFile.setDisable(false);
            _associationSetsList.clear();
            _associationSet = associations;
            _associationSetsList.add(aName.toString());
            refreshFileList(lview_associationFiles, _associationSetsList);
            lbl_creatingAssociations.setText("Complete");
            lbl_creatingAssociations.setDisable(true);
            pbar_progressbar.progressProperty().unbind();
            pbar_progressbar.progressProperty().setValue(0);
        });

        new Thread(grabber).start();
    }

    public void onDeleteFile(){

        ObservableList<Integer> selectedIndices = lview_dataFiles.getSelectionModel().getSelectedIndices();

        for(Integer index : selectedIndices){
            _filenamesList.remove(index.intValue());
            refreshFileList(lview_dataFiles, _filenamesList);
            _loadedSets.remove(index.intValue());
            lview_dataFiles.refresh();
            clearTable();
        }
    }

    private void clearTable(){
        // clear the table
        tbl_dataTable.getItems().clear();
        tbl_dataTable.getColumns().clear();
        hbox_optionsBox.getChildren().clear();
    }

    private void createTable(Dataset<String> dataset){
        if(dataset == null || dataset.count() == 0)
            return;
        _tableRows = FXCollections.observableArrayList(dataset.getRecords());
        addColumnsToTable(dataset.getRecord(0));
        tbl_dataTable.setItems(_tableRows);
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
        hbox_optionsBox.getChildren().addAll(rbtns);
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
            tbl_dataTable.getColumns().addAll(tc);
            _columnHeaders.add(tc);
        }
    }

}
