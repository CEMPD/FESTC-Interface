package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.EpicAppFields")
public class EpicAppFields extends PageFields {

	private String finishedCrops;
	private String simYear;
	
	public EpicAppFields() {
		//NOTE: no-op
	}

	public String getFinishedCrops() {
		return finishedCrops;
	}
	
	public void setFinishedCrops(String finishedCrops) {
		this.finishedCrops = finishedCrops;
	}
	
	public String getSimYear() {
		return simYear;
	}
	
	public void setSimYear(String year) {
		this.simYear = year;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return EpicAppFields.class.getCanonicalName();
	}
	
}
