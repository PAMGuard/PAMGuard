package detectionview.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamguardMVC.PamDataUnit;
import clipgenerator.ClipDataUnit;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import detectionview.DVControl;
import detectionview.annotate.DVAnnotationWrapper;
import soundPlayback.ClipPlayback;

public class DVClipDecorations extends ClipDisplayDecorations {

	private DVControl dvControl;
	private PamDataUnit trigData;

	public DVClipDecorations(DVControl dvControl, ClipDisplayUnit clipDisplayUnit) {
		super(clipDisplayUnit);
		this.dvControl = dvControl;
	}

	@Override
	public JPopupMenu addDisplayMenuItems(JPopupMenu basicMenu) {
		

		ClipDataUnit clipDataUnit = getClipDisplayUnit().getClipDataUnit();
		trigData = getClipDisplayUnit().getTriggerDataUnit();
		
		JPopupMenu menu = super.addDisplayMenuItems(basicMenu);
		JMenuItem menuItem = new JMenuItem("Play clip");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				playClip();
			}
		});
		menu.add(menuItem);
		
		
		
		DVAnnotationWrapper anHand = dvControl.getDvProcess().getAnnotationHandler();
		if (anHand != null && trigData != null) {
			JMenuItem moreMenu = anHand.createAnnotationEditMenu(trigData);
			if (moreMenu != null) {
				menu.add(moreMenu);
			}
		}
	
		
		return menu;
	}

	protected void playClip() {
		ClipDataUnit clipDataUnit = getClipDisplayUnit().getClipDataUnit();

		ClipPlayback.getInstance().playClip(clipDataUnit.getRawData(), clipDataUnit.getParentDataBlock().getSampleRate(), true);
	}

}
