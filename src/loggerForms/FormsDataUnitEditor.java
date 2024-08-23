/**
 * 
 */
package loggerForms;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import PamController.PamController;
import PamUtils.PamCalendar;

/**
 * @author GrahamWeatherup
 *
 */
public class FormsDataUnitEditor{

	
	/**
	 * @param formDescription
	 * @param formsDataUnit
	 */
	public FormsDataUnitEditor(FormDescription formDescription,
			FormsDataUnit formsDataUnit) {
				
		int frameNo=formDescription.getFormsControl().getFrameNumber();
		
		JFrame frame=PamController.getInstance().getGuiFrameManager().getFrame(frameNo);

		FormsDataUnit updatedData = FormsDataUnitEditDialog.showDialog(frame, formDescription, formsDataUnit);		

		if (updatedData != null) {
//			String warningText = "You are about to edit the LoggerForm " + formDescription.getFormNiceName()+
//			" saved at "+ PamCalendar.formatDateTime2(formsDataUnit.getTimeMilliseconds())+ " UTC";
			//		PamDialog.showWar, warningTitle, warningText)(frame, "WARNING", warningText);
			int ans = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the stored data ?", 
					formDescription.getFormNiceName(), JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.OK_OPTION) {
				formDescription.getFormsDataBlock().updatePamData(updatedData, PamCalendar.getTimeInMillis());
			}
		}
		
	}

	
	
	
}
