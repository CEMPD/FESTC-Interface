package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.VisualizationFields")
public class VisualizationFields  extends PageFields{

	private String mcipDataDir;
	private String beldDataDir;
	private String epicDataDir;
	private boolean mcipDataSelected;
	private boolean beldDataSelected;
	private boolean epicDataSelected;
	
	public VisualizationFields() {
		//NOTE: no-op
	}

	public String getMcipDataDir() {
		return mcipDataDir;
	}

	public void setMcipDataDir(String mcipDataDir) {
		this.mcipDataDir = mcipDataDir;
	}

	public String getBeldDataDir() {
		return beldDataDir;
	}

	public void setBeldDataDir(String beldDataDir) {
		this.beldDataDir = beldDataDir;
	}

	public String getEpicDataDir() {
		return epicDataDir;
	}

	public void setEpicDataDir(String epicDataDir) {
		this.epicDataDir = epicDataDir;
	}

	public boolean isMcipDataSelected() {
		return mcipDataSelected;
	}

	public void setMcipDataSelected(boolean mcipDataSelected) {
		this.mcipDataSelected = mcipDataSelected;
	}

	public boolean isBeldDataSelected() {
		return beldDataSelected;
	}

	public void setBeldDataSelected(boolean beldDataSelected) {
		this.beldDataSelected = beldDataSelected;
	}

	public boolean isEpicDataSelected() {
		return epicDataSelected;
	}

	public void setEpicDataSelected(boolean epicDataSelected) {
		this.epicDataSelected = epicDataSelected;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return VisualizationFields.class.getCanonicalName();
	}

}
