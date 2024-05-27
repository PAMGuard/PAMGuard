# PAMGuard
PAMGuard is a bioacoustics analysis program designed for use in real time research contexts and for the processing of large datasets. PAMGuard provides users access to a suite of state-of-the-art auotmated analysis algorithms alongside displays for visualisation data and a comprehensive data management systems. 

# Why do we need PAMGuard?
PAMGuard fufills two main requirements within marine bioacoustics

1) **Real time operation** - Almost all PAMGuard features and modules work in real time - this allows scientists and industry to detect, classify and loclaise animals in real time on a standard consumer laptop, enabling mitigations and research survey without expensive bespoke software solutions and the transparncy of open source software.  

2) **Processing and visuslisation of large datasets** - 

 
## Installation
PAMGuard is available on Windows and can be downloaded from the [PAMGuard website](www.pamguard.org). Note that we are considering MacOS installers but they are not available at this time. 

## Tutorial
PAMGuard is a modular program with two modes; real-time and viewer. Typically a user will start with real-time model, either in the field collecting data or post processing sound files from a recorder. Once data are processed, users move on to viewer mode where data can be explored and further processed. 

Upon opening PAMGuard for the first time you are greeted with a blank screen. You must add a series of modules to create the desired acosutic workflow. For example if processing sound files then first add the Sound Acquisition module **_File->Add Modules->Sound Processing->Sound Acquisition_**. Then add the desired detection algorothms e.g.  **_File->Add Modules->Detector->Click Detectors_**. Some modules (such as the click detector) have their own displays, others are added to more generalised displays. For example, the whislte and moan detector module shows detections on a spectrgram display. First add a new tab using  **_File->Add Modules->Displays->User Display**. Click on the user display tab and then from the top menu select **_User display-> New Spectrgram_**. Right click on the added spectrgram and select whistle and moan contours to show whistle detections overlaid on the raw spectrgram. 

Make sure to add the database and binary file storage modules **_File->Add Modules->Utilities->..._**) to save data then press the run button (red button) and data will process. PAMGuard can handle huge datasets so runing might take hours or even days. Progress is shown on the bottom of the screen. 

## Features

### Hardware integration
PAMGuard connects with hardware such as various GPS and AIS systems and a multitude of different sound cards (e.g. [National Instruments](www.ni.com) devices, [SAIL DAQ cards](www.smruconsulting.com/contact-us), almost all ASIO sound cards and standard computer sound cards) for real time data collection and processing. PAMGuard also works with some very bespoke hardware such as [DIFAR Sonobuoys]();

### Real time operation
PAMGuard takes advanatge of multi-core processors to run multiple signal processing automatic analysis algorithms in real time to detect whales, dolphins, bats etc. Data are shown in different displayes, including interactive spectrograms and maps. You might be using PAMGuard for simply viewing a spectrgram and making recordings or running deep learning algorithms for multiple species and loclaising the results to view locations on a map. Whatever acosutic workflow a user creates, PAMGuard can run it in real time. 

### Support for compressed audio
PAMGuard supports processing audio data from standard files (e.g. wav, aif) and also compressed files (e.g. .flac and .sud). Notew that sud files are created on SoundTraps widely used marine recorders and can be read by PAMGuard without decompressing - PAMGuard will automtically import click detections if present in sud files. PAMGuard also supports importing detection data from CPODs and FPODs. 

### Comprehensive data management system
PAMGuard is designed to collect/process data from large acosutic datasets. PAMGuard stores data in an SQLite databases and "Binary" files. The database stores important metadata such as when data has been processed and some low volume data streams such as GPS. Binary files are not human readbale but efficient to access - PAMGuard stores detection data (e.g. clicks, whistles, noise, etc) in these files. this allows PAMGuard to rapidly access data from large datasets. Data from binary files can be viewed in PAMGuard viewer mode or can be exported to MATLAB using the PAMGuard-MATLAB library or the exported to R using the R PAMBinaries package. 

### Access to detection and classification algorithms
PAMGuard allows users to inegrate automated detection and classification algorithms directly into their acosutic workflow. There are a multitude of differwent algorothms to choose from, including a basic click detector, whislte and moan detector, GPL detector, click train detectors and many others. The idea behind PAMGuard is allow researchers to access open source state-of-the-art algorithms devleoped within the scientific community - if you want to contribute and get your algorithm into PAMGuard get in touch. 

###Localisation
PAMGuard has a mutltude of different options for acoustic loclaisation. There's a comprehesnive beam forming module for beam forming arrays, a large aperture localiser for 3D loclaisation and target motion analysis for towed hydrophone arrays. 

###Soundscape analysis
PAMGuard has a noise band (which supports third octave noise bands) and long term spectral average module for soundscape analysis. 

### GIS
Almsot all detection data can be visualised on a map. PAMGaurd also supports plotting GPS and AIS data. 

### Suite of data visualisation tools
An important aspect of PAMGuard is the ability for users to explore porcessed data. This is 

### Advanced manual annotation
The displays within PAMGuard support a variety of manual annottion tools. A simple spectrogram 

### Deep learning integration

### Meatadata standard and Tethys compatibility

## Feature roadmap
There's lots of features we would like to add to PAMGuard. If you want to add a feature you can either code it up yourself in Java and submit a pull request or get in touch with us to discuss how to it might be integrated. Some smaller features might be in our roadmap anyway but larger features usually require funding. Some features we are thinking about (but do not necassarily have time for yet) are;

* Support for decidecade noise bands (base 10 filter bank) in noise band monitor to meet Euopean standards
* Capabaility to export data directly from PAMGaurd e.g. as MAT files.
* Automated test suite to make releases more stable. Note that unit and integration tests are also being slowly incorporated. 

## Development 
This is the main code repository for the PAMGuard software and was created on 7 January 2022 from a [sourceforge SVN repository](https://sourceforge.net/p/pamguard/svn/HEAD/tree/) revision r6278.

If you are a PAMGuard developer, you should clone and branch this repository and share with any collaborators in your own workspace. When your work is ready, contact the PAMGuard team to have your changes merged back into this repo.

PAMGuard uses Maven as build tool. 

# Organisation and License
PAMGuard is open source under an MIT license. It is currently primarily managed by the Sea Mammal Research Unit within the [University of St Andrews](https://www.st-andrews.ac.uk/). Please get in touch if you have any questions. 
