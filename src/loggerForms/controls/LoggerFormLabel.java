package loggerForms.controls;

import java.awt.Color;

import javax.swing.Icon;

import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.PropertyDescription;
import loggerForms.PropertyTypes;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;

public class LoggerFormLabel extends PamLabel {

	
	private LoggerForm loggerForm;
	
	public LoggerFormLabel(LoggerForm loggerForm) {
		super();
		this.loggerForm = loggerForm;
	}

	public LoggerFormLabel(LoggerForm loggerForm, Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		this.loggerForm = loggerForm;
	}

	public LoggerFormLabel(LoggerForm loggerForm, Icon image) {
		super(image);
		this.loggerForm = loggerForm;
	}

	public LoggerFormLabel(LoggerForm loggerForm, String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		this.loggerForm = loggerForm;
	}

	public LoggerFormLabel(LoggerForm loggerForm, String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		this.loggerForm = loggerForm;
	}

	public LoggerFormLabel(LoggerForm loggerForm, String text) {
		super(text);
		this.loggerForm = loggerForm;
	}
	
	
	@Override
	public void setBackground(Color bg) {
		/**
		 * See if the form has a colour specified. If it does
		 * then don't change the colour for different night / day schemes
		 * otherwise follow the normal night / day scheme rulse. 
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
//				this.setForeground(col);
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
			return;
		}
		super.setForeground(bg);
	}
}
