package rawDeepLearningClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import rawDeepLearningClassifier.defaultModels.RightWhaleModel1;

/**
 * Manages the downloading and unzipping of models. 
 */
public class DLDownloadManager {
	
	public  DLDownloadManager() {
		
	}
	
	/**
	 * Check whether has been downloaded already to a default local folder? 
	 * @param modelURI - the URL of the model
	 * @return 
	 * @return true if the model has been downloaded. 
	 */
	public boolean isModelDownloaded(URL modelURL) {
		return isModelDownloaded(modelURL,  getModelName(modelURL));
	}
	
	
	/**
	 * Check whether has been downloaded already? 
	 * @param modelURI - the URL of the model
	 * @param modelName - the model name that has been used to name the temporary folder. 
	 * @return true if the model has been downloaded. 
	 */
	public boolean isModelDownloaded(URL modelURL, String modelName) {
		return getModelDownloadedFile( modelURL,  modelName).exists();
	}
	
	/**
	 * Get the path to the model if it has been downloaded. 
	 * @param modelURL - the URL to the model
	 * @param modelName - the model name. Use getModelName(modelURL) to use the default model name. 
	 * @return a file object - may not exist. 
	 */
	public File getModelDownloadedFile(URL modelURL, String modelName) {
		//get the name of the file
		String fileName = FilenameUtils.getName(modelURL.toString());
		
		return new File(getModelFolder(modelName)  + File.separator + fileName);
	}

	
	/**
	 * Get the path to a model. If the URI is a URL then the model is download to a local folder and the path to
	 * the local folder is returned. 
	 * @param model - the model to load. 
	 * @param modelName - the name of the model - this is used to create the local folder name if the model is downloaded. 
	 * @return the path to the model The model might be a zip file, py file, koogu file. 
	 */
	public URI downloadModel(URI modelURI) {
		String modelName = getModelName(modelURI);
		return downloadModel( modelURI,  modelName);
	}

	/**
	 * Get a model name based on it's filename
	 * @param modelURI - URI to the model
	 * @return the model name. 
	 */
	private String getModelName(URI modelURI) {
		return FilenameUtils.getBaseName(modelURI.getPath());
	}
	
	/**
	 * Get a model name based on it's filename
	 * @param modelURL - URL to the model
	 * @return the model name. 
	 */
	private String getModelName(URL modelURI) {
		return FilenameUtils.getBaseName(modelURI.getPath());
	}


	/**
	 * Get the path to a model. If the URI is a URL then the model is download to a local folder and the path to
	 * the local folder is returned. 
	 * @param model - the model to load. 
	 * @param modelName - the name of the model - this is used to create the local folder name if the model is downloaded. 
	 * @return the path to the model The model might be a zip file, py file, .kgu file. 
	 */
	public URI downloadModel(URI modelURI, String modelName) {
		
		if ("file".equalsIgnoreCase(modelURI.getScheme())) {
		    // It's a file path
			return modelURI; //the model is already a path. 
			
		} else {
		    // It's a URL
			 try {
				 System.out.println("Download: " + modelURI.toURL().getPath()); 
				 String folder = getModelFolder(modelName);
				 if (folder==null) {
					 System.err.println("DLDefaultModelManager.loadModel: Unable to make model folder: ");
				 }
				File file = downLoadModel(modelURI.toURL(), getModelFolder(modelName) );
				return file.toURI();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		
	}

	private static boolean isRedirected( Map<String, List<String>> header ) {
		for( String hv : header.get( null )) {
			if(   hv.contains( " 301 " )
					|| hv.contains( " 302 " )) return true;
		}
		return false;
	}
	

	/**
	 * Get the model folder - create it if it doesn't exist. 
	 * @return file object for new folder
	 */
	static public String getModelFolder() {
		String settingsFolder = System.getProperty("user.home");
		settingsFolder += File.separator + "Pamguard_deep_learning";
		// now check that folder exists
		File folder =  makeFolder( settingsFolder); 
		if (folder==null) return null; 
		return folder.getPath();
	}
	
	/**
	 * Get the model folder - create it if it doesn't exist. 
	 * @param modelname - the name of the model - this is used to create the local folder name. 
	 * @return file object for new folder
	 */
	static public String getModelFolder(String modelname) {
		String folder =  getModelFolder(); 
		if (folder == null) return null;
		// now check that folder exists
		File dlFolder =  makeFolder( folder + File.separator + modelname);
		if (dlFolder==null) return null;
		return dlFolder.getPath();
	}
	
	/**
	 * Make a settings folder.
	 * @param settingsFolder
	 * @return
	 */
	private static File makeFolder(String settingsFolder) {
		File folder = new File(settingsFolder);
		if (folder.exists() == false) {
			folder.mkdirs();
			if (folder.exists() == false) {
				return null;
			}
		}
		return folder;
	}
	
	


	/**
	 * Download a model to the default file location. 
	 * @param link - the link to download from
	 * @param outFolder - the folder in which to save the file. 
	 * @return the model file output. 
	 * @throws IOException - exception if something goes wrong. 
	 */
	private static File downLoadModel(URL url, String outFolder) throws IOException {
	
		String link = url.toString();
		
		//get the name of the file
		String fileName = FilenameUtils.getName(link);
		
		File outFile = new File( outFolder + File.separator + fileName);
		
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		Map< String, List< String >> header = http.getHeaderFields();
		
		//handle any redorects - e.g. from GitHub
		while( isRedirected( header )) {
			link = header.get( "Location" ).get( 0 );
			url    = new URL( link );
			http   = (HttpURLConnection)url.openConnection();
			header = http.getHeaderFields();
		}
		
		
		InputStream  input  = http.getInputStream();
		byte[]       buffer = new byte[4096]; //download in 4kB chunks
		int          n      = -1;
		OutputStream output = new FileOutputStream(outFile);
		while ((n = input.read(buffer)) != -1) {
			System.out.println("Chunk: " + n); 
			output.write( buffer, 0, n );
		}
		output.close();
		
		return outFile;
	}
	
	
	
	
	public static void main(String[] args) {
		DLDownloadManager dlDefaultModelManager = new DLDownloadManager();
		 System.out.println("Test downloading a model: "); 

		URI path = dlDefaultModelManager.downloadModel(new RightWhaleModel1().getModelURI(), new RightWhaleModel1().getModelName()); 	
	}

}
