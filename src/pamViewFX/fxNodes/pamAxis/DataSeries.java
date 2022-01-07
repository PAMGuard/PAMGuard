package pamViewFX.fxNodes.pamAxis;

public class DataSeries {
	
	/**
	 * The series name. 
	 */
	private String seriesName=""; 
	
	/*
	 * Holds x, y data series.  
	 */
	private double[][] data; 
	
	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public double[][] getData() {
		return data;
	}

	public void setData(double[][] data) {
		this.data = data;
	}


}