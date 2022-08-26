# Matched click classifier

## Overview

The matched click classifier is an alternative to the in built click classifier in the click detection module which uses two click templates (target and reject) to classiify individual clicks detections from the click detection module. The idea behind this classifier is to more accurately classify rarer clicks when there 

## How it works

The classifier is based on a matched filter i.e. a canditate click detection is compared to a template and it's maximum correlation value with the template is then used for classification. Each click is compared to both a _match_ and a _reject_ template. If the ratio of the correlation match to the reject template exceeds a certain threshold then the click is classified. There can be multiple combinations of match to reject templates allowing the matched classifier to deal with with different types of clicks or even clicks from multiple species.


## Configuring the matched click classifier

The matched click classifier seetings are accessed via **Settings-> Matched click classifier**_. The settings are split into the three sections, general settings, click waveform and click templates. 

<p align="center">
  <img width="950" height="520" src = "resources/matched_click_dialog_summary.png">
</p>

_The settings pane of the matched click classifier_

### General Settings

The general settings allows for channel options, species ID and the default colours for classified clicks to be to be set. 
_
Channel Options _allows users to define whether a single clcik from one channel, all clicks or an averaged click should be used to be used for classification in multi-channle situations. if there is only one channel then this makes no difference. 

_Click Type_ sets the number that defines the species ID. Make sure this is not the same as any mof the species IDs in the default click classifier (this is why the default is set so high). 

_Symbol_ and _Fill_ define the default colours clicks which have been classified by the matched click classifier should be plotted on displays. 

###  Click Waveform Settings

Before a click is classified it undergoes some pre-conditioning by the matched click classifier. 

_Restrict paramter extraction to XX samples_ sets the maximum lengthm of the waveform to the classifier. If this is selected then center of the click is located and samples trimmed around the center. In some use cases, for example SoundTrtap detections, which may be 10,000 samples long, setting a lower number of maximum samples can greatly increase processing speed and imporove the accuracy of results. 

_Peak threshold_ and _Smoothing_ are both paramters used to find the click center to set the maximum number of samples. The click length is measured by calculating the waveform envelope using a Hilbert Transform. The envelope is smoothed using a mvoing average filter (the Smoothing paramter defines the size of the averaging window). the 


