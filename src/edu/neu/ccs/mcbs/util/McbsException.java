package edu.neu.ccs.mcbs.util;

public class McbsException extends Exception {

	private static final long serialVersionUID = 1997753363232807009L;

	public McbsException() {
	}

	public McbsException(String message) {
		super(message);
	}

	public McbsException(Throwable cause) {
		super(cause);
	}

	public McbsException(String message, Throwable cause) {
		super(message, cause);
	}

	public McbsException(String message, Throwable cause,
	        boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
