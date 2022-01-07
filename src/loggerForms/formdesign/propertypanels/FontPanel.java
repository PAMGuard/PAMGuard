package loggerForms.formdesign.propertypanels;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import PamView.JFontChooser;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class FontPanel extends PropertyPanel {

	private JFontChooser fontChooser;
	private JButton fontButton;
	private JLabel sample;
	private int currentFont;
	
	
	public FontPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(fontButton = new JButton("Select"));
		addItem(sample = new JLabel("Sample Text"));
		fontButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectFont();
			}
		});
		sample.setFont(formEditor.getFormDescription().getFONT());
	}
	
	protected void selectFont() {
		if (fontChooser == null) {
			fontChooser = new JFontChooser();
			fontChooser.setFont(fontButton.getFont());
		}
		int ans = fontChooser.showDialog(fontButton);
		if (ans == JFontChooser.OK_OPTION) {
			sample.setFont(fontChooser.getSelectedFont());
		}
		
	}

	@Override
	public void propertyEnable(boolean enabled) {
		fontButton.setEnabled(enabled);
	}

	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
	}


	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation = super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		Font font = sample.getFont();
		itemInformation.setProperty(UDColName.Title.toString(), font.getFontName());
		itemInformation.setProperty(UDColName.Length.toString(), font.getSize());
		return itemInformation;
	}

}
