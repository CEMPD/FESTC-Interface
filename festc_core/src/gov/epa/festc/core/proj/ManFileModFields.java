package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.ManFileModFields")
public class ManFileModFields  extends PageFields{
	
	public ManFileModFields() {
		//NOTE: no-op
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return ManFileModFields.class.getCanonicalName();
	}
	
}
