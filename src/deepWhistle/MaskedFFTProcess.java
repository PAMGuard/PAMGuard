package deepWhistle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * PamProcess that applies a mask to the real-part of incoming FFTDataUnit objects.
 * This initial implementation buffers a defined number of seconds of FFT slices
 * then sends a batch to a worker thread to compute masks and apply them.
 */
public abstract class MaskedFFTProcess extends PamProcess {

	private DeepWhistleControl control;
	private FFTDataBlock inputFFTData;
	private FFTDataBlock maskedFFTDataBlock;
	private MaskedFFTParamters params;
	private boolean prepOk = false;

	// buffer incoming FFT units (cloned) until we have bufferSeconds worth
	private final LinkedList<FFTDataUnit> buffer = new LinkedList<>();
	private int unitsToBuffer = 0;

	// single-thread executor reused for processing batches
	private ExecutorService executor;
	
	/**
	 * Default mask implementation (does nothing). Either subclass and set
	 * a different maks or set a new mask via setMask().
	 */
	private PamFFTMask mask = new DummyFFTMask();


	public MaskedFFTProcess(DeepWhistleControl control) {
		super(control, null);
		this.control = control;
		
	
		setParentDataBlock(parentDataBlock);

		// create output FFTDataBlock; defaults will be updated when prepareProcess() runs
		maskedFFTDataBlock = new FFTDataBlock("Masked FFT", this, 0, 1, 256);
		addOutputDataBlock(maskedFFTDataBlock);
	}

	@Override
	public void pamStart() {
		// nothing special for now
	}

	@Override
	public void pamStop() {
		// nothing special for now
	}

	@Override
	public void destroyProcess() {
		super.destroyProcess();
		// shutdown executor if present
		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
			executor = null;
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (!prepOk) return;
		if (!(arg instanceof FFTDataUnit)) return;

		FFTDataUnit inUnit = (FFTDataUnit) arg;

		// channel check
		if ((inUnit.getChannelBitmap() & maskedFFTDataBlock.getChannelMap()) == 0) {
			return;
		}

		ComplexArray fft = inUnit.getFftData();
		if (fft == null) return;

		// clone unit (deep copy of complex array) to store in buffer
		ComplexArray fftClone = fft.clone();
		FFTDataUnit clonedUnit = new FFTDataUnit(inUnit.getTimeMilliseconds(), inUnit.getChannelBitmap(), inUnit.getStartSample(), inUnit.getSampleDuration(), fftClone, inUnit.getFftSlice());

		List<FFTDataUnit> batchToProcess = null;
		synchronized (buffer) {
			buffer.add(clonedUnit);
			if (unitsToBuffer <= 0) {
				// fallback to at least 1 if unitsToBuffer not yet computed
				unitsToBuffer = 1;
			}
			if (buffer.size() >= unitsToBuffer) {
				// copy buffer into batch and clear
				batchToProcess = new ArrayList<>(buffer);
				buffer.clear();
			}
		}

		if (batchToProcess != null) {
			// submit batch for processing on worker thread
			try {
				final List<FFTDataUnit> batch = batchToProcess;
				executor.submit(() -> {
					List<FFTDataUnit> processed = null;
					try {
						processed = applyMask(batch);
					} catch (Exception ex) {
						ex.printStackTrace();
						return;
					}
					if (processed != null) {
						// push processed units into output data block
						for (FFTDataUnit u : processed) {
							try {
								maskedFFTDataBlock.addPamData(u);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				});
			} catch (RejectedExecutionException rex) {
				// executor rejected; drop batch and continue
				rex.printStackTrace();
			}
		}
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		// nothing special yet
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		
		System.out.println("MaskedFFTProcess: preparing process");
		
		params = control.getDeepWhistleParameters();
		
		inputFFTData = (FFTDataBlock) control.getPamConfiguration().getDataBlockByLongName(params.dataSourceName);
		
		
		setParentDataBlock(inputFFTData);
		
		//init the mask
		this.mask.initMask();
		
		
		if (inputFFTData == null) {
			System.err.println("MaskedFFTProcess: no input FFT data block found: " + params.dataSourceName);
			prepOk = false;
			return;
		}
		
		System.out.println("MaskedFFTProcess: input params channels: " + (params.channelMap & inputFFTData.getChannelMap()) + params.channelMap);

		// copy information from input block
		maskedFFTDataBlock.setChannelMap(params.channelMap & inputFFTData.getChannelMap());
		maskedFFTDataBlock.setFftHop(inputFFTData.getFftHop());
		maskedFFTDataBlock.setFftLength(inputFFTData.getFftLength());
		setSampleRate(inputFFTData.getSampleRate(), false);

		// compute how many units correspond to bufferSeconds
		double hopSec = (double) inputFFTData.getHopSamples() / inputFFTData.getSampleRate();
		if (params.bufferSeconds <= 0) params.bufferSeconds = 1.0; // safety
		unitsToBuffer = (int) Math.ceil(params.bufferSeconds / hopSec);
		if (unitsToBuffer < 1) unitsToBuffer = 1;

		// create single-thread executor if not existing
		if (executor == null || executor.isShutdown()) {
			executor = Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, getProcessName() + "-mask-worker");
				t.setDaemon(true);
				return t;
			});
		}

		prepOk = true;
	}
	
	

	@Override
	public boolean prepareProcessOK() {
		prepareProcess();
		return prepOk;
	}

	/**
	 * Default batch processor. Applies mask to the real part of each FFTDataUnit.
	 * Subclasses should override this to implement deep-learning mask generation.
	 * The method is called on a worker thread and should return the list of processed
	 * FFTDataUnit objects (typically the same objects with their ComplexArray modified
	 * or replaced).
	 */
	protected List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {
		return this.mask.applyMask(batch);
	}


	/**
	 * @return the input FFT data block
	 */
	public FFTDataBlock getInputFFTData() {
		return inputFFTData;
	}
	
	/**
	 * Get the Mask used to process the FFT data.
	 * @return
	 */	
	public PamFFTMask getMask() {
		return mask;
	}

	/**
	 * Set the Mask used to process the FFT data.
	 * @param mask
	 */
	public void setMask(PamFFTMask mask) {
		this.mask = mask;
	}

}