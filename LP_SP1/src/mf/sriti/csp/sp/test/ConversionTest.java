package mf.sriti.csp.sp.test;

import mf.sriti.csp.sp.converter.ConversionManager;
import mf.sriti.csp.sp.util.GeneralException;

public class ConversionTest {

	public static void main(String[] args) throws GeneralException{
		ConversionManager conversionManager = ConversionManager.getInstance();
		conversionManager.process();
	}

}
