Click Detector Click Detector Map Options  
Click Detector Map Options Click detector map options are accessed from the _**Click Display>Map Options�**_ menu. These apply only to automatically detected click trains. Click trains may contain varying levels of information. Short click trains will only have a bearing to the detected source. Longer trains that alter bearing by more than a few degrees will also have a calculated range.  Sequences of sperm whale clicks from a single source are generally broken up into shorter trains by the click train detection algorithm.  Click trains with range information are always shown. The shorter and more numerous click trains that do not have range information can rapidly clutter the map. By selecting the radio buttons the operator can choose to plot either: 

  * none of the short click trains,
  * all of the short click trains, 
  * or only those which are separated from other plotted click trains by a minimum time interval or change in bearing.

If no range information is present, the bearing lines on the map are drawn at a fixed length.    
  
  
[Previous: Click Detector Side Panel](ClickDetector_clickSidePanel.html) [ ]()

---

Click Detector Graphic Overlays   
PAMGuard Map Data can be overlaid on the [PAMGuard map](../../../mapping/mapHelp/docs/MapOverlays.html) at three different levels: Clicks, Tracked Clicks and Tracked Events. Overlaying all clicks will generally display too much data and can slow PAMGuard down. However this can be useful if the map options are used to only display a few seconds of the most recent data. Tracked Clicks and Tracked events will be shown if click trains are manually or automatically detected (see [Click Train Identification](ClickDetector_clickTrainLocalization.html)). Event localisations will start to show as soon as two or more clicks are marked or sufficient clicks have been automatically assigned to a click train for target motion analysis. If a localisation has not been calculated, then just a bearing line to the first click in the train will be shown.   
  
Spectrogram Clicks can be shown as an overlay on the [spectrogram](../../../displays/spectrogramDisplayHelp/docs/UserDisplay_Spectrogram_Overlays.html). They will appear as small triangles at the top and bottom of the display.   
The colour of the triangles can be the colour of the species id of a single click, the colour assigned to a click train or a mixture of both - using the click train colour if one is assigned, otherwise using the individual click classification. To control the colours, go to Click Display>Display Overlays and select the option you require.   
  
  
Radar Display Bearing and amplitude of single clicks or bearing and range of localised click trains can be shown on the [radar display](../../../displays/radarDisplayHelp/docs/UserDisplay_Radar_Configuring.html). As with the spectrogram, the colour of individual clicks can be set from the Click Display > Overlays menu.   
  
   
  
[Previous: Click Detector Side Panel](ClickDetector_clickSidePanel.html) [Next: Click Classification ](ClickDetector_clickClassification.html)

---

Click Detector Basic Click Classification   
The Basic click Classifier is the same as the click classifier in the IFAW RainbowClick software. The basic classifier is generally adequate for the classification of high frequency harbour porpoise clicks.  
  
  
Clicks are classified according to any or all of 4 different tests. Each test has a check box to the left of its parameters which can be used to enable or disable particular tests. All enabled tests must be passed for a positive classification to result. **Test 1.** _Energy Band comparison_  
The summed acoustic energy in two frequency bands (Test band and Control band) is compared. The energy in each band must lie in the ranges indicated and the ratio between the energy in the test band must exceed that in the control band by the specified number of decibels. **Test 2.** _Peak Frequency position_  
The peak frequency must lie within a certain range. The search for the peak frequency need not cover the entire spectrum - for instance, if low frequency noise is always present, it will make more sense to start the search at a higher frequency as shown in the example, where the search is between 20kHz and 250kHz, and the peak frequency must lie in the range 100kHz to 150kHz. **Test 3.** _Peak frequency width_  
Sets limits on the width of the spectral peak. The width is defined as the minimum amount of spectrum required to sum up to the specified percentage of the total click energy. **Test 4.** _Mean frequency_  
Sets limits on the mean frequency of the click energy within the specified frequency band. **Test 5.** _Click length_  
Sets limits on the length of the click in milliseconds. The length is defined as the minimum length of the data required to sum up to the specified percentage of the total click energy. **Acoustic Alarm**  
Allows user to select the alarm associated with this click type. The alarms shown in the drop-down list are the ones previously defined in the [Acoustic Alarm Settings window](ClickDetector_clickAlarmOptions.html "Acoustic Alarm Settings window"). The user is also able to specify a maximum amount of elapsed time between detections, to minimize alarming on spurious clicks and focus on specific inter-click intervals. The alarm will not sound on a click detection if the elapsed time since the previous click detection is greater than this number.  
  
Species Default settings The 'Species Defaults' button allows the user to use stored standard settings for beaked whale and harbour porpoise classifiers.   
[Previous:Click Classification](ClickDetector_clickClassification.html) [Next: Advanced Click Classification ](ClickDetector_betterClassification.html)

---

Click Detector Click Classification With Frequency Sweep   
Overview The click classifier with frequency sweep was added to PAMGuard in 2010 to provide a wider choice of species identification parameters, particularly for the detection of beaked whales. Operation is similar to that of the [ Basic click Classifier](ClickDetector_basicClassification.html) in so much that a number of different tests can be carried out on each click and the click will have to satisfy all of those tests in order to be classified.   
  
  
General options In the general options section, enter the species name, a unique identification code and select the symbol to be used for this species on the click detector display. Channel options Click detection is generally conducted on more than one channel. If this is the case, then the click classifier can be set to either:

  1. Require positive identification on all channels individually
  2. Require positive identification on only one channel
  3. Use mean parameter values over all channels

Restricting the click length Click waveforms from the click detector are generally longer than the click itself. This is partly due to the pre and post samples added to the waveform but can also be caused by the click arriving at different times on different channels. The addition of extra waveform data before or after the click has little effect if the click has a high signal to noise ratio, but for quiet clicks, the additional data is a significant source of noise for some parameter measurements. There is therefore an option to restrict the length of the data used in the parameter extractions. If this option is used, then the peak of the waveform envelope is found (see Click Length below) and an equal amount of wave data taken from each side of the envelope maximum.  Generally the click length should be set to a power of 2 (e.g. 128, 256, 512, etc) since the FFT's used in many of the calculations require data that is an exact power of 2 long. Other values can be used, in which case the shortened data will be padded with zeros prior to FFT calculations. Waveform Options The first tab "Waveform Options" controls classification parameters relating to the click waveform Pre Filtering Select this option if you want to filter the click prior to parameter extraction. Generally measurements such as the click length will be more accurate if unnecessary noise is first filtered from the click Amplitude Range This option can be used to exclude very quiet of loud clicks. Quiet clicks in particular tend to cause false classification since their properties are poorly defined so they tend to mis-classify at random Click Length The click length is measured by first calculating the [analytic waveform](http://en.wikipedia.org/wiki/Analytic_signal) (or signal envelope) of the click using the [Hilbert transform](http://en.wikipedia.org/wiki/Hilbert_transform) of the waveform data This is then smoothed using a [moving average filter](http://en.wikipedia.org/wiki/Moving_average) defined by the user (smoothing parameter in dialog). The maximum of the smoothed envelope is then found and the click length taken as the length of the data between points either side of that maximum which remain above the maximum value minus the threshold (Threshold parameter in dialog). The test is passed if the click length lies within the range set by the user (Length range parameters in dialog). Zero Crossings Some species of whale produce frequency modulated clicks, i.e. the click frequency changes during the course of the click.  The power spectrum of a click is an average of the spectral energy over the duration of the click and is therefore unable to show changes in frequency during the course of the click. Although it is possible to extract more detailed frequency information using a [ Wigner-Ville transform](http://en.wikipedia.org/wiki/Wigner_quasi-probability_distribution) of the waveform data. These are slow to compute and therefore not suitable for real time classification. The classifier therefore extracts frequency information by examining zero crossings of the waveform data. A zero crossing is defined as the signal waveform going from a positive to a negative value or vice-versa. The classifier searches the waveform for zero crossings only within the region of the click between the thresholded limits from the click length estimation described above. Once zero crossings have been found, the frequency between each zero crossing is calculated. If there are three or more zero crossings (permitting two or more estimates of frequency) the frequency sweep is calculated by fitting a linear model of frequency against time. Two tests can then be applied to the data. The first is the total number of zero crossings which must lie within the range set in the dialog. The second is the frequency sweep estimated from the zero crossing data.  Spectrum Options The second tab controls classification parameters extracted from the clicks spectrum   
  
Energy Bands The energy band test compares the acoustic energy in a test band with the energy in two control bands. The user should enter the frequency ranges of each band and a threshold value for each of the control bands. The test is passed if the test band energy exceeds each of the control band energies by more than the threshold values. If only one control band is required, set both frequency limits of the second control band to zero. Peak and Mean Frequency The peak and mean frequency are measured from the power spectrum of the click waveform. Search and integration range The peak search and the frequencies over which the mean frequency is summed can be restricted using the search and integration range parameters in the dialog. The power spectrum can also be smoothed using a [moving average filter](http://en.wikipedia.org/wiki/Moving_average) to remove noisy spikes from the spectral data. Peak Frequency If the peak frequency test is enabled, the peak frequency (taken as the highest point in the smoothed spectral data between the limits of the Search and Integration range) must lie between the limits entered in the dialog.  Peak Width The width of the spectral peak is measured by first finding the amplitude of the power spectrum at the peak frequency. The peak width is taken as the frequency range of the data either side of that peak which are at an amplitude above the peak amplitude minus the threshold (threshold parameter in dialog). The test is passed if the width of the peak lies within the set range.  Mean Frequency The mean frequency is calculated using   
  
where _i_ is the range of frequency bins within the search and integration range _I_ is the intensity of the spectrum at each frequency bin _i_ _f_ is the frequency (Hz) at each frequency bin _i_ The test is passed if the mean frequency lies within the set range.  Other Options The third tab controls other options   
  
Alarm You can create an audible alarm by setting an alarm on the tab and by enabling it on the more general classifiction panel   
Species Default settings The 'Species Defaults' button allows the user to use stored standard settings for beaked whale and harbour porpoise classifiers.   
[Previous: Basic Click Classification](ClickDetector_basicClassification.html) [Next: Tracking and Click Train Identification ](ClickDetector_clickTrainLocalization.html)

---

Click Detector Click Alarm Options   
  
  
Options Window Alarms can be used to provide an audible signal when certain [click types](ClickDetector_clickClassification.html "Click Type Classification dialog") are detected. The Alarm Options dialog allows the user to create, modify and delete any number of alarms. The Alarm Options dialog can be opened from the **_Click Detection >Audible Alarm_** menu. The alarm can be a predefined 'beep', or a user-selected wav file. Use the Rename button to rename the current alarm (the alarm visible in the drop-down box). Use the Remove button to delete the current alarm. Note that there must always be at least one alarm in the list - in such a case, this button is not selectable. Use the Add button to create a new alarm. This dialog is only used to create a list of available alarms. Associating a _specific_ alarm with a _specific_ click type is done in the [Individual Click Classification dialog](ClickDetector_clickClassification.html).    
[Previous: Tracking and Click Train Identification](ClickDetector_clickTrainLocalization.html) [Next: Click Detector Viewer Functions - Overview](offline_Overview.html)

---

Click Detector Click Classification   
Overview Individual clicks detected by the click detector can be classified using one of two different click classifiers. The first, [ Basic click Classifier](ClickDetector_basicClassification.html) is the same as the click classifier in the IFAW RainbowClick software. The basic classifier is generally adequate for the classification of high frequency harbour porpoise clicks and is primarily retained for backwards compatibility with the RainbowClick classifier. The second [Classifier With Frequency Sweep](ClickDetector_betterClassification.html) uses additional classification parameters and is more suitable for the detection of lower frequency odontocete clicks, particularly those of beaked whales. The classifier is set up from the **_Click Detection >Click Classification_** menu.   
  
  
Click Classifier Selection Select the type of click classifier you wish to use from the drop down list. This will either be the [ Basic click Classifier](ClickDetector_basicClassification.html) or the [Classifier With Frequency Sweep](ClickDetector_betterClassification.html). You must also check the _'Run classification online'_ box if you want clicks to be processed as they are detected. You should check the _'Discard unclassified clicks'_ box if you want clicks which do not pass any of the classification criteria to be discarded. **Use this feature with great caution and only when you are confident that the classifiers are working well for the clicks that you do want.** Click Types The click classification dialog contains a list of defined click types. If more than one type is defined, then each click is tested against each type in sequence and the click is classified as belonging to the first type with a matching set of classification criteria. If the click does not match the criteria of any of the classifiers, then it is unclassified. Use the New, Edit and Delete buttons to add, modify and remove items from the list. If an audible alarm has been associated with this click type, checking/unchecking the alarm column will enable/disable it. Alarms can be associated with click types in the Individual Click Classification dialog (press the Edit button to open the dialog). Each click is checked against the different click types in sequence. As soon as one set of criteria is matched, the classifier will stop searching other click types. It is therefore sometimes important to arrange the different types in a particular order. Use the Up and Down buttons to move different click types up and down in the list. Individual species classifiers can be enabled or disabled, for example if you wish to temporarily stop checking for a particular click type. Classified clicks can also be discarded. For example, if there was a particular noise source causing false triggers of the click detector (e.g. a depth sounder), it may be possible to set up a classifier for those detections and immediately discard them. If either the New or the Edit button is pressed, the Individual Click Classification dialog will be displayed. The behaviour of this dialog will depend on the type of classifier selected.   
[Previous: Click Detector Graphic Overlays](ClickDetector_MapOverlays.html) [Next: Basic Click Classification ](ClickDetector_basicClassification.html)

---

Click Detector Overview The click detector is used to detect transient signals, primarily from odontocete species such as sperm whales, beaked whales and harbour porpoises.   
Creating an instance of the click detector From the **_File>Add modules>Detectors_** menu, or from the pop-up menu on the data model display, select _'Click Detector'_. Enter a descriptive name for the new detector (e.g. sperm whale detector, beaked whale detector, etc) and press Ok.    
Configure the Data Source Go to **_Click Detection>Detection Parameters_** to configure the click detector.    
  
  
The click detector requires a source of raw data before it can operate. This may come directly from a [Sound Acquisition module](../../../sound_processing/AcquisitionHelp/docs/AcquisitionOverview.html) (e.g. a sound card or a National Instruments board) or from processed data such as the output from a [Decimator](../../../sound_processing/decimatorHelp/docs/decimator_decimator.html) or [Filter](../../../sound_processing/FiltersHelp/Docs/Filters_filters.html).  Select an appropriate input source from the drop down list on the click detector Detection Parameters menu, Channel Grouping Use the channel grouping controls to arrange the data channels into groups. Channels in the same group are analysed together so that if one channel in a group is triggered, all channels in that group are read-out together to create a detected click. The click detector works best if data are analysed in pairs of channels. It can then use the time delay within each pair to calculate a bearing. If multiple pairs of hydrophones are used, these bearings can be crossed to estimate source locations. (Click detector output .clk files can also be further analysed with RainbowClick software if the channels are arranged in pairs). If you only have a pair of hydrophones, then select either 'One group' or 'User groups' and set the group numbers to be the same. If you have multiple pairs of hydrophones, select 'User groups' and arrange the group numbers so that each group contains two channels as shown above. If you select 'no grouping' each channel will be analysed totally independently. There are currently no PAMGuard functions which can re-group these single channel clicks for source localisation. Data flow and advanced configuration The data flow through the click detector is shown below   
  
  
The filters and trigger decision parameters can all be adjusted by the user to be optimal for a particular species; and for the background noise conditions from a particular vessel and operating area. Setting many of these parameters is an expert procedure and requires both experience and a thorough understanding of the types of sound being detected.   
Filters You will see in the figure above that two filters are used; a digital prefilter and a digital trigger filter. Two filters are required for the following reason. For optimal detection efficiency, the trigger only receives data in the frequency band in which the animal is making sound. However, classifiers that assign clicks to a particular species will require data from more parts of the spectrum. For instance, when detecting harbour porpoises which produce narrow band clicks between 100 and 150kHz, the trigger filter is set to a band pass filter covering only that frequency range. The pre-filter however is set to a high pass filter at 20kHz. The clip data therefore contains data in the spectrum between 20kHz and 250kHz, which is used by the classifier to distinguish between narrow band porpoise clicks and broad band clicks from other sources. Both filters can be configured. Go to **_Click Detection>Digital pre Filter..._** or **_Digital trigger filter..._** respectively. Both filters configured using the [Filter Design](../../../sound_processing/FiltersHelp/Docs/Filters_panel.html) panel.    
Trigger decision The trigger automatically makes a measure of background noise and then compares the signal level to the noise level. When the signal level reaches a certain threshold above the noise level a click clip is started. When the signal level falls to below the threshold for more than a set number of bins, the click clip ends and the clip is sent to the localisation and classification modules. Trigger parameters are set in the Triggers tab of the **_Click Detection>Detection Parameters_** menu:    
Individual channels can be 'turned off' in the trigger by un-checking the appropriate check boxes. Note however, that if a deactivated channel is part of a group, that channel will still be read if another channel in that group triggers. This feature can be used to reduce false triggers if one channel develops a noise problem.    
The maths behind it all The noise level _**N**_ at sample _i_ is measured using    
and the signal level _**S**_ is measured using    
where _α N_ is either the _long filter_ parameter when no click is active (i.e. the signal is below threshold) or the _Long Filter 2_ parameter when the signal is above threshold. _α S_ is the _Short filter_ parameter. A click is started / stopped when the ratio S/N goes above / below the _Threshold_ parameter.    
Click Length   
  
The click only ends when all chosen channels have been below threshold for the number of samples specified by "Min Click Separation" . This parameter is important since most clicks will rise above and fall below threshold several times, particularly if they are quiet and not extending much above the detection threhsold. Further, if you're dealing with multi-channel data, clicks on different channels need to be kept together for PAMGuard to be able to estimate Time Differences of Arrival and bearings. This parameter should therefore be set to either the maximum separation of your hydrophones in samples, or the length of a typical sound from species of interest, or the maximum of both.  The maximum length of a click is set such that click clips are limited to a maximum number of samples. Set this so that it is at least greater than the length of a typical click for your species of interest + the separation between hydrophones + the presample + the postsample. When a clip is created, a number of extra samples are added to the clip before the first and last samples to rise above threshold and after the last sample to be above threshold (pre sample and post sample in the dialog). Set this to about half the length of a typical click for your species of interest.  To calculate the number of samples equivalent to a given time, simply multiply the sample rate by the time (in seconds). To calculate the number of samples from a distance, then you first convert the distance to a time, then convert that time to a number of samples. e.g. if you had hydrophones with a 1m separation and are sampling at 192kHz, the maximum delay between the hydrophones is about 1.0/1500 = .00067s, or .67ms. The equivalent number of samples is therefore .00067 x 192000 = 128 samples.  If your hydrophone separation is small, and you've a limited number of species, selecting these parameters is not difficult. However, if you've a larger separation and are trying to separate clicks that are close together in time, e.g. small cetacean buzz clicks it might get tricky. Similarly, you're not going to be able to chose detection parameters which will separate small cetacean buzz clicks and at the same time keep together the multiple pulses in a loud sperm whale click. For this, you'd need to implement multiple click detectors with different settings.    
Delays If you're using multi channel data, then the Click Detector will measure Time Difference of Arrival between the signals on each channel in each detected click which is the first step in estimating bearings. Several options are available to improve the robustness of delay estimation, which are described [here.](../../../localisation/docs/localisation_tdoa.html)   
  
Echoes Noise TEXT   
  
[]() [Next: Click Detector Displays ](ClickDetector_clickDetectorDisplays.html)

---

Click Detector Click Detector Displays   
Data display and an interactive Graphical User Interface are fundamental to the workings of the click detector. Clicks from many species are often quite indistinct, especially when detected in noise and at the limits of detectability. Visually, it is often easy for an operator to pick out click trains on the display which consist of lines of regularly spaced clicks on consistent, slowly changing bearings. The click detector has a bespoke display panel containing a number of different displays for looking both at click trains and at individual clicks. It also displays information in the PAMGuard side panel and can overlay information on the map and other standard displays.      [Previous: Click Detector Overview](ClickDetector_clickDetector.html) [Next: Tab Panel Displays](ClickDetector_clickTabPanelDisplays.html)

---

Click Detector Click Side Panel   
  
  
The side panel displays a running tally of the number of clicks and click events that have occurred. Also displayed is the name of the alarm that is currently sounding (or _No Alarm Sounding_ , as shown above, if there is no alarm at the moment). Pressing the Test Alarm button will sound each of the currently defined alarms, one at a time, with a short pause in between.   [Previous: Tab Panel Displays](ClickDetector_clickTabPanelDisplays.html) [Next: Click Detector Graphic Overlays ](ClickDetector_MapOverlays.html)

---

Click Detector Click Storage Options  
Click Storage Options   
  
Database Storage Individual clicks may be written to the PAMGuard database. Be warned though that writing to the database can be slow and can seriously affect overall PAMGuard performance RainbowClick files The click detector also stores click data in a format compatible with the IFAW RainbowClick software. This allows RainbowClick can be used for offline data analysis. Offline analysis is currently not as well developed in PAMGuard as it is in Rainbowclick.  For the files to be compatible with RainbowClick, the click detector must be [configured](ClickDetector_clickDetector.html) to analyse pairs of channels. If you have multiple pairs of hydrophones you should use RainbowClick version 4.06.0000 or higher.  To enable RainbowClick compatible file storage check the _'Create RainbowClick File(s)'_ check box.  Use the browse button and edit box to set the output directory for your data.  Use the File Initials text field to set a number of characters to be used at the start of the file name. The remainder of the name is constructed automatically with the date and time at the start of the file.  To avoid single files becoming too large, use the _'File Length'_ edit box to set a maximum length for each file.    
  
[Previous: Tracking and Click Train Identification](ClickDetector_clickTrainLocalization.html) [Next: Click Alarm Options](ClickDetector_clickAlarmOptions.html)

---

Click Detector Click Detector Tab Panels   
Click Detector Displays The click detector display contains four main sub windows, although it is possible to have more than one instance of the main bearing / time display if desired. Two optional displays, IDI Histogram and Wigner Plot, are also available by selecting **_Click Display >Add Display_** in the toolbar menu.   Right clicking on the bearing / time display brings up a pop-up menu from which you can configure the display.   
Bearing Time Display   
  
Each detected click is shown as a circle or ellipse on the scrolling display. The display options, available by right clicking on the display, can be used to select the parameter to display on the vertical axis: bearing, amplitude or inter-click-interval. Note that inter-click intervals will only be shown in the automatic click train identifier is running. The example shows 20s of data with three sperm whales currently slightly ahead of the hydrophone array. The duration of the display may be adjusted using the right hand (vertical) slide bar. The horizontal slide bar is disabled during data acquisition. Manual tracking Click trains identified by eye may be tracked manually. To track manually, press the default mouse button anywhere on the display. While the mouse is held down, the display will stop scrolling allowing you to position the mouse above a particular click. Release the mouse over a click and that click will be 'tracked'. Tracked clicks may be overlaid on the map display and also on radar type displays. Manual tracking with least squares localisation If the same operation is performed using the right mouse button, then a pop-up menu appears and the operator is asked to assign clicks to numbered groups. Clicks within the same tracking group are assumed to be from the same source. PAMGuard then uses a least squares fit to estimate the position of the source based on the most likely crossing point of the bearing lines for each tracked group. If a location has been successfully calculated, it can be displayed on the map and radar displays. Automatic Tracking If the automatic click train identifier is running, clicks will be coloured when they are assigned to a click train. As with manual tracking, PAMGuard will use a least squares fit to estimate the position of the source based on the most likely crossing point of the bearing lines. As with manual tracking, automatic click trains are displayed on the map, either as bering lines (when no position has been calculated) or as a bearing line and point if a position has been calculated.   
Click Waveform Display The waveform of each click is displayed as it is detected. Click Spectrum The power spectrum of each click is displayed as it is detected. Trigger The trigger window shows the amplitude of the signal on each channel as a decaying histogram. The vertical red line represents the trigger threshold set in the detection parameters dialog (cross reference). Level meters for each channel are also shown on the right hand side of the trigger window. Display Options Display options for windows on the tab panel are accessed from the **Click Display >Display Settings** menu or by right clicking on the display.   
  
The axis panel (shown above) shows the same basic three options for the vertical axis that can be selected from the pop-up menu. It also allows you to select limits for the vertical axis when ICI or Amplitude are displayed. The number of horizontal grid lines may also be set (note that this is stored separately for the three types of vertical axis).   
  
The size at which each click is displayed can also be set. Clicks sizes will scale between the lower and upper bounds based on their amplitude and duration.   
  
If the [ click classifier](ClickDetector_clickClassification.html) is running it is possible to select which types of clicks get displayed.   IDI Histogram The IDI (Inter-Detection Interval) Histogram Display provides a visual interpretation of the inter-detection interval on two different scales. The display uses a horizontal split-pane to present the data in both a high-resolution (left pane) and low-resolution (right pane) format. The relative proportion of the panes can be changed by dragging the center divider to the right or to the left.  The vertical axis is the elapsed time since the acoustic data collection was started, and is common to the two panes. The units are minutes. Each row in a pane represents a histogram of data accumulated over the course of a certain time period (the _time bin_), as defined by the IDI Display parameters (see below). In the image above, the time bin is 1 second. This means a histogram is generated every second from the IDI data that has been compiled over the last second, and displayed on the screen. Each pane has it's own horizontal axis. Each column represents a range of IDI values, where the size of the range is defined by the IDI Display parameters (see below). In the image above, the IDI bin size is 1 ms for the high-resolution pane and 5 ms for the low-resolution pane. The highest value bin is 70 ms for the high-resolution pane and 700 ms for the low-resolution pane. Taking the low-resolution pane as an example, the first bin would count the number of inter-dection invertvals that fall between 1 ms and 5 ms, the second bin would count the number of inter-dection intervals that fall between 6 ms and 10 ms, etc. Each row/column combination, therefore, is a cell that represents the number of inter-detection intervals counted in a specific IDI range over a specific time period. The color of each cell represents the magnitude of that count, with black indicating 0 and red indicating a maximum value as defined by the IDI Display parameters (see below). In the image above, the maximum counts in the high-resolution pane is 20 and the maximum counts in the low-resolution pane is 50. When a click is detected, the inter-detection interval is calculated as the elapsed time since the previous click detection (in milliseconds). The IDI bin with a range spanning that data point is determined, and the count within that bin is incremented by one. When the time bin has elapsed, the counts in each IDI bin are converted to a color and displayed on the screen as a new row at the bottom of the pane. Each previous row is moved up by 1 to make room. The row at the top of the pane is discarded. When repositioning/tiling the window or resizing the split panes, the display may on occasion not update properly. In such a circumstance, simply adjust the size of the window slightly in order to force a redraw.   IDI Display Parameters Dialog The IDI-Display Parameters Dialog can be accessed by right-clicking on the display window and selecting Plot Options. 3 parameters can be specified for both the high-resolution and low-resolution histograms:

  * Bin Scale: the size of each bin in the horizontal scale, in milliseconds
  * Max Value: the value of the highest bin in the scale, in milliseconds. In the above image for the low-resolution histogram, a bin size of 5 ms and a maximum value of 700 ms means there are (700/5=) 140 bins with ranges 1-5ms, 6-10ms, 11-15ms ... 691-695ms, 696-700ms.
  * Max Counts: the highest number of counts in a bin, corresponding to the color red

The two time parameters are:

  * Bin Scale: the size of each time bin over which the histogram data is compiled, in seconds
  * Max Value: the maximum amount of time shown on the chart, in minutes. In the above image, 1 minute of data with 1 second bins means a maximum of 60 histograms displayed at a time.

The histogram data can also be captured in a csv file, the name and location of which is specifed by the user. The first column in each row is the time of the click detection. Each column thereafter contains the counts found in a histogram bin - first the bins in the high-resolution histogram, followed by the bins in the low-resolution histogram. The values in the header row represent the upper limit of the bins (ie. for bin 1-5ms the header displays a 5, for bin 6-10ms the header displays a 10, etc). The user should use caution when saving data, as the process is processor-intensive and will slow down other program functions.     [Previous: Click Detector Displays](ClickDetector_clickDetectorDisplays.html) [Next: Click Detector Side Panel](ClickDetector_clickSidePanel.html)

---

Click Detector Tracking and Click Train Identification   
Click Train Identification   
  
To automatically detect click trains, check the Run Click Train Id. Click trains are sequences of clicks on a consistent bearing with a consistent inter click interval. This system is currently optimised for sperm whale click train identification. If a click train reaches sufficient length and the bearing change is adequate, target motion analysis is used to automatically calculate a range and bearing to the sound source. Bearings and ranges to click trains can be overlaid on the [PAMGuard map](../../../mapping/mapHelp/docs/overview.html).    
  
[Previous: Click Classification](ClickDetector_clickClassification.html) [Next: Click Storage Options](ClickDetector_clickStorageOptions.html)

---

Click Detector Tracking and Click Train Identification   
Target Motion Analysis   
A common method for tracking animals, particularly sperm whales, is to use [Target Motion Analysis](../../../localisation/targetmotion/docs/targetmotion_overview.html) or TMA. TMA works by measuring bearings to a sound source from multiple locations along a track-line. The crossing points of the bearings indicates the location of the tracked sound source.  There are two ways of linking clicks together to form click trains when using PAMGuard in 'normal' mode, i.e. analysing data in real time. On the click detector bearing time display, clicks assigned to a train are shown in colour, depending on the options selected from the displays settings menu. Manual Click Train Identification  The most effective method of click train identification is for the operator to manually select clicks from the click detector bearing time display which they believe come from the same animal. To track clicks manually, right click anywhere on the bearing time display, scrolling will then pause and you can then release the mouse over the click you wish to mark. A small popup menu will appear with a single option to create a New Click Train   Wait a while until the angle to that click train has changed and mark another one in the same way. This time, the menu will have the options of adding the click to the existing click train, or creating a new click train. If you ware tracking multiple animals, then start a New Click Train. Automatic Click Train Identification PAMGuard can attempt to mark Click Trains automatically. To enable automatic click train identification go to Click Detection > Click Train Identification Clicks are associated if there is a regular Inter Click Interval (ICI) and the clicks are on a slowly and steadily changing bearing. In the control section set the minimum number of clicks for a train to be created, the minimum angle for Target Motion Analysis (TMA) to be run and in the minimum interval between location and database updates. This last parameter is important since the localisation calculations can take 100's of milliseconds, so repeating them every time a click is added to a click train can cause PAMGuard to run too slowly. ICI changes controls the minimum and maximum allowable ICI and the change ratio governs how much the ICI can change for a new click to be added to an existing click train. For example, if the currently measured ICI is 1.0s and the change ratio is 1.2, then the ICI to the next click can be anything between 1/1.2 = 0.83s and 1.2s. Angle changes is the maximum angular deviation allowed for the next click. As a click train is starting, this is the angle difference between clicks. Once a train has enough clicks for a localisation, it's the angle off from that localisation point. Correcting Mistakes If you make a mistake tracking manually or if you think the automatic train identification has made a mistake, click on the click that's been assigned incorrectly and a slightly different menu appears giving four options.   


  1. Remove the click from the train (the click will then not be assigned to anything) 
  2. Reassign ALL clicks in that train to a different train 
  3. Reassign that click as a new click train. 
  4. Select the train the click should be assigned to (if other clicks have been started)

It is very common for the automatic click train identification algorithm to break up an obvious click train into many smaller ones. This happened when there are pauses or changes in click rate. Reassigning all clicks in a train is a handy way of correcting these errors in order to generate long click trains which will yield accurate localisations. Database Output Click train information is written to the PAMGuard database using the same database tables used for offline data analysis. This means that any event marking which took place in real time will be available for further offline analysis. Two database tables are used: The "events table" contains one record of information per click train, the second "clicks table" contains a record for each click in each event. To maintain compatibility with earlier versions, the two database tables are called Click_Detector_OfflineEvents and Click_Detector_OfflineClicks (the name Click_Detector_ in these table names will change to whatever the name of the click detector created in the PAMGuard configuration you are using and if you have multiple click detectors, multiple tables with different names will be created.)   
  
[Previous: Advanced Click Classification](ClickDetector_betterClassification.html) [Next: Click Storage Options](ClickDetector_clickAlarmOptions.html)

---

Click Detector SoundTrap Click Detector If you are using a SountTrap recording device with built in click detection from [Ocean Instruments](https://www.oceaninstruments.co.nz/), you may need to use a modified version of the [Click Detector](ClickDetector_clickDetector.html).  The SoundTrap click detector allows you to detect and store clicks at high frequencies (say 384kS/sec), suitable for odontocete echolocation clicks, and at the same time, record audio data files at a lower frequency (e.g. 96 or 48kS/sec). This optimises disk space usage and makes long deployments of several months possible without running out of data storage.  Having two sample rates present within a single PAMGuard configuration is possible using [Decimator modules. However such configurations become particularly complicated to configure when the sample rate of the recorded files is lower than the sample rate of the click detector. We therefore recommend that you use a modified version of the [Click Detector](ClickDetector_clickDetector.html), which manages it's own sample rate and channel information based on information extracted from the SoundTrap data. Note that the SoundTrap click detector should only be used for clicks automatically detected by the SoundTrap. If you want to detect clicks from the SoundTrap recordings, then use a normal [Click Detector](ClickDetector_clickDetector.html) in the normal way. Creating an instance of the SoundTrap Click Detector From the **_File>Add modules>Detectors_** menu, or from the pop-up menu on the data model display, select _'SoundTrap Click Detector'_ near the bottom of the Detectors list. Enter a descriptive name for the new detector and press Ok.  Importing SoundTrap Data SoundTrap data are stored in proprietary files called [SUD files](../../../sound_processing/AcquisitionHelp/docs/sudfiles.html). There are two ways in which you can get data from [SUD files](../../../sound_processing/AcquisitionHelp/docs/sudfiles.html) into the SoundTrap Click Detector.  The Old Way The 'standard' way of using SoundTrap data was to inflate all of the data from the compressed SUD files. For details of this process, see the SoundTrap user manuals and the SoundTrap Host software. Normally, several inflated files are generated from each sud file:

  1. A wav file: Audio data in standard wav file format
  2. An XML file: Metadata on the SoundTrap configuration, file start times in various formats, etc.
  3. If the click detector was running BCL and DWV files, which contain the times of clicks and click waveforms respectively.

To convert the SUD files to the binary storage format used by PAMGuard, working in the [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html), create a Binary Store, a SoundTrap Click Detector and also create a 'SoundTrap Detector Import' module. Then use the import module to import the BCL and DWV data into PAMGuard. Once imported you can run [Click Classifiers](ClickDetector_clickClassification.html) and use other Click Detector offline functions to mark events, etc.  If you want to run additional analysis on the WAV file data (for example to [make noise measurements](../../../sound_processing/NoiseBands/Docs/NoiseBands.html) or to [detect whistles](../../whistleMoanHelp/docs/whistleMoan_Overview.html)), create a different PAMGuard configuration to process those data. The (better) New Way Current versions of PAMGuard can read [SUD files](../../../sound_processing/AcquisitionHelp/docs/sudfiles.html) directly, without first unpacking them into WAV, XML, BCL and DWV files. This not only reduces the amount of disk space you need by about x4, but also saves a lot of time. Better still, you can now set up PAMGuard in normal mode to simultaneously process the audio data in the [SUD file](../../../sound_processing/AcquisitionHelp/docs/sudfiles.html) with one set of detectors, and simultaneously extract the click detector data into appropriate files for a SoundTrap Click Detector.  Start PAMGuard in [Normal Mode](../../../overview/PamMasterHelp/docs/normalMode.html) and add a [Sound Acquisition](../../../sound_processing/AcquisitionHelp/docs/AcquisitionOverview.html) module. Add a SoundTrap Click Detector, a [Binary Store](../../../utilities/BinaryStore/docs/binarystore_overview.html) store and a [Database](../../../utilities/generalDatabaseHelp/docs/database_database.html) module (optional). In the [Sound Acquisition dialog](../../../sound_processing/AcquisitionHelp/docs/AcquisitionConfiguration.html) select a single SUD file or a folder of SUD files. At this point, the SoundTrap Click Detector will be automatically configured with the correct sample rate (which won't be the sample rate displayed in the Sound Acquisition module).  Configure any [Click Classifiers](ClickDetector_clickClassification.html) you want to be run on the SoundTrap click data as it is imported. You can then add any other detectors and measurement processes you want to run on the SoundTrap audio data, this may include instances of the normal Click Detector module if you want to detect clicks in the lower frequency audio data. Process the data in the normal way and clicks will automatically be generated within the SoundTrap click detector Further process you data using the [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html) in the normal way. SoundTrap Dates and Timezones Soundtrap dates can be confusing (to be polite about it) sud file names generally use a local time for the computer that either set up the soundtrap, or downloaded the files from the soundtrap. However, the sud files contain a UTC time stamp, which will be correct if the local time and time zone on your computer were correct - basically, they will be correct if your computer is connecting to the internet and settings it's own clock. PAMGuard will extract this UTC time and use it for output file names and for data.  So, for example, if you were on the US East coast (UTC-5) and collected data at 1700 (5pm) on 10 August, 2025 you'd get a sud file called 8565.250810170000.sud, which would generate a binary file called SoundTrap_Click_Detector_SoundTrap_Click_Detector_Clicks_20250810_210000.pgdf, i.e. at 5pm on the East Coast, it's 2100 UTC.  When processing sud file data, you should therefore leave the timezone information in the Acquisition module on it's default setting of UTC - zero offset. If you've been fiddling with the time settings on your computer, this may no longer be the case in which case you may be able to correct the times in the data to UTC using the time zone settings in the Acquisition module. Generally though, you shouldn't touch it! Similarly, if you've unpacked the sud file data, the wav file will have the same name as the sud file. However, PAMGuard is smart, and will look in the xml file (again, has the same name) for the correct UTC time and use that. So keep all the files you extracted from the sud file together. You'll get different results if you take the wav files without the xml files to what you'll get if you keep the xml files with them wav's.

---

Click Detector Viewer Function Adding GPS offline The gpsData table in PAMGuard has a specific format which must be copied for offline analysis.   
  
UTC  
UTCMilliseconds  
PCLocalTime  
GpsDate  
PCTime  
GPSTime  
Latitude  
Longitude  
SpeedType  
Heading  
HeadingType  
TrueHeading  
MagneticHeading  
MagneticVariation  
GPSError  
DataStatus  
  
  
You have to add or imprt the gpsData table into your database for the offline analysis. If you followed the steps described in [creating binaries offline](../../../utilities/BinaryStore/docs/CreateBinariesOffline.html), you still need to add the GPS processing module **_File>Add module...>Maps and Mapping>GPS processing_** first time you open up PAMGuard Viewer. Also if you want to have a map you also need to add the module **_File>Add module...>Maps and Mapping>Map_**. You need to close PAMGuard Viewer after you added these modules, and open it up again so it can load the GPS data.

---

Click Detector Viewer Functions Importing RBC Files If you've used Rainbow Click or older versions of PAMGuard you may have click data saved in .clk files. PAMGuard requires binary files to display click data in the viewer mode however has the functionality to batch convert .clk files to binary format. Select **_Click Detection > Batch Convert .clk to .pgdf files_**. This will bring up the following window.   
  
  
  
Select the location of your .clk files and the location you want to save the new Binary file to, then click _'Start'_.   
  
[Previous: Click Viewer Overview](offline_Overview.html) [Next: Navigating through Data ](offline_Navigating.html)

---

Click Detector Viewer Functions Marking out Events The PAMGuard viewer click detector has the ability to allow manual marking of events. Events usually consist of a group of clicks associated with one acoustic encounter of a particular species. Event information is saved to the database and in certain situations, such as when using towed arrays, can be used to localise an animal�s position.   
  
**Creating an Event**   
  
To mark an event bring up the bearing/amplitude time display (in the Click Detector tab). The Figure shows a porpoise click train which needs to be marked and stored as an event. Note that the bearing time display is very similar to the online version just that instead of the trigger window you get a [Wigner plot ](http://en.wikipedia.org/wiki/Wigner_quasi-probability_distribution) as default setting.   
  
  
  
To see the classified clicks better (in this case porpoise clicks - the red triangles) you can untick the box at the top of the window _'Unclassified clicks'_. The bearing time display will now only show the classified clicks (depending on your settings you have more options i.e. echos, other species, etc.). Right click on a click and select _'Label click...'_.   
  
  
  
This will bring up the event dialog box. Here you can create a new event by selecting the _'New Event'_ button or add the click to an existing event by selecting any event from the Event list.   
  
  
  
When creating a new event the species/event type can be selected from the drop down menu. To add a new species/event type to the database simply right click on the drop down menu and select _'Edit list'_.   
  
  
  
In the Lookup Editor window select _'Add Item'_ and create a Code, Text and Symbol for your new species/event type. Press ok. Now you can select your new Event type/species from the drop down list in the click event dialog (previous figure). The Estimated number of animals and the comment are optional but always useful to fill in. (Note: Dont worry about the event number, PAMGuard creates an ID automatically.)   
  
  
  
Multiple clicks can be selected using the advanced area selection tool. Double click on the bearing/amplitude time display. This will bring up a dotted line which you can use to draw around a group of clicks. By joining up the ends of the dotted line the selected area should appear grey.   
  
  
  
All the clicks within this grey area can be added to an event by right clicking inside the grey area and selecting _'Label Clicks'_ or use the key short cut _'Ctrl+L'_. Note that there are also other options available such as removing clicks from a certain event etc. So if you made a mistake, don�t panick. Other useful tools for marking an event include the [amplitude selector](../../../detectors/clickDetectorHelp/docs/offline_tools.html) and [zoom selection](../../../detectors/clickDetectorHelp/docs/offline_tools.html).   
  
  
Following the described steps your event is saved. You can now localise it with [target motion analysis](../../../localisation/targetmotion/docs/targetmotion_Localising.html).   
  
All events are saved in the database. To get an overview about your events go to _'Click Detection'_ and then _'Show events'_.   
  
  
  
Depending on your selection you see all events in your data set or only the ones which are in the current period. Right click on an event and you have the option for editing, deleting or going to the event. This comes in handy when you want to move between events.   
  
  
  
Events which contain clicks with bearing information can be viewed in the map tab- simply right click and select _'Click Detector, Tracked Clicks'_. If using a towed array events can be localised using the [target motion analysis ](../../../localisation/targetmotion/docs/targetmotion_overview.html)module. Localisation information will also be displayed on the map.   
  
  
  
You can also send Events to [Rocca](../../../detectors/roccaHelp/docs/rocca_Overview.html) for click analysis. Go to _'Click Detection'_ and then _'Rocca Measurement'_. A new window will open up displaying all of the currently defined events. Select the events to analyse, and then click _'Analyze Selection'_   
  
  
  
[Previous: Navigating through Data](offline_Navigating.html) [Next: Reclassifying Click ](offline_Reclassify_clicks.html)

---

Click Detector Viewer Function Navigating through Data The PAMGuard viewer mode allows navigation through large data sets. The [data map](../../../utilities/datamap/docs/datamap.html) can be used to look at large volumes of data over long periods of time. Possible areas of interest can be found by using the datagram and click counter in the appropiate data density graphs (i.e. click detector-clicks). You can switch between the views by right clicking on the display.   
  
  
  
If an area of interest is found right click on the data map and select _'center data here'_ or _'start data here'_. This will load the clicks at this time into the bearing time display. Here individual clicks can be selected, viewed and [added to an event](offline_MarkingOutEvents.html).  
  
See [Viewer Overview](../../../overview/PamMasterHelp/docs/viewerMode.html) for more details on bearing time display navigation.   
  
[Previous: Importing Rainbow Click Files](offline_ImportingRBC.html) [Next: Marking out Events](offline_MarkingOutEvents.html)

---

Click Detector Viewer Function Click Viewer Overview An offline analysis functionality, which is similar to that in the IFAW RainbowClick software, is available in PAMGuard. To use this functionality, click detector data must be available in the [binary storage format](../../../utilities/BinaryStore/docs/binarystore_overview.html) and PAMGuard must be running in [Viewer Mode](../../../overview/PamMasterHelp/docs/viewerMode.html).  The click detector viewer mode provides several powerful tools for data analysis: 

  * The [data map](../../../utilities/datamap/docs/datamap.html) allows for rapid navigation and visualisation of large data sets. 
  * Events can be [manually marked](offline_MarkingOutEvents.html) and saved to a database.
  * Data collected using towed arrays can be localised using [target motion analysis](../../../localisation/targetmotion/docs/targetmotion_overview.html).

Beginning Analysis To begin using the offline click viewer you must have a set of [binary files](../../../utilities/BinaryStore/docs/binarystore_overview.html) and a [database](../../../utilities/generalDatabaseHelp/docs/database_database.html). If this is NOT the case, you have to [create binaries offline](../../../utilities/BinaryStore/docs/CreateBinariesOffline.html) (i.e. after you have collected you data in the field). Ideally the database will have been created whilst collecting data. It should then contain GPS co-ordinates, a list of the binary files, settings information etc. When you open up PAMGuardViewer the first option is to select the database.   
  
  
  
By selecting this database, viewer will load automatically the corresponding binary-, settings- and data files but sometimes it gets the directory path wrong (especially if your data is on an external hard drive and you plugged it in and out frequently). Just double check if the binary storage options are correct (Note: In this case the drive is wrong).   
  
  
  
The programme will then load the data and create the [data map](../../../utilities/datamap/docs/datamap.html) which can take a couple of minutes if you are opening up the data for the first time. Use the data map for [navigation](../../../detectors/clickDetectorHelp/docs/offline_Navigating.html). Now you start to [manually mark events](offline_MarkingOutEvents.html).   
  
  
  
If no database is available then a a new one must be created. Open PAMGuard viewer and select a blank database. A message will appear.   
  
  
  
Select _'OK'_. A dialog asking you to select binary files will then appear. Select the path to your processed binary files and click 'OK'. The following window should then appear. (Note that your binary files must contain .psfx files. These should be automatically created when binary files are created.)   
  
  
  
Right click on the small red triangle and select _'load settings from....'_. Clicks should then be loaded from the binary files and a datamap created.    
  
  
  
Selecting the Click Detector tab should bring up the clicks contained in the binary files. If GPS data needs to be added a new table must be manually created in the database. The table must be named _''gpsData'_ and should be in a specific format ([Adding GPS Data offline](../../../detectors/clickDetectorHelp/docs/offline_AddingGPSData.html)).   
  
  
[Next: Importing Rainbow Click files ](offline_ImportingRBC.html)

---

Click Detector Viewer Functions Reanalyse click types Sometimes your initial click parameter settings need changing. There is an option in Viewer to reanalyse click types which will change the corresponding binary files. Go to **_Click Detection>Reanalyse click types..._**.   
  
  
  
The click reprocessing window will open giving you various options to proceed. First you need to decide which data you would like to reclassify. The options are: Loaded data, all data or new data. To reprocess all data is time consuming so if you try out different parameter settings use the option of loaded data as it is much quicker. Once you are content with your new settings you can apply it to the whole data set. In the case of offline tasks which change database entries (not all do), checking the _Delete old database entries_ checkbox will ensure old entries are deleted and duplicates are not made.   
  
  
  
The parameters you can change are the same as in the[click detector](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickDetector.html). Reclassify clicks This brings you to the [click classification](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickClassification.html). Here you can change either the click parameters or add a new classification. Echo Detection This opens the Echo Detection window. Here you can change the parameters for echo classification. MORE JAMIE?   
  
Recalculate click delays This opens the delay measurement window. Here you can alter the setting of how click delays are calculated.   
  
  
  
You can choose either of the two options, both or none.   
  
**Option 1:** Filter data before measurement   
This option has the following settings:   
  
  
  
**Option 2:** Use wave form envelope. EXPLAIN?? MORE JAMIE. Click bearings This option allows zou to recalculate click bearings. JAMIE MORE.   
  
You can run all 4 reclassification options at once or tick the ones which are suitable for the analysis. Press _'Start'_ to beginn the recalculation process. You can see how far advanced the recaclulation process is in the progress bar. During re-processing the cancel button will change to a _'Stop'_ button and when the process is finished it will change to a _'Close'_ button. When recalculating large data sets the Close button might not appear after the last file. Just press Stop instead.

---

Click Detector Viewer Functions Reclassify click types Sometimes your initial click parameter settings need changing. There is an option in Viewer to reclassify click types which will change the corresponding binary files. Go to _'Click Detection'_ and click '_Reanalyse click types_ '.   
  
  
  
The click reprocessing window will open giving you various options to proceed. First you need to decide which data you would like to reclassify. The options are: Loaded data, all data or new data. To reprocess all data is time consuming so if you try out different parameter settings use the option of loaded data as it is much quicker. Once you are content with your new settings you can apply it to the whole data set. JAMIE: WHAT DOES the DELETE OLD DATABASE ENTRIES DO?   
  
  
  
The parameters you can change are:  Reclassify clicks This brings you to the [click classification](../../../detectors/clickDetectorHelp/docs/ClickDetector_clickClassification.html). Here you can change either the click parameters or add a new classification. Echo Detection This opens the Echo Detection window. Here you can change the parameters for echo classification.    
  
Recalculate click delays This opens the delay measurement window. Here you can alter the setting of how click delays are calculated.   
  
  
  
You can choose either of the two options, both or none.   
  
**Option 1:** Filter data before measurement   
This option has the following settings:   
  
  
  
**Option 2:** Use wave form envelope. This takes the Hilbert Transform of the wave and uses this to calculate time delays instead of the true waveform. This is useful for spectraly pure signals which have characteristic wave envelopes, such as Harbour Porpoise clicks.  Click bearings This option allows you to recalculate click bearings. These may have changed due to altering the spacing between hydrophone elements or perhaps you have recalculated the click delays, hence the bearings will have changed.   
  
You can run all 4 reclassification options at once or tick the ones which are suitable for the analysis. Press _'Start'_ to beginn the recalculation process. You can see how far advanced the recaclulation process is in the progress bar. During re-processing the cancel button will change to a _'Stop'_ button and when the process is finished it will change to a _'Close'_ button. When recalculating large data sets the Close button might not appear after the last file. Just press Stop instead.    
  
[Previous: Marking out Events](offline_MarkingOutEvents.html) [Next: Offline Click Analysis Tools ](offline_Tools.html)

---

Click Detector Viewer Functions Tools for marking an event   
There are a few tools which are useful whilst [marking an event](../../../detectors/clickDetectorHelp/docs/offline_MarkingOutEvents.html) or working with the click detector in [Viewer Mode](../../../overview/PamMasterHelp/docs/viewerMode.html).   


  1. Amplitude Selector
  2. Zoom Selection

Amplitude Selector The amplitude selector is effectivly a filter. It will only show you the clicks after a certain decibel threshold. This becomes useful if your data shows extensive background noise.   
  
  
  
You can also enhance a click event by applying the amplitude selector as it makes the click train usually cleaner, which will improve [Target Motion Analysis](../../../localisation/targetmotion/docs/targetmotion_overview). The amplitude selector can be activated by a right click on the bearing time display selecting _'Show amplitude selector'_.   
  
  
  
A window for the amplitude selector will open showing an amplitude histogram and some descriptive statistics from the loaded data. The default setting for minimum amplitude is set to zero. Go to the field and put a sensible number in judging by the histogram and tick the box _'Display only clicks of set amplitude'_. A red line appears in the histogram which you can drag with the mouse which changes the amplitude threshold. The clicks shown in the bearing time display change accordingly. When the amplitude selector is active a message appears at the bottom of the bearing time display which reads how many clicks are not displayed and the amplitude threshold currently set.   
  
  
  
Zoom Selection There are different ways to zoom. A simple rectangular area of clicks can be selected by clicking on the bearing time display and dragging the mouse. A black square will apear which you can zoom into with a double click.   
  
  
You can also use the advanced area selection tool (the one where it gets grey) for more complex click sections. Same principle applies. Alternatively to the double click to zoom you can also right click and select _'Zoom in'_. Right click and select _'Zoom right out'_ to return to the full click display.  
  
  
[Previous: Reclassifying Clicks](offline_Reclassify_clicks.html)
