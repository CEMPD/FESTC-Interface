package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.SiteFilesFields")
public class SiteFilesFields extends PageFields {	

	private String minAcres;
	private String co2Fac;
	
	public SiteFilesFields() {
		//NOTE: no-op
	}
	 
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return SiteFilesFields.class.getCanonicalName();
	}
	
	public void setMinAcres(String acres) {
		this.minAcres = acres;
	}
	public String getMinAcres() {
		return minAcres;
	}

	public void setCO2Fac(String co2fac) {
		this.co2Fac = co2fac;
	}
	public String getCO2Fac() {
		return co2Fac;
	}
}
