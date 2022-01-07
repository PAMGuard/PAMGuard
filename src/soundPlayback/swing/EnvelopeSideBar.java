package soundPlayback.swing;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import soundPlayback.preprocess.EnvelopeTracer;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class EnvelopeSideBar implements PreprocessSwingComponent {

	private EnvelopeTracer envelopeTracer;

	private BasicSidebarLayout basicSidebarLayout;
	
	private JButton menuButton;
	
	private EnvelopeSlider envelopeSlider;
	
	
	public EnvelopeSideBar(EnvelopeTracer envelopeTracer) {
		this.envelopeTracer = envelopeTracer;
		envelopeSlider = new EnvelopeSlider();
		menuButton = new JButton("\u00B7\u00B7\u00B7");
		menuButton.setMargin(new Insets(0, 6, 0, 6));
		menuButton.setToolTipText("Envelope Tracer Options");
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(envelopeSlider, menuButton);
		basicSidebarLayout.setToolTipText("<html>Mix raw audio data with envelope traced waveform.<p>"+
		"This can be used to simultaneously listen for ultrasonic click trains <br>and normal audio band sounds.</html>");
		envelopeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mixChanged();
			}
		});
		menuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuButtonAction(e);
			}
		});
	}

	protected void mixChanged() {
		envelopeTracer.setMixRatio(envelopeSlider.getDataValue());
		sayValue();
	}

	protected void menuButtonAction(ActionEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Pre Filter ...");
		menuItem.setToolTipText("Band Pass Filter waveform prior to envelope trace");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				envelopeTracer.preFilterMenu(e);
			}
		});
		popMenu.add(menuItem);
		menuItem = new JMenuItem("Post Filter ...");
		menuItem.setToolTipText("Low Pass Filter envelope after tracing");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				envelopeTracer.postFilterMenu(e);
			}
		});
		popMenu.add(menuItem);
		popMenu.show(menuButton, menuButton.getWidth()/2, menuButton.getHeight()/2);
	}

	@Override
	public JComponent getComponent() {
		return basicSidebarLayout.getComponent();
	}

	@Override
	public void update() {
		envelopeSlider.setDataValue(envelopeTracer.getMixRatio());
		sayValue();
	}
	
	private void sayValue() {
		basicSidebarLayout.setTextLabel(envelopeTracer.getStateText());
	}

}
