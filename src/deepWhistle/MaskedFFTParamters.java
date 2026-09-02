package deepWhistle;

import java.io.Serializable;

/**
 * Parameters for DeepWhistle (initial masking-only implementation).
 */
public class MaskedFFTParamters implements Serializable {

    public static final long serialVersionUID = 1L;

    /** Long name of the FFT data source block */
    public String dataSourceName = null;

    /** Channel bitmap for channels to process (default - all) */
    public int channelMap = -1;

    /** Name for the output masked FFT block (short name) */
    public String outputName = "Masked FFT";

    /** Number of seconds to buffer before processing */
    public double bufferSeconds = 1.0;

    /** Additional simple options for future use */
    public boolean enabled = true;

	public int dataSourceIndex;

    /**
     * The type of FFT mask to use. Currently only {@link FFTMaskType#DEEP_WHISTLE}
     * is implemented but more will be added in the future.
     */
    public FFTMaskType maskType = FFTMaskType.DEEP_WHISTLE;

    /**
     * The local path to the model used by the selected mask. This is set once a
     * model has been downloaded (or located locally) and is used by the mask to
     * load the model. May be null if no model has been downloaded yet.
     */
    public String modelPath = null;

    public MaskedFFTParamters() {
    }

}