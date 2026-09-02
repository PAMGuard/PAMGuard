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

    // the active channels (the channels in the output channel map), in ascending
    // order. Blocks from all of these are batched together into a single mask call.
    private int[] activeChannels = new int[0];

    // the FFT length and hop the process (and mask) was last prepared for. Used to
    // detect when the source FFT settings change underneath us (e.g. the user changes
    // the parent FFT module) so we can clear stale buffered units and re-initialise.
    private int preparedFftLength = -1;
    private int preparedFftHop = -1;

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

    /**
     * The mask type the current mask was created from. Used to detect when the
     * user has selected a different mask so the mask is only recreated when
     * necessary.
     */
    private FFTMaskType currentMaskType = null;


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

        // If the source FFT length or hop has changed since we were prepared (e.g. the
        // user changed the parent FFT module's settings while running), the buffered
        // units and the mask/model are now the wrong size. Drop everything and re-prepare
        // so we don't mix FFT lengths in a batch (which crashes the spectrogram maths).
        if (inputFFTData != null
                && (inputFFTData.getFftLength() != preparedFftLength || inputFFTData.getFftHop() != preparedFftHop)) {
            System.out.println("MaskedFFTProcess: FFT settings changed (length " + preparedFftLength
                    + " -> " + inputFFTData.getFftLength() + ", hop " + preparedFftHop
                    + " -> " + inputFFTData.getFftHop() + ") - clearing buffers and re-preparing");
            clearBuffers();
            prepareProcess();
            if (!prepOk) return;
        }

        // channel check
        if ((inUnit.getChannelBitmap() & maskedFFTDataBlock.getChannelMap()) == 0) {
            return;
        }

        ComplexArray fft = inUnit.getFftData();
        if (fft == null) return;

        // clone unit (deep copy of complex array) to store in buffer
        ComplexArray fftClone = fft.clone();
        FFTDataUnit clonedUnit = new FFTDataUnit(inUnit.getTimeMilliseconds(), inUnit.getChannelBitmap(), inUnit.getStartSample(), inUnit.getSampleDuration(), fftClone, inUnit.getFftSlice());

        // A batch is only dispatched once EVERY active channel has a full block ready,
        // and all channels' blocks are then processed together in one call. Because all
        // channels are fed FFT slices at the same rate their blocks become ready at the
        // same time, so this adds no latency but lets a model-based mask run a single
        // batched inference across all channels (much better GPU utilisation). A channel
        // that briefly runs ahead simply keeps its surplus slices buffered for the next
        // block.
        List<List<FFTDataUnit>> multiBatch = null;
        synchronized (buffer) {
            int channel = PamUtils.PamUtils.getSingleChannel(clonedUnit.getChannelBitmap());

            buffer[channel].add(clonedUnit); // add the data unit to the correct channel's buffer
            if (unitsToBuffer <= 0) {
                // fallback to at least 1 if unitsToBuffer not yet computed
                unitsToBuffer = 1;
            }

            if (activeChannels != null && activeChannels.length > 0 && allChannelsReady()) {
                // extract exactly one block (unitsToBuffer slices) from each active channel.
                multiBatch = new ArrayList<>(activeChannels.length);
                for (int ch : activeChannels) {
                    List<FFTDataUnit> chBatch = new ArrayList<>(unitsToBuffer);
                    for (int i = 0; i < unitsToBuffer; i++) {
                        chBatch.add(buffer[ch].removeFirst());
                    }
                    multiBatch.add(chBatch);
                }
            }
        }

        if (multiBatch != null) {
            // submit batch for processing on worker thread with backpressure
            try {
                // wait until there is capacity; also increments pending counter atomically
                if (!acquireSubmissionSlot()) {
                    return; // interrupted while waiting
                }

                final List<List<FFTDataUnit>> batch = multiBatch;
                executor.submit(() -> {
                    try {
                        List<List<FFTDataUnit>> processed = applyMaskChannels(batch);
                        if (processed != null) {
                            // push processed units into output data block
                            for (List<FFTDataUnit> chBatch : processed) {
                                for (FFTDataUnit u : chBatch) {
                                    try {
                                        maskedFFTDataBlock.addPamData(u);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
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

    /**
     * @return true if every active channel has at least one full block
     *         ({@link #unitsToBuffer} slices) buffered. Caller must hold the
     *         {@code buffer} lock.
     */
    private boolean allChannelsReady() {
        for (int ch : activeChannels) {
            if (buffer[ch].size() < unitsToBuffer) {
                return false;
            }
        }
        return true;
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

        // drop any stale buffered units (may be from a previous FFT length)
        clearBuffers();

        System.out.println("MaskedFFTProcess: input params channels: " + (params.channelMap & inputFFTData.getChannelMap()) + params.channelMap);

        // copy information from input block
        maskedFFTDataBlock.setChannelMap(params.channelMap & inputFFTData.getChannelMap());
        maskedFFTDataBlock.setFftHop(inputFFTData.getFftHop());
        maskedFFTDataBlock.setFftLength(inputFFTData.getFftLength());
        setSampleRate(inputFFTData.getSampleRate(), false);

        // the channels we process, batched together into a single mask call.
        activeChannels = PamUtils.PamUtils.getChannelArray(maskedFFTDataBlock.getChannelMap());

        // create the mask based on the selected mask type. Only recreate the mask
        // if the user has selected a different mask type so we don't needlessly
        // reload the model. This is done before computing the buffer length so the
        // mask can request a preferred buffer.
        if (params.maskType != null && (this.mask == null || currentMaskType != params.maskType)) {
            PamFFTMask newMask = params.maskType.createMask(this);
            if (newMask != null) {
                setMask(newMask);
                currentMaskType = params.maskType;
            }
        }

        // compute how many units correspond to the buffer length. A mask may request
        // a preferred buffer length (e.g. SAM-Whistle needs ~3 s blocks to match the
        // size the model was trained on); otherwise the user-configured bufferSeconds
        // is used.
        double hopSec = (double) inputFFTData.getHopSamples() / inputFFTData.getSampleRate();
        if (params.bufferSeconds <= 0) params.bufferSeconds = 1.0; // safety
        double bufferSeconds = this.mask.getPreferredBufferSeconds();
        if (bufferSeconds <= 0) bufferSeconds = params.bufferSeconds;
        unitsToBuffer = (int) Math.ceil(bufferSeconds / hopSec);
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

        // record the FFT settings we are now prepared for so newData can detect changes.
        preparedFftLength = inputFFTData.getFftLength();
        preparedFftHop = inputFFTData.getFftHop();

        prepOk = true;
    }

    /**
     * Clear all the per-channel FFT buffers. Called on prepare and whenever the
     * source FFT length changes so units of different lengths are never mixed.
     */
    private void clearBuffers() {
        synchronized (buffer) {
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] != null) {
                    buffer[i].clear();
                }
            }
        }
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
     * Apply the mask to one block from each active channel in a single call. Called
     * on the worker thread. A model-based mask can use this to run one batched
     * inference across all channels.
     *
     * @param channelBatches - one FFT block per active channel.
     * @return the processed blocks in the same channel order.
     */
    protected List<List<FFTDataUnit>> applyMaskChannels(List<List<FFTDataUnit>> channelBatches) {
        return this.mask.applyMaskChannels(channelBatches);
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
