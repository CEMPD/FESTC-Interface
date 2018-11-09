package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "gov.epa.festc.core.proj.Beld4DataGenFields")

public class Beld4DataGenFields  extends DomainFields{
	//private String nlcdYear;
	private String nlcdFile;
	private boolean nlcdDataSelected;
	private boolean modisDataSelected;
	
	public Beld4DataGenFields() {
		//NOTE: no-op
	}
	
	public void setNLCDfile(String nlcdFile) {
		this.nlcdFile = nlcdFile;
	}
	public String getNLCDfile() {
		return nlcdFile;
	}
	
	public boolean isNlcdDataSelected() {
		return nlcdDataSelected;
	}

	public void setNlcdDataSelected(boolean nlcdDataSelected) {
		this.nlcdDataSelected = nlcdDataSelected;
	}	
	
	public boolean isModisDataSelected() {
		return modisDataSelected;
	}

	public void setModisDataSelected(boolean modisDataSelected) {
		this.modisDataSelected = modisDataSelected;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return Beld4DataGenFields.class.getCanonicalName();
	}
}
