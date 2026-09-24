# Hawaiian monk seal call detector test data

Test data for `MonkSealYoloTest`. The detector is the Hawaiian monk seal call detector V1.2
(Rasmussen, Sills, Parnell, Reichmuth 2026), a MATLAB `yolov4ObjectDetector` which detects and
classifies six call types (Croak, Groan, Growl, Moan, Rumble, Whoop) in one minute spectrograms.

- `monk_seal_hms_clip_20210513_003425.wav` - a 129.8 s clip of the demo file supplied with the
  MATLAB app, starting at the third one minute segment.
- `monk_seal_hms_matlab_detections.mat` - detections from running the MATLAB app's detection code on
  the clip (`make_monk_seal_reference.m`). Bounding boxes are in image pixels (192 x 2368).
- `fix_monk_seal_mish.py` - the network was exported from MATLAB to TorchScript via ONNX, which
  cannot export MATLAB's Mish activations, and they had been replaced with LeakyReLU(0.1). This
  script replaces them with Mish again. Afterwards the TorchScript output matches the MATLAB network
  to ~1e-4.

The model (`monk_seal_hms_yolov4.zip`, TorchScript model + `.pdtf` metadata) is too large for the
repository and is downloaded by the test from the PAMGuard deeplearningmodels GitHub repository.
