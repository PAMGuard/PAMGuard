# CPOD module help

## Introduction  
CPODs and FPODs are click data loggers widely used in research and indstry developed by [Chelonia Ltd](www.chelonia.co.uk). PAMGuard's CPOD module allows users to import CPOD _and_ FPOD data into PAMGuard so it can be viewed using PAMGuard's visualisation tools. A common use case for this module is to display both CPOD/FPOD data and a lower frequency recording device together - for example a typical PAM setup is to use a CPOD to record high frequency echolocation clicks then a lower frequency recorder (e.g. 96Khz sample rate) to record dolphin whisltes, noise, fish sounds etc.

## Quick overview of CPOD/FPOD data
CPODs and FPODs do not record raw audio - they run a simple on board click detector and then save some basic metrics on each detected click e.g. peak frequency, bandwidth, end frequency etc. FPODs, the successor fo CPODs, also record a little extra waveform data on a small subset of detected clicks. Once a CPOD or FPOD has been recovered, it is processed using CPOD.exe or FPOD.exe software respectivly which runs a click train classifier. The click train classifier extracts sequences of successive clicks that are lilely from the same source, e.g a dolphin, porpoise or echosounder. It then assigns a likely species to a click train or classes it as unknown. The outputs form th click trian detector are saved to a CP3 file which is essentially a file that contains only clicks that have been assigned to click trains. 

## Adding the CPOD module
To add the CPOD module go to **_File->Add Modules->Sensors->CPOD_**. The module requires the binary storage module in PAMGuard **_File-> Add modules->Utilities->Binary File_**. Once both the CPOD and Binary file storage modules have been added open the CPOD settings using **_Settings->CPOD importer_**. 

## Importing CPOD/FPOD data
The module has three possible modes of importing data 
- Import raw detection data i.e. CP1 or FP1 data. PAMGuard will display the CPOD detection but no click train ID data is available.
- Import just the click trains data i.e. CP3 or FP3 files - PAMGuard will only import clicks which are part of click trains
- Import both both raw and click train data (recomended). PAMGuard imports the raw detections then uses the click train data to assign detections to click trains.

Users can use the file button to select a single file (e.g. an FP3 file) or the folder button to select a folder of CPOD/FPOD files. If the folder button is used and there are both CP1/FP1 (detections) and CP3/FP3 (click trains) files then PAMGuard will automtically load all files and assign detections to click trains. Once files have imported select **_Import_** and the data will be imported into PAMGuard - note this can take some time, epsecially if importing CP1/FP1 files. 

## Visualising CPOD/FPOD data
CPOD data is shown in PAMGurd's data map which shows a datagram similar to the click detector. The datgram shows the frequeny density of CPOD/FPOD clicks constructed from the peak frequency paratmeter fo each click The datagram can be useful for quicly navigating to sections of data that may contain porpoises and/or dolphins

## Exporting CPOD/FPOD data
CPOD and FPOD data can be exported to .RData and .mat using PAMGuard's exporter. The fields saved by the exporter are the same as a standard PAMGuard detection (see exporter help). The additonal fields for CPODs and FPODs are 

 - bandwidth: the bandwidth of the click in Hz
 - numcycles: the number of cycles fo the click in Hz
 - peakfreq: the peak frequency of the click in Hz
 - endfreq: the end frequency of the click in Hz
 - SPL: the CPOD measure of sound pressure level which is an integer between 0 and 255. 
 - slope: the slope which is a paramter measured by the CPOD and FPOD.
 - wave: this will be empty for most clicks but some clicks from FPODs will have a waveform. Note that this is reconstructed from zero corssings and is NOT a clip from the raw sound data.
 - species: if the CPOD is part of a click train then species will be 0 for UNKNOWN, 1 for NBHF, 2 for DOLPHIN and 3 for SONAR. -1 indicates a click is not part of a click train. 


