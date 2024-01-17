package fftManager;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamProcess;
import dataPlotsFX.data.DataTypeInfo;

public class FFTDataBlock extends DataBlock2D<FFTDataUnit> {
	
	private int fftLength;
	
	private int fftHop;
	
	private boolean recycle = false;
	
	private int maxRecycledComplexArrays = 10000;
	
	private List<Complex[]> recycledComplexData = new Vector<Complex[]>();

	/**
	 * Gain of the window function (will generally be >0 and < 1)
	 */
	private double windowGain = 1.0;
	
	private static DataTypeInfo dataTypeInfo = new DataTypeInfo(ParameterType.FREQUENCY, ParameterUnits.HZ);

	public FFTDataBlock(String dataName,
			PamProcess parentProcess, int channelMap, int fftHop, int fftLength) {
		super(FFTDataUnit.class, dataName, parentProcess, channelMap);
		this.fftHop = fftHop;
		this.fftLength = fftLength;
//		Timer t = new Timer(1000, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				System.out.printf("%s recycled complex data arrays = %d\n", getDataName(), recycledComplexData.size());
//			}
//		});
//		t.start();
	}
//	
//	private String getLongName() {
//		return String.format("%s %d pt FFT, %d hop", getDataName(), fftLength, fftHop);
//	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getLongDataName()
	 */
	@Override
	public String getLongDataName() {
		// TODO Auto-generated method stub
		return super.getLongDataName();
	}

	public String getOldLongDataName() {
		return getParentProcess().getProcessName() + ", " + getDataName();
	}

	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		if (this.fftLength != fftLength) {
			this.fftLength = fftLength;
			noteNewSettings();
		}
	}

	public int getFftHop() {
		return fftHop;
	}

	public void setFftHop(int fftHop) {
		if (this.fftHop != fftHop) {
			this.fftHop = fftHop;
			noteNewSettings();
		}
	}


	@Override
	synchronized public void dumpBlockContents() {
		ListIterator<FFTDataUnit> listIterator = getListIterator(0);
		FFTDataUnit unit;
		System.out.println(String.format("***** Data Dump from %s *****", getDataName()));
		while (listIterator.hasNext()) {
			unit = listIterator.next();
			System.out.println(String.format("Index %d, Time %d, Channels %d, SequenceNums %d, Sample %d, Duration %d",
					unit.getAbsBlockIndex(),
					unit.getTimeMilliseconds(), unit.getChannelBitmap(),
					unit.getSequenceBitmap(),
					unit.getStartSample(), unit.getSampleDuration()));
		}
	}
	

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public void clearAll() {
		super.clearAll();
		recycledComplexData.clear();
	}

	public void recycleComplexArray(Complex[] complexArray) {
		if (recycle && recycledComplexData.size() < maxRecycledComplexArrays) {
			recycledComplexData.add(complexArray);
		}
	}

//	@Override
//	protected void removedDataUnit(FFTDataUnit pamUnit) {
//		// TODO Auto-generated method stub
//		super.removedDataUnit(pamUnit);
//		Object o = pamUnit;		
//	}

	public Complex[] getComplexArray(int arrayLength) {
		Complex[] newArray = null;
		int n = recycledComplexData.size();
		if (n > 0) {
			newArray = recycledComplexData.remove(n-1);
		}
		if (newArray == null || newArray.length != arrayLength) {
			if (newArray != null) {
				System.out.printf("Wrong array size had %d want %d in %s\n", newArray.length, arrayLength, getDataName());
			}
			newArray = Complex.allocateComplexArray(arrayLength);
		}
		return newArray;
	}
	
	@Override
	public double getDataGain(int iChan) {
		return windowGain;
	}

	/**
	 * @param windowGain the windowGain to set
	 */
	public void setWindowGain(double windowGain) {
		this.windowGain = windowGain;
	}

	public void setRecycle(boolean recycle) {
		this.recycle = recycle;
	}

	public boolean isRecycle() {
		return recycle;
	}

	@Override
	public int getHopSamples() {
		return getFftHop();
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		return fftLength/2;
	}

	@Override
	public double getMinDataValue() {
		return 0;
	}

	@Override
	public double getMaxDataValue() {
		return getSampleRate()/2.;
	}

	@Override
	public int getChannelMap() {
		return super.getChannelMap();
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return dataTypeInfo;
	}
	@Override
	public Element getDataBlockXML(Document doc) {
		Element el = super.getDataBlockXML(doc);
		el.setAttribute("FFTLength", String.format("%d", getFftLength()));
		el.setAttribute("FFTHop", String.format("%d", getFftHop()));
		return el;
	}



}
