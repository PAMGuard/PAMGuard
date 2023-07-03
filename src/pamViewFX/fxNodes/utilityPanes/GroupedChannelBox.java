package pamViewFX.fxNodes.utilityPanes;

import org.controlsfx.control.CheckComboBox;

import PamUtils.PamArrayUtils;
import PamView.GroupedSourceParameters;

/**
 * A ComboBox which shows a selectable list of channel groups. 
 * 
 * @author Jamie Macaulay
 *
 */
public class GroupedChannelBox extends CheckComboBox<String> {

	/**
	 * Set the channel grouping for the check box. 
	 * @param params - the parameters. 
	 */
	public void setSource(GroupedSourceParameters params) {

		this.getItems().clear();

		for (int i=0; i<params.countChannelGroups(); i++) {

			int group = params.getGroupChannels(i);

			int[] chanArray = PamUtils.PamUtils.getChannelArray(group); 
			
			System.out.println("CHANNEL ARRAY:"); 
			PamArrayUtils.printArray(chanArray);

			String channels = ""; 
			for (int j =0 ; j<chanArray.length; j++) {
				channels +=  chanArray[j];
				if (j<chanArray.length-1) {
					channels+=", ";
				}
			}
			this.getItems().add(channels); 
		}
	}

}
