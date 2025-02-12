package pamViewFX.fxStyles;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PamFXSwingComparison extends Application {

    @Override
    public void start(Stage primaryStage) {
    	
	    try {
			UIManager.setLookAndFeel(new FlatLightLaf() );

		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
        // JavaFX Controls
        Button fxButton = new Button("JavaFX Button");
        Label fxLabel = new Label("JavaFX Label");
        TextField fxTextField = new TextField();
        CheckBox fxCheckBox = new CheckBox("JavaFX CheckBox");
        RadioButton fxRadioButton = new RadioButton("JavaFX RadioButton");
        ComboBox<String> fxComboBox = new ComboBox<>();
        fxComboBox.getItems().addAll("Item 1", "Item 2", "Item 3");
        ListView<String> fxListView = new ListView<>();
        fxListView.getItems().addAll("List Item 1", "List Item 2");

        // Apply CSS
        Scene scene = new Scene(new VBox()); // Placeholder root; we'll set it later
//        scene.getStylesheets().add(getClass().getResource("/Resources/css/pamCSS_windows.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/Resources/css/pamCSS_flatlaf.css").toExternalForm());

        GridPane fxGrid = new GridPane();
        fxGrid.setHgap(10);
        fxGrid.setVgap(10);
        fxGrid.setPadding(new Insets(20));

        fxGrid.add(fxButton, 0, 0);
        fxGrid.add(fxLabel, 0, 1);
        fxGrid.add(fxTextField, 0, 2);
        fxGrid.add(fxCheckBox, 0, 3);
        fxGrid.add(fxRadioButton, 0, 4);
        fxGrid.add(fxComboBox, 0, 5);
        fxGrid.add(fxListView, 0, 6);


        // Swing Controls
        JButton swingButton = new JButton("Swing Button");
        JLabel swingLabel = new JLabel("Swing Label");
        JTextField swingTextField = new JTextField();
        JCheckBox swingCheckBox = new JCheckBox("Swing CheckBox");
        JRadioButton swingRadioButton = new JRadioButton("Swing RadioButton");
        JComboBox<String> swingComboBox = new JComboBox<>(new String[]{"Item 1", "Item 2", "Item 3"});
        JList<String> swingList = new JList<>(new String[]{"List Item 1", "List Item 2"});

        JPanel swingPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        swingPanel.add(swingButton, gbc);
        gbc.gridy++;
        swingPanel.add(swingLabel, gbc);
        gbc.gridy++;
        swingPanel.add(swingTextField, gbc);
        gbc.gridy++;
        swingPanel.add(swingCheckBox, gbc);
        gbc.gridy++;
        swingPanel.add(swingRadioButton, gbc);
        gbc.gridy++;
        swingPanel.add(swingComboBox, gbc);
        gbc.gridy++;
        swingPanel.add(new JScrollPane(swingList), gbc); // Add a scroll pane for the JList


        // Embed Swing Panel in JavaFX
        javafx.embed.swing.SwingNode swingNode = new javafx.embed.swing.SwingNode();
        swingNode.setContent(swingPanel);

        // Main Layout (Side-by-Side)
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(20));

        HBox comparisonPane = new HBox();
        comparisonPane.setSpacing(20);
        comparisonPane.setAlignment(Pos.CENTER);
        comparisonPane.getChildren().addAll(fxGrid, swingNode);


        root.getChildren().add(comparisonPane);

        scene.setRoot(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Swing vs. JavaFX Comparison");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}