package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.ManageSpinupFields")
public class ManageSpinupFields extends PageFields {

	private String fertYear;
	
	public ManageSpinupFields() {
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
		return ManageSpinupFields.class.getCanonicalName();
	}
	
}
