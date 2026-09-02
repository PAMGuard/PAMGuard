# Asio Sound Cards

Many sound cards are provided with two sets of drivers, the 'normal ones' which make the card operate using the normal system sound card drivers (see [Sound Cards](AcquisitionSoundCard.html)) and ASIO drivers which were developed by [Steinberg](http://www.steinberg.net/en/home.html) for professional studio applications.

The advantages of using ASIO drivers are

  * They support more than two input channels.
  * They don't perform unnecessary sample rate conversions.
  * They don't mess with your audio data in any way whatsoever.



  


## Setting up an Asio sound card

In the data acquisition dialog select "Asio Sound Card" as the Data Source Type and a list of available Asio sound cards will appear in the drop down list under the title "Select Audio Line". Select the card you with to use. 

### Channel Configuration

Asio sound cards often support data acquisition from more than two channels.

The number of available channels (and the quality of the data) will of course still vary from card to card.

Another very important feature is that you can define which channels you want to read out by mapping the hardware channels on the device to software channels in PAMGuard

When you select an Asio sound card, the 'Sampling' section of the acquisition dialog will expand to show a set of drop down boxes, the bumber of boxes corresponding to the number of channels you are reading out:

For each software channel, select the hardware channel you wish to use.

**Note that PAMGuard channel numbering starts at 0. Your hardware channel numbering may start at 1. So be aware. If you've put a plug into socket 1, you probably want to select channel 0, etc.**

  


[Previous: Sound Cards](AcquisitionSoundCard.html)

[Next: Audio Files](AcquisitionFile.html)

---

# Sound Acquisition Configuration

To access the module's settings, select _**Detection>Sound Acquisition...**_ from the main menu and click on the name of your module. This will bring up an Audio Data Acquisition dialog similar to the one shown below. 

  


The dialog is divided into four separate regions. the information in each region may vary depending on the type of input device you are using.

### Data Source Type

From the drop down list, select the type of input device you wish to use. Different input devices offer different numbers of channels, sample rates and quality of data. For many applications using just two channels of data in the human audio band, a standard sound card is adequate, but for higher sample rates, or higher numbers of channels, ASIO sound cards or National Instruments Data Acquisition devices may be required

Note that not all input types are supported on all platforms.  


Data Source Type | Windows | Mac | Linux | Restrictions / Notes  
---|---|---|---|---  
[Sound Card](AcquisitionSoundCard.html) | Yes | Yes | Yes |  Maximum of two channels.  
Considerable variation in quality between devices   
[ASIO Sound Card](AcquisitionAsioSoundCard.html) | Yes | Not yet | No | Some cards allow sampling on > 2 channels  
[Audio File](AcquisitionFile.html) | Yes | Yes | Yes | Analyse archived data. You should consider using [Mixed Mode](..\\..\\..\\overview\\PamMasterHelp\\docs\\mixedMode.html) for this if you with to correctly localise sounds relative to GPS data.   
[Audio File folder  
or multiple files ](AcquisitionFileFolder.html) | Yes | Yes | Yes | Batch process many files.  
[National Instruments DAQ Cards](AcquisitionNIDAQ.html) | Yes | No | No | Support for multiple channels at high sample rates  
[SAIL Daq Cards](SAILDaqCard.html) | Yes | No | No | USB input, high speed sampling on up to 12 channels (four per card).  
[Simulated Sound Sources](AcquisitionSimulator.html) | Yes | Yes | Yes | Simulated sources are useful for training, testing and developing new modules  
  
### Select audio line (or specific input device or file)

The information displayed here will vary depending on the type of Data Source. Please refer to instructions on specific data source configuration.

### Sampling

The information displayed here will vary depending on the type of Data Source. Please refer to instructions on specific data source configuration.

### Calibration

The peak to peak voltage range of the device and the preamplifier gain are used by some PAMGuard modules to calculate absolute received signal levels in dB re. 1μ Pa. For some devices, this information may be filled in automatically, for others you will need to enter it yourself. If you don't know the input sensitivity of your device, don't worry, PAMGuard will run, but amplitudes may not be accurate.

If your sound acquisition system has a DC offset in it (many do), tick the 'Subtract DC ...' button and enter a time constant. A single pole high pass filter will then be applied to all incoming data.

(Note that the 'Bandwidth' field has been removed since it was not used in any way)

  


[Previous: Acquisition Overview](AcquisitionOverview.html)

[Next: Sound Cards](AcquisitionSoundCard.html)

---

# Audio Files

As well as analysing data from sound cards in real time, PAMGuard can be used to analyse archived data from audio filed in [WAV](https://en.wikipedia.org/wiki/WAV), [AIF](https://en.wikipedia.org/wiki/Audio_Interchange_File_Format), [FLAC](https://en.wikipedia.org/wiki/FLAC), or [SUD](sudfiles.html) file format.

  


## Setting up an Audio File

In the data acquisition dialog select "Audio File" as the Data Source Type

The dialog will then show a panel where you can select a file

the sample rate and the number of channels in the 'Sampling' section of the dialog will be disabled since these values will be read from the header of the sound file. 

### Processing Speed and listening while you analyse

If no [Sound Playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module is included in the PAMGuard configuration, or if no channels are selected in the [Sound Playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module, then file data will be analysed as fast as possible, which could be considerably faster than real time.

If a [Sound playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module is present and is in use, then analysis will run in real time and you will be able to hear the sounds being played back from the computers sound card

. 

If the repeat button is selected, as soon as processing of the file is somplete, processing will start again at the beginning of the file. This feature can be useful when using PAMGuard to display sounds during public events, training, etc.

### Dates and Times

If possible, the data and time will be read from the file name.

PAMGuard will strip text from the beginning and end of the file name and then attempt to match the remaining characters to a number of different date templates.

During analysis, all times displayed on the screen and all times written to the database and binary storage will be based on the time extracted from the file name and the sample number within the file.

If a valid date cannot be extracted from the file name, then times will be based on the time at which analysis starts and the sample number within the file.

Ideally the file times should be in UTC. If they are not, then the time zone of the file data can be [set from the button to the right of the displayed file time](FileTimeZone.html).

Supported date templates are:   
"yyyy.MM.dd_HH.mm.ss"   
"yyyy.MM.dd-HH.mm.ss"   
"yyyyMMdd_HHmmss"   
"yyyyMMdd-HHmmss"   
"yy.MM.dd_HH.mm.ss"   
"yy.MM.dd-HH.mm.ss"   
"yyMMdd_HHmmss"   
"yyMMdd-HHmmss"   
"yyyy.MM.dd_HH.mm"   
"yyyy.MM.dd-HH.mm"   
"yyyyMMdd_HHmm"   
"yyyyMMdd-HHmm"   
"yy.MM.dd_HH.mm"   
"yy.MM.dd-HH.mm"   
"yyMMdd_HHmm"   
"yyMMdd-HHmm"   
"yy.DDD_HH.mm.ss"   
"yy.DDD-HH.mm.ss"   
"yyDDD_HHmmss"   
"yyDDD-HHmmss"   
"yy.DDD_HH.mm"   
"yy.DDD-HH.mm"   
"yyDDD_HHmm"   
"yyDDD-HHmm" 

PAMGuard can automatically understand a wide variety of common date formats used in file names. However, it's not possible to handle every way that people have thought up of representing dates and the system is confused by additional numbers in file names. Look [here](FileTimeZone.html) to see how you can define your own file name formats.

  


[Previous: ASIO Sound Cards](AcquisitionAsioSoundCard.html)

[Next: Audio File Folders](AcquisitionFileFolder.html)

---

# Audio File Folders

If you've a whole folder (or many folders) full or archived data than you wish to re-analyse, then you can set PAMGuard up to work it's way through the whole lot in one go

Processing speed and dates and times operate in the same way as for [single audio file analysis](AcquisitionFile.html)

  


## Setting up Audio File Folders

### Select and entire folder

You can select an entire folder by pressing the "Select Folder of Files" button and browsing for the folder containing the files you want to analyse.

#### Sub folders

If you want to analyse multiple sub folders of data, then check the 'Include sub folders' box.

### Select multiple files

If you don't want to select all files in a folder, then navigate into the folder and highlight just file files you want to analyse.

### Merging files

Files will be analysed in alphabetical order. It is not uncommon that a single recording session will have been broken into many short files. You can have PAMGuard merge files which are part of continuous recording together by checking the 'Merge contiguous files' box. If this box is not checked, then PAMGuard will stop at the end of each file and then restart on the next file.

### Time and Date

During analysis, all times displayed on the screen and all times written to the database or binary storage will be based on the time extracted from the file name and the sample number within the file.

If a valid date cannot be extracted from the file name, then times will be based on the time at which analysis starts and the sample number within the files.

Ideally the file times should be in UTC. If they are not, then the time zone of the file data can be [set from the button to the right of the displayed file time](FileTimeZone.html).

  


[Previous: Audio Files](AcquisitionFile.html)

[Next: National Instruments DAQ Cards](AcquisitionNIDAQ.html)

---

# Audio File Folders

If you've a whole folder (or many folders) full or archived data than you wish to re-analyse, then you can set PAMGuard up to work it's way through the whole lot in one go

Processing speed and dates and times operate in the same way as for [single audio file analysis](AcquisitionFile.html)

  


## Setting up Audio File Folders

### Select an entire folder

You can select an entire folder by pressing the "Select Folder or Files" button and browsing for the folder containing the files you want to analyse.

#### Sub folders

If you want to analyse multiple sub folders of data, then check the 'Include sub folders' box

#### Repeating the List

In order to have PAMGuard automatically restart processing from the beginning of the first audio file when the end of the last audio file is reached, check the 'repeat' box.

### Select multiple files

If you don't want to select all files in a folder, then navigate into the folder and highlight just the files you want to analyse.

### Merging files

Files will be analysed in alphabetical order. It is not uncommon that a single recording session will have been broken into many short files. You can have PAMGuard merge files which are part of continuous recording together by checking the 'Merge contiguous files' box. If this box is not checked, then PAMGuard will stop at the end of each file and then restart on the next file.

### Time and Date

During analysis, all times displayed on the screen and all times written to the database or binary storage will be based on the time extracted from the file name and the sample number within the file.

If a valid date cannot be extracted from the file name, then times will be based on the time at which analysis starts and the sample number within the files.

PAMGuard can automatically understand a wide variety of common date formats used in file names. However, it's not possible to handle every way that people have thought up of representing dates and the system is confused by additional numbers in file names. Look [here](FileTimeZone.html) to see how you can define your own file name formats.

Ideally the file times should be in UTC. If they are not, then the time zone of the file data can be [set from the button to the right of the displayed file time](FileTimeZone.html).

### Starting/Stopping/Restarting Analysis

During batch processing, when PAMGuard reaches the end of one audio file it will automatically begin processing the next file.  In the event that an audio file cannot be properly loaded, PAMGuard will move on to the next audio filerather than halt processing .  Similarly if the user stops processing by manually pressing the Stop button, upon restart PAMGuard will begin processing the next audio file in the list.  

If the user wishes to restart batch processing at the beginning of the list, either after manually halting processing or upon reaching the end of the last audio file, the Sound Acquisition settings dialog must be opened.  When the OK button is pressed, the list is reinitialized and processing will proceed from the beginning.

  


[Previous: Audio Files](AcquisitionFile.html)

[Next: National Instruments DAQ Cards](AcquisitionNIDAQ.html)

---

# National Instruments DAQ Cards

[National Instruments](http://www.ni.com) Data Acquisition devices offer PAMGuard users the highest sample rates and the highest numbers of channels.

PAMGuard will work with M-series DAQ cards. In principle, any M-series card should work with PAMGuard, but the software has so far only been tested with PCI-6250 and USB-6251 devices.

If you plan to use a different device, you are strongly advised to install the National Instruments software (available from the [National Instruments web site](http://www.ni.com)) and set up and test a simulated card prior to purchase. While this is not a 100% guarantee that the card will work with PAMGuard, if the simulated device doesn't work, it's very unlikely that the real one will. The PAMGuard team welcome any information regarding success or failure with other devices. 

  


## Setting up National Instruments Cards

National Instruments cards and software drivers should be installed according to the instructions provided by National Instruments. The National Instruments software should be installed before starting PAMGuard

Select "National Instruments DAQ Cards" in the drop down list at the top of the Audio Data Acquisition dialog. You can then configure the card using the sound acquisition dialog shown below.

### Using a single National Instruments device

Make sure that "Use Multiple DAQ boards" is not selected and choose the device you wish to use from the "Master Device" drop down list (there may be several National Instruments devices installed on a single system).

#### Terminal Configuration

The terminal configuration you select will depend on how your hydrophone and pre-amplifiers have been connected to the National Instruments device. Detailed information on the four available configurations and how to use them is given in the National Instruments help files which should be installed on your system.

  
Terminal Configuration | Overview  
---|---  
Referenced single ended | Signals are referenced to a common ground (the AIGND) terminal  
Non-referenced single ended | Signals are referenced to a common terminal (AISENSE) which is not necessarily the same as ground  
Differential | Differential input signals are usedNB. This may halve the number of available channels  
Pseudo Differential | Differential input, but one side of the differential input is tied to ground via a resistor  
  
### Sample rate and number of channels

Set the sample rate in Hz and the number of channels. This must be within the limits of the device. With most devices, the sample rate must be reduced for higher channel numbers, e.g. a PCI-6250 device can sample two channels at 500 kHz per channels but can only sample four channels at 250 kHz per channel.

### Hardware channels

Select the hardware channels you wish to read. Note that it's not possible to read the same channel more than once.

Whichever hardware channels are selected, software channel numbering in PAMGuard will always be 0, 1, 2, etc...

### Range

The drop down list of ranges for each channel will be populated with a list of available input ranged for the selected device.

Select the range you wish to use. Note that the Peak to peak voltage range in the Calibration panel at the bottom of the dialog will automatically set to the range of the first channel. Values used in calculations of sound pressure levels in PAMGuard will use the correct values for each channel.

If the range list is empty, then it is likely that the device you have selected is not working correctly

### Using multiple National Instruments devices

If you require more channels or higher sample rates than can be achieved using a single National Instruments device, it is possible to simultaneously sample from multiple devices. e.g. you could use two USB-6251 devices to sample four channels at 500 kHz sample rate per channel. In principle, you can use as many different devices as you like. In practice you will be limited by the number of PCI slots and USB ports on your computer and by the rate at which PAMGuard can process the data

To use multiple devices, check the "Use multiple DAQ boards" box on the dialog. The 'Master Device' selection will no longer be available and individual devices should be selected as shown below.

#### Device and Channel Ordering

All Channels on each device must me grouped together as shown in the above example which reads channels 0 and 1 on Dev4, then channels 0 and 1 on Dev7. Reading Dev4 Channel 0 / Dev 7 Channel 0 / Dev 4 Channel 1 / Dev 7 Channel 1 is would not be allowed.

#### IMPORTANT Timing information when using multiple devices

When using a single device all timing (i.e. how often the device acquires samples) is taken from an internal clock in that one device. These are generally very accurate and the number of samples you expect to acquire in a given time interval will be very close to that time interval x the sample rate. However, if multiple devices are used, and each device acquires samples based on it's own internal clock, the exact number of samples acquired by each device will gradually differ. For example, even if the clocks were 0.0001% accurate, after just two seconds sampling at 500 kHz sample rate, one device would have acquired one more sample than the other device. Small timing differences based on sample counts between signals arriving on multiple channels would therefore rapidly become meaningless.

When using multiple devices, the internal clock in the first (or master) device is therefore sent to the other devices which use the master clock signal in preference to their own internal clock.

**You must physically connect the clock signal from the master device to the other devices yourself: Using a single wire, connect the PFI 1 terminal of the master device to the PFI 1 terminals of all the other devices (yes, it is PFI 1 to PFI 1).**

If you fail to do this, the other devices will not acquire data and you will continually receive the message "NIDaq Error DAQmxReadAnalogF64 code -200284 Measurements: Some or all of the samples requested have not yet been acquired ..." 

  


[Previous: Audio File Folders](AcquisitionFileFolder.html)

[Next: SAIL Daq Cards](SAILDaqCard.html)

---

# Sound Acquisition Overview

PAMGuard is designed to analyse data in real time using a sound card or other data acquisition device or from previously recorded data stored as a hard disk recording. 

### Adding a Sound Acquisition Module

Add a Sound Acquisition module using _**File>Add Modules>Sound Processing>Sound Acquisition**_ and enter a name of your choice. 

### Using Multiple Sound Acquisition Modules

It is possible to have multiple sound acquisition modules, which is particularly useful if you want to run detectors at different sample rates. 

  
  


[Next: Acquisition Configuration](AcquisitionConfiguration.html)

---

# Simulated Sources

PAMGuard can simulate data for a variety of different sound types from one or multiple sources.

Simulated data are useful in training and are can be used by developers to test detection and localisation algorithms.

  


## Setting up simulated sources

In the data acquisition dialog select "Simulated Sources" as the Data Source Type.

The dialog will then show a panel where you can create and alter any number of simulated objects. 

### Background Noise

Background noise is simulated with a flat spectrum. Enter the spectrum level background noise level in dB re.1μPa/√Hz.

### Propagation Model

Two very simple propagation models are currently available. These are Spherical Spreading and Spherical Spreading with a surface echo. If the latter is selected, the sea surface is assumed to be a perfect reflector. 

It is possible that more sophisticated models will be added in the future.

### Creating and editing Simulated Objects

You can Add, Copy Remove and Edit objects in the simulated objects table using the buttons situated below the list of simulated objects.

If you Add, Copy, or Edit an object, the following dialog will appear

The location for a new source is set at the current vessel position. Although it is possible to edit these positions here, it is easier to drag them to the desired location on the map display using the mouse. 

Give the sound source a name, select the sound type and set the signal amplitude in dB re.1μPa p-p.

Set the interval between successive sounds. If you check the 'Randomise' box, intervals will be selected at random between 0 and twice the Mean Interval. Otherwise, sounds will be regularly spaced in time. Note that the first one or each sound will always be generated at a random start time so that regular sounds with the same interval do not always perfectly coincide.

You can also set the source depth, course and speed over ground.

### Processing speed and listening to simulated data

If no [Sound Playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module is included in the PAMGuard configuration, or if no channels are selected in the [Sound Playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module, then simulated data will be analysed as fast as possible, which could be considerably faster than real time.

If a [Sound playback](../../soundPlaybackHelp/docs/soundPlayback_soundPlayback.html) module is present and is in use, then analysis will run in real time and you will be able to hear the sounds being played back from the computers sound card

.   


[Previous: SAIL Daq Cards](SAILDaqCard.html)

---

# Sound Cards

Sound Cards are by far the cheapest and most commonly used data acquisition device since nearly all modern computers are equipped with one. 

  


## Setting up a sound card

Setting up a sound card is easy. In the data acquisition dialog select "Sound Card" as the Data Source Type and a list of available sound cards will appear in the drop down list under the title "Select Audio Line". Select the card you with to use and the number of channels (which can only be either 1 or 2).

Note that sound cards which have more than two input channels are likely to appear as multiple two channel devices and you will not be able to read out more than two channels at a time. 

## Notes and Warnings

### Laptops

The audio quality of many sound cards is very poor and some, particularly those built into laptops which are prone to picking up electrical noise from the motherboard.

### USB

Many external sound cards connect using USB. While convenient, we have often experienced problems with USB sound cards 'dropping' data. This happens more when the computer is busy (which it will be when you're running PAMGuard). These drop outs can result in the lost of several percent of your audio input data.

If you are using a laptop and need an external sound card, try to get one that connects using Firewire (IEEE 1394).

### Sample Rate Conversions

The Windows drivers are clever. That's not a good thing ! They are designed such that multiple applications on your computer can all use the same sound card, and all think they are using it at different sample rates, all at the same time. This is of course impossible.

What's really happening is that the sound card is sampling at one sample rate, and Windows is converting the data to whatever sample rate you've requested. This means that your beautiful high frequency data that you sampled at 192 kHz may actually be sampling the data at 48kHz and then up-sampling to 192kHz again without you knowing it. This will of course destroy all data at higher frequencies.

In windows 11, it's reasonably easy to get around this, by going to the "Sound Settings" part of the control panel. 

  
  


In this case, I want to configure the E1DA Cosmos ADC PCM32/384 card, so click on the little arrow to the right of that card to open the advanced properties controls for that card.

You can also see that the card I want to use with PAMGuard is not the default device for my laptop. That is a good thing ! It means that any beeps or boings that Windows want's to make are going to go to a different device to the one I'm using for data collection.

  
  


You can see that the card is currently set to sample 2 channels, 16 bit, at 48kHz which is what it's going to do, even though I want to sample at a higher frequency. 

  
  


From the drop down list of formats select the sample rate and data quality that you want, close the dialogs and test.

Always check that the card is really doing what you want it to do by running a spectrogram display on the raw data to ensure that you genuinely do have data at high frequencies. 

Some manufacturers provide alternative Windows drivers which you should use wherever possible.

If possible, test your sound card with a signal generator while viewing a spectrogram of the data to check that the high frequency data are OK

### Unwanted, unexpected and unnecessary filters

Most sound cards are designed for human audio. Since we can't hear much above about 15kHz (our children and pets may be able to do a bit better) some sound card manufacturers include a filter at around 20kHz and even if the software is allowing you to sample at high sample rates, you may not be getting any data through at high frequencies

Ideally use a sound card where the manufacturer has published a technical specification showing a flat frequency response over the range you're interested in. As with sample rate conversions, test your sound card with a signal generator while viewing a spectrogram of the data to check that the high frequency data are OK

### Calibration

Most sound cards do not come with calibration information.

If calibration is important, you'll have to do this yourself. Note though that settings may change depending on any adjustments made to the computers audio settings (e.g. from the windows mixer). 

  


[Previous: Acquisition Configuration](AcquisitionConfiguration.html)

[Next: Asio Sound Cards](AcquisitionAsioSoundCard.html)

---

# Asio Sound Card Selection

## Overview

### 

These instructions guide a user on how to configure the Asio Sound Card module in PAMGuard.

In PAMGuard users can tell the program to read a certain number of channels e.g 1, 3, 6, 7 ...

  
  


This figure shows how to select 4 channels (0,1,2,4) in the Audio Data Acquisition module.  
  
---  
  
  


This figure shows to create 4 panels for these selected 4 channels.  
  
---  
  
  


### 

This figure shows the user display of these 4 channels.  
  
---  
  
  


### 

This figure shows the result of 8 channels.  
  
---

---

# File Time Options

## File Times

PAMGuard will attempt to automatically derive a date and time from audio file names. While we have programmed in a large number of different time and date formats, we cannot capture them all so it's sometimes necessary to tell PAMGuard exactly how to extract date and time information from a file name. Characters (a-z) and be stripped automatically, but problems arise if other numbers are present.

An example of names that can't be unpacked automatically might be

1677738025.180912073628.d24.d8.wav

The actual date is 12 September 2018 at 07:36:28. however it is impossible for PAMGuard to work out that the date information is between the second and third '.' characters and that the rest of the name should be ignored.

To define your own file format, select 'User defined date format' then in the format field, enter a set of characters that will tell PAMGuard how to unpack the information.

For every character in the file name that is NOT part of the date, enter the # character. The number of # must exactly match the number of characters preceding the date part of the name. Leading and trailing non numeric characters will also be stripped automatically after characters corresponding to a # have been removed.

For the date section, Java lays down strict rules for how each character should be represented details of which are [here](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html) and below. Note that most of these are case sensitive. 

When entering a data format, be very careful. For instance 'hh' will be interpreted as a 12 hour time format, which will lead to errors. 'HH' is the correct character sequence for 24 hour time. Also be very careful of the difference between 'MM' for month and 'mm' for minutes, and between lower case 'ss' for seconds and upper case 'SSS' for milliseconds.

The table below shows all available format specifiers. The ones you're most likely to use are highlighted in bold type

Letter | Date or Time Component | Presentation | Examples  
---|---|---|---  
G | Era designator |  Text | AD  
**y** | **Year** |  **Year** | **1996; 96**  
Y | Week year |  Year | 2009; 09  
**M** | **Month in year (context sensitive)** | **Month** |  **July; Jul; 07**  
L | Month in year (standalone form) | Month |  July; Jul; 07  
w | Week in year | Number | 27  
W | Week in month | Number | 2  
D | Day in year | Number | 189  
**d** | **Day in month** | **Number** | **10**  
F | Day of week in month | Number | 2  
E | Day name in week | Text | Tuesday; Tue  
u | Day number of week (1 = Monday, ..., 7 = Sunday) | Number |  1  
a | Am/pm marker | Text |  PM  
**H** | **Hour in day (0-23)** | **Number** | **0**  
k | Hour in day (1-24) | Number | 24  
K | Hour in am/pm (0-11) | Number | 0  
h | Hour in am/pm (1-12) | Number | 12  
**m** | **Minute in hour** |  **Number** | **30**  
**s** | **Second in minute** | **Number** | **55**  
**S** | **Millisecond** | **Number** | **978**  
z | Time zone |  General time zone | Pacific Standard Time; PST; GMT-08:00  
Z | Time zone |  RFC 822 time zone | -0800  
X | Time zone |  ISO 8601 time zone | -08; -0800; -08:00  
  
The example below shows an appropriate date format string used to correctly unpack the above example file name

  
  


## Time Zone

Ideally, all PAMGuard data should be referenced to UTC, this makes it easy to align with GPS data and data from other sources.

Sometimes, it is necessary to process audio files which do not have a UTC based time encoded in their file name, but are referenced to some other time zone.

The File Date Settings dialog allows you to set the time zone and any additional time offsets for an audio file or set of audio files.

The time zone offset and optional daylight saving will be SUBTRACTED from the time value extracted from the file name.

The additional time offset will be ADDED to the time value extracted from the file name.

Check and double check that the time extracted from the file and corrected for time zone and offset is correct before processing sound file data.

See [additional notes](../../../detectors/clickDetectorHelp/docs/SoundTrapClickDetector.html) on SUD file times if you're processing Soundtrap data.

---

# SAIL Data Acquisition Cards

SAIL Daq Cards have been developed specifically for high speed USB data acquisition from hydrophones by [St Andrews Instrumentation Ltd](http://www.sa-instrumentation.com/products-and-services/javascript/sail-daq/). 

Each card can support up to four channels of high speed audio input at sample rates of 62.5, 250, 500 or 1000kHz sample rate. 

The analogue front end has been specifically designed for direct connection to most hydrophones, saving the need for additional preamplifiers and filter units between the hydrophone and acquisition system. A single high input impedance channel is suitable for direct connection to hydrophones which do not have an integral preamplifier. 

Gain and filter settings are all software selectable, with gains of between 0 and 36dB in 6dB steps and second order Butterworth high pass filters at frequencies of 0, 10, 100, 2000 and 20000Hz. 

Up to three cards can be synchronised for simultaneous data acquisition on up to 12 hydrophone channels. 

  


## Setting up SAIL Daq Cards

Setting up a sound card is easy. In the data acquisition dialog select "SAIL Daq Cards" as the Data Source Type. 

Enable the channels you wish to use and set the gain and sample rates.

If more than one card is present, additional channels will be shown. Note that when multiple cards are in use, each card is ordered by it's serial number. Press the "Flash LED's" button to assist in identifying individual cards

  
  


[Previous: National Instruments DAQ Cards](AcquisitionNIDAQ.html)

[Next: Simulated Sound Sources](AcquisitionSimulator.html)

---

# PAMGuard

## Sound Acquisition

  


### Adding a Sound Acquisition Module

Add a Sound Acquisition module using File>Add modules�>Sound Acquisition and enter a name of your choice. To access the module's settings, select Detection from the main menu and click on the name of your module. This will bring up an Audio Data Acquisition dialog, as shown below.

  


  


### Selecting a Device and Audio Line

Choose Sound Card from the Data Source Type drop down menu. The Select Audio Line drop down list will then display the available mixer lines. Choose the line associated with your sound card input(s).

  


### Sampling Settings

  * **Sample Rate:** Enter a sample rate that is supported by your sound card (you may need to consult your sound card documentation).

  * **Number of channels:** Enter the number of input channels from which you want to acquire data. For non-ASIO audio lines, this will be a maximum of 2.

  * **Peak-Peak voltage range:** This value represents the maximum input voltage range capable for your card, without overdriving the input (clipping) and is used to scale the sound data in PAMGuard. You may find this value from your device's specifications. 

  * **Preamplifier gain:** If you have any hardware which provides gain in your system, this value should equal the total gain (in dB). 

  * **DC Background subtraction:** If your sound acquisition system has a DC offset in it (many do), tick the 'Subtract DC ...' button and enter a time constant. A single pole high pass filter will then be applied to all incoming data.




(Note that the 'Bandwidth' field has been removed since it was not used in any way)

  


[Next: Configuring ASIO Sound Cards](AsioHelp.html)

---

# SoundTrap data files

### SUD Files

SoundTrap recorders from [Ocean Instruments](https://www.oceaninstruments.co.nz/) store data in proprietary files called SUD files. See SoundTrap documentation for information on how to extract SUD files from SoundTrap recorders. 

Earlier versions of PAMGuard required you to 'inflate' the SUD files into standard wav audio files using the [SoundTrap Host software](https://www.oceaninstruments.co.nz/downloads/) before they could be processed.

The current version of PAMGuard no longer requires this since it can read data directly from the SUD files.

Inflating SUD files to WAV files generally required between 3 and 5 times as much disk space and could also take a fair amount of time, so not inflating will save both!

### SUD Index Files

To efficiently navigate SUD files, PAMGuard makes a map of each file and stores it in a new file in the same folder as the original SUD file. The index files have the same name as the original file, but end with .sudx. The index files are about 2.5% of the size of the original SUD files. When processing SUD files it is therefore important that a) the drive containing the SUD files is less than 95% full and b) that the system running PAMGuard has write access to the folder containing the SUD files. 

### SUD File Times

The name of each SUD file contains the device serial number and a timestamp (data and time) in the form YYMMDDhhmmss. Note that these time stamps are in local time. When processing the file, PAMGuard will extract a UTC timestamp from within each SUD file and apply the UTC time to the data. you may therefore notice that the times of binary files and database entries differ from the times in the file names. 

### Mixing SUD and WAV files

If a SUD file and a WAV file with the same name are in the same folder, then PAMGuard will ignore the SUD file and process the WAV file. Note that in this case, PAMGuard will be unable to extract any metadata from the SUD file and will not extract SoundTrap Click Detections (see below).

### SoundTrap Click Detector

If you are using the SoundTrap build in click detector (see SoundTrap manuals) then the SUD files will also contain detected clicks.

See the help page for the [SoundTrap Click Detector](../../../detectors/clickDetectorHelp/docs/SoundTrapClickDetector.html) for information on how to get these clicks from the SUD files into PAMGuard.
