package tethys.tooltips;

import java.lang.reflect.Field;

import nilus.Deployment;

/**
 * Class to make it easy to find tooltips for a given nilus class and field nams. The constatns
 * could be used directly, or can be found using findTip(Class, String) using the class type
 * and field name for any nilus object. 
 * Tips were generates from a set of csv files extracted from the xml schema using Matlab code, then formatted
 * in Matlab and pasted into this class, so will need to rerun that process should the xml schemas be updated. 
 * 
 * @author dg50
 *
 */
public class TethysTips {
	
//	public static void main(String[] args) {
//		Class cls = Deployment.Data.class;
//		String field = "Audio";
//		String foundTip = findTip(cls, field);
//		System.out.println(foundTip);
//	}
	
	/**
	 * find the tooltip for a given class and field within that class. 
	 * @param aClass
	 * @param field
	 * @return found tip or null
	 */
	public static String findTip(Class aClass, String fieldName) {
		Package pack = aClass.getPackage();
		String packName = pack.toString();
		String clsName = aClass.getCanonicalName();
		if (clsName.startsWith("nilus.") == false) {
			return null;
		}
		clsName = clsName.substring(6);
		clsName = clsName.replace('.', '_');
		String varName = clsName + "_" + fieldName;
		// now try to find that field in this class and get it's value. 
		Field field = null;
		try {
			field = TethysTips.class.getDeclaredField(varName);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		if (field == null) {
			return null;
		}
		Object tip = null;
		try {
			tip = field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
		if (tip == null) {
			return null;
		}
		
		return tip.toString();
	}

	// Annotations taken from schemata_csv
	public static final String Calibration_Id = "Identifier of instrument, preamplifier, or hydrophone. Corresponds to elements in Deployment: Deployment/Instrument/Id, Deployment/Sensors/Audio/HydrophoneId, Deployment/Sensors/Audio[i]/PreampId. As instruments may be calibrated multiple times, it is not an error for duplicate Id values to appear. It is recommended that the three different types of identifiers (instrument, hydrophone, preamp) be distinct, but the Type element may be used to distinguish them if they are not.";
	public static final String Calibration_TimeStamp = "Date and time of calibration";
	public static final String Calibration_Type = "hydrophone, preamplifier, or end-to-end Indicates type of calibration";
	public static final String Calibration_Process = "Process used to calibrate instrument.";
	public static final String Calibration_ResponsibleParty = "Who conducted/managed the calibration?";
	public static final String Calibration_IntensityReference_uPa = "Reference intensity in µ Pascals for dB measurements. Commonly used: underwater acoustics: 1 terrestrial acoustics: 20";
	public static final String Calibration_Sensitivity_dBV = "Optional measurement of transducer sensitivity at 1 kHz.";
	public static final String Calibration_Sensitivity_V = "Optional measurement of Sensitivity_dBV on a linear scale, provided by many transducer manufacturers.";
	public static final String Calibration_Sensitivity_dBFS = "Optional measurement for digital transducers. Digital transducers do not output voltage measurements. In this case, the 1 kHz sensitivity measurement is measured relative to peak output of the full-scale signal instead of RMS. It should be noted that for sinusoidal signals, the RMS sensitivity will be 3 dB lower (Lewis, 2012). Lewis, J. (2012). \"Understanding Microphone Sensitivity,\" Analog Dialogue 46(2). 14-16.";
	public static final String Calibration_FrequencyResponse = "Lists of frequencies (Hz) and responses (dB). Lists must be of equal length.";
	public static final String Calibration_MetadataInfo = "Information about who is responsible for this metadata record, when it was created, and how often it is updated.";
	public static final String Calibration_Process_Method = "Text based description of algorithm or citation";
	public static final String Calibration_Process_Software = "Name of software that implements the algorithm or supports human analysts. This might be the name of a plug-in or extension module that is part of a larger program or system.";
	public static final String Calibration_Process_Version = "Software version identifier";
	public static final String Calibration_Process_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Calibration_Process_SupportSoftware = "Software required in addition to the algorithm itself, e.g. Matlab, Ishmael, PAMGUARD, Triton, etc.";
	public static final String Calibration_Process_SupportSoftware_Version = "Software version identifier.";
	public static final String Calibration_Process_SupportSoftware_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Calibration_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Calibration_QualityAssurance_Quality = "Measurement is: unverified, valid, invalid";
	public static final String Calibration_QualityAssurance_AlternateCalibration = "Provide an alternative calibration Id that should be used (if available) when the Quality value is invalid.";
	public static final String Calibration_MetadataInfo_Contact = "based on ISO 19115";
	public static final String Calibration_MetadataInfo_Date = "Last update.";
	public static final String Calibration_MetadataInfo_UpdateFrequency = "How often are these data updated? as-needed, unplanned, or yearly";
	public static final String Calibration_MetadataInfo_Contact_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";

	// Annotations taken from schemata_csv
	public static final String Deployment_Id = "Character sequence that uniquely identifies this deployment.";
	public static final String Deployment_Description = "Objectives, abstract and high-level methods.";
	public static final String Deployment_Project = "Name of project associated with this deployment. Can be related to a geographic region, funding source, etc.";
	public static final String Deployment_DeploymentId = "A number related to either the Nth deployment operation in a series of deployments or the Nth deployment at a specific site. This is different from Id which is a unqiue identifier for the deployment. If a vessel deployed 5 instruments, they might all have the same DeploymentId. While not enforced, it is expected that the combination of Project, DeploymentId, and (Site or Cruise) to be unique.";
	public static final String Deployment_DeploymentAlias = "Alternative deployment description.";
	public static final String Deployment_Site = "Name of a location where instruments are frequently deployed. Can be something as simple as a letter or a geographic name. Strongly recommended for long-term time series recorded at a specific point.";
	public static final String Deployment_SiteAliases = "Alternative names for the deployment location";
	public static final String Deployment_Cruise = "Name of deployment cruise.";
	public static final String Deployment_Platform = "On what platform is the instrument deployed? (e.g. mooring, tag)";
	public static final String Deployment_Region = "Name of geographic region.";
	public static final String Deployment_Instrument = "Instrument type and identifier.";
	public static final String Deployment_SamplingDetails = "Information about recordings on each channel. Sample rate, quantization bits, etc.";
	public static final String Deployment_Data = "Data from instrument, a URI is provided when not present (typical for audio).";
	public static final String Deployment_DeploymentDetails = "Instrument deployment location, time, etc.";
	public static final String Deployment_RecoveryDetails = "Instrument recovery, location, time, etc.";
	public static final String Deployment_Sensors = "Sensors on instrument.";
	public static final String Deployment_MetadataInfo = "Party responsible for this record. Some applications may make this mandatory.";
	public static final String Deployment_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Deployment_Description_Abstract = "Overview of effort.";
	public static final String Deployment_Description_Method = "High-level description of the method used.";
	public static final String Deployment_Instrument_Type = "Instrument type, e.g. HARP, EAR, Popup, DMON, Rock Hopper, etc.";
	public static final String Deployment_Instrument_InstrumentId = "Instrument identifier, e.g. serial number";
	public static final String Deployment_Instrument_GeometryType = "Sensor attachment \"rigid\" - relative geometry is fixed, \"cabled\" - relative geometry may be expected to deform depending on movement, currents, etc.";
	public static final String Deployment_SamplingDetails_Channel_ChannelNumber = "Channels and sensors are bound together from Start to End. While not enforced, we assume channels are numbered from 1 to N.";
	public static final String Deployment_SamplingDetails_Channel_SensorNumber = "Audio sensor index within the Sensors element. This allows us to associate a channel with a physical hydrophone.";
	public static final String Deployment_SamplingDetails_Channel_Sampling = "Sampling rate and quantization may change over time.";
	public static final String Deployment_SamplingDetails_Channel_Gain = "Initial gain setting (assumed 0 if not populated) and any subsequent changes.";
	public static final String Deployment_SamplingDetails_Channel_DutyCycle = "Duty cycle is represented by the recording duration and the interval from the start of one recording session to the next. A duration of 3 m and an interval of 5 m would represent a 60% duty cycle, 3 m on, 2 m off.";
	public static final String Deployment_SamplingDetails_Channel_Sampling_Regimen = "Sampling regimen may change over time. Each entry shows the start of a sampling configuration.";
	public static final String Deployment_SamplingDetails_Channel_Sampling_Regimen_SampleRate_kHz = "Number of samples per second in kHz, e.g. 192 is 192,000 samples/s";
	public static final String Deployment_SamplingDetails_Channel_Sampling_Regimen_SampleBits = "Number of bits per sample.";
	public static final String Deployment_SamplingDetails_Channel_Gain_Regimen_Gain_rel = "Only used if gain is not calibrated. Relative gain may be a number on a dial.";
	public static final String Deployment_SamplingDetails_Channel_DutyCycle_Regimen = "Duty cycling regimen may change over time. Each entry shows the start of a duty cycle configuration. The abscence of entries indicates continuous sampling as would having equal values in RecordingDuration_m and RecordingInterval_m.";
	public static final String Deployment_SamplingDetails_Channel_DutyCycle_Regimen_TimeStamp = "Indicates when the duty cycle becomes active. It remains active until the next Regimen entry.";
	public static final String Deployment_SamplingDetails_Channel_DutyCycle_Regimen_RecordingDuration_s = "The amount of time in minutes during each recording interval when the data logger is recoring. Use the attribute Offset_s when the recording does not begin at the beginning of each recording interval.";
	public static final String Deployment_SamplingDetails_Channel_DutyCycle_Regimen_RecordingInterval_s = "Time between consecutive recordings. If RecordingDuration_s is 1800 s and RecordingInterval_s is 3600 s, then we record for the 30 min of each hour.";
	public static final String Deployment_QualityAssurance_Description = "Text based description of process.";
	public static final String Deployment_QualityAssurance_ResponsibleParty = "based on ISO 19115";
	public static final String Deployment_QualityAssurance_Quality = "If no quality assurance, create an entry of Category unverified spanning the acoustic record.";
	public static final String Deployment_QualityAssurance_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Deployment_QualityAssurance_Description_Abstract = "Overview of effort.";
	public static final String Deployment_QualityAssurance_Description_Method = "High-level description of the method used.";
	public static final String Deployment_QualityAssurance_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Deployment_QualityAssurance_Quality_Category = "categories: unverified, good, compromised, unusable";
	public static final String Deployment_QualityAssurance_Quality_FrequencyRange = "QA metric applies to what frequency range?";
	public static final String Deployment_QualityAssurance_Quality_Comment = "Additional qualitative information";
	public static final String Deployment_Data_Audio = "Information about audio data.";
	public static final String Deployment_Data_Tracks = "A set of measurements about a ship/instrument's track line.";
	public static final String Deployment_Data_Audio_URI = "Uniform Resource Indicator that points to audio content. Examples: digital object identifier, web address, or even a simple string describing the storage location.";
	public static final String Deployment_Data_Audio_Processed = "Pointer to location of data that has been processed (e.g. checked for quality, decimated, etc.)";
	public static final String Deployment_Data_Audio_Raw = "Pointer to raw data from the instrument.";
	public static final String Deployment_Data_Tracks_Track = "A set of sorted (by time) points associated with one or more tracklines.";
	public static final String Deployment_Data_Tracks_TrackEffort = "Not all measurements are associated with an instrument/ship's planned trackline (e.g. when in chase mode or transiting between tracklines). Specify times for track effort here if needed.";
	public static final String Deployment_Data_Tracks_URI = "Pointer to trackline information.";
	public static final String Deployment_Data_Tracks_Track_TrackId = "Optional trackline number. If unimportant, everything can be put in one Points element.";
	public static final String Deployment_Data_Tracks_Track_Point = "Timestamped measurements: long/lat, bearing, etc. Points should be sorted by timestamp.";
	public static final String Deployment_Data_Tracks_Track_Point_Bearing_DegN = "Bearing in degrees [0, 360) relative to true or magnetic north (as specified by north attribute, default magnetic)";
	public static final String Deployment_Data_Tracks_Track_Point_Speed_kn = "Speed in knots";
	public static final String Deployment_Data_Tracks_Track_Point_Pitch_deg = "Instrument pitch [0, 360) degrees";
	public static final String Deployment_Data_Tracks_Track_Point_Roll_deg = "Instrument roll [0, 360) degrees";
	public static final String Deployment_Data_Tracks_Track_Point_Elevation_m = "Instrument elevation (meters) relative to average sea level.";
	public static final String Deployment_Data_Tracks_Track_Point_GroundElevation_m = "Ground or seabed elevation (meters) relative to average sea level.";
	public static final String Deployment_Data_Tracks_Track_Point_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Deployment_Data_Tracks_Track_Point_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Deployment_Data_Tracks_TrackEffort_OnPath_FocalArea = "This element is used to provide names that specify a focal area in which the study was conducted, such as a National Marine Sanctuary.";
	public static final String Deployment_Data_Tracks_TrackEffort_OffPath_FocalArea = "This element is used to provide names that specify a focal area in which the study was conducted, such as a National Marine Sanctuary.";
	public static final String Deployment_DeploymentDetails_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Deployment_DeploymentDetails_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Deployment_DeploymentDetails_ElevationInstrument_m = "The elevation at which this instrument is positioned.";
	public static final String Deployment_DeploymentDetails_DepthInstrument_m = "Not usually required. This field is designed to record depth with respect to the ground or seabed. Uses for this field include mines and alpine lakes.";
	public static final String Deployment_DeploymentDetails_Elevation_m = "Elevation of ground/sea bed";
	public static final String Deployment_DeploymentDetails_TimeStamp = "Time at which instrument was deployed/recovered. Lost instruments: set recovery time to deployment time.";
	public static final String Deployment_DeploymentDetails_AudioTimeStamp = "Recording start or end - May differ from deployment time.";
	public static final String Deployment_DeploymentDetails_ResponsibleParty = "based on ISO 19115";
	public static final String Deployment_DeploymentDetails_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Deployment_RecoveryDetails_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Deployment_RecoveryDetails_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Deployment_RecoveryDetails_ElevationInstrument_m = "The elevation at which this instrument is positioned.";
	public static final String Deployment_RecoveryDetails_DepthInstrument_m = "Not usually required. This field is designed to record depth with respect to the ground or seabed. Uses for this field include mines and alpine lakes.";
	public static final String Deployment_RecoveryDetails_Elevation_m = "Elevation of ground/sea bed";
	public static final String Deployment_RecoveryDetails_TimeStamp = "Time at which instrument was deployed/recovered. Lost instruments: set recovery time to deployment time.";
	public static final String Deployment_RecoveryDetails_AudioTimeStamp = "Recording start or end - May differ from deployment time.";
	public static final String Deployment_RecoveryDetails_ResponsibleParty = "based on ISO 19115";
	public static final String Deployment_RecoveryDetails_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Deployment_Sensors_Audio_Number = "Sensor index. May be used to associate the sensor with other parts of the schema. For example, for Audio sensors, the Channel/SensorNumber can be set to a specific Sensor/Audio/Number, permitting us to determine information about the a hydrophone assembly.";
	public static final String Deployment_Sensors_Audio_SensorId = "A value that uniquely identifies this sensor, e.g. a serial number.";
	public static final String Deployment_Sensors_Audio_Geometry = "Geometry relative to platform";
	public static final String Deployment_Sensors_Audio_Name = "Optional sensor name";
	public static final String Deployment_Sensors_Audio_Description = "Optional description of sensor.";
	public static final String Deployment_Sensors_Audio_HydrophoneId = "Optional hydrophone identifier.";
	public static final String Deployment_Sensors_Audio_PreampId = "Optional preamplifier identifier.";
	public static final String Deployment_Sensors_Depth_Number = "Sensor index. May be used to associate the sensor with other parts of the schema. For example, for Audio sensors, the Channel/SensorNumber can be set to a specific Sensor/Audio/Number, permitting us to determine information about the a hydrophone assembly.";
	public static final String Deployment_Sensors_Depth_SensorId = "A value that uniquely identifies this sensor, e.g. a serial number.";
	public static final String Deployment_Sensors_Depth_Geometry = "Geometry relative to platform";
	public static final String Deployment_Sensors_Depth_Name = "Optional sensor name";
	public static final String Deployment_Sensors_Depth_Description = "Optional description of sensor.";
	public static final String Deployment_Sensors_Sensor_Number = "Sensor index. May be used to associate the sensor with other parts of the schema. For example, for Audio sensors, the Channel/SensorNumber can be set to a specific Sensor/Audio/Number, permitting us to determine information about the a hydrophone assembly.";
	public static final String Deployment_Sensors_Sensor_SensorId = "A value that uniquely identifies this sensor, e.g. a serial number.";
	public static final String Deployment_Sensors_Sensor_Geometry = "Geometry relative to platform";
	public static final String Deployment_Sensors_Sensor_Name = "Optional sensor name";
	public static final String Deployment_Sensors_Sensor_Description = "Optional description of sensor.";
	public static final String Deployment_Sensors_Sensor_Type = "Description of data gathered by this sensor, e.g., temperature";
	public static final String Deployment_Sensors_Sensor_Properties = "List of property elements describing the sensor. These may be arbitrary. Example: Properties can have child Units with value °C. Children may be nested.";
	public static final String Deployment_MetadataInfo_Contact = "based on ISO 19115";
	public static final String Deployment_MetadataInfo_Date = "Last update.";
	public static final String Deployment_MetadataInfo_UpdateFrequency = "How often are these data updated? as-needed, unplanned, or yearly";
	public static final String Deployment_MetadataInfo_Contact_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";


	// Annotations taken from schemata_csv
	public static final String Detections_Id = "Identification string that is unique to all documents of this type (currently optional, will be required in the future)";
	public static final String Detections_Description = "Objectives, abstract and high-level methods.";
	public static final String Detections_DataSource = "Acoustic data identifier.";
	public static final String Detections_Algorithm = "Detailed methods.";
	public static final String Detections_QualityAssurance = "Description of quality assurance checks (if any).";
	public static final String Detections_UserId = "User that submitted the document.";
	public static final String Detections_Effort = "Span and scope of detection effort.";
	public static final String Detections_OnEffort = "Collection of individual detections.";
	public static final String Detections_OffEffort = "Collection of off-effort (ad-hoc) detections. Each detection has the same format as the OnEffort ones.";
	public static final String Detections_MetadataInfo = "Party responsible for this record. Some applications may make this mandatory.";
	public static final String Detections_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Detections_Description_Abstract = "Overview of effort.";
	public static final String Detections_Description_Method = "High-level description of the method used.";
	public static final String Detections_DataSource_EnsembleId = "Serves as a foreign key into the ensembles collection and must match an Id element in an ensemble document. Ensembles are used to group instruments together for a common purpose (e.g. large aperture array).";
	public static final String Detections_DataSource_DeploymentId = "Serves as a foreign key into the Deployments collection and must match an Id element in a deployment document.";
	public static final String Detections_Algorithm_Method = "Text based description of algorithm or citation";
	public static final String Detections_Algorithm_Software = "Name of software that implements the algorithm or supports human analysts. This might be the name of a plug-in or extension module that is part of a larger program or system.";
	public static final String Detections_Algorithm_Version = "Software version identifier";
	public static final String Detections_Algorithm_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Detections_Algorithm_SupportSoftware = "Software required in addition to the algorithm itself, e.g. Matlab, Ishmael, PAMGUARD, Triton, etc.";
	public static final String Detections_Algorithm_SupportSoftware_Version = "Software version identifier.";
	public static final String Detections_Algorithm_SupportSoftware_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Detections_QualityAssurance_Description = "Text based description of process.";
	public static final String Detections_QualityAssurance_ResponsibleParty = "based on ISO 19115";
	public static final String Detections_QualityAssurance_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Detections_QualityAssurance_Description_Abstract = "Overview of effort.";
	public static final String Detections_QualityAssurance_Description_Method = "High-level description of the method used.";
	public static final String Detections_QualityAssurance_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Detections_Effort_Start = "Timestamp indicating the start of systematic effort to find the species and phenomena listed in the Effort/Kind entries.";
	public static final String Detections_Effort_End = "Timestamp indicating end of systematic effort.";
	public static final String Detections_Effort_dBReferenceIntensity_uPa = "All dB measurements are made relative to this value in uPa. Typical values are 1 for underwater acoustics and 20 for terrestrial acoustics.";
	public static final String Detections_Effort_AnalysisGaps_Aperiodic = "Used to describe meaningful gaps in the analysis effort. Problems with the data should not be addressed here, but rather in Deployment/QualityAssurance/Quality. Note that tools may not take Gaps into account when reporting effort statistics.";
	public static final String Detections_Effort_AnalysisGaps_Periodic_Regimen = "Peridoic analysis regimen may change over time. Each entry shows the start of an analysis regimen. The abscence of entries indicates continuous analysis as would having equal values in AnalysisDuration_s and AnalysisInterval_s. The time offsets in these fields are with respect to actual time. Duty cycled data are not taken into account in their specification. As an example, if we analyzed the first 30 min of each hour and the deployment's recording duty cycle were 15 min of recording every 30 min, this analysis duration would only result in 15 min of analysis every hour.";
	public static final String Detections_Effort_AnalysisGaps_Periodic_Regimen_TimeStamp = "Indicates when the regimen becomes active. It remains active until the next Regimen entry.";
	public static final String Detections_Effort_AnalysisGaps_Periodic_Regimen_AnalysisDuration_s = "When analysis starts, the data are analyzed for this many seconds. Optional attribute Offset_s may be used to denote the number of seconds after the timestamp that analysis started. If Offset_s is not present, analysis starts at the Timestamp.";
	public static final String Detections_Effort_AnalysisGaps_Periodic_Regimen_AnalysisInterval_s = "Time between consecutive effort. If AnalysisDuration_s is 1800 s and AnalysisInterval_s is 3600 s, then we perform analysis on the first 30 min of each hour starting at TimeStamp.";
	public static final String Detections_Effort_AnalysisGaps_Aperiodic_Start = "Timestamp indicating the start of a gap in the systematic effort to find the species and phenomena listed in the Effort/Kind entries.";
	public static final String Detections_Effort_AnalysisGaps_Aperiodic_End = "Timestamp indicating end of systematic effort gap.";
	public static final String Detections_Effort_AnalysisGaps_Aperiodic_Reason = "Reason for gap in analysis.";
	public static final String Detections_Effort_Kind_SpeciesId = "Integrated Taxonomic Information System species identifier http://www.itis.gov/ for positive numbers. Negative numbers are used for physical phenomena.";
	public static final String Detections_Effort_Kind_Call = "Name that describes call.";
	public static final String Detections_Effort_Kind_Granularity = "Type of detections: call - individual call, encounter - set of calls, binned - presence detected within period specified by bin size attribute in Effort. grouped – A set of individual detections of any granularity that have been grouped together. Examples include situations such as song or other groupings (e.g. detections of the same animals picked up on multiple instruments). Grouped detections may specify the individual detections regardless of their granularity that are part of the group. This is different from granularities encounter and binned where one might expect multiple calls to occur, but the individual detections are not recorded.";
	public static final String Detections_Effort_Kind_Parameters_Subtype = "subcategory of call";
	public static final String Detections_Effort_Kind_Parameters_FrequencyMeasurements_Hz = "Specifies a list of frequencies at which measurements are made. Each detection for this Kind should have a list of FrequencyMeasurements where each item corresponds to a frequency in this list. Useful for studying ambient sound or soundscapes. Be sure to declare Effort/ReferenceIntensity_uPa.";
	public static final String Detections_OnEffort_Detection_Input_file = "Optional name of audio file (or indirect representation) from which this detection was generated.";
	public static final String Detections_OnEffort_Detection_Start = "Time at which event started. For many detectors, this may not the actual starting time of the event.";
	public static final String Detections_OnEffort_Detection_End = "Optional end time of event.";
	public static final String Detections_OnEffort_Detection_Count = "An optional count of the number of times a call occurred within a bin or across an encounter.";
	public static final String Detections_OnEffort_Detection_Event = "Optional tag for identifying this event uniquely within the stream. For human analysts, it is typical to use the time at which the detection was made in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ). When present, the combination of the event and attributes that uniquely identify the set of detections (or document name) must be uniqe.";
	public static final String Detections_OnEffort_Detection_UnitId = "Specifies ensemble unit (when using an ensemble source).";
	public static final String Detections_OnEffort_Detection_SpeciesId = "Integrated Taxonomic Information System species identifier http://www.itis.gov/ for positive numbers. Negative numbers are used for physical phenomena.";
	public static final String Detections_OnEffort_Detection_Call = "In most cases, the call field should be present. May be omitted if the goal is species detection only, or repeated for multiple types of calls when the granularity effort is not \"call\".";
	public static final String Detections_OnEffort_Detection_Image = "Name of image file (spectrogram, etc.)";
	public static final String Detections_OnEffort_Detection_Audio = "Name of audio file (short snippet)";
	public static final String Detections_OnEffort_Detection_Parameters_Subtype = "subcategory of call";
	public static final String Detections_OnEffort_Detection_Parameters_Score = "Measure from detector, e.g. likelihood ratio, projection, etc.";
	public static final String Detections_OnEffort_Detection_Parameters_Confidence = "Measure of confidence in detection. Range: [0, 1]";
	public static final String Detections_OnEffort_Detection_Parameters_QualityAssurance = "Detection is: unverified, valid, invalid";
	public static final String Detections_OnEffort_Detection_Parameters_ReceivedLevel_dB = "dB relative to reference intensity defined in Effort/ReferenceIntenstiy_uPa";
	public static final String Detections_OnEffort_Detection_Parameters_FrequencyMeasurements_dB = "List of received levels at various frequencies relative to the reference value defiend in Effort/ReferenceIntensity_uPa. The frequency measurements should be consistent for each species and call type and must correspond to a a list of frequencies defined in Effort/Kind/SubType.";
	public static final String Detections_OnEffort_Detection_Parameters_Peaks_Hz = "Typically used for spectra of short echolocation bursts, notes the spectral peaks in a list sorted from low to high frequency.";
	public static final String Detections_OnEffort_Detection_Parameters_Duration_s = "When the call granularity is binned or encounter, this may be used to describe the mean duration of calls during the bout. As an example, at SIO we use this to track the mean duration of binned anthropogenic sources such as explosions.";
	public static final String Detections_OnEffort_Detection_Parameters_Sideband_Hz = "Signal sideband frequencies in a list sorted from low to high frequency.";
	public static final String Detections_OnEffort_Detection_Parameters_EventRef = "Reference to other detections for hierarchical organization.";
	public static final String Detections_OnEffort_Detection_Parameters_UserDefined = "Study specific parameters";
	public static final String Detections_OnEffort_Detection_Parameters_Tonal_Offset_s = "List of offsets from start in seconds";
	public static final String Detections_OnEffort_Detection_Parameters_Tonal_Hz = "Frequency measurement for each Offset_s (Hz). List must be of same length as Offset_s";
	public static final String Detections_OnEffort_Detection_Parameters_Tonal_dB = "Optional intensity measurment (dB) for each Offset_s (dB). List must be of the same length as Offset_s";
	public static final String Detections_OffEffort_Detection_Input_file = "Optional name of audio file (or indirect representation) from which this detection was generated.";
	public static final String Detections_OffEffort_Detection_Start = "Time at which event started. For many detectors, this may not the actual starting time of the event.";
	public static final String Detections_OffEffort_Detection_End = "Optional end time of event.";
	public static final String Detections_OffEffort_Detection_Count = "An optional count of the number of times a call occurred within a bin or across an encounter.";
	public static final String Detections_OffEffort_Detection_Event = "Optional tag for identifying this event uniquely within the stream. For human analysts, it is typical to use the time at which the detection was made in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ). When present, the combination of the event and attributes that uniquely identify the set of detections (or document name) must be uniqe.";
	public static final String Detections_OffEffort_Detection_UnitId = "Specifies ensemble unit (when using an ensemble source).";
	public static final String Detections_OffEffort_Detection_SpeciesId = "Integrated Taxonomic Information System species identifier http://www.itis.gov/ for positive numbers. Negative numbers are used for physical phenomena.";
	public static final String Detections_OffEffort_Detection_Call = "In most cases, the call field should be present. May be omitted if the goal is species detection only, or repeated for multiple types of calls when the granularity effort is not \"call\".";
	public static final String Detections_OffEffort_Detection_Image = "Name of image file (spectrogram, etc.)";
	public static final String Detections_OffEffort_Detection_Audio = "Name of audio file (short snippet)";
	public static final String Detections_OffEffort_Detection_Parameters_Subtype = "subcategory of call";
	public static final String Detections_OffEffort_Detection_Parameters_Score = "Measure from detector, e.g. likelihood ratio, projection, etc.";
	public static final String Detections_OffEffort_Detection_Parameters_Confidence = "Measure of confidence in detection. Range: [0, 1]";
	public static final String Detections_OffEffort_Detection_Parameters_QualityAssurance = "Detection is: unverified, valid, invalid";
	public static final String Detections_OffEffort_Detection_Parameters_ReceivedLevel_dB = "dB relative to reference intensity defined in Effort/ReferenceIntenstiy_uPa";
	public static final String Detections_OffEffort_Detection_Parameters_FrequencyMeasurements_dB = "List of received levels at various frequencies relative to the reference value defiend in Effort/ReferenceIntensity_uPa. The frequency measurements should be consistent for each species and call type and must correspond to a a list of frequencies defined in Effort/Kind/SubType.";
	public static final String Detections_OffEffort_Detection_Parameters_Peaks_Hz = "Typically used for spectra of short echolocation bursts, notes the spectral peaks in a list sorted from low to high frequency.";
	public static final String Detections_OffEffort_Detection_Parameters_Duration_s = "When the call granularity is binned or encounter, this may be used to describe the mean duration of calls during the bout. As an example, at SIO we use this to track the mean duration of binned anthropogenic sources such as explosions.";
	public static final String Detections_OffEffort_Detection_Parameters_Sideband_Hz = "Signal sideband frequencies in a list sorted from low to high frequency.";
	public static final String Detections_OffEffort_Detection_Parameters_EventRef = "Reference to other detections for hierarchical organization.";
	public static final String Detections_OffEffort_Detection_Parameters_UserDefined = "Study specific parameters";
	public static final String Detections_OffEffort_Detection_Parameters_Tonal_Offset_s = "List of offsets from start in seconds";
	public static final String Detections_OffEffort_Detection_Parameters_Tonal_Hz = "Frequency measurement for each Offset_s (Hz). List must be of same length as Offset_s";
	public static final String Detections_OffEffort_Detection_Parameters_Tonal_dB = "Optional intensity measurment (dB) for each Offset_s (dB). List must be of the same length as Offset_s";
	public static final String Detections_MetadataInfo_Contact = "based on ISO 19115";
	public static final String Detections_MetadataInfo_Date = "Last update.";
	public static final String Detections_MetadataInfo_UpdateFrequency = "How often are these data updated? as-needed, unplanned, or yearly";
	public static final String Detections_MetadataInfo_Contact_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";


	// Annotations taken from schemata_csv
	public static final String Ensemble_Id = "Ensemble name (unique identifier).";
	public static final String Ensemble_Unit = "Associates a virtual unit of an ensemble with an actual deployment.";
	public static final String Ensemble_ZeroPosition = "Provides a zero point to which relative localizations can be referenced.";
	public static final String Ensemble_Unit_UnitId = "A unique unit number that identifies this instrument within the ensemble.";
	public static final String Ensemble_Unit_DeploymentId = "Reference to a deployment document Id field. Uniquely identifies the deployment.";
	public static final String Ensemble_ZeroPosition_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Ensemble_ZeroPosition_Latitude = "Expressed in degrees North [-90, 90]";


	// Annotations taken from schemata_csv
	public static final String Localize_Id = "Identification string that is unique to all documents of this type";
	public static final String Localize_Description = "Text based description of process.";
	public static final String Localize_DataSource = "Indicates the deployment or ensemble from which the process (e.g. detector) derived information.";
	public static final String Localize_Algorithm = "Description of an algorithm or process.";
	public static final String Localize_QualityAssurance = "Description of quality assurance checks (if any).";
	public static final String Localize_ResponsibleParty = "Person/organization responsible for generating metadata";
	public static final String Localize_UserId = "User that submitted the document.";
	public static final String Localize_IntermediateData = "Derived data that is used for the localizations that the user wishes to retain.";
	public static final String Localize_MetadataInfo = "Party responsible for this record. Some applications may make this mandatory.";
	public static final String Localize_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Localize_Description_Abstract = "Overview of effort.";
	public static final String Localize_Description_Method = "High-level description of the method used.";
	public static final String Localize_DataSource_EnsembleId = "Serves as a foreign key into the ensembles collection and must match an Id element in an ensemble document. Ensembles are used to group instruments together for a common purpose (e.g. large aperture array).";
	public static final String Localize_DataSource_DeploymentId = "Serves as a foreign key into the Deployments collection and must match an Id element in a deployment document.";
	public static final String Localize_Algorithm_Method = "Text based description of algorithm or citation";
	public static final String Localize_Algorithm_Software = "Name of software that implements the algorithm or supports human analysts. This might be the name of a plug-in or extension module that is part of a larger program or system.";
	public static final String Localize_Algorithm_Version = "Software version identifier";
	public static final String Localize_Algorithm_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Localize_Algorithm_SupportSoftware = "Software required in addition to the algorithm itself, e.g. Matlab, Ishmael, PAMGUARD, Triton, etc.";
	public static final String Localize_Algorithm_SupportSoftware_Version = "Software version identifier.";
	public static final String Localize_Algorithm_SupportSoftware_Parameters = "Structured tags to describe parameters used in algorithm execution.";
	public static final String Localize_QualityAssurance_Description = "Text based description of process.";
	public static final String Localize_QualityAssurance_ResponsibleParty = "based on ISO 19115";
	public static final String Localize_QualityAssurance_Description_Objectives = "What are the objectives of this effort? Examples: Beamform to increase SNR for detection. Detect every click of a rare species. Verify data quality.";
	public static final String Localize_QualityAssurance_Description_Abstract = "Overview of effort.";
	public static final String Localize_QualityAssurance_Description_Method = "High-level description of the method used.";
	public static final String Localize_QualityAssurance_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Localize_ResponsibleParty_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";
	public static final String Localize_Effort_Start = "Time at which we started looking for location information.";
	public static final String Localize_Effort_End = "Time at which we stopped looking for location information.";
	public static final String Localize_Effort_CoordinateSystem = "What type of localization information is produced?";
	public static final String Localize_Effort_LocalizationType = "Type of localization effort: Bearing, Ranging, Point, Track";
	public static final String Localize_Effort_CoordinateSystem_Type = "How are positions represented? WGS84: global positioning system lat/long, cartesian: m relative to a point, UTM: universal trans Mercatur map-projection, Bearing: Polar measurements of angle and possibly distance.";
	public static final String Localize_Effort_CoordinateSystem_Relative = "For bearings, this gives the direction vector for the zero bearing. Angles are measured counter-clockwise to this vector. For cartesian coordinates, this provides the zero point relative to the deployment position or the ReferencePoint of an ensemble.";
	public static final String Localize_Effort_CoordinateSystem_UTM = "Parameters for Universal Trans Mercatur projections. NEED TO INVESTIGATE FURTHER AS TO WHETHER THIS OR ANY OTHER PROJECTION IS WORTH ADDING";
	public static final String Localize_Effort_CoordinateSystem_Relative_Bearing = "Designed to be a subset of OpenGML DirectionVectorType: http://schemas.opengis.net/gml/3.1.1/base/direction.xsd Unlike the OpenGML, direction may not be specified as a vector, and the verticalAngle is optional.";
	public static final String Localize_Effort_CoordinateSystem_Relative_Bearing_HorizontalAngle = "Angle between a reference vector in the horizontal plane [0, 360]";
	public static final String Localize_Effort_CoordinateSystem_Relative_Bearing_VerticalAngle = "Angle between a reference vector in the vertical plane: [-90, 90]";
	public static final String Localize_Effort_CoordinateSystem_UTM_Zone = "NS zone [1-60]. Each zone covers 80°S to 84°N in 6° width zones. Zone 1 180 is 180-186° E with increasing zone #s corresponding to 6° eastward increments.";
	public static final String Localize_Effort_ReferencedDocuments_Document_Type = "What type of document is being referenced? Detections or Localizations";
	public static final String Localize_Effort_ReferencedDocuments_Document_Id = "Unique identifier string for the document being referenced.";
	public static final String Localize_Effort_ReferencedDocuments_Document_Index = "All localizations that wish to reference other detections or localizations from the referenced document should use this index value.";
	public static final String Localize_IntermediateData_Correlations_Correlation_Primary = "Primary hydropphone";
	public static final String Localize_IntermediateData_Correlations_Correlation_Secondary = "Secondary hydropphone";
	public static final String Localize_IntermediateData_Correlations_Correlation_Correlations = "Correlation between detections on primary hydrophone and signals on secondary hydrophones. Each column j is the set of lags corresponding to the j'th detection on the primary hydrophone.";
	public static final String Localize_Localizations_Localization_Event = "Optional tag typically in ISO datetime format YYYY-MM-DDTHH:MM:SSZ identifying this event uniquely within the stream. For human analysts, it is typical to use the time at which the detection was made. When present, the combination of the event and attributes that uniquely identify the set of detections (or document name) must be uniqe.";
	public static final String Localize_Localizations_Localization_TimeStamp = "Time of localization in reference time frame (e.g. time of arrival at primary hydrophone)";
	public static final String Localize_Localizations_Localization_SpeciesId = "Species can be identified by the detections from the detections that are referenced. As these references are not mandatory, the optional SpeciesID can be used to identify the species that produced the localized source.";
	public static final String Localize_Localizations_Localization_QualityAssurance = "Detection is: unverified, valid, invalid";
	public static final String Localize_Localizations_Localization_Bearing = "Direction towards acoustic source.";
	public static final String Localize_Localizations_Localization_Ranging = "Range and direction towards acoustic source. Combine with bearing, rename angular Rename StdError to Error specify in methods/algorrithm";
	public static final String Localize_Localizations_Localization_WGS84 = "Longitude, latitude and possibly elevation of surce.";
	public static final String Localize_Localizations_Localization_Cartesian = "Relative distance to source from receiver zero point.";
	public static final String Localize_Localizations_Localization_Track = "Series of associated positions and timestamps";
	public static final String Localize_Localizations_Localization_References_TimeReferenceEnsembleUnit = "STILL NEEDED? Time references which unit of the ensemble (see TimeReferenceChannel) when ensembles are used.";
	public static final String Localize_Localizations_Localization_References_TimeReferenceChannel = "STILL NEEDED? Events are detected at different times on different channels, making it necessary to provide the instrument and channel on which the timestamp references.";
	public static final String Localize_Localizations_Localization_References_Reference = "Detections/localization used in constructing this localization.";
	public static final String Localize_Localizations_Localization_References_Reference_Index = "Must match instance of Index in ReferencedDocuments. This permits identification of a specific document.";
	public static final String Localize_Localizations_Localization_References_Reference_EventRef = "Event identifier that uniquely identifies a detection or localization within a referenced document.";
	public static final String Localize_Localizations_Localization_Bearing_HorizontalAngle = "Angle between a reference vector in the horizontal plane [0, 360]";
	public static final String Localize_Localizations_Localization_Bearing_VerticalAngle = "Angle between a reference vector in the vertical plane: [-90, 90]";
	public static final String Localize_Localizations_Localization_Bearing_Ambiguous = "Left right horizontal ambiguity about the bearing reference vector exists?";
	public static final String Localize_Localizations_Localization_Bearing_StdError = "Standard error in degrees for the measurement.";
	public static final String Localize_Localizations_Localization_Bearing_StdError_HorizontalAngle = "Angle between a reference vector in the horizontal plane [0, 360]";
	public static final String Localize_Localizations_Localization_Bearing_StdError_VerticalAngle = "Angle between a reference vector in the vertical plane: [-90, 90]";
	public static final String Localize_Localizations_Localization_Ranging_HorizontalAngle = "Angle between a reference vector in the horizontal plane [0, 360]";
	public static final String Localize_Localizations_Localization_Ranging_VerticalAngle = "Angle between a reference vector in the vertical plane: [-90, 90]";
	public static final String Localize_Localizations_Localization_Ranging_Range_m = "Distance to localized animal/object/phenomenon in meters.";
	public static final String Localize_Localizations_Localization_Ranging_Ambiguous = "Left right horizontal ambiguity about the bearing reference vector exists?";
	public static final String Localize_Localizations_Localization_Ranging_StdError_HorizontalAngle = "Angle between a reference vector in the horizontal plane [0, 360]";
	public static final String Localize_Localizations_Localization_Ranging_StdError_VerticalAngle = "Angle between a reference vector in the vertical plane: [-90, 90]";
	public static final String Localize_Localizations_Localization_Ranging_StdError_Range_m = "Distance to localized animal/object/phenomenon in meters.";
	public static final String Localize_Localizations_Localization_WGS84_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Localize_Localizations_Localization_WGS84_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Localize_Localizations_Localization_WGS84_AlternatePosition = "Add LongLat3 and StdError Cartesian";
	public static final String Localize_Localizations_Localization_WGS84_StdError_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Localize_Localizations_Localization_WGS84_StdError_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Localize_Localizations_Localization_Cartesian_BearingIDs = "If multiple bearings were used to create this localization, their ids can be provided.";
	public static final String Localize_Localizations_Localization_Cartesian_Longitude = "Expressed in degrees East [0, 360)";
	public static final String Localize_Localizations_Localization_Cartesian_Latitude = "Expressed in degrees North [-90, 90]";
	public static final String Localize_Localizations_Localization_Cartesian_BearingIDs_EventRef = "Reference to individual bearing within this XML document.";
	public static final String Localize_Localizations_Localization_Track_WGS84 = "Series of points or list of values for each type long/lat/depth/elevation/timestamp parameters for each call: receivedLevel_dB sourceLevel_dB ambient_dB";
	public static final String Localize_Localizations_Localization_Track_Cartesian = "Todo: define Cartesian list";
	public static final String Localize_Localizations_Localization_Track_WGS84_Bounds = "Bounding box for tempo-spatial data from northwest to southeast quadrant. If elevation/depth information is available, separate depth boundaries are given as well. Note that longitudes are always degrees east. When a track crosses 0° east, the northwest longitude will be > than the souteast longitude (e.g. a 2° path from 359°E to 1°E)";
	public static final String Localize_Localizations_Localization_Track_WGS84_Bounds_NorthWest_Longitude = "Longitude is expressed in degrees East [0,360)";
	public static final String Localize_Localizations_Localization_Track_WGS84_Bounds_NorthWest_Latitude = "Expressed in degrees North [-90,90]";
	public static final String Localize_Localizations_Localization_Track_WGS84_Bounds_SouthEast_Longitude = "Longitude is expressed in degrees East [0,360)";
	public static final String Localize_Localizations_Localization_Track_WGS84_Bounds_SouthEast_Latitude = "Expressed in degrees North [-90,90]";
	public static final String Localize_MetadataInfo_Contact = "based on ISO 19115";
	public static final String Localize_MetadataInfo_Date = "Last update.";
	public static final String Localize_MetadataInfo_UpdateFrequency = "How often are these data updated? as-needed, unplanned, or yearly";
	public static final String Localize_MetadataInfo_Contact_contactInfo_onlineResource = "We do not fully conform to the onlineResources of ISO 19115";}
