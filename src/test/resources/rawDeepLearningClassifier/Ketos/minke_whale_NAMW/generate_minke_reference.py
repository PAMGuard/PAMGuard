"""
Generate reference scores for the PAMGuard Ketos minke whale unit test.

Runs the North Atlantic minke whale pulse-train detector (Mouy et al. 2025,
https://github.com/xaviermouy/minke-whale-detector, DOI:10.5281/zenodo.15151160)
on a wav file using the functions in scripts/run_ketos_detector_denoise.py and
writes the raw model output (softmax scores) for every segment to a CSV file.

Two sets of scores are written for each segment:

  * file_denoise    - exactly as run_ketos_detector_denoise.py, i.e. the 60 s
                      median equalizer is applied to the spectrogram of the
                      whole file before it is split into 5 s segments.
  * segment_denoise - the median equalizer is applied to each 5 s segment on
                      its own. This is what PAMGuard does because the deep
                      learning module only has access to one segment at a time.

The CSV columns are:
    seg_index, start_sec, noise_file_denoise, minke_file_denoise,
    noise_segment_denoise, minke_segment_denoise

where start_sec is the start time of the first FFT frame of the segment.

Tested with python 3.10, ketos 2.7.1 (installed with --no-deps), tensorflow
2.15.1, numpy 1.26.4, scipy 1.15.3, dask-image 2026.5.0 and the ecosound source
from https://github.com/xaviermouy/ecosound on the python path.

Usage:
    python generate_minke_reference.py --model model_NAMW_20240523.kt
        --wav 79296_Brunswick03_002K_M06_UTCm5_CH6_20161227_110000.wav
        --detector_dir /path/to/minke-whale-detector --out minke_reference_scores.csv

@author: Jamie Macaulay
"""

import argparse
import os
import sys
import tempfile
import types

import numpy as np
import tensorflow as tf

# ketos 2.7 creates Adam optimizers with the "decay" argument which the Keras
# 2.11+ optimizers reject. The legacy optimizer is identical for inference.
tf.keras.optimizers.Adam = tf.keras.optimizers.legacy.Adam

import matplotlib

matplotlib.use("Agg")
# the detector script forces the TkAgg backend on import - stop it doing that.
matplotlib.use = lambda *args, **kwargs: None

import ketos.neural_networks
from ecosound.core.audiotools import Sound
from ecosound.core.spectrogram import Spectrogram
from scipy import ndimage


def spectrogram_no_denoise(infile, spec_config):
    """The same as calc_spectrogram in run_ketos_detector_denoise.py but without the denoising"""
    sound = Sound(infile)
    sound.read(channel=0)
    spectro = Spectrogram(spec_config["window"], spec_config["window_func"], spec_config["window"],
                          spec_config["step"], sound.waveform_sampling_frequency, unit="sec", verbose=False)
    spectro.compute(sound, spec_config["dB"], spec_config["use_dask"], spec_config["dask_chunks"])
    spectro.crop(frequency_min=spec_config["freq_min"], frequency_max=spec_config["freq_max"], inplace=True)
    return spectro


def median_equalize(spec, window_bins):
    """Median equalizer applied to a single segment (frequency x time)"""
    smed = ndimage.median_filter(spec, size=(1, window_bins), mode="mirror")
    out = spec - smed
    out[out < 0] = 0
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--model", required=True, help="Path to the ketos model (.kt)")
    parser.add_argument("--wav", required=True, help="Path to the wav file")
    parser.add_argument("--detector_dir", required=True, help="Path to the minke-whale-detector repository")
    parser.add_argument("--out", default="minke_reference_scores.csv", help="Output CSV file")
    parser.add_argument("--step_sec", type=float, default=0.05, help="Segment step in seconds (detector default 0.05)")
    parser.add_argument("--debug_segments", type=int, nargs="*", default=[],
                        help="Segment indices for which to save the model input image as CSV")
    args = parser.parse_args()

    # compute_avg_score was removed from later ketos versions. It is only used for
    # smoothing scores in the detector's run() function which is not called here.
    import ketos.neural_networks.dev_utils.detection as ketos_detection
    if not hasattr(ketos_detection, "compute_avg_score"):
        ketos_detection.compute_avg_score = None

    sys.path.insert(0, os.path.join(args.detector_dir, "scripts"))
    import run_ketos_detector_denoise as det

    tmp_dir = tempfile.mkdtemp()
    os.makedirs(os.path.join(tmp_dir, "model"))
    os.makedirs(os.path.join(tmp_dir, "audio"))

    model, audio_repr = ketos.neural_networks.load_model_file(args.model, os.path.join(tmp_dir, "model"),
                                                               load_audio_repr=True)
    spec_config = audio_repr[0]["spectrogram"]
    print(spec_config)

    det_args = types.SimpleNamespace(step_sec=args.step_sec, batch_size=256)

    # 1) Exactly as run_ketos_detector_denoise.py
    name, _ = det.decimate(args.wav, os.path.join(tmp_dir, "audio"), spec_config["rate"], channel=1)
    audio_file = os.path.join(tmp_dir, "audio", name)
    spectro = det.calc_spectrogram(audio_file, spec_config)
    scores_file, seg_times_sec = det.classify_spectro_segments(spectro, model, spec_config, det_args)

    # 2) Median equalizer applied to each segment individually.
    spectro_raw = spectrogram_no_denoise(audio_file, spec_config)
    time_res = spectro_raw.time_resolution
    step_bins = int(np.ceil(args.step_sec / time_res))
    duration_bins = int(np.ceil(spec_config["duration"] / time_res))
    window_bins = round(spec_config["denoiser"][0]["window_duration_sec"] / time_res)
    data = spectro_raw.spectrogram
    seg_indices = list(range(0, data.shape[1] - duration_bins, step_bins))
    assert np.allclose(seg_times_sec, np.dot(seg_indices, time_res))

    print("Input image: %d time bins x %d frequency bins, median window %d bins, %d segments"
          % (duration_bins, data.shape[0], window_bins, len(seg_indices)))

    scores_seg = []
    for i in range(0, len(seg_indices), det_args.batch_size):
        batch = np.array([median_equalize(data[:, idx:idx + duration_bins], window_bins).T
                          for idx in seg_indices[i:i + det_args.batch_size]])
        scores_seg.append(model.run_on_batch(batch, return_raw_output=True))
    scores_seg = np.concatenate(scores_seg)

    for idx in args.debug_segments:
        seg = median_equalize(data[:, idx:idx + duration_bins], window_bins).T
        np.savetxt(os.path.splitext(args.out)[0] + "_input_%d.csv" % idx, seg, delimiter=",", fmt="%.6f")

    out = np.column_stack([seg_indices, seg_times_sec, scores_file[:, 0], scores_file[:, 1],
                           scores_seg[:, 0], scores_seg[:, 1]])
    np.savetxt(args.out, out, delimiter=",", fmt=["%d", "%.2f", "%.6f", "%.6f", "%.6f", "%.6f"],
               header="seg_index,start_sec,noise_file_denoise,minke_file_denoise,"
                      "noise_segment_denoise,minke_segment_denoise", comments="")

    diff = np.abs(scores_file[:, 1] - scores_seg[:, 1])
    print("File vs segment denoise: mean abs diff %.4f, max %.4f, fraction within 0.1: %.3f"
          % (diff.mean(), diff.max(), np.mean(diff < 0.1)))
    for thr in (0.5, 0.9):
        a = scores_file[:, 1] >= thr
        b = scores_seg[:, 1] >= thr
        print("Threshold %.1f: file denoise positives %d, segment denoise positives %d, agreement %.3f"
              % (thr, a.sum(), b.sum(), np.mean(a == b)))


if __name__ == "__main__":
    main()
