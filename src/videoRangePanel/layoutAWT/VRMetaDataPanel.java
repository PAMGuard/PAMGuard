package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import PamView.panel.PamPanel;
import videoRangePanel.VRControl;

@SuppressWarnings("serial")
public class VRMetaDataPanel extends PamPanel{
	
	private JTextArea textArea; 
	public static Color backCol=new Color(50,50,50,150);
	private Color foreCol=Color.WHITE;
	private JScrollPane scrollPane;

	
	public VRMetaDataPanel(VRControl vrControl){
		super();
		
		textArea=new JTextArea();
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setForeground(foreCol); 
		textArea.setBackground(backCol);
		
		scrollPane=new JScrollPane(textArea);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		this.setBackground(backCol);
		this.add(BorderLayout.CENTER, scrollPane);
		
		setMetaText(null);

	}
	
	public void setMetaText(ArrayList<String> text){
		//clear the pane
		textArea.setText(null);
		//set text strings
		textArea.append("Image Metadata"+"\n");
		if (text!=null){
			for (int i=0; i<text.size(); i++){
				textArea.append(text.get(i)+"\n");
			}
		}
		else {
			textArea.append("No metadata"+"\n");
		}
		//set the scroll bar to the top
//		this.revalidate();
		textArea.setCaretPosition(0);
	}
	

	@Override
	public void paintComponent(Graphics g) {
	        g.setColor(backCol);
	        Rectangle r = g.getClipBounds();
	        g.fillRect(r.x, r.y, r.width, r.height);
	        super.paintComponent(g);
	}
	
	public void clearMetadata(){
		textArea.setText(null);
	}

	public JTextArea getTextPanel() {
		return textArea;
	}
	
	public JScrollPane getScrollPane(){
		return scrollPane;
	}

}
