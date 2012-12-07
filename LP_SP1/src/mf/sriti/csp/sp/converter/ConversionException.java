package mf.sriti.csp.sp.converter;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import mf.sriti.csp.sp.util.GeneralException;

public class ConversionException extends GeneralException {

	public ConversionException() {
		// TODO Auto-generated constructor stub
	}

	public ConversionException(ResourceBundle rb, String key, Object[] objects) {
		super(rb, key, objects);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(ResourceBundle rb, String key, String arg) {
		super(rb, key, arg);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(ResourceBundle rb, String key) {
		super(rb, key);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(Logger logger, String key, Object[] objects) {
		super(logger, key, objects);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(Logger logger, String key, String arg) {
		super(logger, key, arg);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(Logger logger, String key) {
		super(logger, key);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(String msg, String... strings) {
		super(msg, strings);
		// TODO Auto-generated constructor stub
	}

	public ConversionException(Exception e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

}
