package test.java.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericModelParser;

/**
 * Tests writing and reading JSON data from models. 
 * @author Jamie Macaulay 
 *
 */
class DLJSONParserTest {

	@Test
	void test() {
		
		//load an old file 
		String relJSONpath = 	"./src/test/resources/rawDeepLearningClassifier/Generic/right_whale/legacy_format/right_whale_DL_settings.pdtf";
		
		Path path = Paths.get(relJSONpath);
		
		GenericModelParams genericParmas = new GenericModelParams(); 
		
		genericParmas = GenericModelParser.readGenericModelParams(path.toFile(), genericParmas, null); 
		
		System.out.println("----------------- LEGACY ---------------");
		System.out.println(genericParmas);

		
		// write to a new file
		relJSONpath = 	"./src/test/resources/rawDeepLearningClassifier/Generic/right_whale/right_whale_DL_settings.pdtf";
		path = Paths.get(relJSONpath);
		
		GenericModelParser.writeGenericModelParams(path.toFile(), genericParmas); 
		
		//open the new file
		GenericModelParams genericParmas_new = GenericModelParser.readGenericModelParams(path.toFile(), genericParmas, null); 
		
		
		genericParmas_new.defaultShape = genericParmas_new.shape;
		genericParmas_new.defualtOuput = genericParmas_new.outputShape;

		
		System.out.println("----------------- CURRENT ---------------");

		System.out.println(genericParmas_new);
		
		//now compare the paramters 
		assertEquals(genericParmas, genericParmas_new); 
		
		
	}

}
