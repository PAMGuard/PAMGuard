package PamView;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import PamController.NewModuleDialog;
import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.PamModuleInfo;
import PamView.PamColors.PamColor;
import PamView.PamObjectViewer.PamControllerView.DataBlockThing;
import PamView.PamObjectViewer.PamControllerView.ProcessThing;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamDesktopPane;
import PamView.panel.PamPanel;
import PamView.panel.PamScrollPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;

/**
 * Make a diagram of the overall PAM process layout. A bit like UML !
 * 
 * Contains menus to add modules, remove them, rename them, etc.
 * 
 * @author Doug Gillespie
 * 
 */
public class PamObjectViewer implements PamViewInterface, ComponentListener,
		PamSettings {

	static PamObjectViewer singleInstance;

	ObjectFrame objectFrame;

	LayoutPanel layoutPanel;

	ArrayList<PamProcessView> processList;

	ArrayList<PamControllerView> controllerList;

	JButton closeButton;

	PopupListener popupListener;

	JMenuItem printMenuItem, layoutMenuItem;

	boolean doneLayout;

	JScrollPane scrollPanel;

	JPanel mainPanel;

	PamObjectViewerSettings pamObjectViewerSettings = new PamObjectViewerSettings();

	JCheckBoxMenuItem viewByController, viewByProcess;

	JCheckBoxMenuItem showProcesslessModules, showProcesslessObservers;

	final int columnGap = 20;
	final int yStart = 20;
	final int xStart = 20;
	final int yGap = 20;
	
	private Color processColour = new Color(255, 192, 0);
	
	private static final Color instArrowColour = Color.BLUE;
	private static final Color arrowColour = Color.DARK_GRAY;

	public static final int arrowSize = 6;
	public static final int instArrowSize = arrowSize;
	
	private Stroke arrowStroke, instantArrowStroke;

	private PamConfiguration pamConfiguration;

	// Font controllerFont, processFont, datablockFont;

	private PamObjectViewer(Frame frame) {
		
		arrowStroke = new BasicStroke(1.5f);
		instantArrowStroke = new BasicStroke(1.5f);

		popupListener = new PopupListener();

		objectFrame = new ObjectFrame(frame);

//		MakeDiagram();

		PamController.getInstance().addView(this);

		PamSettingManager.getInstance().registerSettings(this);
	}

	static public PamObjectViewer getObjectViewer(Frame frame) {
		if (singleInstance == null) {
			singleInstance = new PamObjectViewer(frame);
		}
		return singleInstance;
	}

	static public void Show(Frame frame, PamConfiguration pamConfiguration) {

		getObjectViewer(frame).setConfiguration(pamConfiguration);
		getObjectViewer(frame).objectFrame.setVisible(true);
		
		singleInstance.MakeDiagram();
		
		// Go through all of the processes/datablocks in every view and update button/tooltip text.
		// Mostly done for the FFT Engine process because it includes the FFT size in the
		// process name, and without this code the name would get set the first time you open
		// the gui and then never change
		if (singleInstance.controllerList!=null) {
			for (PamControllerView aView : singleInstance.controllerList) {
				aView.setToolTipText("PamControlledUnit : " + aView.pamControlledUnit.getUnitName());
				for (ProcessThing aProcessThing : aView.processThings) {
					aProcessThing.updateButtonText();
				}
				for (DataBlockThing aDataBlockThing : aView.dataBlockThings) {
					aDataBlockThing.updateButtonText();
				}
			}
		}
		if (singleInstance.processList!=null) {
			for (PamProcessView aView : singleInstance.processList) {
				aView.updateButtonText();
			}
		}
	}

	private void setConfiguration(PamConfiguration pamConfiguration) {
		this.pamConfiguration = pamConfiguration;
	}

	void MakeDiagram() {

		if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS) {
			makeProcessDiagram();
		} else if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYCONTROLLER) {
			makeControllerDiagram();
		}
		// if (pamObjectViewerSettings.showProcesslessModules) {
		// makeProcesslessModules();
		// }
		layoutDiagram();
	}

	private void makeControllerDiagram() {

		clearDiagram();
		
		if (pamConfiguration == null) {
			return;
		}
		
		PamControlledUnit pamControlledUnit;
		PamControllerView pamControllerView;
		controllerList = new ArrayList<PamControllerView>();
		for (int iUnit = 0; iUnit < pamConfiguration.getNumControlledUnits(); iUnit++) {
			pamControlledUnit = pamConfiguration.getControlledUnit(iUnit);
			if (pamControlledUnit.getNumPamProcesses() == 0
					&& !pamObjectViewerSettings.showProcesslessModules) {
				continue;
			}
			pamControllerView = new PamControllerView(pamControlledUnit);
			controllerList.add(pamControllerView);
			layoutPanel.add(pamControllerView);
			pamControllerView.addComponentListener(this);
		}

	}

	private void layoutControllerDiagram() {
		if (controllerList == null) {
			return;
		}
		int x = xStart;
		int y = yStart;
		/*
		 * Find the first process and link back to any upstream datablock.
		 */
		if (controllerList == null || controllerList.size() == 0) {
			return;
		}

		int maxColums = controllerList.size() + 2;
		

		int[] columnWidths = new int[maxColums];
		int[] columnBottoms = new int[maxColums];
		int[] columnLefts = new int[maxColums];
		for (int i = 0; i < maxColums; i++) {
			columnBottoms[i] = yStart;
		}
		int chainPos;
		PamControllerView pamControllerView;
		for (int i = 0; i < controllerList.size(); i++) {
			pamControllerView = controllerList.get(i);
			if (pamControllerView.firstProcess == null) {
				chainPos = 1;
			} else {
				chainPos = pamControllerView.firstProcess.getChainPosition();
				chainPos = Math.min(chainPos, maxColums - 1);
			}
			columnWidths[chainPos] = Math.max(columnWidths[chainPos],
					pamControllerView.getWidth());
		}
		columnLefts[0] = xStart;
		for (int i = 1; i < maxColums; i++) {
			columnLefts[i] = columnLefts[i - 1] + columnWidths[i - 1]
					+ columnGap;
			columnBottoms[i] = yStart;
		}
		for (int i = 0; i < controllerList.size(); i++) {
			pamControllerView = controllerList.get(i);
			if (pamControllerView.firstProcess == null) {
				chainPos = 1;
			} else {
				chainPos = pamControllerView.firstProcess.getChainPosition();
				chainPos = Math.min(chainPos, maxColums - 1);
			}
			pamControllerView.setLocation(columnLefts[chainPos],
					columnBottoms[chainPos]);
			columnBottoms[chainPos] += pamControllerView.getHeight() + yGap;
		}

	}

	private void makeProcessDiagram() {

		clearDiagram();

		processList = new ArrayList<PamProcessView>();
		// go through objects and create a little window for each process,
		// controlled unit, etc.
		PamProcessView pamProcessView;

		int x = xStart;
		int y = yStart;

		PamControlledUnit pamControlledUnit;
		PamProcess pamProcess;
		for (int iUnit = 0; iUnit < pamConfiguration.getNumControlledUnits(); iUnit++) {
			pamControlledUnit = pamConfiguration.getControlledUnit(iUnit);
			for (int iP = 0; iP < pamControlledUnit.getNumPamProcesses(); iP++) {
				pamProcess = pamControlledUnit.getPamProcess(iP);
				pamProcessView = new PamProcessView(pamControlledUnit,
						pamProcess);
				pamProcessView.setLocation(x += 30, y += 30);
				processList.add(pamProcessView);
				layoutPanel.add(pamProcessView);
				pamProcessView.addComponentListener(this);
			}
		}
	}

	private void checkProcessDiagram() {

	}

	/**
	 * Automaticaly arranges the Model view on the page
	 * 
	 */
	private void layoutProcessDiagram() {
		// first work out which column each PamProcessView goes in
		// by counting the number of back links between blocks.
		int columns = 0;
		int column;
		int[] columContents;
		int[] columnWidth;
		int[] columnBottom;
		int[] columnLeft;
		int x, y;
		int xMax = 0, yMax = 0;
		for (int i = 0; i < processList.size(); i++) {
			columns = Math.max(columns, processList.get(i).pamProcess
					.getChainPosition());
		}
		columContents = new int[columns + 1];
		columnWidth = new int[columns + 1];
		columnBottom = new int[columns + 1];
		columnLeft = new int[columns + 1];

		for (int i = 0; i < columnBottom.length; i++) {
			columnBottom[i] = yStart;
		}

		for (int i = 0; i < processList.size(); i++) {
			column = processList.get(i).pamProcess.getChainPosition();
			columnWidth[column] = Math.max(columnWidth[column], processList
					.get(i).getWidth());
		}

		columnLeft[0] = xStart;
		for (int i = 1; i < columnBottom.length; i++) {
			columnLeft[i] = columnLeft[i - 1] + columnWidth[i - 1] + columnGap;
		}

		// now lay them out
		PamProcess layoutParent;
		ViewGroupInfo parentView; // findViewGroupInfo
		for (int i = 0; i < processList.size(); i++) {
			column = processList.get(i).pamProcess.getChainPosition();
			y = columnBottom[column];
			// check the y of the parent, and don't take the max y,
			layoutParent = processList.get(i).pamProcess.getParentProcess();
			if (layoutParent != null) {
				parentView = findViewGroupInfo(layoutParent
						.getParentDataBlock());
				if (parentView != null) {
					y = Math.max(y, parentView.processView.getY());
				}
			}
			x = columnLeft[column];
			processList.get(i).setLocation(x, y);
			columnBottom[column] = processList.get(i).getY()
					+ processList.get(i).getHeight() + yGap;
			yMax = Math.max(yMax, columnBottom[column]);
			xMax = Math.max(xMax, columnLeft[column] + columnWidth[column]
					+ columnGap);
		}
		// finally, check the width and height of the main window and reseize if
		// it's too small.

		doneLayout = true;
	}

	void checkLayoutSize() {

		Rectangle totalRect = new Rectangle();
		if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS) {
			if (processList != null) {
				for (int i = 0; i < processList.size(); i++) {
					totalRect = totalRect.union(processList.get(i).getBounds());
				}
			}
		} else {
			for (int i = 0; i < controllerList.size(); i++) {
				totalRect = totalRect.union(controllerList.get(i).getBounds());
			}
		}

		layoutPanel
				.setPreferredSize(new Dimension((int) totalRect.getMaxX() + 20,
						(int) totalRect.getMaxY() + 20));

		scrollPanel.getViewport().doLayout();
	}

	// SetLayoutSize()

	void clearDiagram() {
		layoutPanel.removeAll();
		layoutPanel.invalidate();
		layoutPanel.repaint();
		if (processList == null)
			return;
		for (int i = 0; i < processList.size(); i++) {
			processList.get(i).setVisible(false);
		}
		processList = null;
	}

	@Override
	public void setTitle(String title) {

	}

	class ObjectFrame extends JFrame implements ActionListener {

		ObjectFrame(Frame frame) {
			setTitle("Pamguard Data Model");

			// fixed case of Resources 17/8/08 DG.
			setIconImage(new ImageIcon(ClassLoader
					.getSystemResource("Resources/pamguardIcon.png"))
					.getImage());
//			setIconImages(getOwner().getIconImages());

			setSize(new Dimension(800, 700));
			setLocation(100, 100);

			mainPanel = new PamPanel();
			mainPanel.setLayout(new BorderLayout());

			scrollPanel = new PamScrollPane(layoutPanel = new LayoutPanel());

			mainPanel.add(BorderLayout.CENTER, scrollPanel);

			JPanel s = new JPanel();
			s.add(closeButton = new JButton("Close"));
			closeButton.addActionListener(this);
			mainPanel.add(BorderLayout.SOUTH, s);

			setContentPane(mainPanel);
			if (frame != null) {
				Point parentLoc = frame.getLocation();
				setLocation(parentLoc.x+10, parentLoc.y+12);
			}

			setFrameName();

		}

		private void setFrameName() {
			switch (pamObjectViewerSettings.viewStyle) {
			case PamObjectViewerSettings.VIEWBYCONTROLLER:
				setTitle("PAMGUARD Data Model - one window per PAM module");
				break;
			case PamObjectViewerSettings.VIEWBYPROCESS:
				setTitle("PAMGUARD Data Model - one window per PAM process");
				break;
			default:
				setTitle("PAMGUARD Data Model");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Component#setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean b) {
			// go through all the thingies and enable or disable their timers
			if (processList != null) {
				for (int i = 0; i < processList.size(); i++) {
					if (b
							&& pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS) {
						processList.get(i).blockTimer.start();
					} else {
						processList.get(i).blockTimer.stop();
					}
				}
			}
			if (controllerList != null) {
				for (int i = 0; i < controllerList.size(); i++) {
					if (b
							&& pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYCONTROLLER) {
						controllerList.get(i).blockTimer.start();
					} else {
						controllerList.get(i).blockTimer.stop();
					}
				}
			}
			super.setVisible(b);
			if (!doneLayout) {
				layoutDiagram();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == closeButton) {
				setVisible(false);
			} else if (e.getSource() == printMenuItem) {
				// System.out.println("Print");
				PrinterJob printerJob = PrinterJob.getPrinterJob();
				printerJob.printDialog();
			} else if (e.getSource() == layoutMenuItem) {
				// System.out.println("Print");
				layoutDiagram();
			}
		}
	}

	private void layoutDiagram() {
		if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS) {
			layoutProcessDiagram();
		} else if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYCONTROLLER) {
			layoutControllerDiagram();
		}
	}

	class LayoutPanel extends PamDesktopPane {

		private boolean hasInstant;

		LayoutPanel() {
			super();
			addMouseListener(popupListener);
			setDefaultColor(PamColor.BORDER);
			// PamColors.getInstance().registerComponent(this, PamColor.BORDER);
		}

		@Override
		public void paintComponent(Graphics g) {

			if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS) {
				paintProcessLinks(g);
			} else if (pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYCONTROLLER) {
				paintControllerLinks(g);
			}
			paintKey(g);
		}

		private void paintKey(Graphics g) {
			int w = scrollPanel.getWidth();
			int h = scrollPanel.getHeight();
			if (scrollPanel.getVerticalScrollBar().isVisible()) {
				w -= scrollPanel.getVerticalScrollBar().getWidth();
			}
			if (scrollPanel.getHorizontalScrollBar().isVisible()) {
				h -= scrollPanel.getHorizontalScrollBar().getHeight();
			}
			FontMetrics fm = g.getFontMetrics(getFont());
			int fh = fm.getHeight();
			String s1 = "Standard Data Consumers";
			String s2 = "Data Modifiers";
			int lineLen = 30;
			int gap = 10;
			int x = w - fm.stringWidth(s1) - gap*2 - lineLen;
			int y = h - fh;
			Graphics2D g2d = (Graphics2D) g;
			if (hasInstant) {
				g2d.setStroke(instantArrowStroke);
				drawKeyLine(g, x, y, s2, lineLen, gap, instArrowColour, arrowSize, instArrowSize);
				y -= fh;
			}
			g2d.setStroke(arrowStroke);
			drawKeyLine(g, x, y, s1, lineLen, gap, arrowColour, 0, arrowSize);
			
		}

		private void drawKeyLine(Graphics g, int x, int y, String str, int lineLen, int gap, Color lineColour, int startArow, int endArrow) {
			g.setColor(lineColour);
//			g.drawLine(x, y, x + lineLen, y);
			drawSteppedArrow(g, x, y, x + lineLen, y, y, startArow, endArrow);
			x += lineLen + gap;
			FontMetrics fm = g.getFontMetrics(getFont());
			y += (fm.getAscent()-1)/2;
			g.drawString(str, x, y);
		}

		private void paintControllerLinks(Graphics g) {

			Graphics2D g2D = (Graphics2D) g;
			PamControlledUnit pamControlledUnit;
			PamProcess pamProcess;
			PamControllerView pamControllerView, sourceControllerView;
			PamControlledUnit sourceControlledUnit;
			PamDataBlock sourceBlock;
			Point dest, orig;
			int bestYGap;
			Rectangle sourceBounds, destBounds;
			hasInstant = false;
			if (controllerList == null) {
				return;
			}
			for (int i = 0; i < controllerList.size(); i++) {
				pamControllerView = controllerList.get(i);
				pamControlledUnit = pamControllerView.pamControlledUnit;
				for (int iP = 0; iP < pamControlledUnit.getNumPamProcesses(); iP++) {
					pamProcess = pamControlledUnit.getPamProcess(iP);
					dest = pamControllerView.getProcessLeftPos(iP);
					sourceBlock = pamProcess.getParentDataBlock();
					if (sourceBlock == null) {
						continue;
					}
					sourceControlledUnit = sourceBlock.getParentProcess()
							.getPamControlledUnit();
					sourceControllerView = findControllerView(sourceControlledUnit);
					if (sourceControllerView == null) {
						continue;
					}
					boolean instant = pamProcess.getParentDataBlock().isInstantObserver(pamProcess);
					hasInstant |= instant;
					if (sourceControllerView == pamControllerView) {
						orig = sourceControllerView.getBlockLeftPos(sourceBlock);
						dest = pamControllerView.getProcessLeftPos(iP);
					} else {
						orig = sourceControllerView.getBlockRightPos(sourceBlock, instant);
					}
					if (orig == null) {
						continue;
					}
					sourceBounds = sourceControllerView.getBounds();
					destBounds = pamControllerView.getBounds();
					bestYGap = (int) ((Math.max(sourceBounds.getY(), destBounds
							.getY()) + Math.min(sourceBounds.getMaxY(),
							destBounds.getMaxY())) / 2);
					Graphics2D g2d = (Graphics2D) g;
					if (sourceControllerView == pamControllerView) {
						bestYGap = orig.y;
						g.setColor(arrowColour);
						g2d.setStroke(arrowStroke);
						drawArrow(g, orig.x, orig.y, dest.x, dest.y, bestYGap, 0, arrowSize);
					} 
					else if (instant){
						g2d.setStroke(instantArrowStroke);
						g.setColor(instArrowColour);
						drawArrow(g, orig.x, orig.y, dest.x, dest.y, bestYGap, arrowSize, instArrowSize);
					}
					else {
						g2d.setStroke(arrowStroke);
						g.setColor(arrowColour);
						drawArrow(g, orig.x, orig.y, dest.x, dest.y, bestYGap, 0, arrowSize);
					}
				}
			}
		}

		private void paintProcessLinks(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			// go through each block, find it's observer processes and draw
			// lines to them
			// this is probablymost easily done in reverse since each process
			// knows it's source data block
			PamProcess thisProcess;
			Point destPoint;
			Point sourcePoint;
			int bestYGap;
			ViewGroupInfo sourceGroupInfo;
			PamProcessView sourceView, destView;
			Rectangle sourceBounds, destBounds;
			PamObserver pamObserver;

			g.setColor(PamColors.getInstance().getColor(PamColor.PLAIN));

			for (int i = 0; i < processList.size(); i++) {

				destView = processList.get(i);

				thisProcess = destView.pamProcess;

				destPoint = getViewGroupLeftPos(destView.processInfo);

				sourcePoint = findComponentRightPos(thisProcess
						.getParentDataBlock());

				if (sourcePoint != null) {
					/*
					 * if it's the longest observer, then draw it in red.
					 */
					sourceView = findViewGroupInfo(thisProcess
							.getParentDataBlock()).processView;

					sourceBounds = sourceView.getBounds();
					destBounds = destView.getBounds();
					bestYGap = (int) ((Math.max(sourceBounds.getY(), destBounds
							.getY()) + Math.min(sourceBounds.getMaxY(),
							destBounds.getMaxY())) / 2);
					
					/*
					 * See if this is an instant process ...
					 */
					boolean instant = thisProcess.getParentDataBlock().isInstantObserver(thisProcess);
					Graphics2D g2d = (Graphics2D) g;
					if (instant) {
						g.setColor(instArrowColour);
						g2d.setStroke(instantArrowStroke);
						drawArrow(g, sourcePoint.x, sourcePoint.y, destPoint.x,
								destPoint.y, bestYGap, arrowSize, instArrowSize);
					}
					else {
						g.setColor(arrowColour);
						g2d.setStroke(arrowStroke);
						drawArrow(g, sourcePoint.x, sourcePoint.y, destPoint.x,
								destPoint.y, bestYGap, 0, arrowSize);
					}
				}
			}
		}

		private void drawIntArrow(Graphics g, int x1, int y1, int y2,
				int xOffset, int headSize) {
			g.drawLine(x1, y1, x1 - xOffset, y1);
			g.drawLine(x1 - xOffset, y1, x1 - xOffset, y2);
			drawStraightArrow(g, x1 - xOffset, y2, x1, y2, headSize);
		}

		private void drawArrow(Graphics g, int x1, int y1, int x2, int y2,
				int bestYGap, int startHeadSize, int endHeadSize) {
			drawSteppedArrow(g, x1, y1, x2, y2, bestYGap, startHeadSize, endHeadSize);
		}

		private void drawSteppedArrow(Graphics g, int x1, int y1, int x2,
				int y2, int bestYGap, int startHeadSize, int endHeadSize) {
			int xMin = 16;
			int xm = (x1 + x2) / 2;
			if (x2 - x1 >= xMin) {
//				g.drawLine(x1, y1, xm, y1);
				drawStraightArrow(g, xm, y1, x1, y1, startHeadSize);
				g.drawLine(xm, y2, x2, y2);
				g.drawLine(xm, y1, xm, y2);
				drawStraightArrow(g, xm, y2, x2, y2, endHeadSize);
			} else {
				// need to jump backwards a bit !
				int ym = (y1 + y2) / 2;
				ym = bestYGap;
//				g.drawLine(x1, y1, x1 + xMin / 2, y1);
				drawStraightArrow(g, x1 + xMin / 2, y1, x1, y1, startHeadSize);
				g.drawLine(x1 + xMin / 2, y1, x1 + xMin / 2, ym);
				g.drawLine(x1 + xMin / 2, ym, x2 - xMin / 2, ym);
				g.drawLine(x2 - xMin / 2, ym, x2 - xMin / 2, y2);
				drawStraightArrow(g, x2 - xMin / 2, y2, x2, y2, endHeadSize);
			}
		}

		private void drawStraightArrow(Graphics g, int x1, int y1, int x2,
				int y2, int headSize) {
			g.drawLine(x1, y1, x2, y2);
			double arrowAngle = Math.atan2(y2 - y1, x2 - x1);
			double headAngle = 135 * Math.PI / 180.;
			double headDir;
			double side;
			for (int i = 0; i < 2; i++) {
				side = -1;
				if (i == 1)
					side = 1.0;
				headDir = arrowAngle + headAngle * side;
				x1 = (int) (x2 + headSize * Math.cos(headDir));
				y1 = (int) (y2 + headSize * Math.sin(headDir));
				g.drawLine(x1, y1, x2, y2);
			}
		}
	}

	Point findComponentRightPos(Object dataSource) {
		Point pt = null;
		ViewGroupInfo vgi = findViewGroupInfo(dataSource);
		if (vgi != null) {
			return getViewGroupRightPos(vgi);
		}
		return pt;
	}

	Point findComponentLeftPos(Object dataSource) {
		Point pt = null;
		ViewGroupInfo vgi = findViewGroupInfo(dataSource);
		if (vgi != null) {
			return getViewGroupLeftPos(vgi);
		}
		return pt;
	}

	Point getViewGroupLeftPos(ViewGroupInfo vgi) {
		Point pt;
		pt = vgi.processView.getLocation();
		// pt.x += vgi.processView.getWidth();
		pt.y += vgi.shownComponent.getY();
		pt.y += vgi.shownComponent.getHeight() / 2;
		pt.y += vgi.processView.getContentPane().getY();
		pt.y += (vgi.processView.getHeight() - vgi.processView.getContentPane()
				.getHeight());
		return pt;
	}

	Point getViewGroupRightPos(ViewGroupInfo vgi) {
		Point pt = getViewGroupLeftPos(vgi);
		pt.x += vgi.processView.getWidth();
		return pt;
	}

	ViewGroupInfo findViewGroupInfo(Object dataSource) {
		Point pt = null;
		PamProcessView processView;
		for (int i = 0; i < processList.size(); i++) {
			processView = processList.get(i);
			for (int j = 0; j < processView.dataBlockInfoList.size(); j++) {
				if (processView.dataBlockInfoList.get(j).pamguardObject == dataSource) {
					return processView.dataBlockInfoList.get(j);
				}
			}
		}
		return null;
	}

	PamControllerView findControllerView(PamControlledUnit pamControlledUnit) {
		for (int i = 0; i < controllerList.size(); i++) {
			if (controllerList.get(i).pamControlledUnit == pamControlledUnit) {
				return controllerList.get(i);
			}
		}
		return null;
	}

	void CheckProcessesViews() {
		if (processList == null)
			return;
		for (int i = 0; i < processList.size(); i++) {
			processList.get(i).FillProcessContent();
			processList.get(i).pack();
		}
	}

	Color getProcessColour(PamProcess pamProcess) {
		if (pamProcess == null) {
			return Color.black;
		}

		return processColour;
	}

	class PamControllerView extends JInternalFrame {

		protected PamControlledUnit pamControlledUnit;

		protected PamProcess firstProcess;

		JPanel p = new JPanel();

		Component[] componentList;

		ArrayList<ProcessThing> processThings;
		ArrayList<DataBlockThing> dataBlockThings;

		PamControllerView(PamControlledUnit pamControlledUnit) {

			super(pamControlledUnit.getUnitName());

			this.pamControlledUnit = pamControlledUnit;

			setToolTipText("PamControlledUnit : "
					+ pamControlledUnit.getUnitName());

			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			processThings = new ArrayList<ProcessThing>();

			dataBlockThings = new ArrayList<DataBlockThing>();

			fillPanelContent();

			setFrameIcon(null);

			setContentPane(p);

			this.pack(); // disaster when off !

			blockTimer.start();

			setVisible(true);
			

		}

		private int getYOffset() {
			return getContentPane().getY() + getHeight()
					- getContentPane().getHeight() - 2;
			// pt.y += (vgi.processView.getHeight() -
			// vgi.processView.getContentPane().getHeight());
		}

		private Point getProcessLeftPos(int processNum) {
			Point pt = new Point();
			pt.x = this.getX();
			Component c = findComponent(processNum);
			if (c == null) {
				return new Point(0, 0);
			}
			pt.y = this.getY() + getYOffset();
			pt.y += c.getY();
				pt.y += c.getHeight() / 2;
			return pt;
		}

		private Point getBlockLeftPos(int processNum, int blockNum) {
			Point pt = new Point();
			pt.x = this.getX();
			Component c = findComponent(processNum, blockNum);
			pt.y = this.getY() + getYOffset();
			pt.y += c.getY() + c.getHeight() / 2;
			return pt;
		}

		private Point getBlockRightPos(int processNum, int blockNum, boolean dataModifier) {
			Point pt = new Point();
			pt.x = this.getX() + this.getWidth();
			Component c = findComponent(processNum, blockNum);
			pt.y = this.getY() + getYOffset();
			pt.y += c.getY();
			if (!dataModifier) {
				pt.y += c.getHeight() / 2;
			}
			else {
				pt.y += c.getHeight() / 4;
			}
			return pt;
		}

		private Point getBlockLeftPos(PamDataBlock dataBlock) {
			Point pt = new Point();
			pt.x = this.getX();
			Component c = findComponent(dataBlock);
			if (c == null) {
				return null;
			}
			pt.y = this.getY() + getYOffset();
			pt.y += c.getY() + c.getHeight() / 2;
			return pt;
		}

		public Point getBlockRightPos(PamDataBlock dataBlock, boolean dataModifier) {
			Point pt = new Point();
			pt.x = this.getX() + this.getWidth();
			Component c = findComponent(dataBlock);
			pt.y = this.getY() + getYOffset();
			pt.y += c.getY();
			if (!dataModifier) {
				pt.y += c.getHeight() / 2;
			}
			else {
				pt.y += c.getHeight() / 4;
			}
			return pt;
		}

		// public Component findComponent(Object o) {
		// PamProcess pamProcess;
		// for (int iP = 0; iP < pamControlledUnit.getNumPamProcesses(); iP++) {
		// pamProcess = pamControlledUnit.getPamProcess(iP);
		// if (o == pamProcess) {
		// return findComponent(iP);
		// }
		// for (int iB = 0; iB < pamProcess.getNumOutputDataBlocks(); iB++) {
		// if (o == pamProcess.getOutputDataBlock(iB)) {
		// return findComponent(iP, iB);
		// }
		// }
		// }
		// return null;
		// }

		public Component findComponent(int processNum) {
			return findComponent(processNum, -1);
		}

		public Component findComponent(int processNum, int blockNum) {
			int iComponent = 0;
			for (int i = 0; i < processNum; i++) {
				iComponent++;
				iComponent += pamControlledUnit.getPamProcess(i)
						.getNumOutputDataBlocks();
			}
			iComponent += blockNum + 1;
			return componentList[iComponent];
		}

		public Component findComponent(PamProcess pamProcess) {
			for (int i = 0; i < processThings.size(); i++) {
				if (processThings.get(i).pamProcess == pamProcess) {
					return processThings.get(i).getProcessButton();
				}
			}
			return null;
		}

		public Component findComponent(PamDataBlock pamDataBlock) {
			for (int i = 0; i < dataBlockThings.size(); i++) {
				if (dataBlockThings.get(i).dataBlock == pamDataBlock) {
					return dataBlockThings.get(i).dataBlockButton;
				}
			}
			return null;
		}

		@Override
		public void setVisible(boolean vis) {
			if (vis) {
				SwingUtilities.invokeLater(new Runnable() {
					/**
					 * This seems to work and resize the text fields on high def displays. 
					 */
					@Override
					public void run() {
						pack();
					}
				});
			}
			super.setVisible(vis);
		}

		void fillPanelContent() {
			p.removeAll();
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = constraints.gridy = 0;
			constraints.gridwidth = 1;
			layout.setConstraints(p, constraints);
			// loop over processes and then over data blocks.
			PamProcess pamProcess;
			PamDataBlock pamDataBlock;
			ProcessThing processThing;
			DataBlockThing dataBlockThing;
			// first add up the total number of rows - will need to know where
			// things
			// are for later on.
			int nComponents = 0;
			for (int iP = 0; iP < pamControlledUnit.getNumPamProcesses(); iP++) {
				nComponents++;
				pamProcess = pamControlledUnit.getPamProcess(iP);
				nComponents += pamProcess.getNumOutputDataBlocks();
			}
			componentList = new Component[Math.max(nComponents, 1)];
			nComponents = 0;
			for (int iP = 0; iP < pamControlledUnit.getNumPamProcesses(); iP++) {
				pamProcess = pamControlledUnit.getPamProcess(iP);
				if (iP == 0) {
					firstProcess = pamProcess;
				}
				processThing = new ProcessThing(pamProcess);
				processThings.add(processThing);
				processThing.getProcessButton().addMouseListener(
						new ProcessPopupListener(pamControlledUnit, this));
				addComponent(p, processThing.getProcessButton(), constraints);
				componentList[nComponents++] = processThing.getProcessButton();
				constraints.gridx++;
				addComponent(p, processThing.getProcessText(), constraints);
				constraints.gridx = 0;
				constraints.gridy++;
				for (int iB = 0; iB < pamProcess.getNumOutputDataBlocks(); iB++) {
					pamDataBlock = pamProcess.getOutputDataBlock(iB);
					dataBlockThing = new DataBlockThing(pamProcess, pamDataBlock);
					dataBlockThing.dataBlockButton
							.addActionListener(new DataBlockListener(
									pamDataBlock));
					dataBlockThings.add(dataBlockThing);
					addComponent(p, dataBlockThing.getDataBlockButton(),
							constraints);
					componentList[nComponents++] = dataBlockThing
							.getDataBlockButton();
					constraints.gridx++;
					addComponent(p, dataBlockThing.getDataBlockText(),
							constraints);
					constraints.gridx = 0;
					constraints.gridy++;
				}
			}
			if (pamControlledUnit.getNumPamProcesses() == 0) {
				processThing = new ProcessThing(null);
				processThings.add(processThing);
				processThing.getProcessButton().addMouseListener(
						new ProcessPopupListener(pamControlledUnit, this));
				addComponent(p, processThing.getProcessButton(), constraints);
				componentList[nComponents++] = processThing.getProcessButton();
				addComponent(p, new JLabel("Module contains no processes"),
						constraints);
			}
		}

		class ProcessThing {

			private PamProcess pamProcess;
			private JButton processButton;
			private JTextField processText;

			public ProcessThing(PamProcess pamProcess) {
				this.pamProcess = pamProcess;
				if (pamProcess != null) {
					processButton = new JButton(pamProcess.getProcessName());
					processButton.setBackground(getProcessColour(pamProcess));
//					processButton.set
					processText = new JTextField(5);
					if (pamControlledUnit.createDetectionMenu(objectFrame) != null) {
						processButton.setToolTipText("PamProcess : "
								+ pamProcess.getProcessName()
								+ " (right click to configure)");
					} else {
						processButton.setToolTipText("PamProcess : "
								+ pamProcess.getProcessName());
					}
					processText
							.setToolTipText("Process CPU Usage (includes observers CPU)");
				} else {
					processButton = new JButton("Module contains no processes");
					processButton.setBackground(Color.PINK);
					// processText = new JTextField(5);
				}
			}

			protected void updateCpuUsage() {
				if (processText != null) {
					processText.setText(String.format(("%3.1f%%"), pamProcess.getCpuPercent()));
				}
			}

			public PamProcess getPamProcess() {
				return pamProcess;
			}

			public JButton getProcessButton() {
				return processButton;
			}

			public JTextField getProcessText() {
				return processText;
			}
			
			public void updateButtonText() {
				if (pamProcess != null) {
					processButton.setText(pamProcess.getProcessName());
					if (pamControlledUnit.createDetectionMenu(objectFrame) != null) {
						processButton.setToolTipText("PamProcess : "
								+ pamProcess.getProcessName()
								+ " (right click to configure)");
					} else {
						processButton.setToolTipText("PamProcess : "
								+ pamProcess.getProcessName());
					}
				}
			}

		}

		class DataBlockThing {
			private PamDataBlock dataBlock;
			private JButton dataBlockButton;
			private JTextField dataBlockText;

			public DataBlockThing(PamProcess pamProcess, PamDataBlock dataBlock) {
				this.dataBlock = dataBlock;
				dataBlockButton = new JButton(dataBlock.getDataName());
				dataBlockText = new JTextField(5);
				dataBlockButton.setToolTipText("Pam DataBlock : "
						+ dataBlock.getDataName());
				dataBlockText.setToolTipText("Current PamDataUnits in "
						+ dataBlock.getDataName());
//				Color c = getDataBlockColour(pamProcess, dataBlock);
//				if (c != null) {
//					dataBlockButton.setBackground(c);
//				}
			}

			protected void updateBlockCount() {
				dataBlockText.setText(String.format("%d", dataBlock
						.getUnitsCount()));
			}

			public PamDataBlock getDataBlock() {
				return dataBlock;
			}

			public JButton getDataBlockButton() {
				return dataBlockButton;
			}

			public JTextField getDataBlockText() {
				return dataBlockText;
			}
			
			public void updateButtonText() {
				dataBlockButton.setText(dataBlock.getDataName());
				dataBlockButton.setToolTipText("Pam DataBlock : "
						+ dataBlock.getDataName());
				dataBlockText.setToolTipText("Current PamDataUnits in "
						+ dataBlock.getDataName());
			}



		}

		Timer blockTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// fill in the CPU usage
				for (int i = 0; i < processThings.size(); i++) {
					processThings.get(i).updateCpuUsage();
				}
				for (int i = 0; i < dataBlockThings.size(); i++) {
					dataBlockThings.get(i).updateBlockCount();
				}
			}
		});

	}

	class PamProcessView extends JInternalFrame {
		public PamProcess pamProcess;
		public PamControlledUnit pamControlledUnit;
		ViewGroupInfo processInfo;
		ArrayList<ViewGroupInfo> dataBlockInfoList;
		JPanel p;
		JTextField cpuTime;
		JButton label;

		PamProcessView(PamControlledUnit pamControlledUnit,
				PamProcess pamProcess) {

			super(pamControlledUnit.getUnitName());

			this.pamControlledUnit = pamControlledUnit;

			this.pamProcess = pamProcess;

			setToolTipText("PamControlledUnit : "
					+ pamControlledUnit.getUnitName());

			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			dataBlockInfoList = new ArrayList<ViewGroupInfo>();

			p = new JPanel();

			FillProcessContent();

			p.invalidate();

			setFrameIcon(null);

			setContentPane(p);

			this.pack(); // disaster when off !

			setVisible(true);

			blockTimer.start();
		}

		void FillProcessContent() {

			// start by clearing the content ...
			dataBlockInfoList.clear();
			p.removeAll();

//			JButton label;
			PamDataBlock pamDataBlock;
			JTextField unitCount;

			// p.setLayout(new
			// GridLayout(pamProcess.getNumOutputDataBlocks()+1,1));
			// use gridbag
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			p.setLayout(layout);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 1;
			layout.setConstraints(p, constraints);
			// constraints.gridx = 1;
			// constraints.gridy = 0;
			// constraints.anchor = GridBagConstraints.CENTER;
			//			
			// addComponent(p, new JLabel(" CPU"), constraints);

			constraints.gridx = 0;
			constraints.gridy = 0;
			addComponent(p, label = new JButton(pamProcess.getProcessName()),
					constraints);
			label.setBackground(getProcessColour(pamProcess));
			if (pamControlledUnit.createDetectionMenu(objectFrame) != null) {
				label.setToolTipText("PamProcess : "
						+ pamProcess.getProcessName()
						+ " (right click to configure)");
			} else {
				label.setToolTipText("PamProcess : "
						+ pamProcess.getProcessName());
			}
			label.addMouseListener(new ProcessPopupListener(pamControlledUnit,
					this));
			processInfo = new ViewGroupInfo(this, label, pamProcess, null);
			constraints.gridx = 1;
			addComponent(p, cpuTime = new JTextField(5), constraints);
			cpuTime
					.setToolTipText("Process CPU Usage (includes observers CPU)");

			constraints.gridwidth = 1;
			ViewGroupInfo viewGroupInfo;
			Color color;
			JButton outBlock;
			// now go through and add a label for the controller and for each
			// outputdatablock
			for (int i = 0; i < pamProcess.getNumOutputDataBlocks(); i++) {
				pamDataBlock = pamProcess.getOutputDataBlock(i);
				constraints.gridx = 0;
				constraints.gridy++;
				// constraints.fill = GridBagConstraints.HORIZONTAL;
				addComponent(p,
						outBlock = new JButton(pamDataBlock.getDataName()),
						constraints);
				outBlock.setToolTipText("Pam DataBlock : "
						+ pamDataBlock.getDataName());
				constraints.gridx = 1;
				// constraints.fill = GridBagConstraints.HORIZONTAL;
				addComponent(p, unitCount = new JTextField(5), constraints);
				unitCount.setToolTipText("Current PamDataUnits in "
						+ pamDataBlock.getDataName());
				dataBlockInfoList.add(viewGroupInfo = new ViewGroupInfo(this,
						outBlock, pamDataBlock, unitCount));
				outBlock.addActionListener(new DataBlockListener(pamDataBlock));
				if ((color = getBlockColour(pamDataBlock)) != null) {
					outBlock.setBackground(color);
				}
			}

		}
		
		public void updateButtonText() {
			if (pamControlledUnit.createDetectionMenu(objectFrame) != null) {
				label.setToolTipText("PamProcess : "
						+ pamProcess.getProcessName()
						+ " (right click to configure)");
			} else {
				label.setToolTipText("PamProcess : "
						+ pamProcess.getProcessName());
			}
			for (ViewGroupInfo aGroupInfo : dataBlockInfoList) {
				try {
					String dataBlockName = ((PamDataBlock) aGroupInfo.pamguardObject).getDataName();
					((JButton) aGroupInfo.shownComponent).setText(dataBlockName);
					((JButton) aGroupInfo.shownComponent).setToolTipText("Pam DataBlock : " + dataBlockName);
					aGroupInfo.unitCount.setToolTipText("Current PamDataUnits in " + dataBlockName);
				} catch (Exception e) {
					// if the cast fails, just move on to the next data block
				}
			}
		}

		Color getBlockColour(PamDataBlock pamDataBlock) {

			switch (PamController.getInstance().getRunMode()) {
			case PamController.RUN_NORMAL:
				return null;
			case PamController.RUN_MIXEDMODE:
				if (pamDataBlock.getMixedDirection() == PamDataBlock.MIX_OUTOFDATABASE) {
					return Color.GREEN;
				} else {
					return null;
				}
			case PamController.RUN_PAMVIEW:
				return Color.GREEN;
			}
			return null;
		}

		Timer blockTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// fill in the CPU usage
				cpuTime.setText(String.format("%3.1f%%", pamProcess.getCpuPercent()));
				// get the data block counts and fill them in
				PamDataBlock dataBlock;
				JTextField countText;
				for (int i = 0; i < dataBlockInfoList.size(); i++) {
					dataBlock = (PamDataBlock) dataBlockInfoList.get(i).pamguardObject;
					countText = dataBlockInfoList.get(i).unitCount;
					countText.setText(String.format("%d", dataBlock
							.getUnitsCount()));
				}
			}
		});

	} // end class PamProcessView

	// class DataBlockListener implements ActionListener {
	// ViewGroupInfo viewGroupInfo;
	// DataBlockListener(ViewGroupInfo viewGroupInfo) {
	// this.viewGroupInfo = viewGroupInfo;
	// }
	// public void actionPerformed(ActionEvent e) {
	// JButton b = (JButton) e.getSource();
	// ObserverListPopup.show(objectFrame, b.getLocationOnScreen(),
	// (PamDataBlock) viewGroupInfo.pamguardObject);
	// }
	// }
	class DataBlockListener implements ActionListener {
		PamDataBlock pamDataBlock;

		DataBlockListener(PamDataBlock pamDataBlock) {
			this.pamDataBlock = pamDataBlock;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			JPopupMenu menu = new JPopupMenu();
			JMenuItem menuItem;
			menuItem = new JMenuItem("Show Observers");
			menuItem.addActionListener(new DataBlockObserverList(pamDataBlock,
					b));
			menu.add(menuItem);
			menuItem = new JMenuItem("Show Annotations");
			menuItem.addActionListener(new DataBlockAnnotationsList(
					pamDataBlock, b));
			menu.add(menuItem);
			menu.show(b, b.getWidth() / 2, b.getHeight() / 2);
			// ObserverListPopup.show(objectFrame, b.getLocationOnScreen(),
			// pamDataBlock);
		}
	}

	class DataBlockObserverList implements ActionListener {
		PamDataBlock pamDataBlock;
		JButton b;

		DataBlockObserverList(PamDataBlock pamDataBlock, JButton b) {
			this.pamDataBlock = pamDataBlock;
			this.b = b;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ObserverListPopup.show(objectFrame, b.getLocationOnScreen(),
					pamDataBlock);
		}
	}

	class DataBlockAnnotationsList implements ActionListener {
		PamDataBlock pamDataBlock;
		JButton b;

		DataBlockAnnotationsList(PamDataBlock pamDataBlock, JButton b) {
			this.pamDataBlock = pamDataBlock;
			this.b = b;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new AnnotationsPopup(objectFrame, b.getLocationOnScreen(),
					pamDataBlock);
		}
	}

	class ProcessPopupListener extends MouseAdapter {

		PamControlledUnit pamControlledUnit;

		JInternalFrame jInternalFrame;

		public ProcessPopupListener(PamControlledUnit pamControlledUnit,
				JInternalFrame jInternalFrame) {
			super();
			this.pamControlledUnit = pamControlledUnit;
			this.jInternalFrame = jInternalFrame;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			MenuItemEnabler menuEnabler;
			PamModuleInfo moduleInfo;
			if (e.isPopupTrigger()) {
				JPopupMenu pm = new JPopupMenu();

				JMenuItem menuItem;
				menuItem = new JMenuItem("Remove "
						+ pamControlledUnit.getUnitName() + "...");
				menuItem.addActionListener(new RemoveAction());
				moduleInfo = pamControlledUnit.getPamModuleInfo();
				if (moduleInfo != null) {
					menuItem.setEnabled(moduleInfo.getRemoveMenuEnabler()
							.isEnabled());
				}
				else {
					menuItem.setEnabled(false);
				}
				pm.add(menuItem);

				menuItem = new JMenuItem("Rename "
						+ pamControlledUnit.getUnitName() + "...");
				menuItem.addActionListener(new RenameAction(objectFrame));
				pm.add(menuItem);
				if (moduleInfo == null) {
					menuItem.setEnabled(false);
				}

				menuItem = pamControlledUnit.createDetectionMenu(objectFrame);
				if (menuItem != null) {
					menuItem.setText("Settings ...");
					pm.addSeparator();
					pm.add(menuItem);
				}
				pm.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		class RemoveAction implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check user really wants to do that ...
				int ans = JOptionPane.showConfirmDialog(objectFrame,
						"Are you sure you want to remove the "
								+ pamControlledUnit.getUnitName()
								+ " from the Pamguard data model ?");
				if (ans == JOptionPane.YES_OPTION
						|| ans == JOptionPane.OK_OPTION)
					pamControlledUnit.removeUnit();
			}
		}

		class RenameAction implements ActionListener {

			private Frame parentFrame;

			public RenameAction(Frame parentFrame) {
				super();
				this.parentFrame = parentFrame;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// check user really wants to do that ...
				String newName = NewModuleDialog.showDialog(parentFrame,
						pamControlledUnit.getPamModuleInfo(), pamControlledUnit
								.getUnitName());
				if (newName != null) {
					pamControlledUnit.rename(newName);
					jInternalFrame.setTitle(newName);
					
					// add a warning here for the user
					String	msg = "<html>Before any other changes are made, you should save the settings and restart Pamguard in order to ensure the ";
					msg += "new module name is properly updated.  Additionally, you should check the parameters of any downstream modules/processes ";
					msg += "to verify they are still referencing the correct module.</html>";
					int newAns = WarnOnce.showWarning(PamController.getMainFrame(), "Rename Module", msg, WarnOnce.OK_OPTION);
				}
			}
		}

	}

	// /* (non-Javadoc)
	// * @see
	// java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	// */
	// class processButtonListener implements ActionListener {
	// public void actionPerformed(ActionEvent e) {
	// JMenuItem menu = pamControlledUnit.CreateDetectionMenu(true);
	// if (menu != null) {
	//	
	// JButton b = (JButton) e.getSource();
	// JPopupMenu pm = new JPopupMenu();
	// pm.add(menu);
	// pm.show(b, b.getX() + b.getWidth()/2, b.getY() +b.getHeight()/2);
	// }
	// }
	// }
	void addComponent(JPanel panel, Component p, GridBagConstraints constraints) {
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}

	class ViewGroupInfo {
		public PamProcessView processView;
		public JComponent shownComponent;
		public Object pamguardObject;
		public JTextField unitCount;

		public ViewGroupInfo(PamProcessView processView,
				JComponent shownComponent, Object pamguardObject,
				JTextField unitCount) {
			super();
			this.processView = processView;
			this.shownComponent = shownComponent;
			this.pamguardObject = pamguardObject;
			this.unitCount = unitCount;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
		checkLayoutSize();
		layoutPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	@Override
	public void componentShown(ComponentEvent e) {

	}

	JPopupMenu getObjectViewerMenu() {
		JPopupMenu viewerMenu = new JPopupMenu();
		// printMenuItem = new JMenuItem("Print ...");
		// viewerMenu.add(printMenuItem);
		// printMenuItem.addActionListener(getObjectViewer().objectFrame);

		viewByController = new JCheckBoxMenuItem("One window per PAM module");
		viewByProcess = new JCheckBoxMenuItem("One window per PAM Process");
		viewByController
				.setSelected(pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYCONTROLLER);
		viewByProcess
				.setSelected(pamObjectViewerSettings.viewStyle == PamObjectViewerSettings.VIEWBYPROCESS);
		viewByController.addActionListener(new setViewByController());
		viewByProcess.addActionListener(new setViewByProcess());
		viewerMenu.add(viewByController);
		viewerMenu.add(viewByProcess);
		viewerMenu.addSeparator();

		showProcesslessModules = new JCheckBoxMenuItem(
				"Show modules with no process");
		showProcesslessModules
				.setSelected(pamObjectViewerSettings.showProcesslessModules);
		viewerMenu.add(showProcesslessModules);
		showProcesslessModules.addActionListener(new ShowProcesslessModules());
		// showProcesslessObservers = new
		// JCheckBoxMenuItem("Show Observers with no process");
		// showProcesslessObservers.setSelected(pamObjectViewerSettings.showProcesslessModules);
		viewerMenu.addSeparator();

		layoutMenuItem = new JMenuItem("Layout Processes ...");
		viewerMenu.add(layoutMenuItem);
		layoutMenuItem.addActionListener(getObjectViewer(null).objectFrame);

		JMenuItem menuItem = new JMenuItem("Module Ordering ...");
		menuItem.addActionListener(new menuModuleOrder());
		viewerMenu.add(menuItem);

		viewerMenu.addSeparator();
		viewerMenu.add(PamModuleInfo
				.getModulesMenu(getObjectViewer(null).objectFrame));
		return viewerMenu;
	}

	class setViewByController implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (pamObjectViewerSettings.viewStyle != PamObjectViewerSettings.VIEWBYCONTROLLER) {
				pamObjectViewerSettings.viewStyle = PamObjectViewerSettings.VIEWBYCONTROLLER;
				objectFrame.setFrameName();
				MakeDiagram();
			}
		}
	}

	class setViewByProcess implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (pamObjectViewerSettings.viewStyle != PamObjectViewerSettings.VIEWBYPROCESS) {
				pamObjectViewerSettings.viewStyle = PamObjectViewerSettings.VIEWBYPROCESS;
				objectFrame.setFrameName();
				MakeDiagram();
			}
		}
	}

	class ShowProcesslessModules implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			pamObjectViewerSettings.showProcesslessModules = !pamObjectViewerSettings.showProcesslessModules;
			showProcesslessModules
					.setSelected(pamObjectViewerSettings.showProcesslessModules);
			MakeDiagram();
		}
	}

	class menuModuleOrder implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (PamController.getInstance().orderModules(
					getObjectViewer(null).objectFrame)) {
				MakeDiagram();
			}
		}
	}

	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				getObjectViewerMenu()
						.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamView.PamViewInterface#ModelChanged()
	 */
	@Override
	public void modelChanged(int changeType) {
		// if (changeType == PamControllerInterface.CHANGED_PROCESS_SETTINGS)
		// if (true) return;
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			MakeDiagram();
			break;
		case PamControllerInterface.ADD_PROCESS:
		case PamControllerInterface.REMOVE_PROCESS:
			MakeDiagram();
			// requires more complicated layout
			break;
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			MakeDiagram();
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			CheckProcessesViews();
			layoutPanel.repaint();
			break;
		case PamControllerInterface.RENAME_CONTROLLED_UNIT:
			MakeDiagram();
			break;
		case PamControllerInterface.REORDER_CONTROLLEDUNITS:
			MakeDiagram();
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamView.PamViewInterface#PamEnded()
	 */
	@Override
	public void pamEnded() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamView.PamViewInterface#PamStarted()
	 */
	@Override
	public void pamStarted() {

	}

	@Override
	public Serializable getSettingsReference() {
		pamObjectViewerSettings.frameRectangle = objectFrame.getBounds();
		return pamObjectViewerSettings;
	}

	@Override
	public long getSettingsVersion() {
		return PamObjectViewerSettings.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "ObjectViewer";
	}

	@Override
	public String getUnitType() {
		return "ObjectViewer";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		pamObjectViewerSettings = (PamObjectViewerSettings) pamControlledUnitSettings
				.getSettings();
		objectFrame.setBounds(pamObjectViewerSettings.frameRectangle);
		objectFrame.setFrameName();
		MakeDiagram();
		return true;
	}

	@Override
	public void addControlledUnit(PamControlledUnit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeControlledUnit(PamControlledUnit unit) {
		// TODO Auto-generated method stub
		// could potentially uswe this function to remove module graphics more
		// subtelly than
		// redrawing the whole model view ?
	}

	@Override
	public void showControlledUnit(PamControlledUnit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFrameNumber() {
		return -1;
	}

	@Override
	public JFrame getGuiFrame() {
		return objectFrame;
	}

	// BusyLayeredPane busyPane = new BusyLayeredPane();
	@Override
	public void enableGUIControl(boolean enable) {
		// if (enable) {
		// objectFrame.remove(busyPane);
		// }
		// else {
		// objectFrame.add(busyPane);
		// }
	}

}
