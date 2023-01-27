package PamModel.parametermanager.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ManagedParameterPanel<T extends ManagedParameters> {
	
	private JPanel mainPanel;
	private Collection<PamParameterData> parameterSet;
	
	private static final int DEFAULT_TEXT_LENGTH = 6;
	private static final int MAX_SINGLE_LINE_LENGTH = 40;
	
	private JTextComponent[] textComponents;

	public ManagedParameterPanel(T parameterExample) {
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamParameterSet exampleSet = parameterExample.getParameterSet();
		parameterSet = exampleSet.getParameterCollection();
		int n = parameterSet.size();
		textComponents = new JTextComponent[n];
		int i = 0;
		for (PamParameterData paramData : parameterSet) {
			textComponents[i] = createComponent(paramData);
			c.gridx = 0;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.NORTHEAST;
			mainPanel.add(new JLabel(paramData.getShortName(), JLabel.RIGHT), c);
			c.gridx++;
			if (textComponents[i] instanceof JTextArea) {
				c.fill = GridBagConstraints.HORIZONTAL;
			}
			else {
				c.fill = GridBagConstraints.NONE;
			}
			c.anchor = GridBagConstraints.WEST;
			mainPanel.add(textComponents[i], c);
			
			textComponents[i].setToolTipText(getTipText(paramData));
			
			c.gridy++;
			i++;
		}
	}
	
	private String getTipText(PamParameterData paramData) {
		String tip = paramData.getToolTip();
		if (tip != null) {
			return tip;
		}
		else {
			return paramData.getFieldName();
		}
	}
	
	private JTextComponent createComponent(PamParameterData paramData) {
		int textLen = paramData.getFieldLength();
		if (textLen == 0) {
			textLen = DEFAULT_TEXT_LENGTH;
		}
		if (textLen <= MAX_SINGLE_LINE_LENGTH) {
			return new JTextField(textLen);
		}
		else {
			JTextField dummyField = new JTextField(2);
//			dummyField.getBorder().
			JTextArea textArea = new JTextArea(textLen/MAX_SINGLE_LINE_LENGTH+1, MAX_SINGLE_LINE_LENGTH);
			textArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			return textArea;
		}
	}
	
	public JComponent getPanel() {
		return mainPanel;
	}
	
	public void setParams(T params) {
		int i = 0;
		PamParameterData newParamData = null;
		Object data = null;
		for (PamParameterData paramData : this.parameterSet) {
			// find the parameter in the new parameters (parameterSet is just a formatting placeholder)
			try {
				newParamData = params.getParameterSet().findParameterData(paramData.getFieldName());
			} catch (FieldNotFoundException e) {
				e.printStackTrace();
			}
			try {
				data = newParamData.getData();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (data != null) {
				textComponents[i].setText(data.toString());
			}
			else {
				textComponents[i].setText(null);
			}
			i++;
		}
	}
	
	public boolean getParams(T params) {
		int i = 0;
		PamParameterData newParamData = null;
		Object data = null;
		for (PamParameterData paramData : this.parameterSet) {
			// find the parameter in the new parameters (parameterSet is just a formatting placeholder)
			try {
				newParamData = params.getParameterSet().findParameterData(paramData.getFieldName());
			} catch (FieldNotFoundException e) {
				e.printStackTrace();
			}
			String txt = textComponents[i].getText();
			try {
				newParamData.setData(txt);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				String msg = "Invalid parameter. Data type should be " + paramData.getField().getType().getName();
				return PamDialog.showWarning(null, newParamData.getShortName(), msg);
			}
		
			i++;
		}
		
		return true;
	}

}
