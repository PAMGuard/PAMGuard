package whistleClassifier.offline;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import whistleClassifier.WhistleClassifierControl;

/**
 * Some offline functions / functionality. 
 * @author Doug Gillespie
 *
 */
public class ClassifierOffline {

	WhistleClassifierControl whistleClassifierControl;
	
	ReclassifyWhistles reclassifyWhistles;

	/**
	 * @return the reclassifyWhistles
	 */
	public ReclassifyWhistles getReclassifyWhistles() {
		return reclassifyWhistles;
	}

	public ClassifierOffline(WhistleClassifierControl whistleClassifierControl) {
		super();
		this.whistleClassifierControl = whistleClassifierControl;
		reclassifyWhistles = new ReclassifyWhistles(whistleClassifierControl);
	}
	
	public int addOfflineMenuItems(Container menu, Frame parentFrame) {
		int n = 0;
		JMenuItem menuItem;
		menuItem = new JMenuItem("Classify Whistles");
		menuItem.addActionListener(new ClassifyWhistles(parentFrame));
		menu.add(menuItem);
		return 1;
	}
	
	class ClassifyWhistles implements ActionListener {

		Frame parentFrame;
		
		public ClassifyWhistles(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			reclassifyWhistles.runReclassify(parentFrame);
		}
		
	}
}
