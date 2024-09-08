package test.rawDeepLearningClassifier;

import org.junit.jupiter.api.Test;

public class PamZipDLClassifierTest {
	
	/**
	 * Test the koogu classifier and tests are working properly for a PAMGuard zip model - i.e. this is a very similar model to Koogu but zipped with a .zip 
	 * filename instead of .kgu. 
	 */
	@Test
	public void zipClassifierTest() {
		//relative paths to the resource folders.
		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/PamZip/blue_whale_24.zip";
		
		//the zip classifier is the same as the 
		String relWavPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/20190527_190000.wav";
		String relMatPath  =	"./src/test/resources/rawDeepLearningClassifier/Koogu/blue_whale_24/rawScores_20190527_190000.mat";
		
		//metadata says it should be used with Koogu classifier.
		KooguDLClassifierTest.runKooguClassifier( relModelPath,  relWavPath,  relMatPath);
	}
	
	
	

}
