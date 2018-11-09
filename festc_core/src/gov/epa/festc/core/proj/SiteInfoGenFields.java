package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.SiteInfoGenFields")
public class SiteInfoGenFields  extends PageFields{

	private String beld4ncf;
	private String minAcres;
	
	public SiteInfoGenFields() {
		//NOTE: no-op
	}
	
	public void setBeld4ncf(String beld4ncf) {
		this.beld4ncf = beld4ncf;
	}
	public String getBeld4ncf() {
		return beld4ncf;
	}
	
	public void setMinAcres(String acres) {
		this.minAcres = acres;
	}
	public String getMinAcres() {
		return minAcres;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return SiteInfoGenFields.class.getCanonicalName();
	}
}
