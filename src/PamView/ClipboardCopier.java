package PamView;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

import javax.swing.JMenuItem;

/**
 * Class to Copy graphics content to the clipboard and the printer 
 * <p>
 * Written so that it can easily be added to any PAMGUARD 
 * component. Provides default menu items for including in
 * pop up menus or main menu.  
 *  
 * @author Doug Gillespie
 *
 */
public class ClipboardCopier implements ClipboardOwner, Transferable, Printable {

	private Component component;

	private Image image;

	private String printJobName = "PAMGUARD";

	/**
	 * 
	 * @param component Component to copy. All children will also be copied. 
	 */
	public ClipboardCopier(Component component) {
		super();
		this.component = component;
	}
	/**
	 * 
	 * @param component Component to copy. All children will also be copied.
	 * @param printJobName name for print job (default is PAMGUARD) 
	 */
	public ClipboardCopier(Component component, String printJobName) {
		super();
		this.component = component;
		this.printJobName = printJobName;
	}

	/**
	 * Copy an image of the component to the clip board
	 */
	public void copyToClipBoard() {
		image = null;
		//		System.out.println("Copy " + component + " to clipboard");
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(this, this);
	}

	/**
	 * Create the image to be transfered to the clipboard. 
	 */
	protected void createTransferImage() {
		Dimension dim = component.getSize();
		BufferedImage bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = bufferedImage.getGraphics();
		g.setColor(component.getBackground());
		g.fillRect(0, 0, dim.width, dim.height);
		component.paint(g);
		image = bufferedImage;
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {

		//		System.out.println("Lost Ownership " + arg1);

	}

	@Override
	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		if (flavor == DataFlavor.imageFlavor) {
			return getTransferImage();
		}
		return null;
	}

	/**
	 * Get the transfer image. Create if necessary. 
	 * @return image to transfer. 
	 */
	private Image getTransferImage() {
		if (image == null) {
			createTransferImage();
		}
		return image;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] f = new DataFlavor[1];
		f[0] = DataFlavor.imageFlavor;
		//		System.out.println("getTransferDataFlavors() ");
		return f;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor == DataFlavor.imageFlavor) {
			return true;
		}
		return false;
	}

	/**
	 * Menu command to print. Sets up job, system does the rest
	 */
	private void printComponent() {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (printJobName != null) {
			job.setJobName(printJobName);
		}
		job.setPrintable(this);         
		boolean ok = job.printDialog();
		if (ok) {
			try {
				job.print();
			} catch (PrinterException ex) {
				/* The job did not successfully complete */
			}
		}	
	}

	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
		/*
		 * Copied from http://java.sun.com/docs/books/tutorial/2d/printing/examples/PrintUIWindow.java
		 */
		if (page > 0) { /* We have only one page, and 'page' is zero-based */
			return NO_SUCH_PAGE;
		}

		/* User (0,0) is typically outside the imageable area, so we must
		 * translate by the X and Y values in the PageFormat to avoid clipping
		 */
		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(pf.getImageableX(), pf.getImageableY());

		/**
		 * need to scale the print job to fit the paper. 
		 * 
		 */
		Rectangle gR = g2d.getClipBounds();
		double xScale = gR.getWidth()/component.getWidth();
		double yScale = gR.getHeight()/component.getHeight();
		double scale = Math.min(xScale, yScale);
		//	        
		if (scale < 1.0) {
			// then need to scale the whole image by that amount so it fits on the page
			AffineTransform at = g2d.getTransform(); // base on existing transform
			if (at == null) {
				at = new AffineTransform();
			}
			at.scale(scale, scale);
			g2d.setTransform(at);
		}

		/* Now print the component and its visible contents */
		component.printAll(g2d);

		/* tell the caller that this page is part of the printed document */
		return PAGE_EXISTS;

	}

	/**
	 * Get a menu item with a default title
	 * of "Copy to clipboard". Menu action listeners
	 * will automatically be set up
	 * @return menu item. 
	 */
	public JMenuItem getCopyMenuItem() {
		return getCopyMenuItem("Copy to clipboard");
	}

	/**
	 * 
	 * Get a menu item with a given title.
	 * Menu action listeners
	 * will automatically be set up
	 * @param menuTitle menu title
	 * @return menu item.
	 */
	public JMenuItem getCopyMenuItem(String menuTitle) {
		JMenuItem menuItem = new JMenuItem(menuTitle);
		menuItem.addActionListener(new CopyToClipboard());
		return menuItem;
	}

	/**
	 * Action listener for menu items. 
	 * @author Doug Gillespie
	 */
	class CopyToClipboard implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			copyToClipBoard();
		}
	}

	/**
	 * Get a menu item with a default title
	 * of "Print ...". Menu action listeners
	 * will automatically be set up
	 * @return menu item. 
	 */
	public JMenuItem getPrintMenuItem() {
		return getPrintMenuItem("Print ...");
	}

	/**
	 * 
	 * Get a menu item with a given title.
	 * Menu action listeners
	 * will automatically be set up
	 * @param menuTitle menu title
	 * @return menu item.
	 */
	public JMenuItem getPrintMenuItem(String menuTitle) {
		JMenuItem menuItem = new JMenuItem(menuTitle);
		menuItem.addActionListener(new PrintAction());
		return menuItem;
	}

	/**
	 * Action listener for menu items. 
	 * @author Doug Gillespie
	 */
	class PrintAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			printComponent();
		}
	}

	public String getPrintJobName() {
		return printJobName;
	}

	public void setPrintJobName(String printJobName) {
		this.printJobName = printJobName;
	}
	
	public void setImage(Image image){
		this.image=image;
	}


}
