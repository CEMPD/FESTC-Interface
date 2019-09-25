package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.ManageSpinupFields")
public class ManageSpinupFields extends DomainFields {

	private String fertYear; // used for check consistent with app 
	
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
