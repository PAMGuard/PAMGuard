package targetMotionOld.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.PamDetection;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import targetMotionOld.EventLocalisationProgress;
import targetMotionOld.TargetMotionLocalisation;
import targetMotionOld.TargetMotionLocaliser;
import targetMotionOld.TargetMotionLocaliser.Interractive;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class EventControlPanel<T extends GroupDetection> implements TMDialogComponent{


	private TargetMotionDialog<T> targetMotionDialog;

	private TargetMotionLocaliser<T> targetMotionLocaliser;

	public static final int SEL_ONE_EVENT = 1;
	public static final int SEL_ALL_EVENTS = 2;
	public static final int SEL_CHANGED_EVENTS = 3;
	
	
	private JPanel mainPanel;

	private JComboBox eventList;
	private JLabel eventText;
	private JTextField currentLocalisation;
	private JRadioButton supervised, unsupervised;
//	private JRadioButton oneEvent, allEvents;
	private JTextField comment;
	private JButton runButton, runAllButton, save, keepOld, setNull, back, stop;
	private int[] eventDBIndexes;
	private PamDataBlock<T> dataBlock;

	private EventLocalisationProgress eventLocalisationProgress = 
		new EventLocalisationProgress(EventLocalisationProgress.DONE, -1);
	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public EventControlPanel(TargetMotionLocaliser<T> targetMotionLocaliser,
			TargetMotionDialog<T> targetMotionDialog) {
		super();
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;
		dataBlock = targetMotionLocaliser.getDataBlock();

		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Event Selection"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		c.ipady = 0;
		PamDialog.addComponent(mainPanel, new JLabel("Events "), c);
		c.gridx += c.gridwidth;
		c.fill = GridBagConstraints.HORIZONTAL;
		PamDialog.addComponent(mainPanel, eventList = new JComboBox(), c);
		c.gridx += c.gridwidth;
		PamDialog.addComponent(mainPanel, new JLabel("  Current Event:", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 3;
		PamDialog.addComponent(mainPanel, eventText = new JLabel("no current event                                               ."), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		int y = c.gridy;
//		PamDialog.addComponent(mainPanel, oneEvent = new JRadioButton("Single Event"), c);
//		c.gridy++;
//		PamDialog.addComponent(mainPanel, allEvents = new JRadioButton("All Events"), c);
//		c.gridy = y;
		c.gridx += c.gridwidth;
		PamDialog.addComponent(mainPanel, supervised = new JRadioButton("Supervised (allows comments)"), c);
		c.gridy++;
		PamDialog.addComponent(mainPanel, unsupervised = new JRadioButton("Un-supervised"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, new JLabel("Current ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 8;
		PamDialog.addComponent(mainPanel, currentLocalisation = new JTextField() , c);
		currentLocalisation.setEditable(false);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, new JLabel("Comment ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 8;
		PamDialog.addComponent(mainPanel, comment = new JTextField(50), c);
		c.gridx = 3;
		c.gridwidth = 1;
		c.gridy++;
		PamDialog.addComponent(mainPanel, runButton = new JButton("Run Current Event"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, runAllButton = new JButton("Run All Events"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, save = new JButton("Save"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, keepOld = new JButton("Keep Old"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, setNull = new JButton("Set Null"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, back = new JButton("Back"), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, stop = new JButton("Stop"), c);

//		ButtonGroup evGroup = new ButtonGroup();
//		evGroup.add(oneEvent);
//		evGroup.add(allEvents);

		ButtonGroup supGroup = new ButtonGroup();
		supGroup.add(supervised);
		supGroup.add(unsupervised);

		eventList.addActionListener(new EventList());
		supervised.addActionListener(new Supervised());
		unsupervised.addActionListener(new UnSupervised());
//		oneEvent.addActionListener(new OneEvent());
//		allEvents.addActionListener(new AllEvents());
		runButton.addActionListener(new RunButton());
		runAllButton.addActionListener(new RunAllButton());
		save.addActionListener(new SaveButton());
		keepOld.addActionListener(new KeepOldButton());
		setNull.addActionListener(new SetNullButton());
		back.addActionListener(new BackButton());
		stop.addActionListener(new StopButton());

		eventList.addItem("No events available in list              ");
//		oneEvent.setSelected(true);
		supervised.setSelected(true);

	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return mainPanel;
	}

	public void enableControls() {
		// see how many results are available. 
		ArrayList<GroupLocResult> results = targetMotionLocaliser.getResults();
		int nResult = results.size();
		runButton.setEnabled(targetMotionDialog.canRun() & 
				eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
		runAllButton.setEnabled(targetMotionDialog.canRun() & 
				eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
		save.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.WAITING && nResult > 0);
		keepOld.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.WAITING);
		setNull.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.WAITING);
		back.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.WAITING);
		stop.setEnabled(eventLocalisationProgress.progressType != EventLocalisationProgress.DONE);
		supervised.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
		unsupervised.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
//		oneEvent.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
//		allEvents.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
		eventList.setEnabled(eventLocalisationProgress.progressType == EventLocalisationProgress.DONE);
	}

	/**
	 * update the list of events. 
	 */
	void updateEventList() {
//		T currentEvent = getListedEvent();
		int currEventIndex = getListedEventDBIndex();
		int newEventIndex = 0;
		eventList.removeAllItems();
		int n = dataBlock.getUnitsCount();
		eventDBIndexes = new int[n];
		ListIterator<T> it = dataBlock.getListIterator(0);
		T dataUnit;
		int i = 0;
		while (it.hasNext()) {
			dataUnit = it.next();
			eventList.addItem(String.format("Id %d; %s", dataUnit.getDatabaseIndex(), 
					PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds())));
			eventDBIndexes[i] = dataUnit.getDatabaseIndex();
			
			if (dataUnit.getDatabaseIndex() == currEventIndex) {
				newEventIndex = i;
			}
			i++;
		}
		if (currEventIndex >= 0 && currEventIndex < eventList.getItemCount()) {
			eventList.setSelectedIndex(newEventIndex);
		}
	}

	/**
	 * Get the event currently selected in the drop down list. 
	 * @return an event. 
	 */
	int getListedEventDBIndex() {
		if (eventDBIndexes == null) {
			return -1;
		}
		int i = eventList.getSelectedIndex();
		if (i >= 0 && i < eventDBIndexes.length) {
			return eventDBIndexes[i];
		}
		return -1;
	}

	@Override
	public void setCurrentEventIndex(int eventIndex, Object sender) {
		// NB this is the databsae index
		if (sender == this) return;
		selectEventIndex(eventIndex);
	}

	
	/**
	 * Called when the database index has been changed somewhere. 
	 * @param selEventDBIndex
	 */
	public void selectEventIndex(int selEventDBIndex) {
		int evIndex = findEventListIndex(selEventDBIndex);
		if (evIndex != eventList.getSelectedIndex() && evIndex >= 0) {
			eventList.setSelectedIndex(evIndex);
		}
		targetMotionLocaliser.setCurrentEventIndex(selEventDBIndex, this);
		targetMotionDialog.enableControls();
		T selEvent = targetMotionLocaliser.findEvent(selEventDBIndex);
		sayEventInfo(selEvent);
		if (selEvent == null) {
			return;
		}
		TargetMotionLocalisation tml = targetMotionLocaliser.getTMLocalisation(selEvent);
		if (tml != null) {
			GroupLocResult tmr = tml.getTargetMotionResult(0);
			if (tmr != null) {
				comment.setText(tmr.getComment());
			}
		}
	}

	public int findEventListIndex(int selEventDBIndex) {
		if (eventDBIndexes == null) {
			return -1;
		}
		for (int i = 0; i < eventDBIndexes.length; i++) {
			if (eventDBIndexes[i] == selEventDBIndex) {
				return i;
			}
		}
		return -1;
	}
	
//	/**
//	 *
//	 * @return the type of event list to process - one, all or changed.  
//	 */
//	protected int getEventSelectionType() {
//		if (oneEvent.isSelected()) {
//			return SEL_ONE_EVENT;
//		}
//		else if (allEvents.isSelected()) {
//			return SEL_ALL_EVENTS;
//		}
//		return 0;
//	}
	
	/**
	 * 
	 * @return true of supervised radio button is selected. 
	 */
	protected boolean isSupervised() {
		return supervised.isSelected();
	}

	/**
	 * Get any text comment from the comment field
	 * @return comment
	 */
	protected String getComment() {
		return comment.getText();
	}
	
	/**
	 * Clear the comment text.
	 */
	protected void clearComment() {
		comment.setText(null);
	}
	
	public void notifyNewResults() {
		int selEventDBIndex = getListedEventDBIndex();
		T selEvent = targetMotionLocaliser.findEvent(selEventDBIndex);
		sayEventInfo(selEvent);
	}

	private void sayEventInfo(T event) {
		if (event == null) {
			eventText.setText("No selected event");
			currentLocalisation.setText("");
			return;
		}
		String s = String.format("Id %d, %s, with %d sub detections", event.getDatabaseIndex(), 
				PamCalendar.formatDateTime2(event.getTimeMilliseconds()), event.getSubDetectionsCount());
		GroupLocalisation loc  = null;
		try {
			loc = (GroupLocalisation) event.getLocalisation();
		}
		catch (ClassCastException e) {
			loc = null;
		}
		if (loc == null) {
			currentLocalisation.setText("This event currently has no localisation information");
		}
		else {
			currentLocalisation.setText(loc.toString());
		}
		eventText.setText(s);
	}

	private class EventList implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int selEvent = getListedEventDBIndex();
			selectEventIndex(selEvent);
		}
	}
	private class Supervised implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
		}
	}
	private class UnSupervised implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
		}
	}
	private class OneEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
		}
	}
	private class ChangedEvents implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
		}
	}
	private class AllEvents implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
		}
	}
	
	private class RunButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionDialog.start(false);
		}
	}
	
	private class RunAllButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionDialog.start(true);
		}
	}
	
	private class SaveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionLocaliser.interractiveCommand(Interractive.SAVE);
		}
	}
	
	private class SetNullButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionLocaliser.interractiveCommand(Interractive.SETNULL);
		}
	}
	
	private class KeepOldButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionLocaliser.interractiveCommand(Interractive.KEEPOLD);
		}
	}
	
	private class BackButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionLocaliser.interractiveCommand(Interractive.BACK);
		}
	}
	
	private class StopButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionLocaliser.interractiveCommand(Interractive.CANCEL);
		}
	}
	@Override
	public boolean canRun() {
		return (eventDBIndexes != null && eventDBIndexes.length > 0);
	}

	public void progressReport(
			EventLocalisationProgress eventLocalisationProgress) {
		this.eventLocalisationProgress = eventLocalisationProgress;
		enableControls();
	}
}
