package gov.epa.festc.core;

import gov.epa.festc.core.proj.PageFields;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * FESTC scenario's data collection.
 *
 * @author IE, UNC
 * @version $Revision$ $Date$
 */

//This statement means that class "Project.java" is the root-element of our java object to xml mapping
@XmlRootElement(namespace = "gov.epa.festc.core")
public class Project {
	List<PageFields> pageList;
	 
	String name;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Project() {
		pageList = new ArrayList<PageFields>();
	}
	
	public void addPage(PageFields page) {
		pageList.add(page);
		//System.out.print("Add field:" +page.getName() + "\n");
		
	}
	
	public PageFields getPage(String name) {
		if (name == null || name.trim().length() == 0)
			return null;
		
		for (PageFields page : pageList) {
			if (page.getName() != null && page.getName().equalsIgnoreCase(name)){
				//System.out.print("field:" +page.getName() + "\n");
				return page;
			}
		}
		
		return null;
	}
	
	@XmlElementRef
	public List<PageFields> getPageList() {
		return pageList;
	}

	public void setPageList(List<PageFields> pageList) {
		this.pageList = pageList;
	}

}
