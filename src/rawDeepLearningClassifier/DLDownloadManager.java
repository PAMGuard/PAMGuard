package rawDeepLearningClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import ai.djl.util.ZipUtils;
import rawDeepLearningClassifier.defaultModels.RightWhaleModel1;

/**
 * Manages the downloading and unzipping of models. 
 */
public class DLDownloadManager {
	
	public static String[] defaultModels = new String[] {"saved_model.pb"};

	/**
	 * Listens for downlaods.
	 */
	private ArrayList<DLDownloadListener> downLoadListeners = new ArrayList<DLDownloadListener>(); 




	public  DLDownloadManager() {

	}

	/**
	 * Notify the download listeners of a change. 
	 * @param status - notify the listeners.
	 * @param downloadedBytes - the number of downloaded bytes. 
	 */
	private void notifyDownLoadListeners(DLStatus status, long downloadedBytes) {
		for (DLDownloadListener listener : downLoadListeners) {
			listener.update(status, downloadedBytes);
		}
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
				try {

					HttpURLConnection huc = (HttpURLConnection) modelURI.toURL().openConnection();

					int responseCode = huc.getResponseCode();

					if (HttpURLConnection.HTTP_OK == responseCode) {
						notifyDownLoadListeners(DLStatus.CONNECTION_TO_URL, -1);
						File file = downLoadModel(modelURI.toURL(), getModelFolder(modelName) );
						
						System.out.println("DOWNLOADED MODEL: " + file.getPath() );

						file = decompressFile(file);

						if (file.isDirectory()) {
							//the file has been decompressed and need to search for model path within this...
							file =  findModelFile(file, defaultModels);
							
							System.out.println("DECOMPRESSED MODEL: " + file );

							if (file == null) return null;
						}
						
						System.out.println("FINAL MODEL: " + file );


						return file.toURI();
					}
					else {
						notifyDownLoadListeners(DLStatus.NO_CONNECTION_TO_URL, -1);
						return null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} 


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}


	}

	private static boolean isArchive(File f) {
		int fileSignature = 0;
		try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
			fileSignature = raf.readInt();
		} catch (IOException e) {
			// handle if you like
		}
		return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
	}


	/**
	 * Find the model file within
	 * @param folder
	 * @return
	 */
	private File findModelFile(File folder, String[] defaultModelNames) {


		Path startDir = folder.toPath();  // replace with your directory path

		try {
			List<Path> filesList = Files.walk(startDir)
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());

			filesList.forEach(System.out::println);

			for (String modelName:defaultModelNames) {
				String name;
				//now search through the list
				for (Path modelFile:filesList) {
					name = modelFile.getFileName().toString();
					if (name.equals(modelName)) {
						return modelFile.toFile();
					}

				}
			}
			return null; 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Once a file has been downloaded, it may need to be decompressed. 
	 * @param file - the file. 
	 * @return the path to the decompressed folder or the path to the original file if not a zip file. 
	 */
	private File decompressFile(File file) {

		if (FilenameUtils.getExtension(file.getPath()).equals("zip")) {

			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(file);

				String fileNameWithOutExt = FilenameUtils.removeExtension(file.getPath());

				File zipFolder = new File(fileNameWithOutExt);

				//Creating the directory
				boolean bool = zipFolder.mkdir();

				//unzip the model into the temporary directory....

				ZipUtils.unzip(fileInputStream, Paths.get(zipFolder.toURI()));

				//now that file has been unzipped search for a valid model file. 

				return zipFolder;

			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

		}
		else {
			return file;
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
	private  File downLoadModel(URL url, String outFolder) throws IOException {

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


		notifyDownLoadListeners(DLStatus.DOWNLOAD_STARTING, 0);

		InputStream  input  = http.getInputStream();
		byte[]       buffer = new byte[512]; //download in 4kB chunks
		int          n      = -1;
		OutputStream output = new FileOutputStream(outFile);
		long count = 0;
		
        Files.copy(input, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
		while ((n = input.read(buffer)) != -1) {
//			System.out.println("Chunk: " + n); 
			count=count+n; //total bytes
			notifyDownLoadListeners(DLStatus.DOWNLOADING, count);
			output.write( buffer, 0, n );
		}
		
		System.out.println("Chunk: " + n); 

		output.close();

		notifyDownLoadListeners(DLStatus.DOWNLOAD_FINISHED, count);


		return outFile;
	}

	/**
	 * Get the number of download listeners. 
	 * @return  the number of download listeners. 
	 */
	public int getNumDownloadListeners() {
		return downLoadListeners.size();
	}

	/**
	 * Add a download listener to the array manager. 
	 * @param e - the listener to add. 
	 * @return true if the listener was added successfully. 
	 */
	public boolean addDownloadListener(DLDownloadListener e) {
		return downLoadListeners.add(e);
	}

	/**
	 * Remove a download listener to the array manager. 
	 * @param e - the listener to remove. 
	 * @return true if the listener was removed successfully. 
	 */
	public boolean removeDownladListener(Object o) {
		return downLoadListeners.remove(o);
	}


	public static void main(String[] args) {
		DLDownloadManager dlDefaultModelManager = new DLDownloadManager();
		System.out.println("Test downloading a model: "); 

		URI path = dlDefaultModelManager.downloadModel(new RightWhaleModel1().getModelURI(), new RightWhaleModel1().getModelName()); 	
	}

}
