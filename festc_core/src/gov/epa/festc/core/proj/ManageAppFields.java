package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.ManageAppFields")
public class ManageAppFields extends DomainFields {

	private String fertYear;
	
	public ManageAppFields() {
		//NOTE: no-op
	}

	public String getFertYear() {
		return fertYear;
	}
	
	public void setFertYear(String fertYear) {
		this.fertYear = fertYear;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return ManageAppFields.class.getCanonicalName();
	}
	
}
