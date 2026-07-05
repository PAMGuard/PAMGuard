package metadata.layoutFX;

import PamController.PamController;
import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import metadata.MetaDataContol;
import metadata.PamguardMetaData;
import nilus.ContactInfo;
import nilus.ContactInfo.Address;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Helper;
import nilus.ResponsibleParty;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.validator.PamValidator;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;

/**
 * JavaFX settings pane for project metadata, replicating the Swing MetaDataDialog.
 * Contains tabs for General information (project info + contact) and Description
 * (objectives, abstract, method).
 * <p>
 * Fields are validated using a {@link PamValidator} which decorates empty required
 * fields with error indicators.
 * 
 * @author PAMGuard
 */
public class MetaDataSettingsPane extends SettingsPane<PamguardMetaData> {

	/**
	 * The main pane.
	 */
	private PamBorderPane mainPane;

	// -- Project information fields --
	private TextField projectField;
	private TextField regionField;
	private TextField cruiseField;
	private TextField siteField;

	// -- Contact / responsible party fields --
	private TextField nameField;
	private TextField organisationField;
	private TextField positionField;
	private TextField emailField;

	// -- Description fields --
	private TextArea objectivesArea;
	private TextArea abstractArea;
	private TextArea methodArea;

	/**
	 * Validator for decorating empty fields.
	 */
	private PamValidator validator;

	/**
	 * Reference to any TethysControl if available.
	 */
	private TethysControl tethysControl;

	public MetaDataSettingsPane(Object ownerWindow) {
		super(ownerWindow);
		tethysControl = (TethysControl) PamController.getInstance().findControlledUnit(TethysControl.unitType);
		validator = new PamValidator();
		mainPane = new PamBorderPane();
		mainPane.setCenter(createMainPane());
	}

	/**
	 * Create the main tabbed pane.
	 */
	private Node createMainPane() {
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		Tab generalTab = new Tab("General", createGeneralPane());
		Tab descriptionTab = new Tab("Description", createDescriptionPane());

		tabPane.getTabs().addAll(generalTab, descriptionTab);

		return tabPane;
	}

	/**
	 * Create the General tab content with project information and contact panels.
	 */
	private Node createGeneralPane() {
		PamVBox vBox = new PamVBox(10);
		vBox.setPadding(new Insets(10));

		vBox.getChildren().add(createProjectInfoPane());
		vBox.getChildren().add(createContactPane());

		return vBox;
	}

	/**
	 * Create the project information section.
	 */
	private Node createProjectInfoPane() {
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(5));

		// Make the text fields grow
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().addAll(labelCol, fieldCol);

		int row = 0;

		gridPane.add(new Label("Project Name"), 0, row);
		projectField = new TextField();
		projectField.setTooltip(new Tooltip("Project name"));
		gridPane.add(projectField, 1, row);

		// validate project name - must not be empty
		validator.createCheck()
			.dependsOn("project_name", projectField.textProperty())
			.withMethod(c -> {
				String val = c.get("project_name");
				if (val == null || val.trim().isEmpty()) {
					c.error("Project Name is required: enter the name of the project this data belongs to");
				}
			})
			.decorates(projectField)
			.immediate();

		row++;
		gridPane.add(new Label("Region"), 0, row);
		regionField = new TextField();
		regionField.setTooltip(new Tooltip("Geographic region"));
		gridPane.add(regionField, 1, row);

		// validate region
		validator.createCheck()
			.dependsOn("region", regionField.textProperty())
			.withMethod(c -> {
				String val = c.get("region");
				if (val == null || val.trim().isEmpty()) {
					c.error("Region is required: enter the geographic region where the data was collected");
				}
			})
			.decorates(regionField)
			.immediate();

		row++;
		gridPane.add(new Label("Cruise Name"), 0, row);
		cruiseField = new TextField();
		cruiseField.setTooltip(new Tooltip("Cruise name"));
		gridPane.add(cruiseField, 1, row);

		row++;
		gridPane.add(new Label("Site"), 0, row);
		siteField = new TextField();
		siteField.setTooltip(new Tooltip("Deployment site"));
		gridPane.add(siteField, 1, row);

		// Wrap in a titled section
		PamVBox wrapper = new PamVBox(5);
		Label title = new Label("General project information");
		title.setStyle("-fx-font-weight: bold;");
		wrapper.getChildren().addAll(title, gridPane);

		return wrapper;
	}

	/**
	 * Create the contact / responsible party section.
	 */
	private Node createContactPane() {
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(5));

		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().addAll(labelCol, fieldCol);

		int row = 0;

		gridPane.add(new Label("Name"), 0, row);
		nameField = new TextField();
		nameField.setTooltip(new Tooltip("Person responsible for data"));
		gridPane.add(nameField, 1, row);

		// validate contact name
		validator.createCheck()
			.dependsOn("contact_name", nameField.textProperty())
			.withMethod(c -> {
				String val = c.get("contact_name");
				if (val == null || val.trim().isEmpty()) {
					c.error("Contact Name is required: enter the name of the person responsible for the data");
				}
			})
			.decorates(nameField)
			.immediate();

		row++;
		gridPane.add(new Label("Organisation"), 0, row);
		organisationField = new TextField();
		organisationField.setTooltip(new Tooltip("Responsible organization"));
		gridPane.add(organisationField, 1, row);

		row++;
		gridPane.add(new Label("Position"), 0, row);
		positionField = new TextField();
		positionField.setTooltip(new Tooltip("Person's role in organization"));
		gridPane.add(positionField, 1, row);

		row++;
		gridPane.add(new Label("Email"), 0, row);
		emailField = new TextField();
		emailField.setTooltip(new Tooltip("Email address or other contact details"));
		gridPane.add(emailField, 1, row);

		PamVBox wrapper = new PamVBox(5);
		Label title = new Label("Contact information");
		title.setStyle("-fx-font-weight: bold;");
		wrapper.getChildren().addAll(title, gridPane);

		return wrapper;
	}

	/**
	 * Create the Description tab content with objectives, abstract, and method text areas.
	 */
	private Node createDescriptionPane() {
		PamVBox vBox = new PamVBox(10);
		vBox.setPadding(new Insets(10));

		objectivesArea = createStyledTextArea(8,
				"What are the objectives of this effort? Examples:\n"
				+ "Beamform to increase SNR for detection.\n"
				+ "Detect every click of a rare species.\n"
				+ "Verify data quality.");

		// validate objectives
		validator.createCheck()
			.dependsOn("objectives", objectivesArea.textProperty())
			.withMethod(c -> {
				String val = c.get("objectives");
				if (val == null || val.trim().isEmpty()) {
					c.error("Objectives is required: describe the goals of this monitoring effort");
				}
			})
			.decorates(objectivesArea)
			.immediate();

		abstractArea = createStyledTextArea(6, "Overview of effort.");

		// validate abstract
		validator.createCheck()
			.dependsOn("abstract", abstractArea.textProperty())
			.withMethod(c -> {
				String val = c.get("abstract");
				if (val == null || val.trim().isEmpty()) {
					c.error("Abstract is required: provide a brief overview of the monitoring effort");
				}
			})
			.decorates(abstractArea)
			.immediate();

		methodArea = createStyledTextArea(6, "High-level description of the method used.");

		vBox.getChildren().addAll(
				createLabelledTextArea("Objectives", objectivesArea),
				createLabelledTextArea("Abstract", abstractArea),
				createLabelledTextArea("Method", methodArea));

		return vBox;
	}

	/**
	 * Create a TextArea with consistent styling to match the dark-themed TextFields.
	 * This ensures the text area background, text colour and border are consistent
	 * with other text input controls in the application.
	 * @param prefRows - preferred number of visible rows
	 * @param tooltipText - tooltip text for the area
	 * @return a styled TextArea
	 */
	private TextArea createStyledTextArea(int prefRows, String tooltipText) {
		TextArea textArea = new TextArea();
		textArea.setPrefRowCount(prefRows);
		textArea.setWrapText(true);
		textArea.setTooltip(new Tooltip(tooltipText));
		// ensure the text area background matches the dark-themed text fields
		textArea.setStyle(
			"-fx-control-inner-background: -color-bg-default; " +
			"-fx-text-fill: -color-fg-default;"
		);
		return textArea;
	}

	/**
	 * Create a labelled text area with a bold title label.
	 */
	private Node createLabelledTextArea(String title, TextArea textArea) {
		PamVBox box = new PamVBox(2);
		Label label = new Label(title);
		label.setStyle("-fx-font-weight: bold;");
		box.getChildren().addAll(label, textArea);
		PamVBox.setVgrow(textArea, Priority.ALWAYS);
		return box;
	}

	@Override
	public PamguardMetaData getParams(PamguardMetaData currParams) {
		// the SettingsDialog passes null here so fetch from the controller if needed
		if (currParams == null) {
			currParams = MetaDataContol.getMetaDataControl().getMetaData();
		}

		// check for validation errors - show a detailed warning if any fields are not filled in
		if (validator.containsErrors()) {
			String content = PamValidator.list2String(validator.getValidationResult().getMessages());
			PamDialogFX.showWarning(null, "Missing project information",
				"Some required fields have not been completed. "
				+ "Please fill in the highlighted fields before closing the dialog:\n\n" + content);
			return null;
		}

		Deployment deployment = currParams.getDeployment();

		// Project information
		deployment.setProject(projectField.getText());
		deployment.setRegion(regionField.getText());
		deployment.setCruise(cruiseField.getText());
		deployment.setSite(siteField.getText());

		// Description
		DescriptionType description = deployment.getDescription();
		if (description == null) {
			description = new DescriptionType();
			deployment.setDescription(description);
		}
		description.setObjectives(objectivesArea.getText());
		description.setAbstract(abstractArea.getText());
		description.setMethod(methodArea.getText());

		// Responsible party / contact
		ResponsibleParty contact = deployment.getMetadataInfo().getContact();
		contact.setIndividualName(nameField.getText());
		contact.setOrganizationName(organisationField.getText());
		contact.setPositionName(positionField.getText());

		if (contact.getContactInfo() == null) {
			ContactInfo ci = new ContactInfo();
			contact.setContactInfo(ci);
			try {
				Helper.createRequiredElements(ci);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
		Address addr = contact.getContactInfo().getAddress();
		if (addr == null) {
			addr = new Address();
			contact.getContactInfo().setAddress(addr);
		}
		addr.setElectronicMailAddress(emailField.getText());

		// Notify Tethys if present
		if (tethysControl != null) {
			tethysControl.sendStateUpdate(new TethysState(StateType.NEWPROJECTSELECTION));
		}

		return currParams;
	}

	@Override
	public void setParams(PamguardMetaData input) {
		if (input == null) {
			return;
		}
		Deployment deployment = input.getDeployment();

		// Project information
		projectField.setText(deployment.getProject());
		regionField.setText(deployment.getRegion());
		cruiseField.setText(deployment.getCruise());
		siteField.setText(deployment.getSite());

		// Description
		DescriptionType description = deployment.getDescription();
		if (description != null) {
			objectivesArea.setText(description.getObjectives());
			abstractArea.setText(description.getAbstract());
			methodArea.setText(description.getMethod());
		} else {
			objectivesArea.setText(null);
			abstractArea.setText(null);
			methodArea.setText(null);
		}

		// Contact
		ResponsibleParty contact = deployment.getMetadataInfo().getContact();
		if (contact != null) {
			nameField.setText(contact.getIndividualName());
			organisationField.setText(contact.getOrganizationName());
			positionField.setText(contact.getPositionName());

			ContactInfo contactInfo = contact.getContactInfo();
			if (contactInfo != null) {
				Address addr = contactInfo.getAddress();
				if (addr != null) {
					emailField.setText(addr.getElectronicMailAddress());
				}
			}
		}

		// run initial validation to decorate any empty required fields
		validator.validate();
	}

	@Override
	public String getName() {
		return "Project Information";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// nothing needed
	}
}
