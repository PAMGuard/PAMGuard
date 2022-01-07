package PamguardMVC.uid.repair;

public interface UIDMessageInterface {

	public void newMessage(UIDRepairMessage uidRepairMessage);
	
	public void repairComplete(boolean repairOK);
	
}
