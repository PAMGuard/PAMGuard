package clickTrainDetector.clickTrainAlgorithms.mht.test;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.ListIterator;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackDataUnits;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;

/**
 *MHTTestGraph for testing the MHT algorithm 
 * @author Jamie Macaulay 
 *
 */ 
public class MHTTestChart extends BorderPane {

	/**
	 * The sim clicks. 
	 */
	private ExampleClickTrains simClicks;

	/**
	 * The chart to show clicks 
	 */
	private LineChart<Number, Number> sc;


	public MHTTestChart() {
		sc = createChart();
		this.setCenter(sc); 
	}

	/**
	 * Set the chart data - plots a bunch of simple clicks 
	 * @param simpleClickDataBlock - the simple click data block. 
	 */
	public void setChartData(SimpleClickDataBlock simpleClickDataBlock) {
		sc.getData().clear();
		XYChart.Series clickSeries = generateClickSet(simpleClickDataBlock);
		sc.getData().addAll(clickSeries);
		//remove lines from the chart
		sc.lookup(".default-color0.chart-series-line").setStyle("-fx-stroke: transparent");
	}


	/**
	 * Create a simple chart for the algorithm 
	 * @return the chart for the algorithm. 
	 */
	private LineChart createChart() {
		final NumberAxis xAxis = new NumberAxis(0, 10, 1);
		final NumberAxis yAxis = new NumberAxis(0, 180, 5);  

		LineChart sc = new LineChart<Number,Number>(xAxis,yAxis);
		sc.setAnimated(false);
		xAxis.setLabel("Time (s)");                
		yAxis.setLabel("Amplitude (dB)");
		sc.setLegendVisible(false);

		//		sc.setStyle(".default-color0.chart-series-line { -fx-stroke: transparent; }\r\n" + 
		//				".default-color1.chart-series-line { -fx-stroke: red; }");
	
//		sc.setTitle("MHT Algorithm Test");

		sc.setPrefSize(500, 400);

		return sc; 
	}

	/**
	 * 
	 * @param ICI - the ICI in seconds
	 * @param amplitude - the amplit
	 * @param startTime
	 */
	private XYChart.Series generateClickSet(SimpleClickDataBlock simpleClicks) {

		XYChart.Series series1 = new XYChart.Series();
		series1.setName("The clicks to test");

		ListIterator<SimpleClick> clickIterator = simpleClicks.getListIterator(0); 

		SimpleClick simpleClick; 
		while (clickIterator.hasNext()) {
			simpleClick=clickIterator.next();
			series1.getData().add(new XYChart.Data(
					simpleClick.timeSeconds, simpleClick.amplitude));
		}

		return series1; 
	}

	/**
	 * 
	 */
	private XYChart.Series generateMHTTrack(TrackDataUnits trackData) {

		XYChart.Series clickMHTSeries = new XYChart.Series();

		ArrayList<PamDataUnit> clicks = trackData.dataUnits; 

		SimpleClick simpleClick; 
		for (int i=0; i<clicks.size(); i++) {
			simpleClick=(SimpleClick) clicks.get(i);
//			System.out.println("Simple click: " + i + "  " + simpleClick); 
			clickMHTSeries.getData().add(new XYChart.Data(
					simpleClick.timeSeconds, simpleClick.amplitude));
		}

		return clickMHTSeries; 
	}

	/**
	 * Update the graphics to show the current possible clicks trains. 
	 * @param mhtTestlgorithm. 
	 */
	public void updateMHTGraphics(MHTKernel<PamDataUnit> mhtKernel) {

		sc.getData().remove(1, sc.getData().size()-1);

		ArrayList<TrackBitSet> activeTracks = mhtKernel.getActiveTracks(); 

		if (activeTracks!=null) {
//			System.out.println("Number of active tracks: " + activeTracks.size()); 

			TrackDataUnits trackData;
			XYChart.Series series;
			
			
//			//Temp
//			int nclicks=100;
//			BitSet bitSet= new BitSet(); 
//			for (int i=0; i<nclicks; i++) {
//				bitSet.set(i, true);
//			}
//			trackData = MHTChi2.getTrackDataUnits(mhtKernel.getReferenceUnit(), bitSet, 1, mhtKernel.getKCount());
//			series = generateMHTTrack( trackData);
//
//			final XYChart.Series seriesrun= series; 
//			sc.getData().add(seriesrun);
////			sc.layout();
//			//temp
			
			for (int i=0; i<activeTracks.size(); i++) {
				trackData = MHTClickTrainAlgorithm.getTrackDataUnits(mhtKernel, activeTracks.get(i).trackBitSet, mhtKernel.getKCount());

//				System.out.println("ACTIVE TRACK SIZE: " + trackData.dataUnits.size()); 
//				System.out.println("ACTIVE TRACK BITSET: " + MHTKernel.printBitSet(activeTracks.get(i).trackBitSet, mhtKernel.getKCount())); 
	
				//generate a series for the chart. 
				series = generateMHTTrack(trackData);

				final XYChart.Series seriesrun= series; 
				sc.getData().add(seriesrun);
			}
			
			//get the confirmed tracks
			for (int i=0; i<mhtKernel.getNConfrimedTracks(); i++) {
				trackData = MHTClickTrainAlgorithm.getTrackDataUnits(mhtKernel, mhtKernel.getConfirmedTrack(i).trackBitSet, mhtKernel.getKCount());

//				System.out.println("ACTIVE TRACK SIZE: " + trackData.dataUnits.size()); 
//				System.out.println("ACTIVE TRACK BITSET: " + MHTKernel.printBitSet(activeTracks.get(i).trackBitSet, mhtKernel.getKCount())); 
	
				//generate a series for the chart. 
				series = generateMHTTrack(trackData);

				final XYChart.Series seriesrun= series; 
				sc.getData().add(seriesrun);
			}
			
			sc.layout();

		}
	}

}