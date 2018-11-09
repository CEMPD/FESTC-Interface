package gov.epa.festc.core;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;

import saf.core.runtime.IApplicationRunnable;
import saf.core.ui.GUICreator;
import saf.core.ui.IAppConfigurator;
import saf.core.ui.ISAFDisplay;
import saf.core.ui.Workspace;
import simphony.util.messages.MessageCenter;

public class FestcPlugin extends Plugin implements IApplicationRunnable {

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(String[] arg0) {
		try {
			// initialize velocity
			Properties p = new Properties();
			p.setProperty("resource.loader", "class");
			p.setProperty("class.resource.loader.class",
							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

			p.setProperty("runtime.log", FestcConstants.APP_LOG_FILE);
			Velocity.init(p);

			// The typical pattern for a SAF application is followed below.
			FestcApplication festc = new FestcApplication();
			IAppConfigurator configurator = new FestcConfigurator(festc);
			Workspace<FestcApplication> workspace = new Workspace<FestcApplication>(festc);
			ISAFDisplay display = GUICreator.createDisplay(configurator, workspace);

			GUICreator.runDisplay(configurator, display);
		} catch (Exception ex) {
			MessageCenter.getMessageCenter(getClass()).error("Error", ex);
		}
	}

}
