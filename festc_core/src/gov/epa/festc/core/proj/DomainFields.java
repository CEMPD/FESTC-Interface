package gov.epa.festc.core.proj;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.DomainFields")
public class DomainFields extends PageFields {

	protected String simYear;
	protected String nlcdYear;
	protected String fertYear;
	protected String minAcre;
	
	protected String sSimYear;
	protected String sFertYear;
	protected String sMinAcre;

	public String getSimYear() {
		return simYear;
	}
	
	public String getNlcdYear() {
		return nlcdYear;
	}
	
	public String defaultNlcdYear() {
		Integer intSimYear = Integer.parseInt(simYear);
		if (intSimYear == null ) nlcdYear = "2006";
		else {
			if (intSimYear >= 2011 ) nlcdYear = "2011";
			if (intSimYear < 2011 || intSimYear > 2005 ) nlcdYear = "2006";
			if (intSimYear < 2006 ) nlcdYear = "2001";
		}
		return nlcdYear;
	}

	public void setSimYear(String year) {
		this.simYear = year;
	}
	
	public void setNlcdYear(String year) {
		this.nlcdYear = year;
	}
	
	public String getSSimYear() {
		return this.sSimYear;
	}
	 
	public void setSSimYear(String year) {
		this.sSimYear = year;
	}
	
	/***
	 * Sets the fertilizer year associated with the app
	 */
	
	public String getSFertYear() {
		return this.sFertYear;
	}
	
	public void setSFertYear(String year) {
		this.sFertYear = year;
	}
	
	public String getFertYear() {
		return this.fertYear;
	}
	
	public void setFertYear(String year) {
		this.fertYear = year;
	}
	
	/***
	 * Sets the minimum crop acres associated with the app
	 */
	
	public String getSMinAcre() {
		return this.sMinAcre;
	}
	
	public void setSMinAcre(String minAcre) {
		this.sMinAcre = minAcre;
	}

	public String getMinAcre() {
		return this.minAcre;
	}
	
	public void setMinAcre(String minAcre) {
		this.minAcre = minAcre;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return DomainFields.class.getCanonicalName();
	}
	
}
