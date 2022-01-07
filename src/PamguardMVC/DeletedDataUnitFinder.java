package PamguardMVC;

public class DeletedDataUnitFinder extends DataUnitFinder {

	public DeletedDataUnitFinder(PamDataBlock pamDataBlock,
			DataUnitMatcher dataUnitMatcher) {
		super(pamDataBlock, dataUnitMatcher);
	}

	public DeletedDataUnitFinder(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
	}

	@Override
	protected void setupList() {
		setListIterator(getPamDataBlock().getRemovedItems().listIterator());
	}
	

}
