package rawDeepLearningClassifier.dataSelector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import rawDeepLearningClassifier.dlClassification.DLClassName;

/**
 * Swing panel for Deep learning predicitons. 
 */
public class DLPredictionPanel implements PamDialogPanel {

	private DLPredictionFilter predicitonFilter;

	private PamPanel contentPanel;

	private JCheckBox[] enableClass;

	private JSpinner[] spinnerClass;

	private JToggleButton lockButton;


	public DLPredictionPanel(DLPredictionFilter dlPredictionFilter) {
		super();
		this.predicitonFilter = dlPredictionFilter; 

		contentPanel = new PamPanel(); 
		contentPanel.setLayout(new GridBagLayout());

		lockButton = new JToggleButton(); 

		FontIcon iconlock = FontIcon.of(MaterialDesignL.LOCK);
		iconlock.setIconSize(20);
		iconlock.setIconColor(Color.DARK_GRAY);

		FontIcon iconlockopen = FontIcon.of(MaterialDesignL.LOCK_OPEN);
		iconlockopen.setIconSize(20);
		iconlockopen.setIconColor(Color.DARK_GRAY);

		lockButton.setIcon(iconlockopen);

		lockButton.addActionListener((action)->{
			if (lockButton.isSelected()) {
				lockButton.setIcon(iconlock);
			}
			else {
				lockButton.setIcon(iconlockopen);
			}
			lockButton.validate();
		});


	}

	@Override
	public JComponent getDialogComponent() {
		return contentPanel;
	}

	@Override
	public void setParams() {

		DLPredictionFilterParams params = predicitonFilter.getParams(); 
		// TODO Auto-generated method stub
		setClassPane(params); 

		for (int i=0; i<params.classSelect.length ; i++) {
			//set the correct params
			enableClass[i].setSelected(params.classSelect[i]);
			spinnerClass[i].setValue(params.minClassPredicton[i]);
		}

	}


	private void setClassPane(DLPredictionFilterParams input) {
		DLClassName[] classNames = predicitonFilter.getDLControl().getDLModel().getClassNames(); 
		contentPanel.removeAll();

		enableClass = new JCheckBox[input.classSelect.length];
		spinnerClass = new JSpinner[input.classSelect.length];

		GridBagConstraints c = new PamGridBagContraints();
		c.ipadx =5;

		c.gridwidth=2;
		contentPanel.add(new Label("Min. prediction for each class"), c);
		c.gridwidth=1;
		c.gridy++;


		for (int i=0; i<input.classSelect.length ; i++) {

			//create the row
			c.gridx = 0;

			enableClass[i] = new JCheckBox(classNames[i].className);
			final int ii = i;
			enableClass[i].addActionListener((action)->{
				spinnerClass[ii].setEnabled(enableClass[ii].isSelected());
			});
			enableClass[i].setToolTipText(classNames[i].className);
			contentPanel.add(enableClass[i], c);

			c.gridx = 1;


			spinnerClass[i] = new JSpinner(new SpinnerNumberModel(0., 0., 1., 0.05));

			Dimension prefSize = spinnerClass[i].getPreferredSize();
			prefSize = new Dimension(60, prefSize.height);
			spinnerClass[i] .setPreferredSize(prefSize);

			spinnerClass[i].addChangeListener(new ChangeListener() {      
				@Override
				public void stateChanged(ChangeEvent e) {
					if (lockButton.isSelected()) {
						for (int j=0; j<spinnerClass.length ; j++) {
							if (j!=ii) {
								spinnerClass[j].setValue(spinnerClass[ii].getValue());
							}
						}
					}
				}
			});

			spinnerClass[i].setToolTipText(classNames[i].className);

			contentPanel.add(spinnerClass[i], c);

			if (i==0 && input.classSelect.length>1) {
				//set a lock button to 
				c.gridx=2; 
				contentPanel.add(lockButton, c);

				//make the lock button the same height as the spinner
				Dimension prefSizeB = lockButton.getPreferredSize();
				lockButton.setPreferredSize(new Dimension(prefSizeB.width, prefSize.height));

			}

			c.gridy++;


		}

		contentPanel.validate();
	}

	@Override
	public boolean getParams() {

		DLPredictionFilterParams currParams = predicitonFilter.getParams(); 

		for (int i=0; i<spinnerClass.length ; i++) {
			currParams.classSelect[i] = enableClass[i].isSelected();
			currParams.minClassPredicton[i] = (double) spinnerClass[i].getValue();
		}

		predicitonFilter.setParams(currParams);

		return true;
	}

}
