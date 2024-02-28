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

/**
 * A Tensorflow model packaged with a jar file. 
 * @author Jamie Macaulay
 *
 */
public class SimpleArchiveModel extends ArchiveModel {


	public SimpleArchiveModel(File file) throws MalformedModelException, IOException {
		super(file);
	}

	@Override
	public String getAudioReprRelPath(String zipFolder) {
		try {
			System.out.println("SETTINGS PATH: " + getRelFilePath(zipFolder, ".pdtf"));
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
			System.out.println("MODEL PATH: " +model);
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

	
	private static String getRelFilePath(String zipFolder, String fileEnd) throws IOException {
		  // find files matched `png` file extension from folder C:\\test
		  try (Stream<Path> walk = Files.walk(Paths.get(zipFolder))) {
		      List<String> result = walk
		              .filter(p -> !Files.isDirectory(p))   // not a directory
		              .map(p -> p.toString().toLowerCase()) // convert path to string
		              .filter(f -> f.endsWith(fileEnd))       // check end with
		              .collect(Collectors.toList());        // collect all matched to a List
		      
		      if (result.size()>0) {
		      String firstFile = result.get(0); 
		      
		      String relative = new File(zipFolder).toURI().relativize(new File(firstFile).toURI()).getPath();
		      
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
