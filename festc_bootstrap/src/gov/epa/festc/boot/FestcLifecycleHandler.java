package gov.epa.festc.boot;

import org.java.plugin.PluginClassLoader;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLifecycleHandler;

import simphony.util.messages.MessageCenter;

/**
 * LifecycleHandler that creates PavePluginClassLoaders.
 * 
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class FestcLifecycleHandler extends StandardPluginLifecycleHandler {

	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(FestcLifecycleHandler.class);

	protected PluginClassLoader createPluginClassLoader(PluginDescriptor descriptor) {
		msgCenter.debug("Creating class loader");
		return new FestcClassLoader(getPluginManager(), descriptor, getClass().getClassLoader());
	}
}
