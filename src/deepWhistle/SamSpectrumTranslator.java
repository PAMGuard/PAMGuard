package deepWhistle;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * DJL translator for the SAM-Whistle model, batched across channels.
 * <p>
 * The SAM-Whistle model is a Segment-Anything based network which expects a batch
 * of three-channel images of shape <code>[N, 3, H, W]</code>. Whistle spectrograms
 * are single-channel, so - exactly as the Python inference code does
 * (<code>np.stack([block, block, block])</code>) - each channel's single
 * spectrogram is replicated across the three image channels.
 * <p>
 * The input is <code>float[N][H][W]</code>: one single-channel spectrogram block
 * per audio channel, where <code>H</code> is frequency and <code>W</code> is time.
 * The output is the flattened confidence surface (sigmoid, values in [0, 1]) for
 * the whole batch - <code>[N, 1, H, W]</code> flattened row-major - which the
 * caller slices back into one <code>H x W</code> mask per channel.
 *
 * @author Jamie Macaulay
 */
public class SamSpectrumTranslator implements Translator<float[][][], float[]> {

	@Override
	public NDList processInput(TranslatorContext ctx, float[][][] data) {
		NDManager manager = ctx.getNDManager();

		int n = data.length;
		int height = data[0].length;
		int width = data[0][0].length;

		//replicate each channel's single spectrogram across the 3 image channels the
		//SAM image encoder expects. Layout is [N, 3, H, W] in row-major (C) order.
		float[] input = new float[n * 3 * height * width];
		int idx = 0;
		for (int b = 0; b < n; b++) {
			for (int c = 0; c < 3; c++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						input[idx++] = data[b][h][w];
					}
				}
			}
		}

		NDArray array = manager.create(input, new Shape(n, 3, height, width));

		NDList list = new NDList();
		list.add(array);
		return list;
	}

	@Override
	public float[] processOutput(TranslatorContext ctx, NDList list) {
		//model output is [N, 1, H, W]; return it flattened row-major.
		return list.get(0).toFloatArray();
	}

	@Override
	public Batchifier getBatchifier() {
		//the input is already batched ([N, 3, H, W]) so no batchifier is needed.
		return null;
	}

}
