package loggerForms.formdesign;

import java.util.ArrayList;

import loggerForms.ItemDescription;
import loggerForms.ItemInformation;

public class FormList<o extends ItemDescription> extends ArrayList<o> {

	private static final long serialVersionUID = 0x1;

//	@Override
//	public FormList clone() {
//		FormList<o> newList =  (FormList<o>) super.clone();
//		newList.clear();
//		for (int i = 0; i < size(); i++) {
//			newList.add((o) this.get(i).clone());
//		}
//		return newList;
//	}

}
