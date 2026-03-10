package IshmaelDetector.layoutFX;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import java.util.List;

import IshmaelDetector.IshDetParams;
import IshmaelDetector.SgramCorrControl;
import IshmaelDetector.SgramCorrParams;
import PamController.SettingsPane;

public class SpecCorrPane extends SettingsPane<IshDetParams> {
    
    
    private TableView<SegmentRow> tableView;
    
    
    private TextField spreadField;
    
    
    private CheckBox useLogCheckBox;
    
    
    private LineChart<Number, Number> contourChart;
    
    
    private SgramCorrParams params;
    
    
	private SgramCorrControl specIshDetControl;


	private VBox root;

    public SpecCorrPane(SgramCorrControl specIshDetControl) {
    	super(null);
//        params = oldParams != null ? oldParams : new SgramCorrParams();
//        dialogStage = new Stage();
//        dialogStage.initOwner(parent);
//        dialogStage.initModality(Modality.APPLICATION_MODAL);
//        dialogStage.setTitle("Spectrogram Correlation Parameters");
    	
    	this.specIshDetControl=specIshDetControl;

         root = new VBox(10);
        root.setPadding(new Insets(10));

        // Table for segment data
        tableView = new TableView<>();
        tableView.setEditable(true);
        TableColumn<SegmentRow, Double> t0Col = new TableColumn<>("t0");
        t0Col.setCellValueFactory(cellData -> cellData.getValue().t0Property().asObject());
        t0Col.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        TableColumn<SegmentRow, Double> f0Col = new TableColumn<>("f0");
        f0Col.setCellValueFactory(cellData -> cellData.getValue().f0Property().asObject());
        f0Col.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        TableColumn<SegmentRow, Double> t1Col = new TableColumn<>("t1");
        t1Col.setCellValueFactory(cellData -> cellData.getValue().t1Property().asObject());
        t1Col.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        TableColumn<SegmentRow, Double> f1Col = new TableColumn<>("f1");
        f1Col.setCellValueFactory(cellData -> cellData.getValue().f1Property().asObject());
        f1Col.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        tableView.getColumns().addAll(t0Col, f0Col, t1Col, f1Col);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Controls for adding/removing rows
        HBox rowControls = new HBox(10);
        Button addRowBtn = new Button("Add Row");
        addRowBtn.setOnAction(e -> tableView.getItems().add(new SegmentRow()));
        Button removeRowBtn = new Button("Remove Selected Row");
        removeRowBtn.setOnAction(e -> {
            SegmentRow selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) tableView.getItems().remove(selected);
        });
        Button pasteBtn = new Button("Paste from Clipboard");
        pasteBtn.setOnAction(e -> pasteFromClipboard());
        rowControls.getChildren().addAll(addRowBtn, removeRowBtn, pasteBtn);

        // Spread and useLog controls
        HBox paramControls = new HBox(10);
        paramControls.getChildren().addAll(new Label("Kernel Width, Hz:"), spreadField = new TextField(), useLogCheckBox = new CheckBox("Use log-scaled spectrogram"));
        spreadField.setPrefWidth(80);

        // Chart for contour
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency");
        contourChart = new LineChart<>(xAxis, yAxis);
        contourChart.setTitle("Time-Frequency Contour");
        contourChart.setPrefHeight(250);
        contourChart.setAnimated(false);

        // Update chart when table changes
        tableView.getItems().addListener((javafx.collections.ListChangeListener<SegmentRow>) c -> updateChart());

        // OK/Cancel buttons
//        HBox buttonBox = new HBox(10);
//        Button okBtn = new Button("OK");
//        okBtn.setOnAction(e -> {
//            if (applyParams()) dialogStage.close();
//        });
//        Button cancelBtn = new Button("Cancel");
//        cancelBtn.setOnAction(e -> dialogStage.close());
//        buttonBox.getChildren().addAll(okBtn, cancelBtn);

        root.getChildren().addAll(new Label("Segments (t0, f0, t1, f1):"), tableView, rowControls, paramControls, contourChart);
        //loadParams();
    }

//    public void showAndWait() {
//        dialogStage.showAndWait();
//    }

    private void loadParams() {
        spreadField.setText(Double.toString(params.spread));
        useLogCheckBox.setSelected(params.useLog);
        tableView.getItems().clear();
        if (params.segment != null) {
            for (double[] seg : params.segment) {
                if (seg.length == 4) tableView.getItems().add(new SegmentRow(seg[0], seg[1], seg[2], seg[3]));
            }
        }
        updateChart();
    }

    private boolean applyParams() {
        try {
            params.spread = Double.parseDouble(spreadField.getText());
            params.useLog = useLogCheckBox.isSelected();
            List<SegmentRow> rows = tableView.getItems();
            params.segment = new double[rows.size()][4];
            for (int i = 0; i < rows.size(); i++) {
                SegmentRow r = rows.get(i);
                params.segment[i][0] = r.getT0();
                params.segment[i][1] = r.getF0();
                params.segment[i][2] = r.getT1();
                params.segment[i][3] = r.getF1();
            }
            return true;
        } catch (Exception ex) {
            // Show error dialog if needed
            return false;
        }
    }

    private void updateChart() {
        contourChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (SegmentRow row : tableView.getItems()) {
            series.getData().add(new XYChart.Data<>(row.getT0(), row.getF0()));
            series.getData().add(new XYChart.Data<>(row.getT1(), row.getF1()));
        }
        contourChart.getData().add(series);
    }

    private void pasteFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String text = clipboard.getString();
            String[] lines = text.split("\r?\n");
            for (String line : lines) {
                String[] tokens = line.split("\t|,");
                if (tokens.length >= 4) {
                    try {
                        double t0 = Double.parseDouble(tokens[0].trim());
                        double f0 = Double.parseDouble(tokens[1].trim());
                        double t1 = Double.parseDouble(tokens[2].trim());
                        double f1 = Double.parseDouble(tokens[3].trim());
                        tableView.getItems().add(new SegmentRow(t0, f0, t1, f1));
                    } catch (NumberFormatException ignored) {}
                }
            }
            updateChart();
        }
    }

    // Helper class for table rows
    public static class SegmentRow {
        private final javafx.beans.property.DoubleProperty t0 = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.DoubleProperty f0 = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.DoubleProperty t1 = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.DoubleProperty f1 = new javafx.beans.property.SimpleDoubleProperty();

        public SegmentRow() { this(0,0,0,0); }
        public SegmentRow(double t0, double f0, double t1, double f1) {
            this.t0.set(t0); this.f0.set(f0); this.t1.set(t1); this.f1.set(f1);
        }
        public javafx.beans.property.DoubleProperty t0Property() { return t0; }
        public javafx.beans.property.DoubleProperty f0Property() { return f0; }
        public javafx.beans.property.DoubleProperty t1Property() { return t1; }
        public javafx.beans.property.DoubleProperty f1Property() { return f1; }
        public double getT0() { return t0.get(); }
        public double getF0() { return f0.get(); }
        public double getT1() { return t1.get(); }
        public double getF1() { return f1.get(); }
    }

	@Override
	public IshDetParams getParams(IshDetParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(IshDetParams params) {
		this.params = (SgramCorrParams) params;
		loadParams() ;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Ishmael Spec. Correlation Pane";
	}

	@Override
	public Node getContentNode() {
		return root;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
}
