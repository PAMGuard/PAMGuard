package loggerForms;

import java.awt.Component;
import java.awt.event.MouseEvent;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;
import PamView.panel.PamTabbedPane;

/**
 * Adaptation of TabbedPane to provide a bit of extra support for logger forms, 
 * such as mouse click actions on the tabs themselves, etc. 
 * different colours
 * @author Doug Gillespie
 *
 */
public class LoggerTabbedPane extends PamTabbedPane /*JTabbedPane*/ implements ColorManaged {

	private PamColor defaultColor = PamColor.BORDER;
	
	private FormsControl formsControl;
	
	public LoggerTabbedPane(FormsControl formsControl) {
//		PamTabbedPane(PamControllerInterface pamControllerInterface, PamGui pamGui)
//		super(formsControl.getPamController(),null);
//		
//		PamController.getInstance().getGuiFrameManager().getViews().get(0).
//		
//		PamGui pg=new PamGui(null, null, (Integer) null);
		
//		pg.findControlledUnit(tabNo)
		super(null,null); //causing NPE
		this.formsControl = formsControl;
//		setToolTipText("Logger Forms");
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}

	@Override
	protected void processMouseEvent(MouseEvent evt)
    {
		/**
		 * Mouse events which need to be processed are:
		 * 1. For subforms, double click will create a form. 
		 * 
		 */
		
//		super.processMouseEvent(evt);//this causes null pointer exception
		
		if (evt.getID() == MouseEvent.MOUSE_PRESSED && evt.getButton() == MouseEvent.BUTTON3){
			//do not call super in this case
			
		}else{
			super.processMouseEvent(evt);
		}
		
//		System.out.println(evt);
		/*
		 * Work out which tab it's on and from there, work out which form description
		 * associates with this tab. From there can probably delegate the actual work
		 * back to the form description. 
		 */

        int index = indexAtLocation(evt.getX(), evt.getY());
        if (index >= 0) {
        	FormDescription formDescription = findFormDescription(index);
//            System.out.println("Hit tab index " + index + " " + formDescription.getFormName());
        	if (formDescription != null) {
        		formDescription.processTabMouseEvent(this, evt);
        	}
        }
    }
	
	/**
	 * Find the form description for a particular tab. 
	 * @param tabIndex tab index
	 * @return form description or null (should only happen if index < 0)
	 */
	public FormDescription findFormDescription(int tabIndex) {
		if (tabIndex < 0){
			return null;
		}
		Component tabComponent = this.getComponentAt(tabIndex);
		int n = formsControl.getNumFormDescriptions();
		FormDescription formDescription;
		for (int i = 0; i < n; i++) {
			formDescription = formsControl.getFormDescription(i);
			if (formDescription.getTabComponent() == tabComponent) {
				return formDescription;
			}
		}
		return null;
	}
	 
	/**
	 * Find the tab index for a particular component.
	 * @param formDescription A form description
	 * @return tab index or -1 if tab cannot be found
	 */
	public int findTabIndex(FormDescription formDescription) {
		Component tabComponent = formDescription.getTabComponent();
		Component c;
		if (tabComponent == null) {
			return -1;
		}
		for (int i = 0; i < this.getTabCount(); i++) {
			if (this.getComponentAt(i) == tabComponent) {
				return i;
			}
		}
		return -1;
	}
	

	public boolean removeTab(FormDescription formDescription) {
		int index=findTabIndex(formDescription);
		if (index!=-1){
			removeTabAt(index);
			return true;
		}
		return false;
	}
}
