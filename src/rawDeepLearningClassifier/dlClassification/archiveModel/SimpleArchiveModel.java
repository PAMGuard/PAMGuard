package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jamdev.jdl4pam.ArchiveModel;

import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;

/**
 * A Tensorflow model packaged with a jar file. 
 * @author Jamie Macaulay
 *
 */
public class SimpleArchiveModel extends ArchiveModel {


	public SimpleArchiveModel(File file) throws MalformedModelException, IOException, EngineException {
		super(file);
	}

	@Override
	public String getAudioReprRelPath(String zipFolder) {
		try {
			//System.out.println("SETTINGS PATH: " + getRelFilePath(zipFolder, ".pdtf"));
			return  getRelFilePath(zipFolder, ".pdtf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getModelRelPath(String zipFolder) {
		try {
			String model = null;
			model = getRelFilePath(zipFolder, ".pb");
			if (model==null) model = getRelFilePath(zipFolder, ".py");
			System.out.println("MODEL PATH: " +model + "  " + zipFolder);
			return model;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getModelFolderName() {
		return "zip_model";
	}

	/**
	 * Get the relative path of file within a zip folder. 
	 * @param zipFolder
	 * @param fileEnd
	 * @return
	 * @throws IOException
	 */
	private static String getRelFilePath(String zipFolder, String fileEnd) throws IOException {
		  try (Stream<Path> walk = Files.walk(Paths.get(zipFolder)))  {
			  List<String> result = walk
		              .filter(p -> {
						try {
							return (!Files.isDirectory(p) && !Files.isHidden(p));
						} catch (IOException e) {
							e.printStackTrace();
							return false;
						}
					})   // not a directory
		              .map(p -> p.toString()) // convert path to string
		              .filter(f -> f.endsWith(fileEnd))       // check end with
		              .collect(Collectors.toList());        // collect all matched to a List
		      
		      if (result.size()>0) {
		      String firstFile = result.get(0); 
		      
		     //System.out.println("First file: " +firstFile);
		      
		      String relative = firstFile.replace(zipFolder, "");
		      
		      return relative;
		      }
		      else return null;
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  return null;
		  }
	}
}
