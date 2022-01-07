package difar.display;

import java.awt.Component;

import difar.DIFARMessage;

public interface DIFARDisplayUnit {
	
	public String getName();
	
	public Component getComponent();
	
	public int difarNotification(DIFARMessage difarMessage);

}
