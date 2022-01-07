package loggerForms.formdesign.propertypanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.PamColors;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class ColourPanel extends PropertyPanel {

	private JButton fontColour;
	private JPanel sample2;
	
	public ColourPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(fontColour = new JButton("Select"));
		addItem(sample2 = new JPanel());
		Dimension cS = fontColour.getPreferredSize();
		cS.width*=3;
		sample2.setPreferredSize(cS);
		sample2.setBorder(BorderFactory.createLineBorder(Color.black));
		fontColour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectColour();
			}
		});
	}

	protected void selectColour() {		
		Color ans = JColorChooser.showDialog(fontColour, "Form Colour", fontColour.getBackground());
		if (ans != null) {
			sample2.setBackground(ans);
			sample2.repaint();
//			PamColors
		}
	}

	@Override
	public void propertyEnable(boolean enabled) {
		fontColour.setEnabled(enabled);		
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#pushProperty(loggerForms.ItemInformation)
	 */
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
		if (itemInformation == null) {
			return;
		}
		String colString = itemInformation.getStringProperty(UDColName.Title.toString());
		if (colString != null) {
			Color col = PamColors.interpretColourString(colString);
			if (col == null) {
				System.out.println("Can't understand colour string " + colString);
			}
			else {
				sample2.setBackground(col);
				sample2.repaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#fetchProperty(loggerForms.ItemInformation)
	 */
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		 itemInformation = super.fetchProperty(itemInformation);
		 if (itemInformation == null) {
			 return null;
		 }
		 Color formCol = sample2.getBackground();
		 itemInformation.setProperty(UDColName.Title.toString(), PamColors.getLoggerColourString(formCol));
		
		return itemInformation;
	}

}
