package loggerForms.cameragrabber.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberParams;
import loggerForms.cameragrabber.data.CameraDataUnit;

public class ImagePanel {

	private CameraGrabber cameraGrabber;
	
	private GrabberPanel grabberPanel;
	
	private JPanel mainPanel, imagePanel, titlePanel;
	
	private JLabel title;

	private int cameraIndex;

	private boolean preview;
	
	private CameraDataUnit currentImageData;
	
	private JButton grabOne, grabSequence;

	/**
	 * @param cameraGrabber
	 * @param grabberPanel
	 */
	public ImagePanel(CameraGrabber cameraGrabber, GrabberPanel grabberPanel, int cameraIndex, boolean preview) {
		super();
		this.cameraGrabber = cameraGrabber;
		this.grabberPanel = grabberPanel;
		this.cameraIndex = cameraIndex;
		this.preview = preview;
		
		mainPanel = new PamPanel(new BorderLayout());
		imagePanel = new PaintPanel();
		titlePanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, imagePanel);
		mainPanel.add(BorderLayout.NORTH, titlePanel);
		title = new JLabel(" ");
		titlePanel.add(BorderLayout.CENTER, title);
		
		if (preview) {
			PamPanel buttonPanel = new PamPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			grabOne = new JButton("Grab Frame");
			grabSequence = new JButton("Grab Sequence");
			buttonPanel.add(grabOne);
			buttonPanel.add(grabSequence);
			titlePanel.add(BorderLayout.EAST, buttonPanel);
			grabOne.setToolTipText("Grab and store a single image");
			grabSequence.setToolTipText("Grab and store a sequence of images");
			grabOne.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					grabOne();
				}
			});

			grabSequence.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					grabSequence();
				}
			});
		}
		GrabberParams gp = cameraGrabber.getGrabberParams();
		CameraParams cp = gp.getCameraParams(cameraIndex);
		makeTitleBorder(cp);
		
	}

	protected void grabOne() {
		cameraGrabber.getGrabberProcess().grabOne(this.cameraIndex);
	}
	
	protected void grabSequence() {
		cameraGrabber.getGrabberProcess().grabSequence(this.cameraIndex);
	}

	private void makeTitleBorder(CameraParams cp) {
		String tit = String.format("%s - %s (%s)", cp.cameraName, cp.imageInitials, preview ? "Preview" : "Capture");
		mainPanel.setBorder(new TitledBorder(tit));
	}

	public JPanel getPanel() {
		return mainPanel;
	}
	
	public void newImage(CameraDataUnit cameraDataUnit) {
		currentImageData = cameraDataUnit;
		if (cameraDataUnit != null) {
		title.setText(PamCalendar.formatDBDateTime(cameraDataUnit.getTimeMilliseconds(), true));
		}
		else {
			title.setText(" ");
		}
		imagePanel.repaint();

	}
	
	private class PaintPanel extends PamPanel {

		private static final long serialVersionUID = 1L;

		public PaintPanel() {
			super();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			CameraDataUnit cameraDataUnit = currentImageData;
			if (cameraDataUnit == null) {
				return;
			}
			BufferedImage image = cameraDataUnit.getImage();
			double imageAspect = (double) image.getWidth() / (double) image.getHeight();
			double panelAspect = (double) imagePanel.getWidth() / (double) imagePanel.getHeight();
			int x, y, w, h;
			if (imageAspect > panelAspect) {
				x = 0;
				w = imagePanel.getWidth();
				y = 0; 
				h = (int) (w / imageAspect);
			}
			else {
				y = 0;
				h = imagePanel.getHeight();
				x = 0;
				w = (int) (h * imageAspect);
			}
			g.drawImage(image, x, y, w, h, 0, 0, image.getWidth(), image.getHeight(), null);
		}
		
	}
}
