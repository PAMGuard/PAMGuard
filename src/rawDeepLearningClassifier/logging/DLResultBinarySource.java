package rawDeepLearningClassifier.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import rawDeepLearningClassifier.dlClassification.DLClassifyProcess;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;
import rawDeepLearningClassifier.dlClassification.DLModelDataBlock;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Binary storage for the all the model results, i.e. all the returned probabilities. 
 * @author Jamie Macaulay 
 *
 */
public class DLResultBinarySource extends BinaryDataSource {

	private DLModelDataBlock dlDataBlock;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	/**
	 * TReference to the DL classifier process. 
	 */
	private DLClassifyProcess dlClassifierProcess;

	public DLResultBinarySource(DLClassifyProcess dlClassifierProcess) {
		super(dlClassifierProcess.getDLPredictionDataBlock());
		this.dlDataBlock = dlClassifierProcess.getDLPredictionDataBlock();
		this.dlClassifierProcess=dlClassifierProcess; 
	}

	@Override
	public String getStreamName() {
		return dlDataBlock.getDataName();
	}

	@Override
	public int getStreamVersion() {
		return 0;
	}

	@Override
	public int getModuleVersion() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#getPackedData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		
		DLDataUnit dlDataUnit = (DLDataUnit) pamDataUnit;
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}

		int type = ModelResultBinaryFactory.getType(dlDataUnit.getPredicitionResult()); 


		ModelResultBinaryFactory.getPackedData(dlDataUnit.getPredicitionResult(), dos, type);
		
		BinaryObjectData packedData = new BinaryObjectData(0, bos.toByteArray());
		return packedData;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkData(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader, int)
	 */
	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(binaryObjectData.getData()));
		PredictionResult result= ModelResultBinaryFactory.sinkData(dis); 


		return new DLDataUnit(baseData, result);

	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkModuleHeader(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader)
	 */
	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkModuleFooter(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader, binaryFileStorage.ModuleHeader)
	 */
	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}

}
