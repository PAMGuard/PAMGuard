package clickDetector.ClickClassifiers;

import java.awt.Frame;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.basicSweep.ZeroCrossingStats;
import clickDetector.layoutFX.clickClassifiers.ClassifyPaneFX;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;

public class NullClassifier implements ClickIdentifier {

	@Override
	public int codeToListIndex(int code) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ClassifyDialogPanel getDialogPanel(Frame windowFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JMenuItem getMenuItem(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSpeciesName(int code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSpeciesList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamSymbol getSymbol(ClickDetection click) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamSymbol[] getSymbols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClickIdInformation identify(ClickDetection click) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParamsInfo(ClickDetection click) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ZeroCrossingStats[] getZeroCrossingStats(ClickDetection click) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * method used to get peak frequency search range from sweep identifier.  Return null for
     * basic identifier
	 * 2014/08/03 MO
     */
	@Override
	public double[] getPeakSearchRange(ClickDetection click) {
		return null;
	}


	@Override
	public int[] getCodeList() {
		// TODO Auto-generated method stub
		return null;
	}


    public ClickTypeCommonParams getCommonParams(int code) {
        return null;
    }

	/**
	 * Returns the click length for the sweep classifier, using the times returned by the
	 * SweepClassifierWorker method getLengthData.  In the case of a different classifier,
	 * a 0 is returned
	 * 2014/10/13 MO
	 */
	public double getClickLength(ClickDetection click) {
		return 0.0;
	}

	@Override
	public ClassifyPaneFX getClassifierPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolData[] getSymbolsData() {
		return null;
	}


}
