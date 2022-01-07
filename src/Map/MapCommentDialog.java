package Map;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import PamUtils.PamCalendar;;

public class MapCommentDialog extends PamView.dialog.PamDialog {

	private static MapCommentDialog singleInstance;
	
	MapComment mapComment;
	
	MapPanel mapPanel;
	
	Point mapPoint;
	
	JLabel dateLabel;
	JLabel latLongLabel;
	JTextArea textArea;
	
	private MapCommentDialog(Frame parentFrame) {
		
		super(parentFrame, "Enter Map Comment", false);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		BoxLayout bl;
		topPanel.setLayout(bl = new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		topPanel.add(dateLabel = new JLabel("  "));
		topPanel.add(latLongLabel = new JLabel("  "));
		
		dialogPanel.add(BorderLayout.NORTH, topPanel);
		dialogPanel.add(BorderLayout.CENTER, textArea = new JTextArea(7, 60));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.addKeyListener(new TextListener());
		
		setDialogComponent(dialogPanel);
		
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
	}
	
	@Override
	public void setVisible(boolean visible) {

		if (visible == true) {
			// now positions it close to the mouse click.
			// but make sure it's still on the window. 
			Point newPoint = new Point(mapPoint);
			if (newPoint.x + getWidth() > mapPanel.getWidth()) {
				newPoint.x = mapPanel.getWidth() - getWidth();
			}
			if (newPoint.y + getHeight() > mapPanel.getHeight()) {
				newPoint.y = mapPanel.getHeight() - getHeight();
			}
			newPoint.x += mapPanel.getLocationOnScreen().x;
			newPoint.y += mapPanel.getLocationOnScreen().y;
			
			setLocation(newPoint);
//			setLocationRelativeTo(mapPanel);
		}

		// TODO Auto-generated method stub
		super.setVisible(visible);

		
	}

	static public MapComment showDialog(Frame parentFrame, MapPanel mapPanel, Point mapPoint, MapComment mapComment) {
	  if (singleInstance == null || singleInstance.getParent() != parentFrame) {
		  singleInstance = new MapCommentDialog(parentFrame);
	  }
	  singleInstance.mapComment = mapComment;
	  singleInstance.mapPanel = mapPanel;
	  singleInstance.mapPoint = mapPoint;
	  singleInstance.setParams(mapComment);
	  singleInstance.setVisible(true);
	  return singleInstance.mapComment;
	}

	private void setParams(MapComment mapComment) {
		dateLabel.setText(PamCalendar.formatDateTime(mapComment.getTimeMilliseconds()));
		latLongLabel.setText(String.format("%s,  %s", mapComment.latLong.formatLatitude(), mapComment.latLong.formatLongitude()));
		textArea.setText(null);
		pack();
	}
	
	@Override
	public void cancelButtonPressed() {
		
		singleInstance.mapComment = null;
		
	}

	@Override
	public boolean getParams() {

		singleInstance.mapComment.comment = new String(textArea.getText());
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	class TextListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
			//if(e.getKeyText(e.getKeyCode())=="Enter"){
			if(e.getKeyCode()==KeyEvent.VK_ENTER){
				okButtonPressed();			
			}
		}

		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
		}

		public void keyTyped(KeyEvent e) {

			
		}
		
	}

}
