# Sound Output

## Configuration

To access the module's settings, select _**Detection>Sound Output...**_ from the main menu . This will bring up the Sound Output dialog.

### Data Source

The sound output module requires a source of raw data before it can operate. Select a raw data source from the drop down list.

Select the data channels. Note that a sound card will only be able to play back two channels at a time and you will therefore only be able to check one or two boxes.

### Options

The available options will depend on the type of sound acquisition device.

#### Sound Card and ASIO Sound Card Input

If using a sound card input, you will only be able to play back out through a sound card, and should ideally select the same device.

You will be restricted to two output channels. Once two channels are selected, other channels will be 'grayed out'. De-select a channel in order to make a fresh selection.

#### Other Input Devices

If using other input devices, (National Instruments cards, SAIL Daq cards, etc.) you can play back through any available device, though it is recommended that you use the same device if at all possible.

The sample rate of the output device does not need to match the input sample rate and PAMGuard will automatically up-sample or decimate data to convert between the input sample rate and the output sample rate.

#### Audio files and File Folders

Select the output device type which can either be a sound card or a National Instruments device.

Set the output sample rate. This can be anything so long as it is supported by the output device you are using.

Set the play-back speed. This is how many times real-time the data will play at.

PAMGuard will automatically up-sample or decimate data to convert between the input sample rate and the output sample rate, taking into account the playback speed.

If listening on headphones via a sound card, then an output sample rate of 48kHz or 96kHz is recommended. however, if you're outputting data from PAMGuard for other purposes, such as driving a high frequency sound source, then you will need to select an appropriately high sample rate and ensure that you are using an output device that supports that sample rate.

**Note:** During file analysis, if no channels are selected and there is no play-back, analysis will continue at the maximum rate possible using all available processor power. If sound play-back is enabled, then analysis will proceed at the speed governed by the sound play-back rate. If the processor can't keep up with analysing data at the play-back rate, then play-back will appear interrupted and 'glitchy'

It is possible to re-configure channels during analysis, so if you are analysing a lot of data, you may want to occasionally turn on sound output on one or two channels to have a listen, then turn them off again so that it processes as fast as possible.

  


[Previous: Sound Output Overview](soundPlayback_soundPlayback.html)

[Next: Sound Output Control](soundPlayback_Control.html)

---

# Sound Output

## Control

  


Many of the sound output's features can be controlled from the PAMGuard side bar which contains controls to

  * High pass filter the data
  * Run and envelope tracer and mix it's output with the raw audio
  * Change the playback speed (only when using PAMGuard viewer, or processing data from files or simulated sources);
  * Amplify or attenuate the data



### High Pass Filter

The filter type is currently fixed as a second order high pass Butterworth filter which is suitable for the removal of unwanted low frequency sound when listening to audio data. Drag the slider to the required cut off frequency and the filter will automatically adjust.

### Envelope Tracing

Many cetacean echolocation clicks are too high frequency to be audible to humans. However, it is possible to hear these sounds by listening to their outline, or "envelope" which is at a much lower frequency then the main waveform and is generally audible.

The envelope tracer works by first high pass or band pass filtering the data, then all negative values in the waveform are set positive before the "rectified" waveform is low pass filtered to smooth it out. The two filters can be adjusted from the drop down menu accessible from the small arrow by the slider.

The operator can select how much of the raw audio and how much of the envelope traced signal are mixed together using the slider. This makes it possible to simultaneously listen to sounds, such as dolphin whistles, which are in the human audio band and high frequency click trains from species such as harbour porpoise.

### Speed

**

Note that this feature will be hidden and disabled when processing data in real time.

**

When processing data from file, simulated data, or when using the [PAMGuard Viewer](../../../overview/PamMasterHelp/docs/viewerMode.html), it is possible to adjust the playback speed. Drag the slider to the desired speed. When adjusting the playback speed, the frequency of the sounds will change. e.g. when playing at half the true speed, the frequency of sounds will halve.

When speed adjustments are made, the selected sample rate of the output device remains the same. The speed change is affected by decimating or upsampling the audio data.

### Gain

Attenuate or amplify the data.

Note that you should also check the sound settings on your computer. Increasing the gain in PAMGuard to a high level when the system's sound card volume is turned right down may lead to signal distortion, so make sure the sound on the computer is turned up first. 

  


[Previous: Sound Output Configuration](soundPlayback_Config.html)

---

# Sound Output

## Overview

The sound output module can be used to play back sounds from PAMGuard through a sound card or other data acquisition device.

This allows you to aurally monitor sounds after they have been modified by PAMGuard modules such as [Filters](../../FiltersHelp/Docs/filters_filters.html) or the [Seismic Veto](../../seismicveto/docs/veto_overview.html)

The availability of the sound output function depends on the type of input selected in the [ Sound Acquisition](../../AcquisitionHelp/docs/AcquisitionOverview.html) module. 

### Real time play back

When processing in real time, it is of course essential that sound output is at the same speed as the sound input. Ideally, you should select the same device for output as you are using for input to guarantee that they are running at exactly the same speed. Sometimes this is either impossible (because the input device doesn't have any outputs) or impractical for other reasons in which case you can use a different device but should be aware that there may be 'glitches' in the output.

If you're using the same device for output as for input, always use the same output sample rate. If using a different device, then you may need to use a different output sample rate, for example if your input data come from a high speed acquisition card sampling at 500kHz and you wish to play back through a sound card which can only output at 48kHz. In this case, PAMGuard will automatically decimate or up-sample the data so that the play-back is in real time. Of course, if you're acquiring high frequency data, you'll only be able to hear the part of that data that's in the audio band.

### File or PAMGuard Viewer play-back

If processing data from file, or using PAMGuard viewer, the speed at which data can play can be varied by the user via controls in the dialog or side panel.

Data will automatically be decimated or up-sampled to match the input sample rate, output sample rate of the device and the selected play speed.

### Creating sound output modules

From the _**F**_** _ile>Add modules>Sound Processing_** menu, or from the pop-up menu on the data model display select "**_Sound Output_** ". Enter a name for the new module and press Ok.

Sound output modules can be inserted anywhere in the PAMGuard data model where there are raw audio data such as the output of a [Sound acquisition module](../../AcquisitionHelp/docs/AcquisitionOverview.html), a [Filter module](../../FiltersHelp/Docs/Filters_filters.html), a [Decimator](../../decimatorHelp/docs/decimator_decimator.html), a [Seismic Veto](../../seismicveto/docs/veto_overview.html), etc.

  
  


[Next: Sound Output Configuration](soundPlayback_Config.html)
