package annotationMark;

import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamProcess;
import annotationMark.fx.MarkPlotProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.data.generic.GenericDataPlotProvider;


public class MarkProcess extends PamProcess {

	private MarkModule annotationModule;
	
	private MarkDataBlock markDataBlock;

	private MarkSQLLogging asql;

	private GenericDataPlotProvider genericDataPloProvider;

	public MarkProcess(MarkModule annotationModule) {
		super(annotationModule, null);
		this.annotationModule = annotationModule;
		markDataBlock = new MarkDataBlock(MarkDataUnit.class, "Mark", this, 0);
		asql = new MarkSQLLogging(markDataBlock, annotationModule.getUnitName());
		markDataBlock.SetLogging(asql);
		markDataBlock.setOverlayDraw(new MarkOverlayDraw(markDataBlock, annotationModule));
		markDataBlock.setPamSymbolManager(new StandardSymbolManager(markDataBlock, MarkOverlayDraw.defaultSymbolData, false));
		addOutputDataBlock(markDataBlock);
		
		TDDataProviderRegisterFX.getInstance().registerDataInfo(genericDataPloProvider = new MarkPlotProviderFX(markDataBlock));
//		markDataBlock.setOverlayDraw(new S);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#destroyProcess()
	 */
	@Override
	public void destroyProcess() {
		TDDataProviderRegisterFX.getInstance().unRegisterDataInfo(genericDataPloProvider);
		super.destroyProcess();
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	/**
	 * @return the markDataBlock
	 */
	public MarkDataBlock getMarkDataBlock() {
		return markDataBlock;
	}

	/**
	 * @return the asql
	 */
	public MarkSQLLogging getAsql() {
		return asql;
	}

}
