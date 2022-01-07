package Map;

import PamController.status.BaseProcessCheck;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;



/**
 * The map needs a process since it now produces data units to go into a data block.
 * @author Doug
 *
 */
public class MapProcess extends PamguardMVC.PamProcess {
	
	private MapController mapController;
	
	private MapCommentDataBlock mapCommentDataBlock;

	public MapProcess(MapController mapController) {
		super(mapController, null);
		this.mapController = mapController;
		mapCommentDataBlock = new MapCommentDataBlock("Map Comments", this);
		addOutputDataBlock(mapCommentDataBlock);
		mapCommentDataBlock.setOverlayDraw(new MapCommentOverlayGraphics(mapCommentDataBlock));
		mapCommentDataBlock.SetLogging(new MapCommentSQLLogging(mapCommentDataBlock));
		mapCommentDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);
		StandardSymbolManager sm;
		mapCommentDataBlock.setPamSymbolManager(sm = new StandardSymbolManager(mapCommentDataBlock, MapCommentOverlayGraphics.defaultSymbol, false));
		
		BaseProcessCheck pp;
		setProcessCheck(pp = new BaseProcessCheck(this, null, 0.05, 0));
		pp.setInputAveraging(10);
		pp.setNeverIdle(true);
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	public PamDataBlock<MapComment> getMapCommentDataBlock() {
		return mapCommentDataBlock;
	}
	
	
	
}
