package generalDatabase.dataExport;

import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

import java.awt.event.MouseEvent;

/**
 * Filter class for use with multiple data types in filtering 
 * database table data. All formatting of data types happens within
 * the concrete classes derived from ValueFilterParams. 
 * 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class ValueFilter<T extends ValueFilterParams> extends DataFilter {

	T valueFilterParams;
	
	private PamTableItem tableItem;
	
	public ValueFilter(DataFilterChangeListener dataFilterChangeListener, 
			T initialParams, PamTableItem tableItem) {
		super(dataFilterChangeListener);
		valueFilterParams = initialParams;
		this.tableItem = tableItem;
	}

	@Override
	public boolean filterSelectAction(MouseEvent e) {
		T newParams = (T) ValueFilterDialog.showDialog(null, this, e.getLocationOnScreen());
		if (newParams != null) {
			valueFilterParams = (T) newParams.clone();
			filterChanged();
			return true;
		}
		return false;
	}

	@Override
	public String getColumnName() {
		return tableItem.getName();
	}

	@Override
	public String getFilterClause(SQLTypes sqlTypes) {
		if (valueFilterParams.isUseMin() == false && valueFilterParams.isUseMax() == false) {
			return null;
		}
		String clause = null;
		if (valueFilterParams.isUseMin() && valueFilterParams.isUseMax()) {
			clause = String.format("%s BETWEEN %s AND %s", tableItem.getName(), 
					valueFilterParams.getMinQueryValue(sqlTypes), valueFilterParams.getMaxQueryValue(sqlTypes));
		}
		else if (valueFilterParams.isUseMin()) {
			clause = String.format("%s >= %s", tableItem.getName(), 
					valueFilterParams.getMinQueryValue(sqlTypes));
		}
		else if (valueFilterParams.isUseMax()) {
			clause = String.format("%s <= %s", tableItem.getName(), 
					valueFilterParams.getMaxQueryValue(sqlTypes));
		}
		return clause;
	}

}
