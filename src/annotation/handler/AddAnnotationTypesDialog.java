package annotation.handler;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import annotation.DataAnnotationType;

/**
 * Dialog listing every annotation type a handler has, grouped (see
 * {@link DataAnnotationType#getAnnotationGroup()}), so the user can choose which appear in
 * the list of annotation types they select from. Ticked types are listed, unticked ones
 * are removed from the list.
 *
 * @author Jamie Macaulay
 */
public class AddAnnotationTypesDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private static final String GENERAL_GROUP = "General annotations";

	private AnnotationChoiceHandler annotationHandler;

	private HashMap<DataAnnotationType<?>, JCheckBox> checkBoxes = new HashMap<>();

	private List<DataAnnotationType<?>> result;

	private AddAnnotationTypesDialog(Window parentFrame, AnnotationChoiceHandler annotationHandler) {
		super(parentFrame, "Add annotation types", false);
		this.annotationHandler = annotationHandler;

		// group the types, keeping the order they're available in.
		LinkedHashMap<String, JPanel> groups = new LinkedHashMap<>();
		for (DataAnnotationType<?> anType : annotationHandler.getAvailableAnnotationTypes()) {
			String group = anType.getAnnotationGroup() == null ? GENERAL_GROUP : anType.getAnnotationGroup();
			JPanel groupPanel = groups.get(group);
			if (groupPanel == null) {
				groupPanel = new JPanel(new GridLayout(0, 3));
				groupPanel.setBorder(new TitledBorder(group));
				groups.put(group, groupPanel);
			}
			JCheckBox cb = new JCheckBox(anType.getAnnotationName());
			cb.setToolTipText(anType.getAnnotationDescription());
			checkBoxes.put(anType, cb);
			groupPanel.add(cb);
		}

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel groupsPanel = new JPanel();
		groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
		for (JPanel groupPanel : groups.values()) {
			groupsPanel.add(groupPanel);
		}
		mainPanel.add(BorderLayout.CENTER, groupsPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton all = new JButton("Select all");
		all.addActionListener((e) -> selectAll(true));
		JButton none = new JButton("Select none");
		none.addActionListener((e) -> selectAll(false));
		buttonPanel.add(all);
		buttonPanel.add(none);
		mainPanel.add(BorderLayout.SOUTH, buttonPanel);

		setDialogComponent(mainPanel);
	}

	/**
	 * Show the dialog.
	 * @param parentFrame parent window.
	 * @param annotationHandler the annotation handler.
	 * @param listedTypes the types currently in the list, which are ticked.
	 * @return the types the user wants listed, in the order they are available, or null if cancelled.
	 */
	public static List<DataAnnotationType<?>> showDialog(Window parentFrame, AnnotationChoiceHandler annotationHandler,
			List<DataAnnotationType<?>> listedTypes) {
		AddAnnotationTypesDialog dialog = new AddAnnotationTypesDialog(parentFrame, annotationHandler);
		for (DataAnnotationType<?> anType : dialog.checkBoxes.keySet()) {
			dialog.checkBoxes.get(anType).setSelected(listedTypes.contains(anType));
		}
		dialog.pack();
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
		return dialog.result;
	}

	private void selectAll(boolean selected) {
		for (JCheckBox cb : checkBoxes.values()) {
			cb.setSelected(selected);
		}
	}

	@Override
	public boolean getParams() {
		ArrayList<DataAnnotationType<?>> listed = new ArrayList<>();
		for (DataAnnotationType<?> anType : annotationHandler.getAvailableAnnotationTypes()) {
			if (checkBoxes.get(anType).isSelected()) {
				listed.add(anType);
			}
		}
		result = listed;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		result = null;
	}

	@Override
	public void restoreDefaultSettings() {
	}

}
