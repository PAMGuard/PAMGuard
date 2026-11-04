# BioDCASE 2026 Task 2 baseline (Ultralytics YOLO11s)

Test data for `BioDCASEYoloTest`, which checks that PAMGuard's YOLO classifier runs an Ultralytics
(YOLOv8/YOLO11) model and returns the same bounding boxes as Ultralytics in Python.

* **Model** - BioDCASE 2026 Task 2 baseline, YOLO11s, Zenodo record 20281116 (CC BY 4.0). Exported to
  TorchScript (input 1 x 3 x 480 x 640, output 1 x 7 x 6300) and packaged with a `.pdtf` as
  `biodcase_task2_yolo11s.zip`. The test downloads the zip from the PAMGuard deeplearningmodels
  repository. Classes are `bmabz` (Antarctic blue whale A, B and Z calls), `d` (blue and fin whale
  D calls) and `bp` (fin whale 20 Hz pulses).
* **Audio** - `20150328_090000.wav` (mostly blue whale) and `20150420_070000.wav` (many fin whale
  calls), one hour at 1 kHz from the Kerguelen 2015 dataset of
  https://data.aad.gov.au/metadata/AcousticTrends_BlueFinLibrary (CC BY 4.0).
* **Reference boxes** - Ultralytics predict, conf 0.1, agnostic NMS, on 30 s segments every 15 s.
  `boxes_20150328_090000.csv` is from Brian Miller (yolo2pamguard reference.py).
  `boxes_20150420_070000.csv` was made with `make_biodcase_reference.py`, which reproduces the
  March file. Boxes are not merged across overlapping segments.

The PAMGuard transforms are `load_audio_poly` (scipy resample_poly), `filter_wav` (Butterworth band
pass, order 20, 5-124 Hz), `spectrogram_scipy` (hann, 256 window, 20 hop, 512 FFT), `spec_product` -1
and `spec_add` 1 (1 - S), `normalize_percentile` 98 and `spec_flip`. The model is letterboxed from
257 x 363 to 480 x 640 as Ultralytics does, with 8 bit quantisation (`"uint8": true`).
