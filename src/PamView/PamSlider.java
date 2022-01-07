package PamView;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.plaf.SliderUI;

import PamView.PamColors.PamColor;

public class PamSlider extends JSlider implements ColorManaged {

	{
//		setPaintTrack(true);
//		setPaintTicks(true);
//		setPaintLabels(true);
	}
    @Override
    public void updateUI() {
    	SliderUI aui = getUI();
//        setUI(getUI());
    	setUI(new SliderUI() {
		});
    	super.updateUI();
        updateLabelUIs();
    }
	
	public PamSlider() {
		// TODO Auto-generated constructor stub
	}

	public PamSlider(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public PamSlider(BoundedRangeModel arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public PamSlider(int arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public PamSlider(int arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public PamSlider(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
}
