package loggerForms.actions;

import PamModel.CommonPluginInterface;

/**
 * Make available as a plugin, so that actions can be added without having to create a complete module
 * this would be suitable for simple actions only. 
 */
public interface PluginLoggerAction extends CommonPluginInterface {

	public LoggerAction getLoggerAction();
	
}
