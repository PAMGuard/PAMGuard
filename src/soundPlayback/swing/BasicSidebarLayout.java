package soundPlayback.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

/**
 * Simple layout for side bar components. 
 * @author dg50
 *
 */
public class BasicSidebarLayout {
	
	private PamPanel mainPanel;
	
	private JLabel textLabel;

	private PlaySliderComponent sliderComponent;

	private BasicSidebarLayout(LayoutManager layoutManager) {
		mainPanel = new PamPanel(layoutManager);
	}

	public static BasicSidebarLayout makeBasicLayout(PlaySliderComponent sliderComponent) {
		return makeBasicLayout(sliderComponent, null);
	}
	
	public static BasicSidebarLayout makeBasicLayout(PlaySliderComponent sliderComponent, JComponent comp2) {
		BasicSidebarLayout bsl = new BasicSidebarLayout(new GridBagLayout());
		bsl.sliderComponent = sliderComponent;
		GridBagConstraints c = new PamGridBagContraints();
		c.ipady = 0;
		c.insets = new Insets(0, 2, 0, 2);
		bsl.textLabel = new PamLabel("No Text");
		PamPanel topP = new PamPanel(new BorderLayout());
		topP.add(BorderLayout.CENTER, bsl.textLabel);
		if (comp2 != null) {
			topP.add(BorderLayout.EAST, comp2);
		}
		bsl.mainPanel.add(topP, c);
		c.gridy++;
		bsl.mainPanel.add(sliderComponent.getSlider(), c);
		return bsl;
	}
		
	public JComponent getComponent() {
		return mainPanel;
	}

	/**
	 * @return the mainPanel
	 */
	public PamPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * @return the textLabel
	 */
	public JLabel getTextLabel() {
		return textLabel;
	}

	public void setTextLabel(String textValue) {
		textLabel.setText(textValue);
	}

	public void setToolTipText(String text) {
		mainPanel.setToolTipText(text);
		textLabel.setToolTipText(text);
		sliderComponent.getSlider().setToolTipText(text);
	}

}
