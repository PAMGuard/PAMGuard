North Atlantic minke whale pulse-train detector (version 20240829)

Mouy X, Awbery T, Davis G, Fernandez-Betelu O, Iorio-Merlo V, Transue L, Van Parijs S, Risch D (2025)
North Atlantic minke whale pulse-train detector (version 20240829). Zenodo. DOI:10.5281/zenodo.15151160
https://github.com/xaviermouy/minke-whale-detector

Files
- model_NAMW_20240523.ktpb - PAMGuard compatible model. Created from models/model_NAMW_20240523.kt in the
  detector repository using convert_kt_to_ktpb.py and the PAMGuard metadata in audio_repr_pg.json. The
  original Ketos recipe and audio representation are also stored inside the .ktpb file.
- 79296_Brunswick03_002K_M06_UTCm5_CH6_20161227_110000.wav - example sound file (2 kHz). Note the original
  file would not load properly in jdl4pam and was re-saved in MATLAB.
- minke_reference_scores.csv - model scores from Python generated with generate_minke_reference.py.
- generate_minke_reference.py - generates the reference scores using the detector's
  run_ketos_detector_denoise.py script.

Transforms
The Python detector computes a dB magnitude spectrogram (0.128 s Hann window, 0.04 s step, 0-800 Hz) and then
applies ecosound's 60 s "median_equalizer" denoiser (subtract a running median along the time axis and
set negative values to zero). In PAMGuard the median equalizer is applied to each 5 s segment (the window
is mirrored to fill 60 s) because the classifier only has access to one segment at a time.

Results (14/09/2026)
- PAMGuard vs python with per segment denoising: 100% of tested segments within 0.05 (max diff 0.0078).
- Python whole file vs per segment denoising (all 11116 segments): 85.4% agree at a 0.5 threshold, 67% of
  scores within 0.1. PAMGuard vs python whole file denoising: 83.4% agree at 0.5 (445 segments tested).

Python environment used: python 3.10, tensorflow 2.15.1, ketos 2.7.1 (pip install --no-deps), ecosound 0.0.25
(pip install --no-deps), numpy 1.26.4, scipy 1.15.3, dask-image 2026.5.0, setuptools<81.
