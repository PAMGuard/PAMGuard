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

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.util.Duration;

/**
 * Usually a {@link ButtonBase} only fires once per mouse click, namely when the mouse is released.
 * This class allows a {@link ButtonBase} to periodically fire while armed, e.g. while the mouse is pressed.
 * <p/>
 * While the button is armed, it waits 500ms to fire the first time. After that it fires every 80ms by default.
 * <p/>
 * Therefore it mimics the behavior of key press events as they fire constantly, too, while the key is pressed.
 * <p/>
 * <h3>Sample Usage</h3>
 * <pre>
 * <code>
 * Button button = new Button("I'll fire constantly");
 * button.setOnAction(new EventHandler&lt;ActionEvent&gt;()} {
 *      {@literal @}Override
 *      public void handle(ActionEvent actionEvent) {
 *         System.out.println("Fired!");
 *      }
 * });
 * ClickRepeater.install(button);
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class ClickRepeater {

    /**
     * This is the initial pause until the button is fired for the first time. This is 500 ms as it the same value used by key events.
     */
    private final PauseTransition initialPause = new PauseTransition(Duration.millis(500));

    /**
     * This is for all the following intervals, after the first one. 80 ms is also used by key events.
     */
    private final PauseTransition pauseTransition = new PauseTransition();

    /**
     * This transition combines the first two.
     */
    private final SequentialTransition sequentialTransition = new SequentialTransition(initialPause, pauseTransition);

    /**
     * Store the change listener, so that it can be removed in the {@link #uninstall(javafx.scene.control.ButtonBase)} method.
     */
    private final ChangeListener<Boolean> changeListener;

    /**
     * Private constructor.
     *
     * @param buttonBase The button.
     */
    private ClickRepeater(final ButtonBase buttonBase, final Duration interval) {
        initialPause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // Fire the button the first time after the initial pause.
                buttonBase.fire();
            }
        });

        pauseTransition.setDuration(interval);
        pauseTransition.setCycleCount(Animation.INDEFINITE);
        pauseTransition.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration duration2) {
                // Every time a new cycle starts, fire the button.
                if (duration.greaterThan(duration2)) {
                    buttonBase.fire();
                }
            }
        });
        changeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                if (aBoolean2) {
                    // If the button gets armed, start the animation.
                    sequentialTransition.playFromStart();
                } else {
                    // Stop the animation, if the button is no longer armed.
                    sequentialTransition.stop();
                }
            }
        };
        buttonBase.armedProperty().addListener(changeListener);
    }

    /**
     * Installs the click repeating behavior for a {@link ButtonBase}.
     * The default click interval is 80ms.
     *
     * @param buttonBase The button.
     */
    public static void install(ButtonBase buttonBase) {
        install(buttonBase, Duration.millis(80));
    }

    /**
     * Installs the click repeating behavior for a {@link ButtonBase} and also allows to set a click interval.
     *
     * @param buttonBase The button.
     * @param interval   The click interval.
     */
    public static void install(ButtonBase buttonBase, Duration interval) {
        // Uninstall any previous behavior.
        uninstall(buttonBase);

        // Initializes a new ClickRepeater
        if (!buttonBase.getProperties().containsKey(ClickRepeater.class)) {
            // Store the ClickRepeater in the button's properties.
            // If the button will get GCed, so will its ClickRepeater.
            buttonBase.getProperties().put(ClickRepeater.class, new ClickRepeater(buttonBase, interval));
        }
    }

    /**
     * Uninstalls the click repeater behavior from a button.
     *
     * @param buttonBase The button.
     */
    public static void uninstall(ButtonBase buttonBase) {
        if (buttonBase.getProperties().containsKey(ClickRepeater.class) && buttonBase.getProperties().get(ClickRepeater.class) instanceof ClickRepeater) {
            ClickRepeater clickRepeater = (ClickRepeater) buttonBase.getProperties().remove(ClickRepeater.class);
            buttonBase.armedProperty().removeListener(clickRepeater.changeListener);
        }
    }
}
