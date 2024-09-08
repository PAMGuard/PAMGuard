package rawDeepLearningClassifier.logging;

import PamView.symbol.PamSymbolChooser;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import annotation.dataselect.AnnotationDataSelCreator;
import generalDatabase.SQLLoggingAddon;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dataPlotFX.DLSymbolModifier;
import rawDeepLearningClassifier.dataSelector.DLDataSelectCreator;

/**
 * Annotation type for data from the matched click classifier. 
 * @author Jamie Macaulay 
 *
 */
public class DLAnnotationType extends DataAnnotationType<DLAnnotation>  {
		
		public static final String NAME = "Deep_learning_result";
		
		private DLAnnotationSQL dlAnnotationSQL;
		
		private DLAnnotationBinary dlAnnotationBinary;

		private DLControl dlControl;

		private DLAnnotationSymbolChooser dlSymbolChooser;

		private DLDataSelectCreator dlDataSelectorCreator;

		public DLAnnotationType(DLControl mtControl) {
			this.dlControl=mtControl;
			dlAnnotationSQL = new DLAnnotationSQL(this);
			dlAnnotationBinary = new DLAnnotationBinary(this);
			dlSymbolChooser = new DLAnnotationSymbolChooser(); 
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
			return DLAnnotation.class;
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
			return dlAnnotationSQL;
		}

		/* (non-Javadoc)
		 * @see annotation.DataAnnotationType#getBinaryHandler()
		 */
		@Override
		public AnnotationBinaryHandler<DLAnnotation> getBinaryHandler() {
			//System.out.println("Get annotation binary handler: "); 
			return dlAnnotationBinary;
		}

//		/* (non-Javadoc)
//		 * @see annotation.DataAnnotationType#getShortIdCode()
//		 */
//		@Override
//		public String getShortIdCode() {
//			return "DLRE";
//		}
		
		@Override
		public String getShortIdCode() {
			//DLRE or Delt 
			return "Delt"; 
		}


		@Override
		public DLSymbolModifier getSymbolModifier(PamSymbolChooser symbolChooser) {
			return new DLSymbolModifier(symbolChooser, this);
		}
		
		public DLControl getDlControl() {
			return dlControl;
		}
		
		
		@Override
		public AnnotationDataSelCreator getDataSelectCreator(String selectorName, boolean allowScores) {
			if (dlDataSelectorCreator == null) {
				dlDataSelectorCreator = new DLDataSelectCreator(dlControl, this);
			}
			return dlDataSelectorCreator;
		}

		


}