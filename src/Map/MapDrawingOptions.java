package Map;

import PamView.ProjectorDrawingOptions;

public class MapDrawingOptions extends ProjectorDrawingOptions {
	
	float opacity = 1;

	/**
	 * @param opacity
	 */
	public MapDrawingOptions(float opacity) {
		super();
		this.opacity = opacity;
	}

	@Override
	public Float getLineOpacity() {
		return opacity;
	}

	@Override
	public Float getShapeOpacity() {
		return opacity;
	}

	/**
	 * @return the opacity
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	/**
	 * work out how opaque the lines should be. 
	 * @param now current time. 
	 * @param earliestToPlot earliest time to plot
	 * @param dataTime data unit time
	 * @return opacity on scale of 0 to 1. 
	 */
	public float calculateOpacity(long now, long earliestToPlot, long dataTime) {
		return opacity = doOpacityCalculation(now, earliestToPlot, dataTime);
	}
	
	private float doOpacityCalculation(long now, long earliestToPlot, long dataTime) {
		if (now - earliestToPlot == 0) {
			return 0f;
		}
		if (dataTime >= now) {
			return 1f;
		}
		if (dataTime < earliestToPlot) {
			return 0f;
		}
		float o = (float) (dataTime - earliestToPlot) / (float) (now-earliestToPlot);
		o = Math.max(Math.min(o, 1.0f), 0.0f);
		return o;
	}
	

}
