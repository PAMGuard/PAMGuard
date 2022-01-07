package pamViewFX.fxNodes.picker;

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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.math.BigDecimal;

/**
 * The number spinner allows users to select a number by spinning through a range of numbers.
 * <p/>
 * The range of numbers can be limited by a {@linkplain #minValueProperty() min} and {@linkplain #maxValueProperty() max} value.
 * <p/>
 * <h3>Screenshots</h3>
 * <img src="doc-files/NumberSpinner.png" />
 * <p/>
 * <h3>Sample Usage</h3>
 * <pre>
 * <code>
 * NumberSpinner numberSpinner = new NumberSpinner();
 * numberSpinner.setMaxValue(100);
 * numberSpinner.setMinValue(-100);
 * numberSpinner.setStepWidth(0.1);
 * numberSpinner.setAlignment(Pos.CENTER_RIGHT);
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class NumberSpinner extends TextField {

    /**
     * The numeric value.
     */
    private final ObjectProperty<Number> value = new SimpleObjectProperty<Number>(this, "value") {
        @Override
        protected void invalidated() {
            if (!isBound() && value.get() != null) {
                if (maxValue.get() != null && value.get().doubleValue() > maxValue.get().doubleValue()) {
                    set(maxValue.get());
                }
                if (minValue.get() != null && value.get().doubleValue() < minValue.get().doubleValue()) {
                    set(minValue.get());
                }
            }
        }
    };

    /**
     * The max value.
     */
    private final ObjectProperty<Number> maxValue = new SimpleObjectProperty<Number>(this, "maxValue") {
        @Override
        protected void invalidated() {
            if (maxValue.get() != null) {
                if (minValue.get() != null && maxValue.get().doubleValue() < minValue.get().doubleValue()) {
                    throw new IllegalArgumentException("maxValue must not be greater than minValue");
                }
                if (value.get() != null && value.get().doubleValue() > maxValue.get().doubleValue()) {
                    value.set(maxValue.get());
                }
            }
        }
    };

    /**
     * The min value.
     */
    private final ObjectProperty<Number> minValue = new SimpleObjectProperty<Number>(this, "minValue") {
        @Override
        protected void invalidated() {
            if (minValue.get() != null) {
                if (maxValue.get() != null && maxValue.get().doubleValue() < minValue.get().doubleValue()) {
                    throw new IllegalArgumentException("minValue must not be smaller than maxValue");
                }
                if (value.get() != null && value.get().doubleValue() < minValue.get().doubleValue()) {
                    value.set(minValue.get());
                }
            }
        }
    };

    /**
     * The step width.
     */
    private final ObjectProperty<Number> stepWidth = new SimpleObjectProperty<Number>(this, "stepWidth", 1);

    /**
     * The number format.
     */
    private final ObjectProperty<NumberStringConverter> numberStringConverter = new SimpleObjectProperty<>(this, "numberFormatter", new NumberStringConverter());

    /**
     * The horizontal alignment of the text field.
     */
    private ObjectProperty<HPos> hAlignment = new SimpleObjectProperty<>(this, "hAlignment", HPos.LEFT);


    /**
     * Default constructor. It aligns the text right and set a default {@linkplain StringConverter StringConverter}.
     */
    public NumberSpinner() {
        getStyleClass().add("number-spinner");
        setFocusTraversable(false);

        // Workaround this bug: https://forums.oracle.com/forums/thread.jspa?forumID=1385&threadID=2430102
        sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observableValue, Scene scene, Scene scene1) {
                if (scene1 != null) {
                    scene1.getStylesheets().add(getClass().getResource("NumberSpinner.css").toExternalForm());
                }
            }
        });
    }

    /**
     * Creates the number spinner with a min and max value.
     *
     * @param minValue The min value.
     * @param maxValue The max value.
     */
    public NumberSpinner(final Number minValue, final Number maxValue) {
        this();
        this.minValue.set(minValue);
        this.maxValue.set(maxValue);
    }

    /**
     * The value property. The value can also be null or {@link Double#NaN} or other non-finite values, in order to empty the text field.
     *
     * @return The value property.
     * @see #getValue()
     * @see #setValue(Number)
     */
    public final ObjectProperty<Number> valueProperty() {
        return value;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     * @see #valueProperty()
     */
    public final Number getValue() {
        return value.get();
    }

    /**
     * Sets the value.
     *
     * @param value The value.
     * @see #valueProperty()
     */
    public final void setValue(final Number value) {
        if (!this.value.isBound()) {
            this.value.set(value);
        }
    }

    /**
     * The max value property.
     *
     * @return The property.
     * @see #getMaxValue()
     * @see #setMaxValue(Number)
     */
    public final ObjectProperty<Number> maxValueProperty() {
        return maxValue;
    }

    /**
     * Gets the max value.
     *
     * @return The max value.
     * @see #maxValueProperty()
     */
    public final Number getMaxValue() {
        return maxValue.get();
    }

    /**
     * Sets the max value.
     *
     * @param maxValue The max value.
     * @throws IllegalArgumentException If the max value is smaller than the min value.
     * @see #maxValueProperty()
     */
    public final void setMaxValue(final Number maxValue) {
        this.maxValue.set(maxValue);
    }

    /**
     * The min value property.
     *
     * @return The property.
     * @see #getMinValue()
     * @see #setMinValue(Number)
     */
    public final ObjectProperty<Number> minValueProperty() {
        return minValue;
    }

    /**
     * Gets the min value.
     *
     * @return The min value.
     * @see #minValueProperty()
     */
    public final Number getMinValue() {
        return minValue.get();
    }

    /**
     * Sets the min value.
     *
     * @param minValue The min value.
     * @throws IllegalArgumentException If the min value is greater than the max value.
     * @see #minValueProperty()
     */
    public final void setMinValue(final Number minValue) {
        if (!this.minValue.isBound()) {
            this.minValue.set(minValue);
        }
    }

    /**
     * The step width property.
     * Specifies the interval by which the value is incremented or decremented.
     *
     * @return The step width property.
     * @see #getStepWidth()
     * @see #setStepWidth(Number)
     */
    public final ObjectProperty<Number> stepWidthProperty() {
        return stepWidth;
    }

    /**
     * Gets the step width.
     *
     * @return The step width.
     * @see #stepWidthProperty()
     */
    public final Number getStepWidth() {
        return this.stepWidth.get();
    }

    /**
     * Sets the step width.
     *
     * @param stepWidth The step width.
     * @see #stepWidthProperty()
     */
    public final void setStepWidth(final Number stepWidth) {
        this.stepWidth.setValue(stepWidth);
    }

    /**
     * The number string converter property.
     *
     * @return The number string converter property.
     * @see #getNumberStringConverter()
     * @see #setNumberStringConverter(javafx.util.converter.NumberStringConverter)
     */
    public final ObjectProperty<NumberStringConverter> numberStringConverterProperty() {
        return numberStringConverter;
    }

    /**
     * Gets the number string converter.
     *
     * @return The number string converter.
     * @see #numberStringConverterProperty()
     */
    public final NumberStringConverter getNumberStringConverter() {
        return numberStringConverter.get();
    }

    /**
     * Sets the number format.
     *
     * @param numberStringConverter The number format.
     * @see #numberStringConverterProperty()
     */
    public final void setNumberStringConverter(final NumberStringConverter numberStringConverter) {
        this.numberStringConverter.set(numberStringConverter);
    }

    /**
     * The horizontal alignment of the text field.
     * It can either be aligned left or right to the buttons or in between them (center).
     *
     * @return The property.
     * @see #getHAlignment()
     * @see #setHAlignment(javafx.geometry.HPos)
     */
    public ObjectProperty<HPos> hAlignmentProperty() {
        return hAlignment;
    }

    /**
     * Gets the horizontal alignment of the text field.
     *
     * @return The alignment.
     * @see #hAlignmentProperty()
     */
    public HPos getHAlignment() {
        return hAlignment.get();
    }

    /**
     * The horizontal alignment of the text field.
     *
     * @param hAlignment The alignment.
     * @see #hAlignmentProperty()
     */
    public void setHAlignment(final HPos hAlignment) {
        this.hAlignment.set(hAlignment);
    }

    /**
     * Increments the value by the value specified by {@link #stepWidthProperty()}.
     */
    public void increment() {
        if (getStepWidth() != null && isFinite(getStepWidth().doubleValue())) {
            if (getValue() != null && isFinite(getValue().doubleValue())) {
                setValue(BigDecimal.valueOf(getValue().doubleValue()).add(BigDecimal.valueOf(getStepWidth().doubleValue())));
            } else {
                if (getMinValue() != null && isFinite(getMinValue().doubleValue())) {
                    setValue(BigDecimal.valueOf(getMinValue().doubleValue()).add(BigDecimal.valueOf(getStepWidth().doubleValue())));
                } else {
                    setValue(BigDecimal.valueOf(getStepWidth().doubleValue()));
                }
            }
        }
    }

    /**
     * Decrements the value by the value specified by {@link #stepWidthProperty()}.
     */
    public void decrement() {
        if (getStepWidth() != null && isFinite(getStepWidth().doubleValue())) {
            if (getValue() != null && isFinite(getValue().doubleValue())) {
                setValue(BigDecimal.valueOf(getValue().doubleValue()).subtract(BigDecimal.valueOf(getStepWidth().doubleValue())));
            } else {
                if (getMaxValue() != null && isFinite(getMaxValue().doubleValue())) {
                    setValue(BigDecimal.valueOf(getMaxValue().doubleValue()).subtract(BigDecimal.valueOf(getStepWidth().doubleValue())));
                } else {
                    setValue(BigDecimal.valueOf(getStepWidth().doubleValue()).multiply(new BigDecimal(-1)));
                }
            }
        }
    }

    /**
     * Utility method for Java 7. (Double.isFinite(double) is only available for Java 8)
     *
     * @param value The value.
     * @return True, if the double value is finite.
     */
    private boolean isFinite(double value) {
        return !Double.isInfinite(value) && !Double.isNaN(value);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("NumberSpinner.css").toExternalForm();
    }
}
