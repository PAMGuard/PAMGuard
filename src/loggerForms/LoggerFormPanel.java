package loggerForms;

import java.awt.Color;
import java.awt.LayoutManager;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.panel.PamPanel;

public class LoggerFormPanel extends PamPanel {

	private LoggerForm loggerForm;
	public LoggerFormPanel(LoggerForm loggerForm) {
		super();
		this.loggerForm = loggerForm;
	}

	public LoggerFormPanel(LoggerForm loggerForm, LayoutManager layout) {
		super(layout);
		this.loggerForm = loggerForm;
	}

	@Override
	public void setBackground(Color bg) {
		/**
		 * See if the form has a colour specified. If it does
		 * then don't change the colour for different night / day schemes
		 * otherwise follow the normal night / day scheme rules. 
		 */
		if (loggerForm == null) {
			return;
		}
		FormDescription formDescription = loggerForm.getFormDescription();
		if (formDescription == null) {
			return;
		}
		PropertyDescription colProp = formDescription.findProperty(PropertyTypes.FORMCOLOR);
		if (colProp == null) {
			colProp = formDescription.findProperty(PropertyTypes.FORMCOLOUR);
		}
		if (colProp == null) {
			super.setBackground(bg);
			return;
		}
		String colStr = colProp.getTitle();
		if (colStr != null){
			Color col = PamColors.interpretColourString(colStr);
			if (col != null) {
				super.setBackground(col);
				return;
			}
		}
		super.setBackground(bg);
	}
	
	@Override
	public void setForeground(Color bg) {
		if (loggerForm == null) {
			return;
		}
		FormDescription formDescription = loggerForm.getFormDescription();
		if (formDescription == null) {
			return;
		}
		PropertyDescription colProp = formDescription.findProperty(PropertyTypes.FORMCOLOR);
		if (colProp == null) {
			colProp = formDescription.findProperty(PropertyTypes.FORMCOLOUR);
		}
		if (colProp == null) {
			super.setForeground(bg);
			return;
		}
		String colStr = colProp.getTitle();
		if (colStr != null){
			super.setForeground(Color.BLACK);
		}
		else{
			super.setForeground(bg);
		}
	}

}
