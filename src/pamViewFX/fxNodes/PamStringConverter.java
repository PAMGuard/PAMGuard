package pamViewFX.fxNodes;


import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

public class PamStringConverter extends  StringConverter<Double> {
	
     private DecimalFormat df = new DecimalFormat("#.##");
     
     public void setSignificantDigits(int n) {
    	 df.setMaximumFractionDigits(n);
     }

     public DecimalFormat getDf() {
		return df;
	}

	public void setDf(DecimalFormat df) {
		this.df = df;
	}

	@Override public String toString(Double value) {
         // If the specified value is null, return a zero-length String
         if (value == null) {
             return "";
         }

         return df.format(value);
     }

     @Override public Double fromString(String value) {
         try {
             // If the specified value is null or zero-length, return null
             if (value == null) {
                 return null;
             }

             value = value.trim();

             if (value.length() < 1) {
                 return null;
             }

             // Perform the requested parsing
             return df.parse(value).doubleValue();
         } catch (ParseException ex) {
             throw new RuntimeException(ex);
         }
     }
}
