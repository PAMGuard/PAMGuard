package detectionview.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import clipgenerator.ClipDataUnit;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import detectionview.DVControl;
import soundPlayback.ClipPlayback;

public class DVClipDecorations extends ClipDisplayDecorations {

	private DVControl dvControl;

	public DVClipDecorations(DVControl dvControl, ClipDisplayUnit clipDisplayUnit) {
		super(clipDisplayUnit);
		this.dvControl = dvControl;
	}

	@Override
	public JPopupMenu addDisplayMenuItems(JPopupMenu basicMenu) {
		JPopupMenu menu = super.addDisplayMenuItems(basicMenu);
		JMenuItem menuItem = new JMenuItem("Play clip");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				playClip();
			}
		});
		menu.add(menuItem);
		return menu;
	}

	protected void playClip() {
		ClipDataUnit clipDataUnit = getClipDisplayUnit().getClipDataUnit();
//		getClipDisplayUnit().getClipDisplayPanel().playClip(clipUnit);

		ClipPlayback.getInstance().playClip(clipDataUnit.getRawData(), clipDataUnit.getParentDataBlock().getSampleRate(), true);
	}

}
