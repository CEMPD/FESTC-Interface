package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.EpicYearlyAverage2CMAQFields")
public class EpicYearlyAverage2CMAQFields  extends DomainFields{
	
	private String beld4ncf;
	 
	public EpicYearlyAverage2CMAQFields() {
		//NOTE: no-op
	}
	
	public void setBeld4ncf(String beld4ncf) {
		this.beld4ncf = beld4ncf;
	}
	public String getBeld4ncf() {
		return beld4ncf;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return EpicYearlyAverage2CMAQFields.class.getCanonicalName();
	}
}
