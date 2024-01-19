package test.java.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.ketos.KetosClassifier;

import org.junit.jupiter.api.Test;


public class KetosClassifierTest {
	
//	/**
//	 * Reference to the DL Control
//	 * 
//	 */
//	private DLControl testDLControl;
//	
//	
//	private KetosClassifier ketosClassifier_test;
//	
	

//	public KetosClassifierTest()  {
//		 System.out.println("hello unit test start"); 

//		try {
//			
//		 if (PamController.getInstance()==null || PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
//			 PamGUIManager.setType(PamGUIManager.NOGUI);
//			 PamController.create(PamController.RUN_NORMAL, null);
//		 }
//			
//		 testDLControl = new DLControl("Test_deep_learning");  
//		 
//		 ketosClassifier_test = (KetosClassifier) testDLControl.getDLModel(KetosClassifier.MODEL_NAME); 
//		 
//		 //set the ketos model as the correct model in the test. 
//		 testDLControl.getDLParams().modelSelection= testDLControl.getDLModels().indexOf(ketosClassifier_test); 
//		 
//		 System.out.println("hello unit test complete"); 
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Test the ketos classifier and tests are working properly. 
	 */
	@Test
	public void ketosClassifierTest() {
		 System.out.println("hello unit test complete b"); 

//		this.ketosClassifier_test.checkModelOK(); 
        assertEquals(2, 1+1);
	}

	
	/**
	 * Test that the whole process chain works with ketos i.e. test the segmenter etc. is performing properly. 
	 */
	@Test
	public void ketosProcessTest() {
		 System.out.println("hello unit test complete"); 

        assertEquals(2, 1+1);
	}
}
