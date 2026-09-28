# Deep Whistle

## Overview

The **Deep Whistle** module uses deep-learning models to enhance tonal whistle sounds (e.g. dolphin whistles) in a spectrogram. It takes the output of an FFT (spectrogram) module and applies a learned **mask** that keeps the time-frequency bins belonging to whistle contours and suppresses everything else (background noise, clicks, broadband transients). The result is a new *masked* FFT data block which can be displayed as a much cleaner spectrogram and/or passed to a downstream detector such as the **Whistle and Moan Detector** to improve detection and reduce false positives.

The module is deliberately general: the masking model is selected from a list of interchangeable **FFT masks**, and new models can be added over time. Two models are currently available:

- **Deep Whistle** – a convolutional network (from the *silbido* DeepWhistle project) trained on synthetic whistle data.
- **SAM-Whistle** – an adaptation of Meta's *Segment Anything Model* (SAM) foundation model, distributed as an example trained on the DCLDE 2011 dataset.

Each model has an information button (ⓘ) next to its name that gives a short description and a link to the paper describing it.

![](resources/deepwhistle_overview.png)

_The Deep Whistle module running in PAMGuard. Top: the source spectrogram with the model output overlaid (blue contours). Bottom: the masked FFT spectrogram, in which only whistle energy remains and can be passed to a whistle detector._

## How it works

For each channel the module buffers a short window of FFT slices, converts them to a spectrogram and applies a set of model-specific pre-processing transforms (frequency trimming, conversion to decibels, normalisation). The buffered spectrogram is passed to the deep-learning model, which returns a **confidence** between 0 and 1 for every time-frequency bin – the probability that the bin is part of a whistle. Bins whose confidence is below the **confidence threshold** are set to zero; the remaining confidences are multiplied into the FFT data. The masked FFT is then published as a new data block.

Because the model works on the spectrogram, the FFT source must be configured with the frequency and time resolution the model expects (see below). The two models were trained with different settings, so the module checks the source automatically and warns if it is not set up correctly.

## Adding the module

The module is added from the PAMGuard **File > Add Modules > Detectors > Deep Whistle** menu. It depends on an **FFT (spectrogram) Engine** module, so add an FFT Engine first (or let PAMGuard add one for you). A typical processing chain is:

`Sound Acquisition → FFT Engine → Deep Whistle → Whistle and Moan Detector`

<!-- TODO: add screenshot of the Add Modules menu / data model here, e.g. resources/deepwhistle_datamodel.png -->

## Configuring the module

Open the settings from the module's menu or the settings dialog. The settings pane is shown in Figure 2.

![](resources/deepwhistle_settings.png)

_The Deep Whistle settings pane._

### Raw data source for FFT

Select the FFT (spectrogram) data block to process and the channels to include. The panel shows the sample rate, FFT length, frequency resolution and time resolution of the selected source so you can check they are suitable.

### FFT length and hop

Each model was trained at a particular time-frequency resolution, expressed as an analysis window and hop in milliseconds (which correspond to an FFT length and hop in samples that depend on the sample rate):

| Model | Analysis window | Hop |
|-------|-----------------|-----|
| Deep Whistle | ~2 ms | ~8 ms |
| SAM-Whistle | ~8 ms | ~2 ms |

The pane validates the selected FFT source against the chosen model. If the FFT length or hop is wrong, a warning is shown together with a **Set FFT parameters** button that configures the parent FFT Engine automatically. When the source is correct a green confirmation message is shown.

### FFT Mask

Choose the masking model from the **FFT Mask** drop-down. When a model is selected it is downloaded automatically from the PAMGuard model repository (a progress indicator is shown while downloading). Once downloaded the model is cached locally, so it is not downloaded again on subsequent runs. The name of the loaded model file is shown below the drop-down.

Two buttons sit next to the model name:

- **ⓘ (information)** – shows a short description of the model and a hyperlink to the scientific paper that describes it (the link text is the full reference; clicking it opens the paper's DOI in your browser).
- **📂 (import)** – lets you import a **custom** model. If you have retrained one of the models on your own data, use this to select the retrained model file; it replaces the downloaded online model for the selected mask. See *Using a custom (retrained) model* below.

<!-- TODO: add a close-up screenshot of the model information pop-up, e.g. resources/deepwhistle_model_info.png -->

### Confidence threshold

The **confidence threshold** (0–1) sets how confident the model must be before a time-frequency bin is kept. Higher values keep only the strongest whistle energy (fewer false positives, but faint whistles may be lost); lower values keep more (more complete contours, but more noise). A value around 0.4–0.5 is a reasonable starting point.

## Output

The module produces a **Masked FFT** data block with the same FFT length and hop as the source. You can:

- Display it on a spectrogram to see the cleaned time-frequency representation.
- Use it as the input to a **Whistle and Moan Detector**, which will then operate on the de-noised spectrogram.

![](resources/deepwhistle_masked_spectrogram.png)

_A masked FFT spectrogram. Only whistle contours remain; background noise and transients have been removed._

## Using a custom (retrained) model

Both models can be retrained on your own annotated data (see the model's own repository for training instructions). To use a retrained model in PAMGuard, click the **import** button next to the model name and select the retrained model file. The custom file simply replaces the online model file for that mask, so PAMGuard loads it in place of the downloaded model. Selecting a different mask and returning, or re-downloading, will revert to the online model.

## Performance and GPU acceleration

The SAM-Whistle model is large, so it benefits greatly from running on a GPU. On **Apple Silicon** Macs the module automatically runs SAM-Whistle on the integrated GPU (via Apple's Metal/MPS backend), which is roughly **3–5× faster** than the CPU. This happens transparently: the module tries the GPU first and falls back to the CPU if the GPU is unavailable, so no configuration is required. The console prints which device is being used (`SamWhistleMask: model running on device …`).

For the GPU path to work the model must be exported **without** `torch.jit.freeze` (the export script `export_torchscript.py` now leaves freezing off by default). A model exported with freezing still runs, but only on the CPU. The smaller Deep Whistle model runs on the CPU and is fast enough that GPU acceleration is not needed.

## Tips and troubleshooting

- **Set the FFT source correctly.** The most common cause of poor results is an FFT length/hop that does not match the model. Use the **Set FFT parameters** button, or configure the FFT Engine manually to the window/hop in the table above.
- **A model produces a lot of false positives.** This usually means the model does not transfer well to your recordings (different species, equipment or noise), or the model file is incomplete. Try adjusting the confidence threshold; if the output is still noisy across the whole spectrogram, the model likely needs retraining/fine-tuning on data similar to yours.
- **Sample rate and frequency range.** The models focus on the 5–50 kHz band. Recordings whose Nyquist frequency is below 50 kHz will only be masked up to the Nyquist frequency.

## References

The models used by this module are described in the following papers (also available from the ⓘ information button in the settings pane):

- **Deep Whistle** — Li, P. *et al.* (2020) 'Learning deep models from synthetic data for extracting dolphin whistle contours', *2020 International Joint Conference on Neural Networks (IJCNN)*. IEEE, pp. 1–10. [doi:10.1109/IJCNN48605.2020.9206992](https://doi.org/10.1109/IJCNN48605.2020.9206992)
- **SAM-Whistle** — Zhang, X. *et al.* (2025) 'Automating time × frequency annotations of delphinid whistles by adapting a foundational transformer neural network', *Scientific Reports*, 15, 37809. [doi:10.1038/s41598-025-21642-x](https://doi.org/10.1038/s41598-025-21642-x)
