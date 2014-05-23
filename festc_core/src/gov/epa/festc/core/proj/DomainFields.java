package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.DomainFields")
public class DomainFields extends PageFields {

	private String simYear;
	private String nlcdYear;

	public String getSimYear() {
		return simYear;
	}
	
	public String getNlcdYear() {
		return nlcdYear;
	}

	public void setSimYear(String year) {
		this.simYear = year;
	}
	
	public void setNlcdYear(String year) {
		this.nlcdYear = year;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return DomainFields.class.getCanonicalName();
	}
	
}
