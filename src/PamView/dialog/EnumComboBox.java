package PamView.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import PamModel.parametermanager.EnumToolTip;

public class EnumComboBox extends JComboBox<Enum> {

	private static final long serialVersionUID = 1L;
	private Class enumClass;
	private Enum[] allowedValues;
	private ListCellRenderer<? super Enum> defaultRenderer;

	public EnumComboBox(Enum[] allowedValues) {
		setAllowedValues(allowedValues);
		setToolTipText("Select a value");
		defaultRenderer = getRenderer();
	    setRenderer(new MyComboBoxRenderer());
		this.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setToolTip();
			}

		});
	}

	public void setAllowedValues(Enum[] allowedValues) {
		this.allowedValues = allowedValues;
		this.removeAllItems();
		if (allowedValues != null) {
			for (int i = 0; i < allowedValues.length; i++) {
				this.addItem(allowedValues[i]);
			}
		}
	}
	
	/**
	 * Sets the tool tip on the main item.  
	 */
	private void setToolTip() {
		String t = getTipText(getSelectedItem());
//		System.out.println("Set tip text to " + t);
		setToolTipText(t);
	}

	/**
	 * Sets the tooltip on the items in the dropdown list while the list is displayed. 
	 * @author dg50
	 *
	 */
	class MyComboBoxRenderer extends BasicComboBoxRenderer {
		//http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = defaultRenderer.getListCellRendererComponent(list, (Enum) value, index, isSelected, cellHasFocus);
			if (component instanceof JComponent) {
				((JComponent) component).setToolTipText(getTipText(value));
			}
			return component;
		}
	}
	
	/**
	 * Gets the tooptip for a given enum if it's a type with tooltips. 
	 * @param value
	 * @return
	 */
	public String getTipText(Object value) {
		if (value instanceof EnumToolTip) {
			return ((EnumToolTip) value).getToolTip();
		}
		return null;
	}

}
