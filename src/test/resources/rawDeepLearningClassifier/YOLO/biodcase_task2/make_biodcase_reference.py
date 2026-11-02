"""
Make reference bounding boxes for BioDCASEYoloTest using the BioDCASE 2026 Task 2
baseline preprocessing and Ultralytics predict (conf 0.1, agnostic NMS).

The preprocessing is from marinebioCASE/task2_2025 baselines/yolo/preprocess_data.py:
resample to 250 Hz, 30 s segments every 15 s, 20th order Butterworth band pass
(5-124 Hz), scipy spectrogram (hann, nperseg 256, hop 20, nfft 512, magnitude),
1 - S normalised between the minimum and 98th percentile, uint8, flipped. The
whole file is resampled with resample_poly before segmenting. This reproduces
boxes_20150328_090000.csv (Brian Miller) to within 0.001 in score.

Usage: python make_biodcase_reference.py task2_baseline_weights_2026.pt 20150420_070000.wav boxes_20150420_070000.csv

Requires ultralytics, scipy and soundfile. The TorchScript model in the PAMGuard zip was made with
YOLO('task2_baseline_weights_2026.pt').export(format='torchscript', imgsz=(480, 640)) using torch 2.7.1.
"""
import os
import sys

import numpy as np
import scipy.signal as ss
import soundfile as sf
from ultralytics import YOLO

FS = 250
DURATION = 30
OVERLAP = 0.5

model_path, wav_path, out_path = sys.argv[1:4]

x, fs = sf.read(wav_path, dtype='float32')
if x.ndim > 1:
    x = x[:, 0]
y = ss.resample_poly(x, FS, int(fs)).astype(np.float32)
block = DURATION * FS
sos = ss.iirfilter(20, [5, 124], btype='band', ftype='butter', output='sos', fs=FS)
model = YOLO(model_path)


def make_image(chunk):
    chunk = ss.sosfilt(sos, chunk)
    _, _, sxx = ss.spectrogram(chunk, fs=FS, window='hann', nperseg=256, noverlap=236, nfft=512,
                               detrend=False, return_onesided=True, scaling='density', mode='magnitude')
    sxx = 1 - sxx
    per = np.percentile(sxx.flatten(), 98)
    sxx = (sxx - sxx.min()) / (per - sxx.min())
    sxx[sxx > 1] = 1
    return np.flipud(np.array(sxx * 255, dtype=np.uint8))


with open(out_path, 'w') as f:
    f.write('file,dataset,start_s,class,confidence,t_start,t_end,f_low,f_high\n')
    i = 0.0
    while i * DURATION < len(y) / FS:
        start = int(i * block)
        chunk = y[start:start + block]
        if len(chunk) < block:
            chunk = np.pad(chunk, (0, block - len(chunk)))
        img = np.ascontiguousarray(np.stack([make_image(chunk)] * 3, -1))
        result = model.predict(img, imgsz=640, conf=0.1, agnostic_nms=True, verbose=False)[0]
        for box, conf, cls in zip(result.boxes.xywhn.numpy(), result.boxes.conf.numpy(), result.boxes.cls.numpy()):
            bx, by, bw, bh = box
            f.write(f"{os.path.basename(wav_path)},Kerguelen2015,{i * DURATION},{model.names[int(cls)]},{conf:.4f},"
                    f"{(bx - bw / 2 + i) * DURATION:.3f},{(bx + bw / 2 + i) * DURATION:.3f},"
                    f"{(1 - (by + bh / 2)) * FS / 2:.2f},{(1 - (by - bh / 2)) * FS / 2:.2f}\n")
        i += OVERLAP
