package fftManager.fftorganiser;

/**
 * List of possible input types for the FFTData Organiser:
 * <br>FFT Data from an FFTDataBlock
 * <br>RAW Data from a PAMRawDataBlock
 * <br>FFT Data from within a data unit (e.g. click spectrum)
 * <br>RAW Data from within a data unit (e.g. a click waveform)
 * @author Doug Gillespie
 *
 */
public enum FFTInputTypes {FFTData, RawData, FFTDataHolder, RAWDataHolder}