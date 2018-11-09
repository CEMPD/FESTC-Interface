package gov.epa.festc.core;

/**
 * Constants used by the FEST-C application
 * 
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public interface FestcConstants {
	String MAIN_GROUP_ID = "gov.epa.festc.core.main_group";
	String DATASET_VIEW = "gov.epa.festc.core.dataset_view";
	String PERSPECTIVE_ID = "gov.epa.festc.core.perspective_one";
	String TOOLS_GROUP = "gov.epa.festc.core.tools_group";
	String FORMULA_VIEW = "gov.epa.festc.core.formula_view";
	String BELD4_VIEW = "gov.epa.festc.core.landuse_view";
	String MCIP2EPIC_VIEW = "gov.epa.festc.core.mcip2epic_view";
	String SITE_INFO_VIEW = "gov.epa.festc.core.siteinfo_gen_view";
	String SITE_FILE_VIEW = "gov.epa.festc.core.siteinfo_file_view";
	String GEN_SOIL_MAN_FILES_VIEW = "gov.epa.festc.core.gen_soil_man_files_view";
	String MAN_FILE_SPINUP_VIEW = "gov.epa.festc.core.man_file_spinup_view";
	String MAN_FILE_APP_VIEW = "gov.epa.festc.core.man_file_app_view";
	String EPIC4APP_VIEW = "gov.epa.festc.core.epic4app_view";
	String TOOLS_VIEW = "gov.epa.festc.core.tools_view";
	String MANAGE_VIEW = "gov.epa.festc.core.manage_view";
	String FORMULA_BAR_GROUP = "gov.epa.festc.core.bar.formula_group";
	String FORMULA_LABEL = "gov.epa.festc.core.bar.formula_label";
	String APP_LOG_FILE = System.getProperty("user.home") + "/festc/velocity.log";
	String OUTPUT_LOG_FILR = System.getProperty("user.home") + "/festc/festc.log";
	String EPIC_VIEW = "gov.epa.festc.core.epic_view";
	String PROCESS_EPIC_SPINUP_VIEW = "gov.epa.festc.core.proc_spinup_view";
	String EPIC2CMAQ_VIEW = "gov.epa.festc.core.epic2cmaq_view";
	String EPIC2SWAT_VIEW = "gov.epa.festc.core.epic2swat_view";
	String EPIC_YEARLY_AVERAGE2CMAQ_VIEW = "gov.epa.festc.core.epic_yearly_average2cmaq_view";
	String VISUALIZE_VIEW = "gov.epa.festc.core.visualize_view";
}
