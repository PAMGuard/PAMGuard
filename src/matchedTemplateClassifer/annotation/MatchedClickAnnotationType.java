package matchedTemplateClassifer.annotation;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import generalDatabase.SQLLoggingAddon;
import matchedTemplateClassifer.MTClassifierControl;

/**
 * Annotation type for data from the matched click classifier. 
 * @author Jamie Macaulay 
 *
 */
public class MatchedClickAnnotationType extends DataAnnotationType<MatchedClickAnnotation>  {
		
		public static final String NAME = "Matched_Clk_Clsfr";
		
		private MatchedClickAnnotationSQL clickAnnotationSQL;
		
		private MatchedClickAnnotationBinary clickAnnotationBinary;

		private MTClassifierControl mtControl;

		public MatchedClickAnnotationType(MTClassifierControl mtControl) {
			this.mtControl=mtControl;
			clickAnnotationSQL = new MatchedClickAnnotationSQL(this);
			clickAnnotationBinary = new MatchedClickAnnotationBinary(this);
			//add to annotations. 
			CentralAnnotationsList.addAnnotationType(this);
		}

		@Override
		public String getAnnotationName() {
			return NAME;
			//return mtControl.getUnitName(); 
		}

		@Override
		public Class getAnnotationClass() {
			return MatchedClickAnnotationType.class;
		}

		@Override
		public boolean canAnnotate(Class dataUnitType) {
			return true;
		}

		/* (non-Javadoc)
		 * @see annotation.DataAnnotationType#getSQLLoggingAddon()
		 */
		@Override
		public SQLLoggingAddon getSQLLoggingAddon() {
			return clickAnnotationSQL;
		}

		/* (non-Javadoc)
		 * @see annotation.DataAnnotationType#getBinaryHandler()
		 */
		@Override
		public AnnotationBinaryHandler<MatchedClickAnnotation> getBinaryHandler() {
			return clickAnnotationBinary;
		}

//		/* (non-Javadoc)
//		 * @see annotation.DataAnnotationType#getShortIdCode()
//		 */
		@Override
		public String getShortIdCode() {
			return NAME;
		}

		/**
		 * Get the MTControl that the annotation is associated with. 
		 * @return the MT control. 
		 */
		public MTClassifierControl getMTControl() {
			return mtControl;
		}

		@Override
		public SymbolModifier getSymbolModifier(PamSymbolChooser symbolChooser) {
			return new MatchedClickSymbolModifier(mtControl, symbolChooser);
		}

}
