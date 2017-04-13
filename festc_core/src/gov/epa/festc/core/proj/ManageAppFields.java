package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.ManageAppFields")
public class ManageAppFields extends DomainFields {

 
	
	public ManageAppFields() {
		//NOTE: no-op
	}



	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return ManageAppFields.class.getCanonicalName();
	}
	
}
