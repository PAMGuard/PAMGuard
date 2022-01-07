package difar.display;

import generalDatabase.lookupTables.LookupEditDialog;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamUtils.PamUtils;
import PamView.MenuItemEnabler;
import PamView.PamColors;
import PamView.dialog.PamButton;
import PamView.panel.PamPanel;
import clipgenerator.ClipDataUnit;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayPanel;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarDataUnit;


public class DifarClipDecorations extends ClipDisplayDecorations /*implements DIFARDisplayUnit*/ {

	private DifarDataUnit difarDataUnit;
	private DifarControl difarControl;
	private ClipEastPanel eastPanel;
	
	DisplayMenus displayMenus;
	
	private MenuItemEnabler deleteEnabler = new MenuItemEnabler();
	private MenuItemEnabler processEnabler = new MenuItemEnabler();
	
	private MenuItemEnabler vesselEnabler = new MenuItemEnabler();
	private ArrayList<MenuItemEnabler> speciesEnablers;
	
	public DifarClipDecorations(DifarControl difarControl, ClipDisplayUnit clipDisplayUnit) {
		super(clipDisplayUnit);
		this.difarControl = difarControl;
		
		difarDataUnit = (DifarDataUnit) clipDisplayUnit.getClipDataUnit();
		speciesEnablers = new ArrayList<MenuItemEnabler>();//(difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().size());
		
		for(int i=0;i<difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().size()+1;i++){
			speciesEnablers.add(new MenuItemEnabler());
		}
	}
	
	MenuItemEnabler getSpeciesEnabler(LookupItem li){
		for (MenuItemEnabler speciesEnabler :speciesEnablers){
			if (speciesEnabler == null || speciesEnabler.getMenuItemList() == null) continue;
			for(AbstractButton ab:speciesEnabler.getMenuItemList()){
				for(ActionListener al:ab.getActionListeners()){
					try{
						if (li==((SpeciesListener)al).lutItem) return speciesEnabler;
						
					}catch(ClassCastException e){
						//not species Listener so ignore it.
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		MenuItemEnabler newE=new MenuItemEnabler();
		speciesEnablers.add(newE);
		return newE;
	}
	
	@Override
	public void decorateDisplay() {
		if (difarDataUnit == null) {
			System.out.println("Unable to decorate clip display unit in DIFAR module since data unit is null");
		}
		ClipDisplayUnit clipDisplayUnit = getClipDisplayUnit();
		if (difarDataUnit.isVessel()) {
			clipDisplayUnit.getAxisPanel().setBackground(Color.GRAY);
		}

		MouseAdapter mouseAdapter;
		if (difarControl.isViewer()) {
			mouseAdapter = new ViewerMouseFuncs(difarDataUnit);
		}
		else {
			mouseAdapter = new NormalMouseFuncs(clipDisplayUnit, difarDataUnit);
		}
		clipDisplayUnit.getimagePanel().setToolTipText(difarDataUnit.getSummaryString());
		clipDisplayUnit.getAxisPanel().addMouseListener(mouseAdapter);
		clipDisplayUnit.getImagePanel().addMouseListener(mouseAdapter);
		
		eastPanel = new ClipEastPanel(clipDisplayUnit, difarDataUnit);
		
		
		PamPanel outerPanel=new PamPanel(new BorderLayout());
		outerPanel.add(eastPanel, BorderLayout.NORTH);
		clipDisplayUnit.add(BorderLayout.EAST, outerPanel);

	}
	@Override
	public void removeDecoration(){
		removeEnablersAndSelecters();
	}
	
	/**
	 * del,proc,ves
	 */
	static Icon[] buttonIcons;
	
	/**
	 * Add more buttons and controls to the E panel of each clip. 
	 * @author doug
	 *
	 */
	private class ClipEastPanel extends PamPanel {
		private ClipDisplayUnit clipDisplayUnit;
		private DifarDataUnit difarDataUnit;
		private ArrayList<JMenuItem> otherSpecies;
		

		public ClipEastPanel(ClipDisplayUnit clipDisplayUnit, DifarDataUnit difarDataUnit){
			if (buttonIcons==null){
				buttonIcons = new Icon[3];
				buttonIcons[0]=new ImageIcon(ClassLoader.getSystemResource("Resources/delete.png"));
				buttonIcons[1]=new ImageIcon(ClassLoader.getSystemResource("Resources/terminal.png"));
				buttonIcons[2]=new ImageIcon(ClassLoader.getSystemResource("Resources/boat.png"));
				
			}
			otherSpecies=new ArrayList<JMenuItem>();
			//			eastPanel.addSeparator();
			setLayout(new GridLayout(0,1));
			
			PamButton buttonItem;
			buttonItem = new PamButton("Delete",buttonIcons[0]);
			buttonItem.addActionListener(new DeleteListener());
			
			add(buttonItem);
			deleteEnabler.addMenuItem(buttonItem);
			buttonItem = new PamButton("Process",buttonIcons[1]);
			buttonItem.addActionListener(new ProcessClipListener());
			add(buttonItem);
			processEnabler.addMenuItem(buttonItem);

			// species list goes here. 
			PamButton cbmi = new PamButton("Vessel",buttonIcons[2]);
			cbmi.addActionListener(new VesselListener());
			add(cbmi);
			vesselEnabler.addMenuItem(buttonItem);
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(cbmi);
			Vector<LookupItem> speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList();
			LookupItem[] speciesListToShowOnPanel = difarControl.getDifarParameters().getFavSpecies();
			PamButton[] favs =new PamButton[speciesListToShowOnPanel.length];
			for (LookupItem item:speciesList) {
				int favSp=-1;
				fav:
					for (int k=0;k<speciesListToShowOnPanel.length;k++){
					LookupItem fav=speciesListToShowOnPanel[k];
					if (item.equals(fav)){
						favSp=k;
						continue fav;
					}
					
				}
				
				if (favSp>-1){
					buttonItem = new PamButton(item.getCode(), item.getSymbol());
					buttonItem.setToolTipText(item.getText());
					buttonItem.addActionListener(new SpeciesListener(item));
					getSpeciesEnabler(item).addMenuItem(buttonItem);
					favs[favSp]=buttonItem;
					buttonGroup.add(buttonItem);
				}else{
					JMenuItem menuItem = new JMenuItem(item.getText(), item.getSymbol());
					menuItem.addActionListener(new SpeciesListener(item));
					getSpeciesEnabler(item).addMenuItem(menuItem);
					otherSpecies.add(menuItem);
					buttonGroup.add(buttonItem);
				}
				
			}
			
			for (PamButton f:favs){
				if(f!=null){
					add(f);
				}
			}
			
			buttonItem = new PamButton("Other");
			buttonItem.addActionListener(new OtherListener());
			add(buttonItem);
			buttonGroup.add(cbmi);
			
			enableEnablersAndSelecters();
		}

		JPopupMenu getJPopupMenu(){
			JPopupMenu menu = new JPopupMenu();
			for (JMenuItem jmi:otherSpecies){
				menu.add(jmi);
			}
			return menu;
		}
		
		class OtherListener implements ActionListener{

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Component button = (Component)e.getSource();
				getJPopupMenu().show(button, button.getWidth(), 0);
			}
			
		}
		
	}
	
	

	private class DisplayMenus {

		JMenuItem[] processItems; // Delete and Process
		JMenuItem[] speciesItems; // Vessels + species
		JMenuItem editItem;		  // edit
		
		/**
		 * the display menus use checkbox menu items for vessels and species
		 * lists. However, these don't toggle like normal checkboxes but
		 * are used more like radio buttons.  
		 */
		public DisplayMenus() {
			processItems=new JMenuItem[2];
			JMenuItem menuItem;
			menuItem = new JMenuItem("Delete");
			menuItem.addActionListener(new DeleteListener());
			deleteEnabler.addMenuItem(menuItem);
			processItems[0]=menuItem;
			menuItem = new JMenuItem("Process");
			menuItem.addActionListener(new ProcessClipListener());
			processEnabler.addMenuItem(menuItem);
			processItems[1]=menuItem;
			processEnabler.addMenuItem(menuItem);
			JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Vessel angle calibration");
			cbmi.addActionListener(new VesselListener());
			
			vesselEnabler.addMenuItem(menuItem);
			
			LookupList speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl);
			Vector<LookupItem> llist = speciesList.getSelectedList();
			speciesItems = new JMenuItem[llist.size()+1];
			speciesItems[0]=cbmi;
			int i=0;
			for (LookupItem item:llist) {
				menuItem = new JCheckBoxMenuItem(item.getText(), item.getSymbol());
				menuItem.addActionListener(new SpeciesListener(item));
				
				
				speciesItems[i+1]=menuItem;
				
				//TODO check this i
				speciesEnablers.get(i).addMenuItem(menuItem);
				i++;
			}

			menuItem = new JMenuItem("Edit classification list ...");
			menuItem.addActionListener(new EditSpeciesList());
			editItem=menuItem;
		}
		

		public void addMenuItems(JPopupMenu popupMenu) {
			popupMenu.addSeparator();
			for (JMenuItem jmi:processItems){
				popupMenu.add(jmi);
			}
			popupMenu.addSeparator();
			for (JMenuItem jmi:speciesItems){
				popupMenu.add(jmi);
			}
			popupMenu.addSeparator();
			popupMenu.add(editItem);
			enableEnablersAndSelecters();

		}
	}

	class DeleteListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			delete();
		}
	}

	class ProcessClipListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			processClip();
		}
	}

	class VesselListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			vesselSelect();
		}
	}

	class SpeciesListener implements ActionListener {
		private LookupItem lutItem;
		public SpeciesListener(LookupItem lutItem) {
			super();
			this.lutItem = lutItem;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			speciesSelect(lutItem);
		}
		LookupItem getLookupItem(){
			return lutItem;
		}
	}
	
	
	

	public void processClip() {
		// only actually do this if we're allowed (i.e. previous unit is cleared from the Difargram). 
		// if we can't process it now, then redraw it. 
		if (difarControl.canDemux()) {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ProcessFromQueue, difarDataUnit));
			removeEnablersAndSelecters();
		}
		else {
			getClipDisplayUnit().repaintUnit();
		}

		enableEnablersAndSelecters();
	}

	public void delete() {
		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.DeleteFromQueue, difarDataUnit));
		removeEnablersAndSelecters();
		enableEnablersAndSelecters();
	}

	private void vesselSelect() {
		difarDataUnit.setVessel(!difarDataUnit.isVessel());
		difarDataUnit.setLutSpeciesItem(null);
		if (difarDataUnit.isVessel()) processClip();
		enableEnablersAndSelecters();
	}

	private void speciesSelect(LookupItem lutItem) {
		difarDataUnit.setVessel(false);
		difarDataUnit.setLutSpeciesItem(lutItem);
		// immediately process it once a species is set. 
		processClip();
		enableEnablersAndSelecters();
	}

	
	LookupItem getLUI(MenuItemEnabler mie){
		Vector<AbstractButton> itemList = mie.getMenuItemList();
		if (itemList!=null){
			LookupItem lutItem = null;
			getLut:
			for (AbstractButton ab:itemList){
				
				ActionListener[] als = ab.getActionListeners();
				for(ActionListener al:als){
					try{
						lutItem=((SpeciesListener)al).getLookupItem();
						ab.setToolTipText(ab.isEnabled()?lutItem.getText():"This option will only be enabled when Vessel is unselected");
						continue getLut;
					}catch(ClassCastException cce){
						System.out.println("Not Species Listener");
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println("Species Listener not found!!!");
			}
//			System.out.println("lutItem: "+ lutItem);
			if (lutItem==null){//vessel
				mie.selectItems(false);
			}else if(lutItem==difarDataUnit.getLutSpeciesItem()){//this species
				mie.selectItems(true);
			}else {//if(lutItem!=difarDataUnit.getLutSpeciesItem()){//other species
				mie.selectItems(false);
			}
		}
		
		return null;
	}
	
	
	/**
	 * Static enablerList causes all enablers to be retained
	 * which will eventually consume all the memory unless these
	 * are removed.
	 */
	void removeEnablersAndSelecters(){
		Vector<AbstractButton> itemList = deleteEnabler.getMenuItemList();
		for (int i = 0; i < itemList.size(); i++){
			deleteEnabler.removeMenuItem(itemList.get(i));
		}
		deleteEnabler.removeMenuItemEnabler();
		itemList = processEnabler.getMenuItemList();
		for (int i = 0; i < itemList.size(); i++){
			processEnabler.removeMenuItem(itemList.get(i));
		}
		processEnabler.removeMenuItemEnabler();
		itemList = vesselEnabler.getMenuItemList();
		for (int i = 0; i < itemList.size(); i++){
			vesselEnabler.removeMenuItem(itemList.get(i));
		}
		vesselEnabler.removeMenuItemEnabler();
		for (MenuItemEnabler s:speciesEnablers){
			itemList = s.getMenuItemList();
			if (itemList!=null){
				for (int i = 0; i < itemList.size(); i++){
					s.removeMenuItem(itemList.get(i));
				}
			}
			s.removeMenuItemEnabler();
		}
	}
	
	/**
	 * deals with the all the logic of the buttons states
	 */
	void enableEnablersAndSelecters(){
		
		deleteEnabler.enableItems(true);//always enabled
		
		vesselEnabler.selectItems(difarDataUnit.isVessel());//false default for this
		vesselEnabler.enableItems(true);//always enabled
		
		for (MenuItemEnabler mie:speciesEnablers){
			mie.enableItems(!difarDataUnit.isVessel());
			
			
			Vector<AbstractButton> itemList = mie.getMenuItemList();
			if (itemList!=null){
				LookupItem lutItem = null;
				getLut:
				for (AbstractButton ab:itemList){
					ActionListener[] als = ab.getActionListeners();
					for(ActionListener al:als){
						try{
							lutItem=((SpeciesListener)al).getLookupItem();
							ab.setToolTipText(ab.isEnabled()?lutItem.getText():"This option will only be enabled when Vessel is unselected");
							continue getLut;
						}catch(ClassCastException cce){
							System.out.println("Not Species Listener");
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
					System.out.println("Species Listener not found!!!");
				}
//				System.out.println("lutItem: "+ lutItem);
				if (lutItem==null){//vessel
					mie.selectItems(false);
				}else if(lutItem==difarDataUnit.getLutSpeciesItem()){//this species
					mie.selectItems(true);
				}else {//if(lutItem!=difarDataUnit.getLutSpeciesItem()){//other species
					mie.selectItems(false);
				}
			}
			
		}
		
		processEnabler.enableItems(canProcess(difarDataUnit));
		for (AbstractButton ab:processEnabler.getMenuItemList()){
			ab.setToolTipText(canProcess(difarDataUnit)?null:"This option will only be enabled when the DIFARGram is empty and Auto-Processing of calls is if turned off.");
		}
		
	}

	public class EditSpeciesList implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			editSpeciesList();
		}
	}

	/**
	 * Edit the species list from the drop down menu.
	 */
	public void editSpeciesList() {
		LookupList newList = LookupEditDialog.showDialog(difarControl.getGuiFrame(), difarControl.getDifarParameters().getSpeciesList(difarControl));
		if (newList != null) {
			difarControl.getDifarParameters().setSpeciesList(newList);
		}

	}
	
	/**
	 * Can immediately process - i.e. there is nothing currently in the queue and the 
	 * data unit has been assigned as a vessel or a whale species. 
	 * @param difarDataUnit
	 * @return true if it can go immediately to the DifarGram
	 */
	private boolean canProcess(DifarDataUnit difarDataUnit) {
		return (difarControl.canDemux() & (difarDataUnit.isVessel() | difarDataUnit.getLutSpeciesItem() != null));
	}

	private class NormalMouseFuncs extends MouseAdapter {

		private DifarDataUnit difarDataUnit;
		private ClipDisplayUnit clipDisplayUnit;

		public NormalMouseFuncs(ClipDisplayUnit clipDisplayUnit, DifarDataUnit difarDataUnit) {
			super();
			this.clipDisplayUnit = clipDisplayUnit;
			this.difarDataUnit = difarDataUnit;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ProcessFromQueue, difarDataUnit));	
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				if (canProcess(difarDataUnit)) {
					difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ProcessFromQueue, difarDataUnit));	
				}
				else {
					// show the menu even though it was a left click. 
					 JPopupMenu m = clipDisplayUnit.getimagePanel().getComponentPopupMenu();
					 if (m != null) {
						 m.show(e.getComponent(), e.getX(), e.getY());
					 }
				}
			}
		}
	}
	
	private class ViewerMouseFuncs extends MouseAdapter {

		private DifarDataUnit difarDataUnit;

		public ViewerMouseFuncs(DifarDataUnit difarDataUnit) {
			super();
			this.difarDataUnit = difarDataUnit;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ProcessFromQueue, difarDataUnit));
			}
		}

	}


	@Override
	public JPopupMenu addDisplayMenuItems(JPopupMenu basicMenu) {
		if (difarControl.isViewer()) {
			return basicMenu;
		}
		displayMenus = new DisplayMenus();
		displayMenus.addMenuItems(basicMenu);
		return basicMenu;
	}


	@Override
	public Color getClipBackground() {
		/**
		 * Work out a background colour for the clip displays based on channel / vessel info
		 */
		Color col = null;
		if (difarDataUnit.isVessel()) {
			col = Color.YELLOW;
		}
		else {
			int iChan = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
			col = PamColors.getInstance().getChannelColor(iChan);
		}
//		return col;
		if (col == null) {
			return col;
		}
		else return new Color(col.getRed(), col.getGreen(), col.getBlue(), 100);
	}

	/* (non-Javadoc)
	 * @see clipgenerator.clipDisplay.ClipDisplayParent#drawOnClipAxis(java.awt.Graphics)
	 */
	@Override
	public void drawOnClipAxis(Graphics g) {
		// draw a coloured border based on channel number
		Graphics2D g2d = (Graphics2D) g;
		int strokeWidth = 2;
		g2d.setStroke(new BasicStroke(strokeWidth));
		int iChan = PamUtils.getLowestChannel(difarDataUnit.getChannelBitmap());
		g.setColor(PamColors.getInstance().getChannelColor(iChan));
//		g.drawRect(0, 0, clipDisplayUnit.getWidth()-strokeWidth, clipDisplayUnit.getHeight()-strokeWidth);
		if (difarDataUnit.isVessel()) {
			FontMetrics fm = g.getFontMetrics();
			// need to be just to the right of the channel number, so work out how long that string was
			String chString = String.format("Ch%d", iChan);
			Rectangle2D sb = fm.getStringBounds(chString, g);
			g.drawString("(V)", (int) (sb.getWidth()+fm.charWidth('c')/2), getClipDisplayUnit().getHeight()-fm.getDescent());
		}
	}
	
	@Override
	public void drawOnClipBorder(Graphics g) {
		// draw a coloured border based on channel number
		Graphics2D g2d = (Graphics2D) g;
		int strokeWidth = 2;
		g2d.setStroke(new BasicStroke(strokeWidth));
		int iChan = PamUtils.getLowestChannel(difarDataUnit.getChannelBitmap());
		g.setColor(PamColors.getInstance().getChannelColor(iChan));
		g.drawRect(0, 0, getClipDisplayUnit().getWidth()-strokeWidth, getClipDisplayUnit().getHeight()-strokeWidth);
	}

}
