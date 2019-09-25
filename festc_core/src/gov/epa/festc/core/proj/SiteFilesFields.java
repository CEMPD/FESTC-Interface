package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.SiteFilesFields")
public class SiteFilesFields extends DomainFields {	

	private String minAcres;
	
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
}
