package pamViewFX.fxNodes.pamChart;

import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

public class PamLineChart<T, W> extends LineChart<T, W> {

	public PamLineChart(Axis<T> xAxis, Axis<W> yAxis,
			ObservableList<javafx.scene.chart.XYChart.Series<T, W>> data) {
		super(xAxis, yAxis, data);
		// TODO Auto-generated constructor stub
	}

	public PamLineChart(Axis<T> xAxis, Axis<W> yAxis) {
		super(xAxis, yAxis);
		// TODO Auto-generated constructor stub
	}
		
}
