package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.EpicSpinupFields")
public class EpicSpinupFields extends PageFields {
 
	private String finishedCrops;
	
	public EpicSpinupFields() {
		//NOTE: no-op
	}

	public String getFinishedCrops() {
		return finishedCrops;
	}
	
	public void setFinishedCrops(String finishedCrops) {
		this.finishedCrops = finishedCrops;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return EpicSpinupFields.class.getCanonicalName();
	}
	
}
