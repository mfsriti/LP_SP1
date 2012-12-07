package mf.sriti.csp.sp.converter;

import mf.sriti.csp.sp.util.GeneralException;
import mf.sriti.csp.sp.util.PropertiesHandler;

public class ConversionPropertiesHandler extends PropertiesHandler {

	public ConversionPropertiesHandler() throws GeneralException {
		super(
				"src\\mf\\sriti\\csp\\sp\\converter\\conversion.properties",
				"UTF-8");
	}

	public String getSourceDir() {
		return getProperty(CSP_SP_SOURCE_DIR);
	}

	public String getTargetOldDir() {
		return getProperty(CSP_SP_TARGET_OLD_DIR);
	}

	public String getTargetNewDir() {
		return getProperty(CSP_SP_TARGET_NEW_DIR);
	}

	public static final String CSP_SP_SOURCE_DIR = "csp.sp.source.dir";
	public static final String CSP_SP_TARGET_OLD_DIR = "csp.sp.target_old.dir";
	public static final String CSP_SP_TARGET_NEW_DIR = "csp.sp.target_new.dir";
}
