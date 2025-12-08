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
						processed = processBatch(batch);
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
	protected List<FFTDataUnit> processBatch(List<FFTDataUnit> batch) {
		
		//System.out.println("MaskedFFTProcess: processing batch of size START "+batch.get(0).getFftData().getReal(0));

		//perform the processing
		ComplexArray out = batch.get(0).getFftData();
		if (out == null) {
			System.err.println("MaskedFFTProcess: no FFT data in first unit of batch");
			return null;
		}

		double[][] mask = getMask(out.length(), batch.size());

		//now apply the mask to each unit
		for (int i = 0; i < batch.size(); i++) {
			out = batch.get(i).getFftData();
			if (out == null) {
				System.err.println("MaskedFFTProcess: no FFT data in unit "+i+" of batch");
				continue;
			}
			for (int j = 0; j < out.length(); j++) {
				
				//to apply a mask must multiply both real and imaginary parts by the mask value
				double re = out.getReal(j) * mask[i][j];
				out.setReal(j, re);
				double im = out.getImag(j) * mask[i][j];
				out.setImag(j, im);
			}
						
		}
		
		//System.out.println("MaskedFFTProcess: processing batch of size DONE "+batch.get(0).getFftData().getReal(0));

		
		return batch;
	}

	/**
	 * Return a mask array of length n. Default is all ones. Subclasses can override.
	 */
	public abstract double[][] getMask(int n, int m);

	/**
	 * @return the input FFT data block
	 */
	public FFTDataBlock getInputFFTData() {
		return inputFFTData;
	}

}