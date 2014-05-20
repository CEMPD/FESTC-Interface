package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.DomainFields")
public class DomainFields extends PageFields {

	private String year;

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return DomainFields.class.getCanonicalName();
	}
	
}
