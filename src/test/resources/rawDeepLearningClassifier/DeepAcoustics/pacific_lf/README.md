# DeepAcoustics Pacific low frequency test data

Test data for `DeepAcousticsPacificLFTest`.

- `PacificLF_05_TinyYOLO_FIXED.zip` - a DeepAcoustics tiny YOLO v4 model (TensorFlow) which detects
  and classifies low frequency baleen whale calls (11 classes) in 40.96 s spectrograms of 400 Hz data.
- `HYDDBA105OOI_20170203-*.wav` - one hour (03:15 to 04:15) of continuous data from the Ocean
  Observatories Initiative hydrophone HYDBBA105 on 2017-02-03, in five minute files.
- `OOI_105_Blue_Fin_20170203_031500_041500_annotations.txt` - the Raven selection table of annotated
  fin whale (18 Hz, 20 Hz) and blue whale (A, B) calls for the same hour, taken from
  `OOI_105_Blue_Fin_20170203_Test_Detections_Final.txt` (the full day's selections, 02:00 to 06:05).
  Times are in seconds from 02:00:00.
