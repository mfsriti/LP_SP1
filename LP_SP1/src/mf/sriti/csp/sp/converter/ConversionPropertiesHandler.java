package mf.sriti.csp.sp.converter;

import mf.sriti.csp.sp.util.GeneralException;
import mf.sriti.csp.sp.util.PropertiesHandler;

public class ConversionPropertiesHandler extends PropertiesHandler {

	public ConversionPropertiesHandler() throws GeneralException {
		super(
				"src\\mf\\sriti\\csp\\sp\\converter\\conversion.properties",
				"UTF-8");
	}

	public String getTemplateDir() {
		return getProperty(CSP_SP_TEMPLATE_DIR);
	}

	public String getSATPREFDir() {
		return getProperty(CSP_SP_TARGET_SATPREF_DIR);
	}

	public String getLPSolveDir() {
		return getProperty(CSP_SP_TARGET_LPSOLVE_DIR);
	}

	public static final String CSP_SP_TEMPLATE_DIR = "csp.sp.template.dir";
	public static final String CSP_SP_TARGET_SATPREF_DIR = "csp.sp.target.satpref.dir";
	public static final String CSP_SP_TARGET_LPSOLVE_DIR = "csp.sp.target.lpsolve.dir";
}
