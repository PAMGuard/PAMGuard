package videoRangePanel;

import PamView.ManagedSymbol;
import PamView.ManagedSymbolInfo;
import PamView.PamSymbol;

public class VRSymbolManager implements ManagedSymbol {
		
		private PamSymbol symbol;
		
		private ManagedSymbolInfo symbolInfo;
		
		public VRSymbolManager(PamSymbol defSymbol, String description) {
			symbolInfo = new ManagedSymbolInfo(description);
//			PamOldSymbolManager.getInstance().addManagesSymbol(this);
			if (getPamSymbol() == null) {
				setPamSymbol(defSymbol);
			}
		}

		@Override
		public PamSymbol getPamSymbol() {
			return symbol;
		}

		@Override
		public ManagedSymbolInfo getSymbolInfo() {
			return symbolInfo;
		}

		@Override
		public void setPamSymbol(PamSymbol pamSymbol) {
			this.symbol = pamSymbol;
		}
		
}


