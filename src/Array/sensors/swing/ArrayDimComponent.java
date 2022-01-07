package Array.sensors.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamController.PamController;
import PamUtils.PamUtils;
import PamView.panel.PamPanel;

public class ArrayDimComponent extends PamPanel {

	private Integer minWidth, minHeight;
	
	private ArraySensorDataUnit[] sensorData;

	private int streamerMap;

	private boolean isViewer;

	public ArrayDimComponent(int streamerMap,Integer minWidth, Integer minHeight) {
		super();
		this.streamerMap = streamerMap;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		int nStreamer = Math.max(1, PamUtils.getNumChannels(streamerMap));
		sensorData = new ArraySensorDataUnit[nStreamer];
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}

	public ArrayDimComponent(int streamerMap) {
		this(streamerMap, null, null);
	}

	public ArraySensorDataUnit getSensorData(int iData) {
		if (iData >= 0 && iData < sensorData.length) {
			return sensorData[iData];
		}
		return null;
	}
	/**
	 * Get a data index, probably the same as the streamer number, but you never know. 
	 * @param iStreamer
	 * @return
	 */
	private int getDataIndex(int iStreamer) {
		int dataInd = PamUtils.getChannelPos(iStreamer, streamerMap);
		if (dataInd >= sensorData.length) {
			dataInd = -1;
		}
		return dataInd;
	}
	
	/**
	 * Get a streamer index for a data index. 
	 * @param iData
	 * @return
	 */
	public int getStreamerIndex(int iData) {
		return PamUtils.getNthChannel(iData, streamerMap);
	}
	
	public int getNStreamers() {
		return PamUtils.getNumChannels(streamerMap);
	}
	
	public boolean setSensorData(int iStreamer, ArraySensorDataUnit sensorData) {
		int iData = PamUtils.getChannelPos(iStreamer, streamerMap);
		if (iData < 0 || iData >= this.sensorData.length) {
			return false;
		}
		this.sensorData[iData] = sensorData;
		repaint(20);
		return true;
	}
	
	public Double getSensorValue(int iStreamer, ArraySensorFieldType fieldType) {
		for (int i = 0; i < sensorData.length; i++) {
			ArraySensorDataUnit data = getSensorData(i);
			if (data == null) {
				continue;
			}
			else {
				Double val = data.getField(iStreamer, fieldType);
				if (val != null) {
					return val;
				}
			}
		}
		return null;
	}
	
	/**
	 * Set a common font.
	 * @param g
	 * @return
	 */
	public Font setFont(Graphics g) {
		Font f = g.getFont();
		Font newF = new Font(f.getFontName(), f.getStyle(), f.getSize()-1);
		g.setFont(newF);
		return newF;
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (minHeight != null) {
			d.height = Math.max(d.height, minHeight);
		}
		if (minWidth != null) {
			d.width = Math.max(d.width, minWidth);
		}
		return d;
	}

	/**
	 * @return the minWidth
	 */
	public Integer getMinWidth() {
		return minWidth;
	}

	/**
	 * @param minWidth the minWidth to set
	 */
	public void setMinWidth(Integer minWidth) {
		this.minWidth = minWidth;
	}

	/**
	 * @return the minHeight
	 */
	public Integer getMinHeight() {
		return minHeight;
	}

	/**
	 * @param minHeight the minHeight to set
	 */
	public void setMinHeight(Integer minHeight) {
		this.minHeight = minHeight;
	}

	/**
	 * @return the isViewer
	 */
	public boolean isViewer() {
		return isViewer;
	}
}
