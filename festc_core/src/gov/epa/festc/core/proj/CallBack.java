package gov.epa.festc.core.proj;

import javax.swing.JPanel;

public interface CallBack {

	public void onCall(String cmd, JPanel contentPanel) throws Exception;
}
