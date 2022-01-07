package pamViewFX.fxNodes;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * A {@link javafx.scene.control.SpinnerValueFactory} implementation designed to
 * iterate through a list of values. This also enables users to set the value.
 * The list has space for one extra set value. When a value which is not part of
 * the list is set, then the it is placed in the list in sorted order. When a
 * new setValue() is called with a number which is not present in the list then
 * the previous set value is deleted.
 *
 *
 * <pre>
 * setConverter(new StringConverter&lt;T&gt;() {
 * 	&#064;Override
 * 	public String toString(T value) {
 * 		if (value == null) {
 * 			return "";
 * 		}
 * 		return value.toString();
 * 	}
 *
 * 	&#064;Override
 * 	public T fromString(String string) {
 * 		return (T) string;
 * 	}
 * });
 * </pre>
 *
 * @param <T> The type of the elements in the {@link java.util.List}. Must
 *            extends number
 * 
 */
public class PamListSpinnerFactory<T extends Number & Comparable> extends SpinnerValueFactory<T> {

    /***********************************************************************
     *                                                                     *
     * Private fields                                                      *
     *                                                                     *
     **********************************************************************/

    private int currentIndex = 0;
    
    /**
     * The index of the last unique value added to the list. 
     */
    private int lastAddIndex = -1; 

    private final ListChangeListener<T> itemsContentObserver = c -> {
        // the items content has changed. We do not try to find the current
        // item, instead we remain at the currentIndex, if possible, or else
        // we go back to index 0, and if that fails, we go to null
        updateCurrentIndex();
    };

    private WeakListChangeListener<T> weakItemsContentObserver =
            new WeakListChangeListener<T>(itemsContentObserver);



    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    /**
     * Creates a new instance of the ListSpinnerValueFactory with the given
     * list used as the list to step through.
     *
     * @param items The list of items to step through with the Spinner.
     */
    public PamListSpinnerFactory(ObservableList<T> items) {
        setItems(items);
        setConverter(new StringConverter<T>() {
            @Override public String toString(T value) {
                if (value == null) {
                    return "";
                }
                return value.toString();
            }

            @Override public T fromString(String string) {
            	//TODO bit dangerous - user should implement their own converter
                try {
					return (T) NumberFormat.getInstance().parse(string);
				} catch (ParseException e) {
					e.printStackTrace();
					return null;
				} 
            }
        });

        valueProperty().addListener((o, oldValue, newValue) -> {
            // when the value is set, we need to react to ensure it is a
            // valid value (and if not, blow up appropriately)
            int newIndex = -1;
            if (items.contains(newValue)) {
                newIndex = items.indexOf(newValue);
            } else {
            	
            	//set the new value in the correct order. 
            	newIndex = setNewValue(newValue); 
//                // add newValue to list
//                items.add(newValue);
//                newIndex = items.indexOf(newValue);
            }
            currentIndex = newIndex;
        });
        setValue(_getValue(currentIndex));
    }
    
    
    /**
     * Set a new value which is not in the list. This is added to the list and the list then sorted
     * @param newValue - the new Value
     * @return the index of the new value. 
     */
    private int setNewValue(T newValue) {
    	
    	//remove the last value from the list
    	if (lastAddIndex>=0) {
    		this.getItems().remove(lastAddIndex); 
    	}
    
    	//reset the last add index
    	lastAddIndex=-1; 
    	
    	//now add the new value...
    	this.getItems().add(newValue); 
    	
    	//sort the array 
    	Collections.sort(getItems(), new NumberComparator<T>());
    	
    	//now the arrayList is sorted return the new index. 
    	lastAddIndex = getItems().indexOf(newValue); 
    	
    	return lastAddIndex; 
    }
    
    /**
     * Can compare Numbers of the same. If different type then throws an exception. 
     * @author Jamie Macaulay
     *
     * @param <T>
     */
    class NumberComparator<T extends Number & Comparable> implements Comparator<T> {

        public int compare( T a, T b ) throws ClassCastException {
            return a.compareTo( b );
        }
    }



    /***********************************************************************
     *                                                                     *
     * Properties                                                          *
     *                                                                     *
     **********************************************************************/
    // --- Items
    private ObjectProperty<ObservableList<T>> items;

    /**
     * Sets the underlying data model for the ListSpinnerValueFactory. Note that it has a generic
     * type that must match the type of the Spinner itself.
     * @param value the list of items
     */
    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    /**
     * Returns an {@link javafx.collections.ObservableList} that contains the items currently able
     * to be iterated through by the user. This may be null if
     * {@link #setItems(javafx.collections.ObservableList)} has previously been
     * called, however, by default it is an empty ObservableList.
     *
     * @return An ObservableList containing the items to be shown to the user, or
     *      null if the items have previously been set to null.
     */
    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    /**
     * The underlying data model for the ListView. Note that it has a generic
     * type that must match the type of the ListView itself.
     * @return the list of items
     */
    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        if (items == null) {
            items = new SimpleObjectProperty<ObservableList<T>>(this, "items") {
                WeakReference<ObservableList<T>> oldItemsRef;

                @Override protected void invalidated() {
                    ObservableList<T> oldItems = oldItemsRef == null ? null : oldItemsRef.get();
                    ObservableList<T> newItems = getItems();

                    // update listeners
                    if (oldItems != null) {
                        oldItems.removeListener(weakItemsContentObserver);
                    }
                    if (newItems != null) {
                        newItems.addListener(weakItemsContentObserver);
                    }

                    // update the current value based on the index
                    updateCurrentIndex();

                    oldItemsRef = new WeakReference<>(getItems());
                }
            };
        }
        return items;
    }



    /***********************************************************************
     *                                                                     *
     * Overridden methods                                                  *
     *                                                                     *
     **********************************************************************/

    /** {@inheritDoc} */
    @Override public void decrement(int steps) {
        final int max = getItemsSize() - 1;
        int newIndex = currentIndex - steps;
        currentIndex = newIndex >= 0 ? newIndex : (isWrapAround() ? wrapValue(newIndex, 0, max + 1) : 0);
        setValue(_getValue(currentIndex));
    }

    /** {@inheritDoc} */
    @Override public void increment(int steps) {
        final int max = getItemsSize() - 1;
        int newIndex = currentIndex + steps;
        currentIndex = newIndex <= max ? newIndex : (isWrapAround() ? wrapValue(newIndex, 0, max + 1) : max);
        setValue(_getValue(currentIndex));
    }



    /***********************************************************************
     *                                                                     *
     * Private implementation                                              *
     *                                                                     *
     **********************************************************************/
    private int getItemsSize() {
        List<T> items = getItems();
        return items == null ? 0 : items.size();
    }

    private void updateCurrentIndex() {
        int itemsSize = getItemsSize();
        if (currentIndex < 0 || currentIndex >= itemsSize) {
            currentIndex = 0;
        }
        setValue(_getValue(currentIndex));
    }

    private T _getValue(int index) {
        List<T> items = getItems();
        return items == null ? null : (index >= 0 && index < items.size()) ? items.get(index) : null;
    }
    
    /*
     * Convenience method to support wrapping values around their min / max
     * constraints. Used by the SpinnerValueFactory implementations when
     * the Spinner wrapAround property is true.
     */
    static int wrapValue(int value, int min, int max) {
        if (max == 0) {
            throw new RuntimeException();
        }

        int r = value % max;
        if (r > min && max < min) {
            r = r + max - min;
        } else if (r < min && max > min) {
            r = r + max - min;
        }
        return r;
    }

}