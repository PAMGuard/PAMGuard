package dataPlots.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamButtonAlpha;
import PamView.dialog.PamDialog;
import PamView.hidingpanel.TabbedHidingPane;
import PamView.panel.PamPanel;

/**
 * 
 * A tabbedhidingpane which includes a settings button on the tab. 
 * @author spn1
 *
 */
public class CompoundHidingTabPane extends TabbedHidingPane{

	private static final long serialVersionUID = 1L;
	
//	public static final ImageIcon settingsImage=new ImageIcon(ClassLoader
//			.getSystemResource("Resources/SettingsButtonSmallWhite.png"));
	
	public static final FontIcon settingsImage =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.SMALL_SIZE, Color.WHITE);


	
	public CompoundHidingTabPane() {
		super();
	}
	
	@Override
	public PamPanel createShowingTab(String tabTitle, Icon tabIcon){
		return new ShowingSettingsPanel(tabIcon,tabTitle);
	}
	
	
	public JButton getSettingsButton(int index){
		return ((ShowingSettingsPanel) super.getTabPanel(index).getShowingTab()).getSettingsButton(); 
		
	}
	
	class ShowingSettingsPanel extends ShowingPanel{
		
		private static final long serialVersionUID = 1L;
		
		private PamButtonAlpha settingsButton;

		protected ShowingSettingsPanel(Icon tabIcon, String tabTitle) {
			super(tabIcon, tabTitle);
			//add settings button
			PamDialog.addComponent(this, settingsButton= new PamButtonAlpha(settingsImage), c);
			settingsButton.setBackground(new Color(0,0,0,0));
			settingsButton.addActionListener(new SettingsListener());

		}
		
		class SettingsListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				repaintPanel();
			}
			
		}
		
		public void repaintPanel(){
			this.repaint();
		}
		
		/**
		 * Get the settings button on the tab. 
		 */
		public JButton getSettingsButton(){
			return settingsButton; 
		}

	
		@Override
		public void extraPainting(Graphics g) {
			Color oldCol=g.getColor();
			if (mouseOverPanel(this) && !settingsButton.getModel().isRollover()){	
				g.setColor(getTabHighlight());
			}
			else{
				g.setColor(getTabBackground());
			}
	        Rectangle r = g.getClipBounds();
	        g.clearRect(r.x, r.y, r.width, r.height);
	        g.fillRect(r.x, r.y, r.width, r.height);
	        g.setColor(oldCol);
		}
	}


}

