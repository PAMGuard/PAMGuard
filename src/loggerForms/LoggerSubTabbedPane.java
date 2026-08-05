package loggerForms;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamTabbedPane;

/**
 * Handler for sub tab commands for sub tab forms. 
 * @author Doug Gillespie
 *
 */
public class LoggerSubTabbedPane extends PamTabbedPane /*JTabbedPane*/ implements ColorManaged {

	private FormDescription formDescription;

	private PamColor defaultColor = PamColor.BORDER;
	
	/**
	 * @param formDescription
	 */
	public LoggerSubTabbedPane(FormDescription formDescription) {
//		super();
		super(null, null);
		this.formDescription = formDescription;
//		setToolTipText("Double click to create a form");
	}
	
	
	
	/*
	 * this should not be done here but is dont to undo the menu in pamtabbedpane, pamtabbedpane should
	 * really have another layer for pamMAINtabbed pane that allows it to be put in other frames.
	 * Also this logger was discussed at moving up a level so would maybe need different listeners on different tabs? 
	 * (non-Javadoc)
	 * @see PamView.PamTabbedPane#processMouseEvent(java.awt.event.MouseEvent)
	 */
	@Override
	protected void processMouseEvent(MouseEvent evt)
    {
		/**
		 * Mouse events which need to be processed are:
		 *
		 * 
		 */
		
//		super.processMouseEvent(evt);//this causes null pointer exception
		
		if (evt.getID() == MouseEvent.MOUSE_PRESSED && evt.getButton() == MouseEvent.BUTTON3){
			//do not call super in this case
			
		}else{
			super.processMouseEvent(evt);
		}
		
    }

	
	
	public JComponent getComponent() {
		return this;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}	
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		Container p = super.getParent();
		if (p != null) {
			p.setBackground(bg);
		}
		
	}

}
