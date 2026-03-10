package deepWhistle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
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
    private final LinkedList<FFTDataUnit>[] buffer = new LinkedList[PamConstants.MAX_CHANNELS];

    private int unitsToBuffer = 0;

    // single-thread executor reused for processing batches
    private ExecutorService executor;

    // backpressure: max number of in-flight batches allowed
    private int maxPendingBatches = 4;
    private final Object pendingLock = new Object();
    private int pendingBatches = 0;
    
    /**
     * Default mask implementation (does nothing). Either subclass and set
     * a different maks or set a new mask via setMask().
     */
    private PamFFTMask mask = new DummyFFTMask();


    public MaskedFFTProcess(DeepWhistleControl control) {
        super(control, null);
        this.control = control;
        
        setParentDataBlock(parentDataBlock);
        
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new LinkedList<FFTDataUnit>();
        }

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
            int channelMap = PamUtils.PamUtils.getSingleChannel(clonedUnit.getChannelBitmap());
            
            buffer[channelMap].add(clonedUnit); // add the data unit to the correct channel's buffer
            if (unitsToBuffer <= 0) {
                // fallback to at least 1 if unitsToBuffer not yet computed
                unitsToBuffer = 1;
            }
            if (buffer[channelMap].size() >= unitsToBuffer) {
                // copy buffer into batch and clear
                batchToProcess = new ArrayList<>(buffer[channelMap]);
                buffer[channelMap].clear();
            }
        }

        if (batchToProcess != null) {
            // submit batch for processing on worker thread with backpressure
            try {
                // wait until there is capacity; also increments pending counter atomically
                if (!acquireSubmissionSlot()) {
                    return; // interrupted while waiting
                }

                final List<FFTDataUnit> batch = batchToProcess;
                executor.submit(() -> {
                    try {
                        List<FFTDataUnit> processed = applyMask(batch);
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
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        releaseSubmissionSlot();
                    }
                });
            } catch (RejectedExecutionException rex) {
                // executor rejected; release slot and drop batch
                releaseSubmissionSlot();
                rex.printStackTrace();
            }
        }
    }

    // Blocks until the number of in-flight tasks is below maxPendingBatches. Returns false if interrupted
    private boolean acquireSubmissionSlot() {
        synchronized (pendingLock) {
            while (pendingBatches >= maxPendingBatches) {
                try {
                    pendingLock.wait(5L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            pendingBatches++;
            return true;
        }
    }

    // Decrements pending counter and notifies any waiting submitters
    private void releaseSubmissionSlot() {
        synchronized (pendingLock) {
            if (pendingBatches > 0) pendingBatches--;
            pendingLock.notifyAll();
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
        
        //System.out.println("MaskedFFTProcess: preparing process");
        
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
        
        //init the mask - this may contain complex model
        boolean mask = this.mask.initMask();
        if (!mask) {
            System.err.println("MaskedFFTProcess: failed to initialize FFT mask");
            prepOk = false;
            return;
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
     */ 
    public PamFFTMask getMask() {
        return mask;
    }

    /**
     * Set the Mask used to process the FFT data.
     */
    public void setMask(PamFFTMask mask) {
        this.mask = mask;
    }

    public MaskedFFTParamters getMaskFFTParams() {
        return this.control.getParameters();
    }
    
    /**
     * Get the number of FFT units that are buffered before processing.
     * @return the number of FFT data units to buffer before processing. 
     */
    public int getUnitsToBuffer() {
        return unitsToBuffer;
    }

    /**
     * Configure the maximum number of in-flight batches allowed before newData blocks.
     */
    public void setMaxPendingBatches(int maxPendingBatches) {
        if (maxPendingBatches < 1) maxPendingBatches = 1;
        synchronized (pendingLock) {
            this.maxPendingBatches = maxPendingBatches;
            pendingLock.notifyAll();
        }
    }

    public int getMaxPendingBatches() {
        return maxPendingBatches;
    }
    
    /**
	 * Get the output masked FFT data block.
	 * 
	 * @return the masked FFT data block.
     */
	protected  PamDataBlock getMaskedFFTDataBlock() {
		return maskedFFTDataBlock;
	}
}
