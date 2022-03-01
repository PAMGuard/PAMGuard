# Click Train Detector

## Overview

When a toothed whale, bat or other echolocator uses echolocation for hunting or sensing their surroundings they usually produce regular clicks/calls which vary slowly in inter-click/call-interval, amplitude, bearing etc. Individual click detections can be difficult to classify from other random transients because recieved waveforms and spectra are distorted by number of factors, such as narrow beam profiles, frequency dependent absorption, propogation effects and animal behaviour. The broadband clicks of many dolphins psecies are especially difficult to distinguish because they are very similar to many other sources of transient noise, such as cavitations from ship propellors. However, the echolocation clicks used by toothed whales (and bats) are not produced in isolation - animals tend to rapidly produce clicks with a slowly varying inter-click-interval (ICI); there are very few non-biological sources which produce regular repetitive sound and so this provides an additional contextual dimension for click classification. An automated algorithm which is based on identifying repeating patterns of sounds therefore has the potential to be significantly more accurate than an algorithm based on identifying individual calls. 

The PAMGuard click train detector module is used to detect and then classify repeating patterns of clicks. It is designed to work with multiple types of acoustic data, from CPOD detections to single channel and multi-channel hydrophone recordings. 

## How it works 

PAMGuard’s click train detector utilises both a detection and classification stage to extract click trains from recordings. 

The detection stage is currently based on a multi hypothesis tracking (MHT) algorithm. This algorithm considers all possible combinations of transient detections creating a large hypothesis matrix which holds potential click trains. As more clicks are added to the hypothesis matrix it grows exponentially and so, to prevent a computer running out memory, it is regularly “pruned” to keep only the most likely click trains over time. The assigned likelihood of a click train is based on number of properties which can be defined in by the user. For example, a user might select, ICI, Amplitude and Correlation as variables to score click trains; this would mean that combinations of clicks with slowly changing ICI, amplitude and waveforms would be favoured by the algorithm and stay in the hypothesis matrix. Other properties such as bearing, click length and peak frequency can also be selected.  A graphical explanation of the click train detection algorithm is shown in Figure 1 and a more detailed explanation of the be found in Macaulay (2019). 

<p align="center">
  <img width="930" height="900" src = "resources/mht_diagram.png">
</p>

_Diagram demonstrating how the click train algorithm works. Black dots are a set of 14 detected clicks at times t1 to t14. The click train algorithm begins at click 1 and creates two possible clicks trains, one that includes the first click (filled circle) and the other in which the click is not part of the click train (non-filled circle). The algorithm then moves to the next click and adds it to the hypothesis matrix. As the number of clicks increases, the hypothesis matrix exponentially expands in size and must be pruned. After a minimum of Npmin clicks (in this case 4) each track hypothesis (possible click train) is assigned a χ<sup>2</sup> score. The track hypothesis with lowest score (defined by larger coloured circles) has it’s branch traced back Np (in this case 3) clicks. Any track hypothesis which do not include the click Np steps back are pruned (defined by the double lines). Clicks which share no click associations with the first track hypothesis are then pruned and the process repeats until all clicks are part of a track or a maximum number of tracks have been considered (in this example there are two tracks). The algorithm then moves to the next click, adds it to the hypothesis matrix, assigns χ<sup>2</sup> scores and traces the lowest χ<sup>2</sup> branch Np steps back, pruning the hypothesis  matrix again; the process repeats until the last click. Note that there is always a track hypothesis with no associated clicks (i.e. the bottom-most branch where no clicks belong to a click train). If a track hypothesis is confirmed and thus removed from the hypothesis matrix, then this track can be used to start another click train_

The advantage of this MHT approach is that the click train detection module is quite general and can cope with a large variety of complex situations and multiple overlapping click trains. The disadvantage is that there are a large number of potential variables which can be set that affect the performance of the detector which can make it complex to initially set up.  

The subsequent classification stage attempts to classify detected click trains to species. Classification is currently based on a series of relatively simple binary classification steps but there is scope for machine learning approaches in future versions. The binary classification is based on parameters such as number of detected clicks, the mean and standard deviation in ICI and bearing and the correlation of the average spectrum of the click train with a predefined spectral template. 

A click train which has been both detected and classified is saved to PAMGuard’s database and can be reclassified in PAMGuard’s viewer mode. 


## Configuring the click train detector

The primary settings to configure can be split into MHT Kernel and χ^2 settings – these are all set in the primary click train detector dialog as shown in Figure 2. 

<p align="center">
  <img width="850" height="700" src = "resources/detection_pane.png">
</p>

_The settings pane of the click train detector._


### MHT Kernel Settings
The MHT Kernel is the part of the detection algorithm which creates and then prunes the large hypothesis matrix which keeps a copy of all possible click trains. MHT Kernel settings are therefore important because they influence speed (a larger number of possible click trains in memory is more processor intensive) and the quality of the detections (the larger the number of possibilities the more likely that “true” click trains are contained in the hypothesis matrix). The specific settings are;

***Prune-back***: The hypothesis matrix needs pruned so that it does not grow exponentially and cause memory issues. The matrix is pruned at Np (see Figure 1) previous detections i.e. if Np is 5 then then then the algorithm selects the most likely click train, moves back five detections back and discards other hypothesis that do not contain the combination of clicks in that branch. Thus, increasing the prune-back means that more hypothesis are kept at any one time but decreasing will lead to faster processing times as less combination are kept in memory. 

***Prune-start***: The initial number of detections before the pruning process starts. This cannot be less than Prune-back and should generally should be set no more than 15 for 8GB of memory. 

***Max no. coasts***: A click train is saved and removed the hypothesis mix once it has passed a number of tests. It must be over three clicks long, survive the pruning process and have missed the max no. coasts. A coast is when a click has been missed from a click train based on ICI. i.e. if the ICI is 2s and a click train goes for 6s without a detected click then there have been 3 coasts. Increasing the maximum number of coasts means that click trains are less fragmented but can come at the cost of keeping click trains in the hypothesis matrix for longer which have ended. 

***Max no. trains***: This is a maximum allowed number of trains in the hypothesis mix. Note this refers to the number of trains which can survive pruning – the actual number of potential click trains in the hypothesis mix will be much larger. Generally, just via pruning, the hypothesis matrix will keep itself below the max no. trains, however, in certain situations it can grow too large and requires a limit. The max no. trains therefore usually have little effect on results but should generally be set to less than 50 to ensure smooth processing  

### χ<sup>2</sup> Settings
χ<sup>2</sup> is a measure of the likelihood that a click train is from a (usually) biological source. The higher the χ<sup>2</sup> value the lower the quality of a click train. 

The χ<sup>2</sup> model used in the click train detector considers both the slowly varying properties of click trains, as well as bonus and penalty factors to discourage fragmentation and aliasing (selecting a multiple of the true ICI) of detected click trains. 

The initial basis of the model is: 

<p align="center">
  <img width="550" height="100" src = "resources/mht_equation.png">
</p>

where _m_ is the number of selected descriptors, e.g. ICI, amplitude, bearing etc., and _y(i,k)_ is the measurement of descriptor _i_ for click _k_ in a click train with n associated clicks.  _t(k+1)_ is the measured time of a click _k_. Each descriptor is divided by q<sub>i</sub> which is a user tuneable parameter that alters the importance each descriptor has on the total χ<sup>2</sup>. Ideally it should correspond to a prediction of the likely variance of the descriptor. 

The descriptors can be enabled and the variance set in the χ<sup>2</sup> Settings pane. The toggle button next to each descriptor sets whether a descriptor is used to score a click train and the variance is then set using the slider or by inputting manually by clicking the settings cog. Increasing the variance means that the descriptor has less of an influence on the calculation of χ<sup>2</sup> and decreasing means that the descriptor has a larger influence on χ<sup>2</sup>.  In some cases, clicks can be so close together that the variance is tiny and thus χ<sup>2</sup> becomes huge e.g. during buzzes. A minimum variance value (_qt<sub>i</sub>_) prevents the variance _(max⁡(q<sub>i</sub> (t<sub>(k+1)</sub>-t<sub>k</sub> ),qt<sub>i</sub> )<sup>2</sup>)_ from falling below very low values. 

Ideally the variance for each parameter would be calculated from a test dataset of manually annotated click trains e.g. by calculating the variance of ICI of all marked click trains.

 
<p align="center">
  <img width="900" height="120" src = "resources/varience_pane.png">
</p>

_Each descriptor has a variance setting which can be changed by moving the slider or manually inputting data by clicking the settings button. Variance is multiplied by the ICI for each click detection because clicks closer together in time the descriptor values will change less. In some cases, clicks can be so close together that the variance is tiny and thus χ<sup>2</sup> in Eq. 1 becomes huge e.g. during buzzes. A Min. Error prevents the variance from falling below very low values._ 

The available descriptors parameters can be set in the click detector settings pane (Figure 3) and works as follows;

***IDI:*** the inter-detection-interval in milliseconds. The algorithm looks for slowly changes in the interval between detections. 

***Amplitude:***  the amplitude in dB re 1/muPa pp. The algorithm looks for slowly changing amplitude values. Note that the algorithm is comparing the change in change in amplitude so that the click train algorithm is not biased against large but consistent changes in amplitude (e.g. due to a narrow beam profile sweeping across a hydrophone). 

***Bearing:*** the bearing of multi-channel clicks in degrees. Slowly changing bearings will increase the likelihood that click trains are detected. Note that in a similar way to Amplitude, the change in change in bearing is considered so that the algorithm is not biased against large but consistent changes in bearings. The bearing parameter has some additional settings which apply a large penalty to clicks trains if there is a large (user-defined) jump in bearing. 

***Correlation:*** the algorithm calculates the peak of the cross-correlation value of subsequent clicks and looks for slowly changing values in the cross-correlation value. This tells the click train algorithm to search for clicks with consistent/slowly changing spectra. The correlation descriptor also has some additional settings which allow the user to pre-filter waveforms before cross-correlation. This is especially useful in removing noise from higher frequency detections. 

***Time Delays:*** the time delay between multi-channel clicks in milliseconds. The algorithm looks for slowly changing values in the time delays between multichannel clicks. This is useful for arrays with more than two hydrophones where an error in a single time delay measurement may cause a substantial error in bearing. Like amplitude and bearing, the time delay values are the change in change in time delays between subsequent clicks to ensure that click trains are not biased against faster changes in bearing. 

***Click Length:***  the length of the saved waveform of a click in milliseconds. This is a crude measure of the length of a click; however, it can be useful in helping the algorithm distinguish between species with long multi-modal clicks such as sperm whales, and much shorter broadband clicks such as dolphins.

***Peak Frequency:*** the peak frequency in Hz. The peak frequency between subsequent clicks is used score click trains. This is useful for click trains with very stable peak frequencies such as echosounders, narrow band high frequency species and perhaps some beaked whale species. 

### Advanced χ<sup>2</sup> Settings
The descriptors used in Eq. 1 on their own do not provide a good score for click train detections. This is because Eq.1 can achieve the same score by either skipping clicks e.g. every second click in a click train, or by splitting click trains into smaller fragments. 

<p align="center">
  <img width="500" height="350" src = "resources/advanced_pane.png">
</p>
 
_The advanced settings for calculating χ^2. These parameters are primarily used to prevent click train aliasing and fragmentation.
The advanced settings (see Figure 4) are a series of additional factors that prevent aliasing and fragmentation and work as flows._ 

***Low ICI Bonus:***  if the median ICI of the possible click train is above a specified maximum value, a large penalty is added which effectively makes it one of the least likely click trains in the hypothesis matrix. If the median ICI is below the maximum value then χ<sup>2</sup>  = (χ<sup>2</sup> (I/(max<sub>k</sub>⁡*I<sub>k</sub>))<sup>LI</sup> where  I is the median ICI, max<sub>k</sub>⁡*I<sub>k</sub> is the maximum ICI in the possible click train and LI is the low ICI Bonus constant term. This bonus term favours lower ICI values, preventing aliased click trains. 

***Long track bonus:*** add a bonus factor for longer click trains to prevent fragmentation. This is the total length of the click train in seconds divided by the total hypothesis matrix time in seconds L which is then multiplied so that χ<sup>2</sup> = (χ<sup>2</sup>*L)<sup>LT</sup> where LT is the long track bonus. 

***Coast penalty:***  add a penalty for “coasting” i.e. when an expected click, based on ICI, is not present in the click train. This penalty is multiplied by the number of coasts i.e. the likely number of missed clicks based on ICI  

***New Track Penalty:***  if a track hypothesis is newly added in the hypothesis matrix, then add a minor penalty factor. This is added until the number of click trains exceeds No. New Track Clicks

## Classification

The classification process attempts to assign a species identification to each detected click trains. Currently there is only one implemented classifier, a simple binary classifier which tests user defined parameters (e.g. IDI, bearing, spectral correlation and classifies each click). Classification parameters are accessed via the classification tab in the settings dialog.

There is currently a basic spectral correlation/IDI/bearing classifier; more complex classifiers can be implemented in the future. 

<p align="center">
  <img width="510" height="800" src = "resources/classifier_pane.png">
</p>

_The classifier settings. Users can add multiple classifiers using the + button next to the classifier tabs.  Each classifier allows the user to choose a number of different approaches to classification based on the goodness of fit, inter-click interval, average spectra and bearings of the click trains. Users can use just one or all of these options and set specific parameters for each._ 

Users can add multiple classifiers by selecting the + button next to the classification tabs. Each classifier allows the user to choose a number of different methods for click train classification based on the goodness of fit, inter-click interval, average spectra and/or bearings of the click trains; for a click train to be classified it must pass all enabled methods (use toggle switches to enable and disable different types of classification). The different classification methods. 

### χ<sup>2</sup> threshold classifier
The click train is classified if it’s overall χ<sup>2</sup> value is lower than the set χ<sup>2</sup> Threshold and it has more than Min. Clicks and the time between the first and last click is greater than Min. Time

### IDI Classifier
The click train is classified if the median/mean and standard deviation in the inter detection interval (IDI) between subsequent clicks are within user defined limits. 

### Spectrum Template Classifier
The click train is classified if the average spectra of the click train has a correlation value above Spectrum Correlation Threshold with a user defined spectral template. The template can be set using the button on the top right of the spectrum plot – a default spectrum can be loaded or a spectrum can be loaded from a .mat or .csv file. A csv file should have the first row as the spectrum and first column of the second row the sample rate.  A .mat file should be a single saved structure with sR (sample rate) and spectrum (array of spectrum values) fields. 

### Bearing Classifier
The click train is classified if minimum and maximum bearing (Bearing Limits) the average change in bearing ( Bearing Mean), the median change in bearing ( Bearing Median) and/or the average standard deviation in bearing change ( Bearing Std) are within user defined limits. 


## Parametrising the classifier

Each classifier has a set of metadata that are added to click trains. This can be accessed through the tooltip or right click menus in various displays. For example, in the Time Base Display FX hover the mouse over a click train or bring the pop menu with a right click. Parameters such as the spectral correlation value, IDI and bearing information etc are displayed which allows users to get an idea of which values to set for the classifier. Currently this requires (like most PAMGuard classifiers) a trial and error approach. It is hoped that future update will allow manually validated data to be used to parametrise both the detection and classification stage of the click train detector. 

<p align="center">
  <img width="700" height="500" src = "resources/rightclickmenu.png">
</p>

_The metadata associated with each classifier is stored with every click train and be accessed through right clicking on or hovering the mouse over a click train detection._ 

## Localisation
The click train detector can be used to localise the position of animals detected by the click train detector using target motion analysis. This generally means that the localisation capabilities are generally restricted to data which has been collected using towed hydrophone arrays. 
 
<p align="center">
  <img width="242" height="430" src = "resources/localisation1.png">
</p>

_Screenshot of the click train localisation settings. Currently, only target motion is supported._ 

Localisation is enabled by ticking Localise click trains. The type of localisation algorithm which is used is selected in the Localisation algorithms (See the localisation section in PAMGuard help for more info on localisation algorithms). Localisation using 3D simplex and MCMC can be processor intensive, especially when there are a large number of clicks in a train and so the Algorithm Limits pane can be used to set a maximum number of input clicks for a localisation. If the maximum is exceeded then clicks are sub sampled from the click train evenly in time.   

Generally, target motion localisation only works well when there are a large number of clicks over a long time period. The Filters tab allows users to select which click trains are localised and also to remove spurious results from unsuccessful localisations. The Pre Localisation Filter allows users to select a minimum number of detections before localisations are attempted and a minimum bearing change in the click train (Min Angle range). Click trains with larger angle ranges will generally result in higher quality localisations.  
 
<p align="center">
  <img width="242" height="430" src = "resources/localisation2.png">
</p>

_The filter tab allows users to pre-filter which click train are localised._

The Results Filter allows for spurious localisation results to be deleted: any results from target motion localisation (which can have more than one possible localisation) which are further away than Maximum Range, shallower than Minimum Depth or deeper than Maximum Depth are discarded.  
Running
The click train detector can be run in real time or post processing. In real time add the module and it will automatically detected click trains once PAMGuard started. 

 <p align="center">
  <img width="200" height="300" src = "resources/offlineprocessing.png">
</p>

_The filter tab allows users to pre-filter which click train are localised._ 

In viewer mode, add the module and then go to Settings>Click Train Detector > Reanalyse click trains….This will bring up PAMGuard’s generic data reprocessing dialog with two settings, Click Train Detector or Click Train Classifier. The Click Train Detector option will run the detection and classification algorithm again. The Click Train Classifier will only run the classification algorithm on existing detected click trains (much faster). Note that users can select how much data to reprocess in the Data dropdown menu – All Data means the entire dataset will be reprocessed, Loaded Data means just the current data loaded in the display (all scrollable data), Select Data allows the user to define two time limits between which all data is reprocessed. 

## Visualising Results
The results from the click train detector can be visualised in a variety of displays in PAMGuard. 

### Click bearing time display
By default, clicks trains will be shown in the Click Detector Module’s in built bearing time display. Different click trains are represented as different colours. Note that you must right click on the display and select Colour by Click Trains 

<p align="center">
  <img width="940" height="500" src = "resources/clicktrain_BT.png">
</p>

_The results of the click train detector displayed on the bearing time display. Different colours correspond to different click trains._

### Time Display FX
The Time Display FX is a more modern display which allows any time-based data to be plotted together on a large variety of y-axis (e.g., frequency, bearing, amplitude etc.). Click trains will be plotted on the time-based display by adding Click detections to the display and then using the right   


<p align="center">
  <img width="940" height="500" src = "resources/clicktrain_TDFX.png">
</p>

_Click train data displayed in the time display FX. Users can right click on click trains to view average spectra and waterfall spectrograms (shown here in top right)._
