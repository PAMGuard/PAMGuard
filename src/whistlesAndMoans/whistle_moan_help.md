Whistle and Moan Detector Branching and Joining   
It is often the case that multiple whistles from different animals will overlap in time and frequency. If large groups of dolphins are encountered, then overlapping whistles tend to be the norm rather than the exception. While this may not be a problem if you are only interested in detecting whistles, if you are measuring bearings to individual whistles or if you are using the [ Whistle Classifier](../../whistleClassifierHelp/docs/whistleClassifier_Overview.html) it is necessary to separate out the different sounds. The Whistle and Moan detector has four options which control how overlapping whistles are handled

  1. Leave branched regions intact
  2. Discard branched regions
  3. Separate all branches
  4. Re-link across joins

This option is set at the bottom of the Whistle and Moan configuration dialog.   
  
  
The different options are illustrated below using three simulated overlapping linear chirps. Leave branched regions intact If this option is selected, then branched regions will be left intact and may contain more one actual sound.   
  
Discard branched joins If this option is selected, than any region that has more than one detected frequency peak in any time slice will be discarded.   
Separate all branches If this option is selected, then all branches will be separated and passed on as individual sounds. A break is created every time the number of consecutive peaks changes. So a pair of crossing whistles will generally be broken into five parts - each of the four branches and the crossing point itself. In the example below, the three sounds have been broken into 12 separate parts.   
  
Re-link across joins If this option is selected, then the algorithm will attempt to rejoin individual tones across joins. First, the sound is broken up as above, it then rejoins the different components according to the following rules:

  1. If there are the same number of sounds in consecutive time slices and each sound in the earlier slice is in contact with one in the later slice, then they are joined with 1:1 correspondence.
  2. A crossing point is defined as a sound which has the same number of other sounds entering it as leave it and has a total length no longer than the set maximum in the options dialog. Sounds entering and leaving a cross are linked across it with the highest frequency sound on one side joining the lowest frequency sound on the other.
  3. If there is a branch (one sound splitting to two or more) or a join (two or more sounds merging into one), then the frequency gradient of the merged part is compared to that of the sounds entering or leaving and the sound with the best match is joined to the merged section. Very short sections (fewer than 10 time slices) are penalised during the comparison to favour longer whistles.

  
  
Which branching and joining method you chose may depend on the application. Generally, re-linking across joins is the best option for small cetacean whistles.    
  
[Previous: Configure the region connector](whistleMoan_ConfigConnect.html)

---

Whistle and Moan Detector Connected Region Search   
Once the [threshold](WhistleMoan_ConfigNoise.html#thresholding) has been applied to the spectrogram data in the [noise removal](whistleMoan_ConfigNoise.html#) stage, the Whistle and Moan detector searches for connected regions in the spectrogram matrix.   
  
Min Frequency and Max Frequency Usually, you will want the Whistle and Moan detector to search the whole range of available frequencies, from 0 Hz to half the sample rate. However, these parameters give you the opportunity to limit the connected region search to a specific frequency band. This can be particularly useful if your data are contaminated by a lot of noise at a particular frequency. Connection Type When searching for connected regions, two main connection types are available. Connect 4 tries to connect each pixel to other pixels directly above, below, to the left or to the right (i.e. 4 possible connections). Connect 8 tries to connect to the sides and on the diagonals (i.e. 8 possible connections). Clearly connect 8 will detect more whistles and can also detect whistles sweeping more rapidly, however connect 8 may also be prone to a higher rate of false alarm if it connects things which shouldn't be connected. Minimum length and Minimum total size There will always be a small number of random spectrogram pixels which are above threshold but are not part of a whistle or tone. Generally (hopefully !) these will few in number and the chances of them connecting to other random pixels to make a large connected region will be small. All connected regions which are shorter than the minimum length, or have fewer pixels than the minimum total size will be discarded.   
[Previous: Configure the noise removal](whistleMoan_ConfigNoise.html) [Next: Configure region branching](whistleMoan_ConfigBranching.html)

---

Whistle and Moan Detector Channel Grouping and Bearing Calculations The Whistle and Moan detector can operate on one or more channels of data. It can also measure time delays between the arrival of sounds on different channels and use these to calculate bearings, and in some instances ranges to detected sounds.  Which channels the Whistle and Moan detector detects on and which channels it uses for bearing and range calculations is controlled by the channel grouping section of the options dialog.   
  
When a [data source](whistleMoan_ConfigSource.html) is selected, the dialog will show a series of check boxes, one for each channel. Select the channels you want to use for detection OR bearing calculation, then to the right of each channel check box, assign the channel to a group. The group numbers themselves are not important - they just need to be different for each group. The Whistle and Moan detector will detect sounds only on the first channel in each group. If a sound is detected, it will then use data from other channels in the same group to measure time delays between the arrival of the signal on the different channels. The Whistle and Moan detector will do this independently for each group. Measuring Range If more than one group is present, then if whistles which overlap in time and frequency are detected on more than one channel group, the Whistle and Moan detector will attempt to cross the bearings measured within each channel group in order to estimate a range to the sound. The example above was set up to work with a hydrophone array which consisted of four hydrophones arranged in two pairs. The separation of each pair was 3m and the distance between pairs was 200m. Detection and bearing calculation was conducted independently from each pair, and the bearings from the two pairs then crossed to estimate ranges.   
[Previous: Configure the data source](whistleMoan_ConfigSource.html) [Next: Configure the noise removal](whistleMoan_ConfigNoise.html)

---

Whistle and Moan Detector Noise Removal and Thresholding Noise removal and thresholding is one of the most important steps in the operation of the Whistle and Moan detector. Noise removal and thresholding is a five stage process. The first stage has to occur before FFT data are calculated in the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html). The remaining four stages can take place either in the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html) module or in the Whistle and Moan detector. Performing the noise removal in the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html) module has the advantage that other PAMGuard processes and displays will have access to the data The Whistle and Moan detector will try to ensure that the correct noise removal processes are run once and only once but looking back at the FFT data source and testing whether noise removal has already been done. Noise removal processes which have already been conducted in an earlier module cannot be repeated. However, the Whistle and Moan detector cannot check, and has no control over the configuration of noise removal processes conducted in earlier modules. Generally, you should use the default settings   
**Click Removal  
Median Filter  
Average Subtraction  
Gaussian Kernel Smoothing  
Thresholding  
**   
Click Removal This stage of the noise removal has to take place on the raw data prior to the calculation of the spectrogram. It is therefore carried out by the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html). The click removal method operates on the time series data prior to the FFT calculation and therefore affects both output streams of the FFT Engine. Click removal measures the standard deviation of the time series data and then multiplies the signal by a factor which increases rapidly for large signal components. This has the effect of reducing the magnitude of short duration transient signals such as echolocation clicks   
  
Other noise removal stages can be controlled from either the Whistle and Moan detector dialog or from the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html)   
  
Median Filter Within each spectrogram slice, the median value about each point is taken and subtracted from that point. Average Subtraction A decaying average spectrogram is computed and subtracted from the current spectrogram value. Gaussian Kernel Smoothing The spectrogram is smoothed by convolving the image with a Gaussian smoothing kernel 1 2 1  
2 4 2  
1 2 1. Thresholding A threshold is applied and all data falling below that threshold set to 0. Although the [Connected Region Search](whistleMoan_ConfigConnect.html) uses only a binary map of parts of the spectrogram which are above or below threshold it is generally more useful to output the input from the raw FFT data which will have been multiplied by the binary map. This will contain phase and amplitude information which can be used by the Whistle and Moan detector for measuring time delays between channels and the overall whistle amplitude.   
[Previous: Configure channel grouping](whistleMoan_ConfigGrouping.html) [Next: Configure the region connector](whistleMoan_ConfigConnect.html)

---

Whistle and Moan Detector Data Source Data Source Setup To use the Whistle and Moan detector, you will need to configure a [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html). When selecting the FFT length and hop, consider the time and frequency resolution of the spectrogram. For instance, a 1024 pt FFT with 50% overlap (512 pt hop) operating on data with a sample rate of 48 kHz will have a frequency resolution of 47Hz and a time resolution of approximately 10ms. Generally, for detecting dolphin whistles with data sampled at 48 kHz, a 512 pt FFT length and 256 pt Hop is suitable. At higher sample rates, scale the FFT length and hop accordingly, i.e. at a sample rate of 96 kHz use a FFT length of 1024 pt and a hop of 512 pt, etc.  Select the data source [Noise removal and thresholding](whistleMoan_ConfigNoise.html.html) is a critical step in the detection process. However, the noise removal methods can also be used by other PAMGuard detectors and displays, so they have been included in both the [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html) as well as in the Whistle and Moan detector. The [ FFT (Spectrogram) Engine](../../../sound_processing/fftManagerHelp/docs/FFTEngine_Overview.html) has two output streams, one which is the raw FFT data and one which has been through some or all of the noise removal processes. Whether you do the noise removal in the FFT Engine or in the Whistle and Moan detector is entirely up to you, but try not to do it twice since that will place an unnecessary load on the processor From the drop down box at the top of the Whistle and Moan detector options dialog, select which data source you want to use.    
  
  
[Previous: Configure the Whistle and Moan Detector](whistleMoan_Configure.html) [Next: Configure channel grouping](whistleMoan_ConfigGrouping.html)

---

Whistle and Moan Detector Configuring the Whistle and Moan Detector From the **_Detection_** menu select **_Whistle and Moan Detector_** and the following dialog will appear.   
  
The dialog contains two tabs, the first controls the [Data source](whistleMoan_ConfigSource.html), [Channel Grouping](whistleMoan_ConfigGrouping.html) and [How sounds are connected](whistleMoan_ConfigConnect.html). The second tab controls [Noise removal and thresholding.](whistleMoan_ConfigNoise.html)   
[Previous: Whistle and Moan Detector Overview](whistleMoan_Overview.html) [Next: Configure the data source](whistleMoan_ConfigSource.html)

---

Whistle and Moan Detector Overview The Whistle and Moan detector can be used to detect any tonal vocalisation, including odontocete whistles and baleen whale calls. Details of the operation of the detector are available in Gillespie et al, (2013). The Whistle and Moan detector supersedes the old PAMGuard Whistle detector which should no longer be used. The Whistle and Moan detector can be used alone, or with the [ Whistle Classifier](../../whistleClassifierHelp/docs/whistleClassifier_Overview.html) which can be used to identify groups of whistles to species.   
Creating a Whistle and Moan detector From the **_File>Add modules>Detectors_** menu, or from the pop-up menu on the data model display, select "Whistle and Moan Detector". Enter a descriptive name for the new detector (e.g. Whistle detector) and press OK.   
General Principle of Detection Detection is a multi-stage process, the main steps being   


  1. Computation of a spectrogram from raw audio data
  2. Processing of the spectrogram to remove noise (especially clicks)
  3. Thresholding to create a binary map of regions above threshold
  4. Connecting regions of the binary map to create sounds
  5. Breaking and then rejoining branches of complex regions (for instance, if two whistles cross)

References _Gillespie, D., Caillat, M., Gordon, J., and White, P. (2013). "Automatic detection and classification of odontocete whistles,"[The Journal of the Acoustical Society of America, 134, 2427-2437](https://asa.scitation.org/doi/10.1121/1.4816555)._ [Next: Configuring the Whistle and Moan Detector](whistleMoan_Configure.html)
