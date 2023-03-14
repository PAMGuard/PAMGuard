package tethys.output;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.w3c.dom.Document;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import Array.SnapshotGeometry;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamSettings;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import dbxml.uploader.Importer;
import metadata.MetaDataContol;
import metadata.deployment.DeploymentData;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.dbxml.DBXMLConnect;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.DeploymentRecoveryPair;
import tethys.pamdata.TethysDataProvider;
import tethys.pamdata.TethysSchema;

/**
 * Class sitting at the centre of all operations. It will talk to PAMGuard
 * objects to get schemas and data and talk to the database connection to move
 * data out (and possibly in). Eventually, a lot of the functionality in here
 * will be moved to worker threads (SwingWorker?) so that it's easy to keep
 * dialogs alive, show progress for big export jobs, etc. For now though, it's a
 * relatively simple set of function which we can use to a) open the database,
 * b) check everything such as schemas, etc. c) export data and d) clean up.
 *
 * @author dg50
 *
 */
public class TethysExporter {

	private TethysControl tethysControl;
	private TethysExportParams tethysExportParams;

	private DBXMLConnect dbxmlConnect;

	public TethysExporter(TethysControl tethysControl, TethysExportParams tethysExportParams) {
		this.tethysControl = tethysControl;
		this.tethysExportParams = tethysExportParams;
		dbxmlConnect = new DBXMLConnect(tethysControl);
	}

	/**
	 * Does the work. In reality this will need an awful lot of changing, for
	 * instance to provide feedback to an observer class to show progress on the
	 * display.
	 *
	 * @return OK if success.
	 */
	public boolean doExport() {

		// boolean dbOK = dbxmlConnect.openDatabase();
		// if (!dbOK) {
		/*
		 * should we set up some exceptions to throw ? Can be a lot more informative
		 * than a simple 'false'
		 */
		// return false;
		// }

		Deployment deployment1 = new Deployment();
		deployment1.setId("1");

		Path tempFile = null;
		try {

			JAXBContext jaxB = JAXBContext.newInstance(Deployment.class);
			Marshaller marshall = jaxB.createMarshaller();
			marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			marshall.marshal(deployment1, sw);
			tempFile = Files.createTempFile("pamGuardToTethys", ".xml");
			Files.write(tempFile, sw.toString().getBytes());

			String fileText = Importer.ImportFiles("http://localhost:9779", "Deployment",
					new String[] { tempFile.toString() }, "", "", false);

			tempFile.toFile().deleteOnExit();

		} catch(IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SnapshotGeometry arrayGeometry = findArrayGeometrey();

		/**
		 * Doug populate instrument fields - may need to add a few things. Marie to
		 * define what we mean by instrument. Instrument names probably need to be added
		 * to the PAMGuard Array dialog and can then be extraced from there. We had some
		 * useful discussion about what constitutes an instrumnet in Tinas dataset where
		 * there was a deployment of 10 MARU's, but the files from all of them were
		 * merged into a single 10 channel wav file dataset and processed together for
		 * detection and localisation. Clearly this goes into a single Tethys database,
		 * but do we put 'MARU Array' as the instrument and then include serial numbers
		 * of individual MARU's with the hydrophone data, or what ?
		 */

		/**
		 * Doug write something here to get most of the 'samplingdetails' schema. This
		 * all comes out of the Sound Acquisition module. Code below shows how to find
		 * this and iterate through various bits of information ... (I've put it in a
		 * separate function. Currently returning void,but could presumably return a
		 * Tethys samplingdetails document?)
		 */

		/*
		 * A load of notes Katie put in ....654654654
		 */

		//1. grab DeploymentRecoveryPair that has deployment details and recovery details
				//a. this is based on start and end times
					//Douglas calculates out dutycycles to only grab the

		//2. loop through the pairs to populate the extra information
			//one pair is one deployment
			//see below for matching


		//id => unique
		//project => project in pamguard
		//deploymentId == id
		//deploymentAlias => blank
		//site => UI addition in pamguard, not done, can be blank
		//siteAlias => blank
		//cruise => UI addition, optional
		//Platform=> UI addition in pamguard
		//region => UI addition
		//Instrument/Type => UI, array manager details (hydrophone names area)
		//Instrument/Id => UI, array manager details
		//Instrument/Geometry => in pamguard array manager
		//SamplingDetails/Channel
			//ChannelNumber => in pamguard, hyrdrophone array
			//SensorNumber => in pamguard,
			//Start => same as timestamp deployment detail
			//End => same as timestamp recovery detail
			//Sampling/Regimen (change sample rate, pamgauard doesnt handle, only on, get channel info in that loop)
				//TimeStamp => start time
				//SampleRate_kHz =>
				//SampleBits =>
			//Gain (another func call to get gain info)
			//DutyCycles => needs to be calculated, not fields in pamguard, have fun Douglas
		//QualityAssurance => not in pamguard, UI, maybe deployment notes, optional
		//Data/Audio (static)
			//URI => folder where audio is saved
		//Data/Tracks
			//Track => GPS datatable (granularity filter)
				//TrackId => not unique between deployments,
			//TrackEffort
				//OnPath => scattered throughout pamguard
			//URI => option, check with Shannon on how they are doing deployments
		//Sensors/Audio (per hydrophone not quad array) streamer info + individual hydrophone data together
			//pamguard hydrophone data
			//number => hydrophoneId
			//sensorId => sensor serial number
			//Geometry => array geometry field goes to
		//Sensors/Depth
			//optional
		//Sensors/Sensor
			//Number => hydrophoneId in pamguard
			//SensorId => addition to UI
			//Geometry => array geometry fields
			//Type => Hydrophone Type





		//get list of deployment recovery details (start, stop times and lat/long)
		//deployment details and recovery details are same structure
		//per pair, go through a loop to fill in each deployment
		DeploymentHandler deploymentHandler = new DeploymentHandler();

		ArrayList<DeploymentRecoveryPair> deployRecover = deploymentHandler.getDeployments();
		if (deployRecover == null) {
			return false;
		}

		/*
		 * This will become the main loop over deployment documents
		 */
		int i = 0;
		for (DeploymentRecoveryPair drd : deployRecover) {

			Deployment deployment = deploymentHandler.createDeploymentDocument(i++, drd);
//			System.out.println(deployment.toString());


		}

		

		/*
		 * go through the export params and call something for every data block that's
		 * enabled.
		 */
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aDataBlock : allDataBlocks) {
			StreamExportParams streamExportParams = tethysExportParams.getStreamParams(aDataBlock);
			if (streamExportParams == null || !streamExportParams.selected) {
				continue; // not interested in this one.
			}
			exportDataStream(aDataBlock, tethysExportParams, streamExportParams);
		}
		/*
		 * Then do whatever else is needed to complete the document.
		 */

		dbxmlConnect.closeDatabase();

		return true;
	}

	/**
	 * find Deployment data. This is stored in a separate PAMGuard module, which may
	 * not be present.
	 *
	 * @return
	 */
	public DeploymentData findDeploymentData() {
		/**
		 * What to do if this isn't present or is incomplete ? Should we be showing this
		 * in the main export dialog at some point ? More a Q for when we make a nicer
		 * UI later in the project.
		 */
		MetaDataContol metaControl = (MetaDataContol) PamController.getInstance()
				.findControlledUnit(MetaDataContol.unitType);
		if (metaControl == null) {
			return null;
		} else {
			return metaControl.getDeploymentData();
		}
	}

	public SnapshotGeometry findArrayGeometrey() {
		/*
		 * this should never be null, but again, we might want to put some warnings and
		 * exception handlers in here anyway. Really just an example to show how to find
		 * this. We'll need to dig a bit elsewhere to get more detailed hydrophone
		 * information.
		 */
		/*
		 * In PAMGuard hydrophones are assigned to streamers, which can have different
		 * methods for estimating their positions from GPS. The geometry is a sum of xyz
		 * in the streamer and xyz in the hydrophone object Within a streamer,
		 * hydrophones are considered rigid relative to each other. The stremer will
		 * floow a choice of modesl (rigid, threading, etc) to estimate it's position
		 * relative to the GPS track. Different errors are used when estimating
		 * localisation errors within and between streamers. The Snapshot geometry sorts
		 * a lot of this out for a point in time and will give back a single object
		 * which is most of what we'll be wanting.
		 */
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		SnapshotGeometry currentGeometry = currentArray.getSnapshotGeometry(PamCalendar.getTimeInMillis());
		/*
		 * The following lines of code show how to get more detailed calibration info
		 * for each hydrophone, but we'll have to think about the easiest way to
		 * repackage this for Tethys. e.g. this function could be modified to return the
		 * correct Tethys object in one go.
		 */
		ArrayList<Hydrophone> hydrophones = currentArray.getHydrophoneArray();
		/*
		 * each object in the list will have more detailed cal information for each
		 * phone. But for the full system calibration we'd need to go to the Acquisition
		 * module.
		 */
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance()
				.findControlledUnit(AcquisitionControl.unitType);
		if (daqControl != null) {
			AcquisitionProcess daqProcess = daqControl.getAcquisitionProcess();
			for (int iPhone = 0; iPhone < hydrophones.size(); iPhone++) {
				Hydrophone aPhone = hydrophones.get(iPhone);
				double totalCal = -daqProcess.rawAmplitude2dB(1, iPhone, false);
				System.out.printf(
						"hydrophone %d has sensitivity %3.1fdB + gain %3.1fdB. Total calibration is %3.1fdB re1U/uPa\n",
						iPhone, aPhone.getSensitivity(), aPhone.getPreampGain(), totalCal);
			}
		}

		return currentGeometry;
	}



	/**
	 * Here is where we export data for a specific data stream to Tethys.
	 *
	 * @param aDataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	private void exportDataStream(PamDataBlock aDataBlock, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		/**
		 * This will probably need to be passed additional parameters and may also want
		 * to return something other than void in order to build a bigger Tethys
		 * document.
		 */
		/*
		 * Some examples of how to do whatever is needed to get schema and data out of
		 * PAMGuard.
		 */
		/*
		 * first we'll probably want a reference to the module containing the data. in
		 * principle this can't get null, since the datablock was found be searching in
		 * the other direction.
		 */
		PamControlledUnit pamControlledUnit = aDataBlock.getParentProcess().getPamControlledUnit();

		TethysDataProvider dataProvider = aDataBlock.getTethysDataProvider();

		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();

		if (dataProvider == null) {
			return;
		}

		TethysSchema tethysSchema = dataProvider.getSchema();
		/*
		 * the schema should have a Document object in it. If we wanted to turn that
		 * into an XML string we can ... (though I'd assume that having the Document is
		 * more useful)
		 */
		String schemaXMLString = pamXMLWriter.getAsString(tethysSchema.getXsd(), false);
		System.out.printf("Schema for %s is %s\n", aDataBlock.getDataName(), schemaXMLString);

		/*
		 * Get the XML settings for that datablock. This is (or should be the parameters
		 * that were controlling that module, with adequate data about upstream
		 * modules). I think this has to go somewhere into the Detections document.
		 */
		Document doc = pamXMLWriter.writeOneModule((PamSettings) pamControlledUnit, System.currentTimeMillis());
		String moduleXML = null;
		if (doc != null) {
			// this string should be XML of all the settings for the module controlling this
			// datablock.
			moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
			System.out.printf("Module settings for datablock %s are:\n", moduleXML);
			System.out.println(moduleXML);
		}

		/**
		 * Now can go through the data. Probably, we'll want to go through all the data
		 * in the project, but we can hold off on that for now and just go for data that
		 * are in memory. We'll also have to think a lot about updating parts of the
		 * database which have been reprocessed - what we want to do, should eventually
		 * all be options set in the dialog and available within TethysExportParams For
		 * now though, we're just going to export data that are in memory. Once basic
		 * export is working, I can easily enough write something which will go through
		 * an entire data set, go through between two times, etc.
		 */
		// so this is a way of iterating through the data that are in memory, which will
		// do for now ..
		// do it with a data copy which can avoid synchronising the entire block for
		// what may be a long time
		// the copy function is itself synched, and is quite fast, so easier and safe
		// this way
		ArrayList<PamDataUnit> dataCopy = aDataBlock.getDataCopy();
		DataSelector dataSelector = aDataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		int nSkipped = 0;
		int nExport = 0;

		for (PamDataUnit aData : dataCopy) {
			/*
			 * see if we want this data unit. PAMGuard has a complicated system of data
			 * selectors specific to each data type. These are centrally managed so you
			 * don't need to worry too much about them. They are identified by name for each
			 * data stream and the behaviour here should follow the selections you made in
			 * the dialog. the data selection system allows different displays to show
			 * different data, so a stream can have many differently named selectors active
			 * at any one time, all doing different things in different parts of PAMGuard.
			 */
			if (dataSelector != null) {
				if (dataSelector.scoreData(aData) <= 0) {
					nSkipped++;
					continue; // don't want this one.
				}
			}

			/*
			 * then we do whatever we need to do to convert this into something for Tethys
			 * this might happen in the tethysSchema object for each data stream ????
			 */

			nExport++;
		}

		System.out.printf("Exported %d data units and skipped %d in %s", nExport, nSkipped,
				aDataBlock.getLongDataName());

	}

}
