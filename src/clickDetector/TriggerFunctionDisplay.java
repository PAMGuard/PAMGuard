package clickDetector;

import java.awt.Graphics2D;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.RawDataDisplay;

public class TriggerFunctionDisplay extends RawDataDisplay {
	
	ClickDetector clickDetector;
	
	ClickTriggerFunctionDataBlock clickTriggerFunctionDataBlock;
	
	public TriggerFunctionDisplay(ClickTriggerFunctionDataBlock clickTriggerFunctionDataBlock) {
		
		super(clickTriggerFunctionDataBlock);
		
		this.clickTriggerFunctionDataBlock = clickTriggerFunctionDataBlock;
		
		clickDetector = clickTriggerFunctionDataBlock.getClickDetector();
		
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		// TODO Auto-generated method stub
		return new TriggerFunctionDisplayPanel(this, displayPanelContainer);
	}

	class TriggerFunctionDisplayPanel extends RawDisplayPanel {

		public TriggerFunctionDisplayPanel(DisplayPanelProvider displayPanelProvider, DisplayPanelContainer displayPanelContainer) {
			super(displayPanelProvider, displayPanelContainer);
			// TODO Auto-generated constructor stub
		}

		RawDataUnit currentDataUnit;
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			currentDataUnit = (RawDataUnit) dataUnit;
			super.addData(o, dataUnit);
		}

		double scaleMax = 50;
		double scaleMin = -10;
		@Override
		protected void drawSamples(Graphics2D g2d, double[] wavData, int pixel, int startSample, int nSamples, int y0, double yScale) {
			// TODO Auto-generated method stub
			
			//int yExt = getInnerHeight() / totalChannels;
			int totalChannels = PamUtils.getNumChannels(clickTriggerFunctionDataBlock.getChannelMap());
			int thisChannel = PamUtils.getSingleChannel(currentDataUnit.getChannelBitmap());
			int yExt = getInnerHeight() / totalChannels;
			y0 = (int) (yExt * scaleMin / (scaleMax - scaleMin));
			y0 += ((thisChannel+1) * yExt);
			yScale = yExt / (scaleMax - scaleMin);
			super.drawSamples(g2d, wavData, pixel, startSample, nSamples, y0, yScale);
			g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
			g2d.drawLine(pixel, y0, pixel+1, y0);
			int yt = (int) (y0 - clickDetector.getClickControl().clickParameters.dbThreshold * yScale);
			g2d.drawLine(pixel, yt, pixel+1, yt);
		}

		@Override
		protected void setAxisRange(double range) {

		}
		
		
	}
}
