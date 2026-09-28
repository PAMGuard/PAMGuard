# PAMGuard Viewer

## Create binary files offline

The Viewer funtion is new so you probably did not include binary storage module when you collected your data. Also what about older data sets you would like to analyse now with viewer. This brief description shows you the necessary steps to process your data so you can create binary files offline so you can use [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html).   
  
First you have collected [Rainbow Click Files](../../../detectors/clickDetectorHelp/docs/offline_ImportingRBC.html). These can be imported and converted directly.   
  
Secondly, you have collected continous recordings (.wav files) but no binaries. You need to process your .wav files offline to create binaries. Open up PAMGuard (online) and add the following modules:[Sound Acquisition](../../../sound_processing/AcquisitionHelp/docs/AcquisitionOverview.html), [Click detector](../../../ detectors/clickDetectorHelp/docs/ClickDetector_clickDetector.html), [Database](../../../utilities/generalDatabaseHelp/docs/database_database.html) and [Binary Stoarage](../../../utilities/BinaryStore/docs/binarystore_overview.html). If you are not interested in clicks but whistles the same principle applies. You just have to add the [whistle & moan detector](../../../detectors/whistleMoanHelp/docs/whistleMoan_Overview.html). Noise measurments can also be processed this way. 

### Sound Acquisition

Go to **_Detection>Sound Aquistion_**. Choose either _'Audio file'_ or _'Audio file folder or multiple files'_ in the Data Dource Type dropdown list. Choose the appropiate folder where your file/files are stored. Make sure the box _'include subfolders is ticked'_ so all files get processed. Check that the box _'Repeat: Ant end of file list, start again'_ is NOT ticked. Press ok.   
  
  
  


### Database selection

Before you select your database, you have to create a blank database you can use. If you collected GPS data during the data collection you can import that table in your blank database. Make sure it is called gpsData and is in the right [GPS format](../../../detectors/clickDetectorHelp/docs/offline_AddingGPSData.html). You can also add your GPS table after you processed the data.   
Go to **_File>database selection_** and select the appropiate data base. 

### Binary Storage

You have to select a folder where the binaries are going to be stored. Go to Go to **_File>Binary Storage options_** for your selection. 

### Click Detector

Set up the [Click detector](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickDetector.html) for your purposes. You can change some paramters again in Viewer mode with the option [Reanalyse click types](../../../detectors/clickDetectorHelp/docs/offline_Reanalyse_click_types.html) but as it it time intensive you should set it up correctly.   
  
After you set up everything you might as well save the .psfx file so you have a template for the future. The data model should look like this. You can also add some notch [filters](../../../sound_processing/FiltersHelp/Docs/Filters_filters.html) beforehand if the data is really noisy.   
  
  
  


The next step is to start processing the data. Go to **_Detection>Start_**. Now the .wav files are processed and binary files are created. This can be quite time consuming depending on how much data you process and how fast your computer is. It could take a couple of hours to a couple of days. PAMGuard will tell you approximatley when it is finishing, which file it is processing and how fast real time it is processing (RT). After it is finished you can open up [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html).

---

# Binary Storage

## Overview

The database is not well suited for all types of data, particularly those such as clicks or whistle contours which have a varying length. The binary storage system stores PAMGuard output in a proprietary data format which is simple and efficient and can handle any type of data.

Data stored using the binary store can be reviewed with the [PAMGuard viewer](../../../overview/PamMasterHelp/docs/viewerMode.html). It is also possible to open the binary files using your own software written in other programming languages such as Matlab or R.

### Adding a Binary Storage module

From the **_File>Add Modules>Utilities_** menu select _'Binary Storage'_ to add binary storage to your configuration.

### Configuring the Binary Store

To configure the binary store, go to the **_File>Binary Storage_**... menu to open the Binary Storage dialog. 

  
  


Select the folder where you want to store the data and optionally set the other three options.

Storing data in folders arranged by date can help with data backup and archiving.

Some PAMGuard detectors make additional measures of background noise. These can be stored either with the binary data in the pgdf files or in separate pgnf files (see below). The former has the advantage of creating fewer files, the latter may make it easier to load and process noise data (e.g. to assess detector performance) in [R or Matlab](matlabandr.html).

Automatically starting new files is important in order that individual files do not get excessively large. 

The binary store can start new files at fixed time intervals, when files reach a certain size or both (i.e. which ever condition occurs first).

### Output Files

Output files are identified by a long name which contains the module type, the module name, the datablock name, the date and the time. e.g. Clicks from a [Click Detector](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickDetector.html) called "Porpoise Detector" will have a name like "Click_Detector_Porpoise_Detector_Clicks_20100331_142400.pgdf"

#### pgdf Files

pgdf files are the main files which contain the binary data. All pgdf files have a common header and footer structure (i.e. the data at the start and at the end of each file). Individual units of data are however different for each module.

pgdf files can contain more than one type of data. In particular, some detectors also include regular measurements of background noise in their binary output.

#### pgnf Files

Some detectors (currently the [Click detector](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickDetector.html) the [Whistle and Moan detector](../../../detectors/whistleMoanHelp/docs/whistleMoan_Overview.html) and the [GPL detector](../../../detectors/gpl/docs/gpldetector.html)) make and store measures of background noise. These can be important in assessing detector performance. The background data can either be stored with the detection data in the pgdf files or written to separate pgnf files. In either case, the stored data are the same, it's a choice of fewer files or whether you want to be able to access noise data relatively quickly without having to load all binary data as well.

#### pgdx Files

pgdx files are indexing files. They contain headers and footers which are identical to those in the pgdf files, but no data. pgdx files speed up indexing processes at the start of a PAMGuard viewer session.

#### psfx Files

psfx files contain PAMGuard configuration data. These are basically the same data are as stored in the standard PAMGuard configuration files, but wrapped with a date and other information.

One of these is written each time PAMGuard is started, providing a permanent record of PAMGuard settings. psfx files can be loaded into the PAMGuard Viewer from the viewer [data map](../../../utilities/datamap/docs/datamap.html) or reloaded as with any other configuration file. 

  
  
  


[Next: Matlab, R, and Python interfaces](matlabandr.html)

---

# Binary Storage

## Updating the Binary Store

Occasionally, we have to make format changes to the binary storage system. This is usually due to new features in some of the modules, but occasionally we also make changes to the main file structure. 

We've done everything we can to keep PAMGuard backwards compatible with older versions, and are not aware of any backwards compatibility issues. 

However, the [Matlab, R, and Python libraries](matlabandr.html), that can read binary files are only maintained for the latest PAMGuard file versions. 

If you're using these libraries with old PAMGuard data, or a data that mixes old and new data file formats, you're going to run into difficulties. 

You can update all data for a PAMGuard project in [Viewer mode](../../../overview/PamMasterHelp/docs/viewerMode.html) so that's it's in the latest format and will be compatible with the external libraries. 

Use the **File > Binary Store > Update Old Binary Files ...** menu to open the update dialog. 

  


This will show options to either overwrite existing data, or write data to a new folder, and a list of data outputs that use binary storage. 

Select the options you want, and press Start. For most datasets, this will only take a few minutes. The first time you open the new dataset with [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html), PAMGuard may need to recreate the [Data Map](../../../utilities/datamap/docs/datamap.html), which will again take a few minutes.

  


[Previous: Matlab, R, and Python interfaces](matlabandr.html)

---

# Binary Storage

## R, Matlab, and Python

While the [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html) provides a powerful tool for reviewing data offline, many researchers want to wrte their own bespoke algorithms to further analyse PAMGuard detections or to plot and view the data in ways not available within the viewer. 

Although PAMGuard is fully open source, modifying the Java source code requires considerable programming expertise and is not for everyone. The PAMGuard team and our collaborators have therefore made available library functions for the [R](https://www.r-project.org/), [Matlab](https://uk.mathworks.com/products/matlab.html), and [Python](https://www.python.org/) programming languages, which allow users to unpack binary files into those programming environments for further bespoke analysis. 

All libraries are freely available. For further information on how to install these libraries, and tutorials on how to use them, please refer to the [PAMGuard Website](https://www.pamguard.org/matlabandr.html).

  


[Previous: Binary store overview](binarystore_overview.html)

[Next: Binary store update](binarystore_update.html)
