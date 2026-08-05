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

    public MaskedFFTParamters() {
    }

}