# Click Train Detector

## Overview

When a toothed whale, bat or other echolocator uses echolocation for hunting or sensing their surroundings they usually produce regular clicks/calls which vary slowly in inter-click/call-interval, amplitude, bearing etc. Individual click detections can be difficult to classify from other random transients because recieved waveforms and spectra are distorted by number of factors, such as narrow beam profiles, frequency dependent absorption, propogation effects and animal behaviour. The broadband clicks of many dolphins psecies are especially difficult to distinguish because they are very similar to many other sources of transient noise, such as cavitations from ship propellors. However, the echolocation clicks used by toothed whales (and bats) are not produced in isolation - animals tend to rapidly produce clicks with a slowly varying inter-click-interval (ICI); there are very few non-biological sources which produce regular repetitive sound and so this provides an additional contextual dimension for click classification. An automated algorithm which is based on identifying repeating patterns of sounds therefore has the potential to be significantly more accurate than an algorithm based on identifying individual calls. 

The PAMGuard click train detector module is used to detect and then classify repeating patterns of clicks. It is designed to work with multiple types of acoustic data, from CPOD detections to single channel and multi-channel hydrophone recordings. 

## How it works 

PAMGuard’s click train detector utilises both a detection and classification stage to extract click trains from recordings. 

The detection stage can use one of three interchangeable algorithms, selected from the **Click Train Detector Algorithm** dropdown in the settings pane (Figure 2):

- **MHT detector** – the original multi-hypothesis tracking algorithm (described immediately below). Very general and powerful but has a large number of parameters to tune.
- **Adaptive MHT detector** – uses the same multi-hypothesis search but with a self-calibrating scoring function that learns each train’s behaviour, so it needs only a handful of intuitive parameters. See *The Adaptive Detector* below.
- **UKF detector** – a tracking-by-detection approach based on an Unscented Kalman Filter, a learned affinity metric and two-stage assignment. See *The UKF Detector* below.

All three take the same click detections as input and produce click trains; they differ only in how they decide which clicks belong to the same train. The classification, localisation and visualisation stages described later are shared by all three algorithms.

The detection stage is currently based on a multi hypothesis tracking (MHT) algorithm. This algorithm considers all possible combinations of transient detections creating a large hypothesis matrix which holds potential click trains. As more clicks are added to the hypothesis matrix it grows exponentially and so, to prevent a computer running out memory, it is regularly “pruned” to keep only the most likely click trains over time. The assigned likelihood of a click train is based on number of properties which can be defined in by the user. For example, a user might select, ICI, Amplitude and Correlation as variables to score click trains; this would mean that combinations of clicks with slowly changing ICI, amplitude and waveforms would be favoured by the algorithm and stay in the hypothesis matrix. Other properties such as bearing, click length and peak frequency can also be selected.  A graphical explanation of the click train detection algorithm is shown in Figure 1 and a more detailed explanation of the be found in Macaulay (2019). 

![](resources/mht_diagram.png)

_Diagram demonstrating how the click train algorithm works. Black dots are a set of 14 detected clicks at times t1 to t14. The click train algorithm begins at click 1 and creates two possible clicks trains, one that includes the first click (filled circle) and the other in which the click is not part of the click train (non-filled circle). The algorithm then moves to the next click and adds it to the hypothesis matrix. As the number of clicks increases, the hypothesis matrix exponentially expands in size and must be pruned. After a minimum of Npmin clicks (in this case 4) each track hypothesis (possible click train) is assigned a χ<sup>2</sup> score. The track hypothesis with lowest score (defined by larger coloured circles) has it’s branch traced back Np (in this case 3) clicks. Any track hypothesis which do not include the click Np steps back are pruned (defined by the double lines). Clicks which share no click associations with the first track hypothesis are then pruned and the process repeats until all clicks are part of a track or a maximum number of tracks have been considered (in this example there are two tracks). The algorithm then moves to the next click, adds it to the hypothesis matrix, assigns χ<sup>2</sup> scores and traces the lowest χ<sup>2</sup> branch Np steps back, pruning the hypothesis  matrix again; the process repeats until the last click. Note that there is always a track hypothesis with no associated clicks (i.e. the bottom-most branch where no clicks belong to a click train). If a track hypothesis is confirmed and thus removed from the hypothesis matrix, then this track can be used to start another click train_

The advantage of this MHT approach is that the click train detection module is quite general and can cope with a large variety of complex situations and multiple overlapping click trains. The disadvantage is that there are a large number of potential variables which can be set that affect the performance of the detector which can make it complex to initially set up.  

The subsequent classification stage attempts to classify detected click trains to species. Classification is currently based on a series of relatively simple binary classification steps but there is scope for machine learning approaches in future versions. The binary classification is based on parameters such as number of detected clicks, the mean and standard deviation in ICI and bearing and the correlation of the average spectrum of the click train with a predefined spectral template. 

A click train which has been both detected and classified is saved to PAMGuard’s database and can be reclassified in PAMGuard’s viewer mode. 


## Configuring the click train detector

The primary settings to configure can be split into MHT Kernel and χ^2 settings – these are all set in the primary click train detector dialog as shown in Figure 2. 

![](resources/detection_pane.png)

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

![](resources/mht_equation.png)

where _m_ is the number of selected descriptors, e.g. ICI, amplitude, bearing etc., and _y(i,k)_ is the measurement of descriptor _i_ for click _k_ in a click train with n associated clicks.  _t(k+1)_ is the measured time of a click _k_. Each descriptor is divided by q<sub>i</sub> which is a user tuneable parameter that alters the importance each descriptor has on the total χ<sup>2</sup>. Ideally it should correspond to a prediction of the likely variance of the descriptor. 

The descriptors can be enabled and the variance set in the χ<sup>2</sup> Settings pane. The toggle button next to each descriptor sets whether a descriptor is used to score a click train and the variance is then set using the slider or by inputting manually by clicking the settings cog. Increasing the variance means that the descriptor has less of an influence on the calculation of χ<sup>2</sup> and decreasing means that the descriptor has a larger influence on χ<sup>2</sup>.  In some cases, clicks can be so close together that the variance is tiny and thus χ<sup>2</sup> becomes huge e.g. during buzzes. A minimum variance value (_qt<sub>i</sub>_) prevents the variance _(max⁡(q<sub>i</sub> (t<sub>(k+1)</sub>-t<sub>k</sub> ),qt<sub>i</sub> )<sup>2</sup>)_ from falling below very low values. 

Ideally the variance for each parameter would be calculated from a test dataset of manually annotated click trains e.g. by calculating the variance of ICI of all marked click trains.

 
![](resources/varience_pane.png)

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

![](resources/advanced_pane.png)
 
_The advanced settings for calculating χ^2. These parameters are primarily used to prevent click train aliasing and fragmentation.
The advanced settings (see Figure 4) are a series of additional factors that prevent aliasing and fragmentation and work as flows._ 

***Low ICI Bonus:***  if the median ICI of the possible click train is above a specified maximum value, a large penalty is added which effectively makes it one of the least likely click trains in the hypothesis matrix. If the median ICI is below the maximum value then χ<sup>2</sup>  = (χ<sup>2</sup> (I/(max<sub>k</sub>⁡*I<sub>k</sub>))<sup>LI</sup> where  I is the median ICI, max<sub>k</sub>⁡*I<sub>k</sub> is the maximum ICI in the possible click train and LI is the low ICI Bonus constant term. This bonus term favours lower ICI values, preventing aliased click trains. 

***Long track bonus:*** add a bonus factor for longer click trains to prevent fragmentation. This is the total length of the click train in seconds divided by the total hypothesis matrix time in seconds L which is then multiplied so that χ<sup>2</sup> = (χ<sup>2</sup>*L)<sup>LT</sup> where LT is the long track bonus. 

***Coast penalty:***  add a penalty for “coasting” i.e. when an expected click, based on ICI, is not present in the click train. This penalty is multiplied by the number of coasts i.e. the likely number of missed clicks based on ICI  

***New Track Penalty:***  if a track hypothesis is newly added in the hypothesis matrix, then add a minor penalty factor. This is added until the number of click trains exceeds No. New Track Clicks

## The Adaptive Detector

The Adaptive detector was designed to address the main drawback of the MHT detector: the large number of interacting parameters (a variance coefficient and minimum error for every descriptor, plus several bonus/penalty factors). It uses the **same multi-hypothesis tracking search** as the MHT detector — so it copes equally well with multiple overlapping click trains — but replaces the χ<sup>2</sup> scoring with a **self-calibrating, predictive-residual** model that needs only a handful of intuitive settings.

### How it scores click trains

For each possible click train the algorithm predicts the next click’s properties (inter-click interval, amplitude and, where available, bearing) from the train’s own recent history. When a new click is considered for the train, the algorithm measures the *residual* between the observed and predicted value and standardises it using a scale that it **estimates from the train itself** (a robust median-absolute-deviation of recent residuals). In other words, instead of you telling the algorithm how much each descriptor is allowed to vary, the algorithm learns the expected variability of each train as it grows. A small physically-meaningful “floor” (scaled by a single sensitivity control) stops a perfectly regular train from rejecting tiny deviations.

The residuals from each enabled feature are combined into a single robust score (a Huber loss makes it tolerant to the occasional outlier click), and every click that fits well earns a small “reward” that favours longer, complete trains and prevents fragmentation. Gaps are bridged automatically by recognising that a click arriving after a pause spans several inter-click intervals (coasting), and trains whose median ICI exceeds the maximum are rejected.

Because the score is normalised by the number of available features, the **same settings work whether or not bearing or waveform data are present**, which makes the detector suitable for single-channel data (no bearing), multi-channel data (with bearing) and data without waveforms.

### Settings

***Max. ICI:*** the absolute maximum inter-click interval (in seconds) allowed within a click train. Trains whose median ICI exceeds this are rejected.

***Sensitivity:*** a single slider from *loose* to *tight*. Loose detects more clicks but may merge nearby trains; tight is stricter and produces cleaner but possibly more fragmented trains. This scales the minimum residual “floor” used for every feature, replacing the per-descriptor variance settings of the MHT detector.

***Detection probability:*** the expected fraction of clicks that are detected (0–1). Lower values let the detector bridge more missed clicks (coasts); higher values keep trains tighter.

***Features used:*** toggle which click properties are used to group clicks. *Inter-click interval (ICI)* and *Amplitude* are available for any data; *Bearing* is enabled only when the source provides a bearing; *Waveform correlation* (which groups clicks by waveform similarity, and also refines the ICI measurement) is enabled only when waveform data are present. Features that the data cannot provide are greyed out automatically.

## The UKF Detector

The UKF detector takes a different, “tracking-by-detection” approach borrowed from modern multi-object tracking. Rather than searching all possible combinations of clicks, it maintains a set of active tracks and, as each new batch of clicks arrives, decides which click (if any) extends each track. The pipeline is:

```
click detections
  → UKF state prediction      (predict each track's next click)
  → learned affinity metric   (a small neural network scores each pairing)
  → two-stage assignment      (Hungarian algorithm, ByteTrack style)
  → UKF state update          (correct each track with its matched click)
```

### Unscented Kalman Filter

Each track is modelled by an **Unscented Kalman Filter (UKF)**. The filter’s state holds the (log) inter-click interval together with a slow drift term, plus amplitude and bearing when those features are enabled. The UKF predicts where and when the next click of a train should appear, and after a click is assigned it updates the track’s state. The “unscented” formulation correctly handles the non-linear parts of the model — the logarithmic ICI (which keeps intervals positive and treats proportional changes consistently) and the circular nature of bearing.

### Learned affinity metric

For every candidate (track, detection) pairing the detector builds a short feature vector — chiefly the *Mahalanobis distance* between the detection and the track’s prediction, plus the individual ICI, amplitude and bearing innovations and how overdue the track is — and passes it through a small neural network that returns an **affinity** between 0 and 1 (1 = a certain match). By default a built-in network reproduces a sensible statistical gate (high affinity when a detection lies close to the prediction, falling off smoothly with distance). Advanced users can supply their own trained network instead (see *Custom affinity network* below).

### Two-stage (ByteTrack) assignment

The affinities become costs in a **Hungarian assignment**, which finds the globally best matching of clicks to tracks in each batch (this is what lets the detector resolve ambiguous situations where two clicks could each belong to either of two tracks). Following the ByteTrack approach, assignment is done in two stages: loud, **high-confidence** clicks are matched first, then the remaining quieter, **low-confidence** clicks are matched against any tracks that are still unassigned. The amplitude threshold separating high- from low-confidence clicks is set by the user, so they can decide how the data are split. Unmatched high-confidence clicks start new tracks; tracks that miss too many clicks are closed and saved.

### Settings

***Max. ICI:*** the absolute maximum inter-click interval (seconds) allowed within a train.

***Frame window:*** the duration of the batching window. Clicks within a window are assigned jointly by the Hungarian algorithm. It should be set below the shortest expected ICI so that successive clicks of one train fall in different windows.

***Max. coasts:*** the maximum number of consecutive missed clicks before a track is closed.

***Min. train length:*** the minimum number of clicks for a track to be saved.

***Features used:*** ICI is always tracked; *Amplitude* and *Bearing* (when available) can be toggled on or off.

***Two-stage association:*** enable the ByteTrack-style high/low-confidence split, and set the **Confidence amplitude** (dB) threshold that separates the two. Set the threshold to 0 to treat all clicks as high-confidence (a single assignment stage).

> **Note:** if you enable two-stage association *and* track amplitude, the amplitude model can itself reject the quiet clicks the second stage is meant to recover (because they genuinely differ in amplitude). For low-confidence recovery driven purely by timing, turn the *Amplitude* feature off.

### Custom affinity network

The affinity metric is a small multi-layer perceptron (MLP). The repository ships with default weights that reproduce a Gaussian/Mahalanobis gate, but you can train your own network and load it from a JSON file by ticking **Use custom affinity network** and selecting the file. A malformed file, or one whose input size does not match, is ignored and the default network is used (a warning is logged).

The JSON file describes the network’s weights and biases:

```json
{
  "weights": [
    [[-0.5, 0.0, 0.0, 0.0, 0.0]],
    [[4.0]]
  ],
  "biases": [
    [3.0],
    [0.0]
  ]
}
```

- **`weights`** is a 3-D array indexed `[layer][output node][input node]`.
- **`biases`** is a 2-D array indexed `[layer][output node]`.
- The number of layers is the length of `weights` (which must equal the length of `biases`). Each hidden layer uses a **tanh** activation; the final layer uses a **sigmoid**, so the network’s single output is an affinity in the range 0–1.
- The input dimension (the length of each inner row of the first layer’s weights) **must be 5** — the size of the feature vector. The five inputs, in order, are:
  - **0** – Mahalanobis distance between the detection and the track’s predicted measurement.
  - **1** – normalised absolute log-ICI innovation.
  - **2** – normalised absolute amplitude innovation (0 when amplitude is not tracked).
  - **3** – normalised absolute bearing innovation (0 when bearing is not tracked).
  - **4** – how overdue the track is (time since its last click ÷ its expected ICI).

The example above is the built-in default network: a single `5 → 1 → 1` gate whose hidden unit computes `tanh(3.0 − 0.5 × Mahalanobis)` and whose output is `sigmoid(4.0 × hidden)`. This gives an affinity of ~1 for a detection right on the prediction, 0.5 at a Mahalanobis distance of 6, and ~0 beyond. A trained network can use more hidden units and make use of all five inputs; simply provide the corresponding `weights` and `biases` arrays.

## Classification

The classification process attempts to assign a species identification to each detected click trains. Currently there is only one implemented classifier, a simple binary classifier which tests user defined parameters (e.g. IDI, bearing, spectral correlation and classifies each click). Classification parameters are accessed via the classification tab in the settings dialog.

There is currently a basic spectral correlation/IDI/bearing classifier; more complex classifiers can be implemented in the future. 

![](resources/classifier_pane.png)

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

![](resources/rightclickmenu.png)

_The metadata associated with each classifier is stored with every click train and be accessed through right clicking on or hovering the mouse over a click train detection._ 

## Localisation
The click train detector can be used to localise the position of animals detected by the click train detector using target motion analysis. This generally means that the localisation capabilities are generally restricted to data which has been collected using towed hydrophone arrays. 
 
![](resources/localisation1.png)

_Screenshot of the click train localisation settings. Currently, only target motion is supported._ 

Localisation is enabled by ticking Localise click trains. The type of localisation algorithm which is used is selected in the Localisation algorithms (See the localisation section in PAMGuard help for more info on localisation algorithms). Localisation using 3D simplex and MCMC can be processor intensive, especially when there are a large number of clicks in a train and so the Algorithm Limits pane can be used to set a maximum number of input clicks for a localisation. If the maximum is exceeded then clicks are sub sampled from the click train evenly in time.   

Generally, target motion localisation only works well when there are a large number of clicks over a long time period. The Filters tab allows users to select which click trains are localised and also to remove spurious results from unsuccessful localisations. The Pre Localisation Filter allows users to select a minimum number of detections before localisations are attempted and a minimum bearing change in the click train (Min Angle range). Click trains with larger angle ranges will generally result in higher quality localisations.  
 
![](resources/localisation2.png)

_The filter tab allows users to pre-filter which click train are localised._

The Results Filter allows for spurious localisation results to be deleted: any results from target motion localisation (which can have more than one possible localisation) which are further away than Maximum Range, shallower than Minimum Depth or deeper than Maximum Depth are discarded.  
Running
The click train detector can be run in real time or post processing. In real time add the module and it will automatically detected click trains once PAMGuard started. 

 ![](resources/offlineprocessing.png)

_The filter tab allows users to pre-filter which click train are localised._ 

In viewer mode, add the module and then go to Settings>Click Train Detector > Reanalyse click trains….This will bring up PAMGuard’s generic data reprocessing dialog with two settings, Click Train Detector or Click Train Classifier. The Click Train Detector option will run the detection and classification algorithm again. The Click Train Classifier will only run the classification algorithm on existing detected click trains (much faster). Note that users can select how much data to reprocess in the Data dropdown menu – All Data means the entire dataset will be reprocessed, Loaded Data means just the current data loaded in the display (all scrollable data), Select Data allows the user to define two time limits between which all data is reprocessed. 

## Visualising Results
The results from the click train detector can be visualised in a variety of displays in PAMGuard. 

### Click bearing time display
By default, clicks trains will be shown in the Click Detector Module’s in built bearing time display. Different click trains are represented as different colours. Note that you must right click on the display and select Colour by Click Trains 

![](resources/clicktrain_BT.png)

_The results of the click train detector displayed on the bearing time display. Different colours correspond to different click trains._

### Time Display FX
The Time Display FX is a more modern display which allows any time-based data to be plotted together on a large variety of y-axis (e.g., frequency, bearing, amplitude etc.). Click trains will be plotted on the time-based display by adding Click detections to the display and then using the right   


![](resources/clicktrain_TDFX.png)

_Click train data displayed in the time display FX. Users can right click on click trains to view average spectra and waterfall spectrograms (shown here in top right)._
