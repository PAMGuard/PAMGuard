package PamView.dialog.smart;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;


/**
 * Text field which can immediately respond to text being entered and 
 * update a corresponding value in a PAMGurd control structure. Will call abstract
 * function valueChange whenever the control is typed in, enter is hit or the focus
 * of the control is lost. Concrete implementations can then update parameters
 * accordingly. <p>
 * Should be able to handle Double, Integer and String types. To add others, just need
 * to add more options to the getValue() function. 
 *
 * @author Doug Gillespie. 
 *
 */
public abstract class ImmediateTextField<T> {

	private String prefix;
	
	private String suffix; 
	
	private JTextField textField;
	
	private PamLabel preLabel, postLabel;
	
	private NumberFormat numberFormat;

	private Class<T> fieldClass;
	
	private boolean colourManaged;
	
	/**
	 * Construct text field that calls back whenever the value is changed. 
	 * @param fieldClass data class (Double, Integer or String) must match the template class
	 * @param fieldLength field length (characters)
	 * @param prefix optional text prefix to display
	 * @param suffix optional text suffix to display
	 * @param colourManaged Manage day / night colours using standard themes. 
	 */
	public ImmediateTextField(Class<T> fieldClass, int fieldLength, String prefix, String suffix, boolean colourManaged) {
		textField = new PamTextField(fieldLength, colourManaged);
		this.fieldClass = fieldClass;
		this.colourManaged = colourManaged;
		setPrefix(prefix);
		setSuffix(suffix);
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textFieldAction(e);
			}
		});
		textField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				textKeyAction(e);
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				ctrlFocusLost(e);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}
	
	protected void ctrlFocusLost(FocusEvent e) {
		valueChange(true);
	}

	protected void textKeyAction(KeyEvent e) {
		valueChange(false);
	}

	protected void textFieldAction(ActionEvent e) {
		valueChange(true);
	}

	private void valueChange(boolean endTyping) {
		String txt = textField.getText();
		T val = null;
		boolean error = false;
		try {
			val = getValue();
		} catch (Exception e) {
			error = true;
		}
		valueChange(val, error, endTyping);
	}

	private T getValue() throws Exception {
	  if (fieldClass == Double.class) {
			try {
				Double d = Double.valueOf(textField.getText());
				return (T) d;
			}
			catch (NumberFormatException e) {
				throw new Exception("Invalid double number");
			}
		}
		else if (fieldClass == Integer.class) {
			try {
				Integer d = Integer.valueOf(textField.getText());
				return (T) d;
			}
			catch (NumberFormatException e) {
				throw new Exception("Invalid integer number");
			}
		}
		else if (fieldClass == String.class) {
			return (T) textField.getText();
		}
		else {
			return null;
		}
	}

	public void setValue(T value) {
		if (value == null) {
			textField.setText(null);
		}
		if (numberFormat != null) {
			textField.setText(numberFormat.format((Double) value));
		}
		else {
			textField.setText(value.toString());
		}
	}
	
	public void setToolTipText(String tip, boolean preandpost) {
		textField.setToolTipText(tip);
		if (preandpost) {
			if (preLabel != null) {
				preLabel.setToolTipText(tip);
			}
			if (postLabel != null) {
				postLabel.setToolTipText(tip);
			}
		}
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		if (prefix != null) {
			preLabel = new PamLabel(prefix + " ", JLabel.RIGHT);
			preLabel.setDefaultColor(colourManaged ? PamLabel.defaultColor : null);
		}
		else {
			preLabel = null;
		}
	}

	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * @param suffix the suffix to set
	 */
	public void setSuffix(String suffix) {
		if (suffix != null) {
			postLabel = new PamLabel(" " + suffix, JLabel.LEFT);
			postLabel.setDefaultColor(colourManaged ? PamLabel.defaultColor : null);
		}
		else {
			postLabel = null;
		}
		this.suffix = suffix;
	}

	/**
	 * @return the numberFormat
	 */
	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat the numberFormat to set
	 */
	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * @return the textField
	 */
	public JTextField getTextField() {
		return textField;
	}

	/**
	 * @return the preLabel
	 */
	public JLabel getPreLabel() {
		return preLabel;
	}

	/**
	 * @return the postLabel
	 */
	public JLabel getPostLabel() {
		return postLabel;
	}
	
	/**
	 * Called whenever the value changes and also when the keyboard focus leaves
	 * the control. Some users may want to respond to every key stroke, others only 
	 * when the keyboard focus leaves the control. 
	 * @param newValue value in text field. Will be null if valueError
	 * @param valueError error in value, e.g. if number is invalid
	 * @param lostFocus focus has moved from the control or enter has been pressed. 
	 */
	public abstract void valueChange(T newValue, boolean valueError, boolean lostFocus);
	
	/**
	 * Add the components to the parent component. By default, components
	 * will be laid out with an increment of the x value in the 
	 * gridbag constraints of one per non null component. If you 
	 * require different bahaviour, get the components individually 
	 * and do as you like. 
	 * @param parent Parent component
	 * @param c gridbag constraints with correct start position for first non null component. 
	 */
	public void addToParent(JComponent parent, GridBagConstraints c) {
		if (preLabel != null) {
			parent.add(preLabel, c);
			c.gridx++;
		}
		parent.add(textField, c);
		c.gridx++;
		if (postLabel != null) {
			parent.add(postLabel, c);
			c.gridx++;
		}
	}
	
}
