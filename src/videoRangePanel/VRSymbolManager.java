package videoRangePanel;

import PamView.ManagedSymbol;
import PamView.ManagedSymbolInfo;
import PamView.PamSymbol;
import PamView.PamOldSymbolManager;

public class VRSymbolManager implements ManagedSymbol {
		
		PamSymbol symbol;
		
		ManagedSymbolInfo symbolInfo;
		
		public VRSymbolManager(PamSymbol defSymbol, String description) {
			symbolInfo = new ManagedSymbolInfo(description);
//			PamOldSymbolManager.getInstance().addManagesSymbol(this);
			if (getPamSymbol() == null) {
				setPamSymbol(defSymbol);
			}
		}

		public PamSymbol getPamSymbol() {
			return symbol;
		}

		public ManagedSymbolInfo getSymbolInfo() {
			return symbolInfo;
		}

		public void setPamSymbol(PamSymbol pamSymbol) {
			this.symbol = pamSymbol;
		}
		
}


