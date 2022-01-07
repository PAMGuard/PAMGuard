package KernelSmoothing;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import fftManager.FFTDataUnit;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

/**
 * Dialog for smoothing kernel options.
 * <p>
 * The smoothing kernel operates on fft data by replacing
 * each point in a spectrgram matrix with a number obtained by 
 * convolving the spectrogram with a 3 by 3 gaussian matrix.
 * <p>
 * Currently the only options are the data source and which
 * channels to operate on.
 * @author Doug Gillespie
 * @see PamView.dialog.PamDialog
 * @see KernelSmoothing.KernelSmoothingControl
 * @see KernelSmoothing.KernelSmoothingParameters
 * @see KernelSmoothing.KernelSmoothingProcess
 *
 */
public class KernelSmoothingDialog extends PamDialog   {

	private static KernelSmoothingDialog singleInstance;
	
	private static KernelSmoothingParameters smoothingParameters;

	JButton okButton, cancelButton;
	
	SourcePanel sourcePanel;
	
	private KernelSmoothingDialog(Frame parentFrame) {
		super(parentFrame, "Kernel smoothing options", false);
		

		/*
		 * KernelSmoothingDialog uses a standard panel to list 
		 * the FFT data sources - this contains all the necessary
		 * drop down list functionality and the optional checkbox list
		 * of channels to select.
		 * Although the simplest thing to do would be to shove
		 * the sourcePanel straight into the dialog with
		 * setDialogComponent(sourcePanel.getPanel());
		 * the followng insertes it into anothe rjpanel with a
		 * small border to space it all out a bit.
		 */
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		sourcePanel = new SourcePanel(this, "FFT Data Source", FFTDataUnit.class, true, true);
		p.add(sourcePanel.getPanel());

		/*
		 * Once you've constructed an approriate dialog panel, set it
		 * here as the main panel of the dialog.
		 * setDialogComponent(sourcePanel.getPanel())
		 * would have been a lot simpler !
		 */
		setDialogComponent(p);
	}
	static public KernelSmoothingParameters showDialog(Frame parentFrame, 
			KernelSmoothingParameters smoothingParameters, PamDataBlock outputDataBlock) {
		/*
		 * All dialogs need to keep a track of the frame they've been created in - this
		 * will probably be either the main GUI or the model viewer. This is important
		 * for correct modal dialog operation. If parentFrame == null, it will work,
		 * but there is a risk the dialog may get 'lost' behind the main GUI and you'll
		 * be stuck since you can't move the GUI until you close the dialog and 
		 * you can't close the dialog because it's behind the GUI.
		 * Since the frame can only be set at object creation time, a new dialog
		 * object is created if the frame changes. The old one will then get eaten by
		 * the garbage collector
		 */
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new KernelSmoothingDialog(parentFrame);
		}
		/*
		 * Clone the parameters into the local static copy
		 */
		KernelSmoothingDialog.smoothingParameters = smoothingParameters.clone();
		
		/*
		 * set the parameters in the dialog display
		 */
		singleInstance.setParams(outputDataBlock);
		
		/*
		 * make the dialog visible
		 */
		singleInstance.setVisible(true);
		
		/*
		 * this next line will not execute until the dialog 
		 * is closed 
		 */
		return KernelSmoothingDialog.smoothingParameters;
	}
	
	private void setParams(PamDataBlock outputDataBlock) {

		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
				.getFFTDataBlocks();
		/*
		 * sourcePanel will make a list of all fft sources. This 
		 * will naturally include this kernel smoothing process.
		 * Since it doesn't make sense for the process to observe it's
		 * own output, the kernel smoothing process's output data block
		 * has been fed into here and will be excluded from the drop down
		 * list.  
		 */
		sourcePanel.excludeDataBlock(outputDataBlock, true);
		sourcePanel.setSource(fftBlocks.get(smoothingParameters.fftBlockIndex));
		sourcePanel.setChannelList(smoothingParameters.channelList);
	}
	
	@Override
	public boolean getParams() {
		smoothingParameters.fftBlockIndex = sourcePanel.getSourceIndex();
		smoothingParameters.channelList  = sourcePanel.getChannelList();
		return true;
	}
	@Override
	public void cancelButtonPressed() {
		KernelSmoothingDialog.smoothingParameters = null;
	}
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
}
