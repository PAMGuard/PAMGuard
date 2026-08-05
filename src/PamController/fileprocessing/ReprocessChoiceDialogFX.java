package PamController.fileprocessing;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

import PamUtils.PamCalendar;

public class ReprocessChoiceDialogFX {

    private ReprocessStoreChoice chosenChoice = null;

    public static ReprocessStoreChoice showDialog(Window parent, StoreChoiceSummary choiceSummary) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (parent != null) {
            dialog.initOwner(parent);
        }
        dialog.setTitle("Existing Output Data");

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(15));

        // Data summary
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-border-width: 1;");
        infoBox.setPadding(new Insets(10));
        infoBox.getChildren().add(new Label(
                String.format("Input data dates: %s to %s",
                        PamCalendar.formatDBDateTime(choiceSummary.getInputStartTime()),
                        PamCalendar.formatDBDateTime(choiceSummary.getInputEndTime()))
        ));
        infoBox.getChildren().add(new Label(
                String.format("Output data dates: %s to %s",
                        PamCalendar.formatDBDateTime(choiceSummary.getOutputStartTime()),
                        PamCalendar.formatDBDateTime(choiceSummary.getOutputEndTime()))
        ));
        infoBox.getChildren().add(new Label(
                choiceSummary.isProcessingComplete() ?
                        "Processing appears to be complete" :
                        "Processing appears to be partially complete"
        ));

        mainBox.getChildren().add(infoBox);

        // Choices
        VBox choiceBox = new VBox(5);
        choiceBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-border-width: 1;");
        choiceBox.setPadding(new Insets(10));
        choiceBox.getChildren().add(new Label("Choose what to do:"));

        ToggleGroup group = new ToggleGroup();
        List<ReprocessStoreChoice> userChoices = choiceSummary.getChoices();
        RadioButton[] choiceButtons = new RadioButton[userChoices.size()];
        for (int i = 0; i < userChoices.size(); i++) {
            ReprocessStoreChoice aChoice = userChoices.get(i);
            RadioButton rb = new RadioButton(aChoice.toString());
            rb.setToggleGroup(group);
            rb.setTooltip(new Tooltip(aChoice.getToolTip()));
            choiceButtons[i] = rb;
            choiceBox.getChildren().add(rb);
        }
        mainBox.getChildren().add(choiceBox);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setStyle("-fx-alignment: center-right;");
        Button okButton = new Button("OK");
        okButton.setDisable(true);
        Button cancelButton = new Button("Cancel");
        buttonBox.getChildren().addAll(cancelButton, okButton);
        mainBox.getChildren().add(buttonBox);

        // Enable OK only when a choice is selected
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });

        final ReprocessStoreChoice[] result = new ReprocessStoreChoice[1];

        okButton.setOnAction(e -> {
            for (int i = 0; i < choiceButtons.length; i++) {
                if (choiceButtons[i].isSelected()) {
                    result[0] = userChoices.get(i);
                    break;
                }
            }
            if (result[0] == ReprocessStoreChoice.OVERWRITEALL) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete / overwrite all existing output data ?",
                        ButtonType.OK, ButtonType.CANCEL);
                alert.setTitle("Overwrite existing data");
                alert.initOwner(dialog);
                ButtonType answer = alert.showAndWait().orElse(ButtonType.CANCEL);
                if (answer == ButtonType.CANCEL) {
                    return;
                }
            }
            dialog.close();
        });

        cancelButton.setOnAction(e -> {
            result[0] = null;
            dialog.close();
        });

        dialog.setScene(new Scene(mainBox));
        dialog.showAndWait();
        return result[0];
    }
}
