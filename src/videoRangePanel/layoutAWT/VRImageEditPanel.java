package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import videoRangePanel.VRControl;
import videoRangePanel.VRParameters;
import PamView.PamSlider;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;

/**
 * This panel control image editing. 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("serial")
public class VRImageEditPanel extends PamPanel {

		private VRControl vrControl;
		private JComboBox<String> scaleStyle;
		protected PamSlider brightness;
		protected PamSlider contrast;
		private VRTabPanelControl vrTabPanelControl;

		public VRImageEditPanel (VRControl vrControl, VRTabPanelControl vrTabPanelControl){
			super();
			this.vrControl = vrControl;
			this.vrTabPanelControl=vrTabPanelControl; 
			
			this.setLayout(new BorderLayout());
			
			PamPanel mp = new PamBorderPanel();
			this.setBorder( new TitledBorder("Image Controls"));
			mp.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			
			c.insets = new Insets(20,0,0,0);  //top padding
			c.gridx=0;
			c.ipady=0;
			c.gridy++;
			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(mp, new PamLabel("Scrolling  "), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx++;
			PamDialog.addComponent(mp, scaleStyle = new JComboBox<String>(), c);
			c.gridy++;
			
			c.weighty = 1.0;   //request any extra vertical space
			c.insets = new Insets(20,0,0,0);  //top padding

			
			c.gridx=0;
			c.gridwidth = 1;
			PamDialog.addComponent(mp, new PamLabel("Brightness"), c);
			c.insets = new Insets(0,0,0,0);  //no top padding
			c.gridy++;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			brightness = new PamSlider(SwingConstants.HORIZONTAL, -20, 20, 0);
			Dimension d = brightness.getPreferredSize();
			d.width = 10;
			brightness.setPreferredSize(d);
			brightness.addChangeListener(new BrightnessChange());
			PamDialog.addComponent(mp, brightness, c);
			c.gridy++;

			c.insets = new Insets(10,0,0,0);  //top padding

			c.gridx=0;
			c.gridwidth = 1;
			PamDialog.addComponent(mp, new PamLabel("Contrast"), c);
			c.insets = new Insets(0,0,0,0);  //no top padding
			c.gridy++;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			contrast = new PamSlider(SwingConstants.HORIZONTAL, -20, 20, 0);
			contrast.setPreferredSize(d);
			contrast.addChangeListener(new BrightnessChange());
			c.gridy++;
			c.gridx=0;
			c.gridwidth = 2;
			PamDialog.addComponent(mp, contrast, c);
			
			for (int i = 0; i < VRParameters.shortScaleNames.length; i++) {
				scaleStyle.addItem(VRParameters.shortScaleNames[i]);
			}
			scaleStyle.addActionListener(new ScaleAction());
					
			mp.setOpaque(false);
			mp.setForeground(Color.white);
			this.setBackground(VRMetaDataPanel.backCol);
			this.add(BorderLayout.NORTH, mp);
			
		}
		
		@Override
		public void paintComponent(Graphics g) {
		        g.setColor(VRMetaDataPanel.backCol);
		        Rectangle r = g.getClipBounds();
		        g.fillRect(r.x, r.y, r.width, r.height);
		        super.paintComponent(g);
		}
		
		
		
		private class BrightnessChange implements ChangeListener {
			
			public void stateChanged(ChangeEvent e) {
				
				if (!brightness.getValueIsAdjusting() && !contrast.getValueIsAdjusting()){
					// brightness value must be between 0 and 2
					float brightVal = 2 * (float) (brightness.getValue() - brightness.getMinimum()) / 
					((float) brightness.getMaximum() - (float) brightness.getMinimum());
					
					// contrast value must be between 0 and 255 (log scale ? )
					// first cal on a scale of -1 to + 1.
					
					// first get a number between 0 and 8.
					float contVal = (float) 8 * (contrast.getValue() - contrast.getMinimum()) / 
					((float) contrast.getMaximum() - (float) contrast.getMinimum());
					// then convert to a log scale between 0 and 255.
					contVal = (float) Math.pow(2, contVal) - 1;
							
	//				System.out.println(String.format("Brightness %.2f, Contrast %.1f", brightVal, contVal));
					
					vrTabPanelControl.getVRPanel().setImageBrightness(brightVal, contVal);
				}
			}
			
		}
		
		private class ScaleAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				vrControl.getVRParams().imageScaling = scaleStyle.getSelectedIndex();
				vrControl.update(VRControl.IMAGE_SCALE_CHANGE);
				vrControl.getVRPanel().newImage();
			}
			
		}
		
		public void update(int updateType){
			switch (updateType){
				case VRControl.SETTINGS_CHANGE:
					//don't want to change image scaling if we don't have to as it cause repaint. 
					if (scaleStyle.getSelectedIndex()!=vrControl.getVRParams().imageScaling) scaleStyle.setSelectedIndex(vrControl.getVRParams().imageScaling);
				break;
				case VRControl.IMAGE_CHANGE:
					//change the brightness back;
					brightness.setValue(0);
					contrast.setValue(0);
				break;
			}
		}

		
		
	
	
}
