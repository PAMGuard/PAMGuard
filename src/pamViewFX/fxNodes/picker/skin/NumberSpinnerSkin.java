package pamViewFX.fxNodes.picker.skin;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013, Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.math.BigDecimal;

import pamViewFX.fxNodes.picker.ClickRepeater;
import pamViewFX.fxNodes.picker.NumberSpinner;

/**
 * The default skin for the {@linkplain NumberSpinner} control.
 *
 * @author Christian Schudt
 */
public final class NumberSpinnerSkin extends StackPane implements Skin<NumberSpinner> {
	
    private static final String TOP = "top";

    private static final String BOTTOM = "bottom";

    private static final String TOP_LEFT = "top-left";

    private static final String BOTTOM_LEFT = "bottom-left";

    private static final String LEFT = "left";

    private static final String RIGHT = "right";

    private static final String BOTTOM_RIGHT = "bottom-right";

    private static final String TOP_RIGHT = "top-right";

    private static final String CENTER = "center";

    private final String[] cssClasses = {TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};

    private final TextField textField;

    private final NumberSpinner numberSpinner;

    private final ChangeListener<IndexRange> changeListenerSelection;

    private final ChangeListener<Number> changeListenerCaretPosition;

    private final ChangeListener<Number> changeListenerValue;

    private final ChangeListener<HPos> changeListenerHAlignment;

    private final Button btnIncrement;

    private final Button btnDecrement;

    private final Region arrowIncrement;

    private final Region arrowDecrement;

    /**
     * @param numberSpinner The control.
     */
    public NumberSpinnerSkin(final NumberSpinner numberSpinner) {

        this.numberSpinner = numberSpinner;

        minHeightProperty().bind(numberSpinner.minHeightProperty());

        // The TextField
        textField = new TextField();
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean1) {
                if (textField.isEditable() && aBoolean1) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textField.selectAll();
                        }
                    });
                }

                // setStyle explicitly is a workaround for this JavaFX 2.2 bug:
                // https://javafx-jira.kenai.com/browse/RT-23085
                String javafxVersion = System.getProperty("javafx.runtime.version");
                if (textField.isFocused()) {
                    getStyleClass().add("number-spinner-focused");
                    if (javafxVersion.startsWith("2.2")) {
                        setStyle("-fx-background-color: -fx-focus-color, -fx-text-box-border, -fx-control-inner-background;\n" +
                                "    -fx-background-insets: -0.4, 1, 2;\n" +
                                "    -fx-background-radius: 3.4, 2, 2");
                    }
                } else {
                    getStyleClass().remove("number-spinner-focused");
                    if (javafxVersion.startsWith("2.2")) {
                        setStyle("-fx-background-color: null;\n" +
                                "    -fx-background-insets: null;\n" +
                                "    -fx-background-radius: null");
                    }
                    parseText();
                    setText();
                }
            }
        });

        // Mimic bidirectional binding: Whenever the selection changes of either the control or the text field, propagate it to the other.
        // This ensures that the selectionProperty of both are in sync.
        changeListenerSelection = new ChangeListener<IndexRange>() {
            @Override
            public void changed(ObservableValue<? extends IndexRange> observableValue, IndexRange indexRange, IndexRange indexRange2) {
                textField.selectRange(indexRange2.getStart(), indexRange2.getEnd());
            }
        };
        numberSpinner.selectionProperty().addListener(changeListenerSelection);

        textField.selectionProperty().addListener(new ChangeListener<IndexRange>() {
            @Override
            public void changed(ObservableValue<? extends IndexRange> observableValue, IndexRange indexRange, IndexRange indexRange1) {
                numberSpinner.selectRange(indexRange1.getStart(), indexRange1.getEnd());
            }
        });

        // Mimic bidirectional binding: Whenever the caret position changes in either the control or the text field, propagate it to the other.
        // This ensures that both caretPositions are in sync.
        changeListenerCaretPosition = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number1) {
                textField.positionCaret(number1.intValue());
            }
        };
        numberSpinner.caretPositionProperty().addListener(changeListenerCaretPosition);

        textField.caretPositionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number1) {
                numberSpinner.positionCaret(number1.intValue());
            }
        });

        // Bind the control's properties to the text field.
        textField.minHeightProperty().bind(numberSpinner.minHeightProperty());
        textField.maxHeightProperty().bind(numberSpinner.maxHeightProperty());
        textField.textProperty().bindBidirectional(numberSpinner.textProperty());
        textField.alignmentProperty().bind(numberSpinner.alignmentProperty());
        textField.editableProperty().bind(numberSpinner.editableProperty());
        textField.prefColumnCountProperty().bind(numberSpinner.prefColumnCountProperty());
        textField.promptTextProperty().bind(numberSpinner.promptTextProperty());
        textField.onActionProperty().bind(numberSpinner.onActionProperty());
        textField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!keyEvent.isConsumed()) {
                    if (keyEvent.getCode().equals(KeyCode.UP)) {
                        btnIncrement.fire();
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode().equals(KeyCode.DOWN)) {
                        btnDecrement.fire();
                        keyEvent.consume();
                    }
                }
            }
        });
        setText();

        changeListenerValue = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                setText();
            }
        };
        numberSpinner.valueProperty().addListener(changeListenerValue);
        changeListenerHAlignment = new ChangeListener<HPos>() {
            @Override
            public void changed(ObservableValue<? extends HPos> observableValue, HPos hPos, HPos hPos1) {
                align(numberSpinner.getHAlignment());
            }
        };
        numberSpinner.hAlignmentProperty().addListener(changeListenerHAlignment);


        // The increment button.
        btnIncrement = new Button();
        btnIncrement.setFocusTraversable(false);
        btnIncrement.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(numberSpinner.valueProperty(), numberSpinner.maxValueProperty());
            }

            @Override
            protected boolean computeValue() {

                return numberSpinner.valueProperty().get() != null && numberSpinner.maxValueProperty().get() != null && numberSpinner.valueProperty().get().doubleValue() >= numberSpinner.maxValueProperty().get().doubleValue();
            }
        });
        btnIncrement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                parseText();
                numberSpinner.increment();
            }
        });
        arrowIncrement = createArrow();
        btnIncrement.setGraphic(arrowIncrement);

        btnIncrement.setMinHeight(0);
        ClickRepeater.install(btnIncrement);


        // The decrement button
        btnDecrement = new Button();
        btnDecrement.setFocusTraversable(false);
        btnDecrement.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(numberSpinner.valueProperty(), numberSpinner.minValueProperty());
            }

            @Override
            protected boolean computeValue() {
                return numberSpinner.valueProperty().get() != null && numberSpinner.minValueProperty().get() != null && numberSpinner.valueProperty().get().doubleValue() <= numberSpinner.minValueProperty().get().doubleValue();
            }
        });
        btnDecrement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                parseText();
                numberSpinner.decrement();
            }
        });
        arrowDecrement = createArrow();
        btnDecrement.setGraphic(arrowDecrement);
        btnDecrement.setMinHeight(0);
        ClickRepeater.install(btnDecrement);

        // Allow the buttons to grow vertically.
        VBox.setVgrow(btnIncrement, Priority.ALWAYS);
        VBox.setVgrow(btnDecrement, Priority.ALWAYS);

        // Allow the text field to allow horizontally.
        HBox.setHgrow(textField, Priority.ALWAYS);
        align(numberSpinner.getHAlignment());
    }

    /**
     * Creates an arrow for the buttons.
     *
     * @return The arrow.
     */
    private Region createArrow() {
        Region arrow = new Region();
        arrow.setMaxSize(8, 8);
        arrow.getStyleClass().add("arrow");
        return arrow;
    }

    /**
     * Aligns the text field relative to the buttons.
     *
     * @param hPos The horizontal position of the text field.
     */
    private void align(HPos hPos) {
        getChildren().clear();
        clearStyles();
        btnIncrement.maxHeightProperty().unbind();
        btnDecrement.maxHeightProperty().unbind();
        btnIncrement.maxWidthProperty().unbind();
        btnDecrement.maxWidthProperty().unbind();
        switch (hPos) {
            case LEFT:
            case RIGHT:
                alignLeftOrRight(hPos);
                break;
            case CENTER:
            	alignTopBottom();
                break;
        }
    }
    
    /**
     * Align the number spinner so that the buttons are on top and bottom of spinner
     */
    private void alignTopBottom(){
    	 btnIncrement.getStyleClass().add(CENTER);
         btnDecrement.getStyleClass().add(CENTER);

         btnIncrement.maxHeightProperty().setValue(Double.MAX_VALUE);
         btnDecrement.maxHeightProperty().setValue(Double.MAX_VALUE);

         
         arrowIncrement.setRotate(180);
         arrowDecrement.setRotate(0);

         VBox vBox=new VBox();
         vBox.getChildren().add(textField);
         vBox.getChildren().add(0, btnIncrement);
         vBox.getChildren().add(btnDecrement);

         /**Had to set button max width here as binding causes strange results**/
         btnIncrement.maxWidthProperty().setValue(Double.MAX_VALUE);
         btnDecrement.maxWidthProperty().setValue(Double.MAX_VALUE);
                  
         getChildren().add(vBox);
    }

    /**
     * Aligns the text field in between both buttons.
     */
    @SuppressWarnings("unused")
	private void alignCenter() {
        btnIncrement.getStyleClass().add(RIGHT);
        btnDecrement.getStyleClass().add(LEFT);
        textField.getStyleClass().add(CENTER);

        btnIncrement.maxHeightProperty().setValue(Double.MAX_VALUE);
        btnDecrement.maxHeightProperty().setValue(Double.MAX_VALUE);

        arrowIncrement.setRotate(-90);
        arrowDecrement.setRotate(90);

        HBox hbox=new HBox();
        hbox.getChildren().addAll(btnDecrement, textField, btnIncrement);
        getChildren().add(hbox);
    }

    /**
     * Aligns the buttons either left or right.
     *
     * @param hPos The HPos, either {@link HPos#LEFT} or {@link HPos#RIGHT}.
     */
    private void alignLeftOrRight(HPos hPos) {
        // The box which aligns the two buttons vertically.
        final VBox buttonBox = new VBox();
        HBox hBox = new HBox();
        switch (hPos) {
            case RIGHT:
                btnIncrement.getStyleClass().add(TOP_LEFT);
                btnDecrement.getStyleClass().add(BOTTOM_LEFT);
                textField.getStyleClass().add(RIGHT);
                hBox.getChildren().addAll(buttonBox, textField);
                break;
            case LEFT:
                btnIncrement.getStyleClass().add(TOP_RIGHT);
                btnDecrement.getStyleClass().add(BOTTOM_RIGHT);
                textField.getStyleClass().add(LEFT);
                hBox.getChildren().addAll(textField, buttonBox);
                break;
            case CENTER:
                break;
        }

        btnIncrement.maxHeightProperty().bind(textField.heightProperty().divide(2.0));
        // Subtract 0.5 to ensure it looks fine if height is odd.
        btnDecrement.maxHeightProperty().bind(textField.heightProperty().divide(2.0).subtract(0.5));
        arrowIncrement.setRotate(180);
        arrowDecrement.setRotate(0);

        buttonBox.getChildren().addAll(btnIncrement, btnDecrement);
        getChildren().add(hBox);
    }

    /**
     * Clears all styles on all controls.
     */
    private void clearStyles() {
        btnIncrement.getStyleClass().removeAll(cssClasses);
        btnDecrement.getStyleClass().removeAll(cssClasses);
        textField.getStyleClass().removeAll(cssClasses);
    }

    /**
     * Parses the text and sets the {@linkplain NumberSpinner#valueProperty() value} accordingly.
     * If parsing fails, the value is set to null.
     */
    private void parseText() {
        if (textField.getText() != null) {
            try {
                numberSpinner.setValue(BigDecimal.valueOf(numberSpinner.getNumberStringConverter().fromString(textField.getText()).doubleValue()));
            } catch (Exception e) {
                numberSpinner.setValue(null);
            }

        } else {
            numberSpinner.setValue(null);
        }
    }

    /**
     * Sets the formatted value to the text field.
     */
    private void setText() {
        if (numberSpinner.getValue() != null && !Double.isInfinite((numberSpinner.getValue().doubleValue())) && !Double.isNaN(numberSpinner.getValue().doubleValue())) {
            textField.setText(numberSpinner.getNumberStringConverter().toString(numberSpinner.getValue()));
        } else {
            textField.setText(null);
        }
    }

    @Override
    public NumberSpinner getSkinnable() {
        return numberSpinner;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {

        // Unbind everything and remove listeners, in order to avoid memory leaks.
        minHeightProperty().unbind();

        textField.minHeightProperty().unbind();
        textField.maxHeightProperty().unbind();
        textField.minWidthProperty().unbind();
        textField.maxWidthProperty().unbind();
        textField.textProperty().unbindBidirectional(numberSpinner.textProperty());
        textField.alignmentProperty().unbind();
        textField.editableProperty().unbind();
        textField.prefColumnCountProperty().unbind();
        textField.promptTextProperty().unbind();
        textField.onActionProperty().unbind();

        numberSpinner.selectionProperty().removeListener(changeListenerSelection);
        numberSpinner.caretPositionProperty().removeListener(changeListenerCaretPosition);
        numberSpinner.valueProperty().removeListener(changeListenerValue);
        numberSpinner.hAlignmentProperty().removeListener(changeListenerHAlignment);
        btnIncrement.disableProperty().unbind();
        btnDecrement.disableProperty().unbind();

    }
}
