package PamModel.parametermanager.swing;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import PamModel.parametermanager.PamParameterData;

public abstract class NumericParameterComponents implements PamParameterComponents {

	private PamParameterData pamParameterData;
	private String textFormat;
	private int fieldLength;
	
	private JComponent title, postTitle;
	private JTextField dataField;
	private JComponent[] allComponents;
	
	public NumericParameterComponents(PamParameterData pamParameterData, String textFormat) {
		this.pamParameterData = pamParameterData;
		this.textFormat = textFormat;
		makeComponents();
	}
	
	private void makeComponents() {
		int nF = 0;
		if (pamParameterData.getFieldName() != null) {
			title = new JLabel(pamParameterData.getFieldName() + " ", JLabel.RIGHT);
			nF++;
		}
		dataField = new JTextField(getFieldLength());
		nF++;
		if (pamParameterData.getPostTitle() != null) {
			postTitle = new JLabel(" " + pamParameterData.getPostTitle(), JLabel.LEFT);
			nF++;
		}
		allComponents = new JComponent[3];
		allComponents[0] = title;
		allComponents[1] = dataField;
		allComponents[2] = postTitle;
	}

	/**
	 * Get a length for the field. This is either set, or can be automatically 
	 * extracted from the textFormat
	 * @return a field length to put in the dataField constructor
	 */
	private int getFieldLength() {
		if (fieldLength > 0) {
			return fieldLength;
		}
		else {
			return autoFieldLength();
		}
	}

	private int autoFieldLength() {
		if (textFormat == null) {
			return 3;
		}
		// find all digits after the initial % in the textFormat
		int firstDig = -1;
		int lastDig = 0;
		for (int i = 0; i < textFormat.length(); i++) {
			if (Character.isDigit(textFormat.charAt(i))) {
				if (firstDig == -1) {
					firstDig = lastDig = i;
				}
				else {
					lastDig = i;
				}
			}
			else if (firstDig >= 0) {
				// we've started finding digits, but have something that isn't a digit. 
				break;
			}
		}
		try {
			String numStr = textFormat.substring(firstDig, lastDig+1);
			return Integer.valueOf(numStr);
		}
		catch (Exception e) {
			return 3;
		}
	}

	@Override
	public JComponent[] getAllComponents() {
		return allComponents;
	}

	/**
	 * @return the pamParameterData
	 */
	public PamParameterData getPamParameterData() {
		return pamParameterData;
	}

	/**
	 * @return the textFormat
	 */
	public String getTextFormat() {
		return textFormat;
	}

	/**
	 * @return the title
	 */
	public JComponent getTitle() {
		return title;
	}

	/**
	 * @return the postTitle
	 */
	public JComponent getPostTitle() {
		return postTitle;
	}

	/**
	 * @return the dataField
	 */
	public JTextField getDataField() {
		return dataField;
	}

	@Override
	public boolean setField() {
		Object data = null;
		try {
			data = getPamParameterData().getData();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (data == null) {
			return false;
		}
		getDataField().setText(String.format(getTextFormat(), data));
		return true;
	}

}
