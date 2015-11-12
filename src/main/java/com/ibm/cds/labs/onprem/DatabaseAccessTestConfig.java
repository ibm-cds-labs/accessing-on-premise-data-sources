package com.ibm.cds.labs.onprem;

public class DatabaseAccessTestConfig {

	private String urlscheme = null;
	private String drivername = null;
	private String testquery = null; 
	
	protected DatabaseAccessTestConfig(String scheme, String driver, String query) {
		urlscheme = scheme;
		drivername = driver;
		testquery = query;
	}
	
	protected String getURLDisplayName() {
		return urlscheme;
	}
	
	protected String getJDBCDriverClassName() {
		return drivername;
	}

	protected String getTestQuery() {
		return testquery;
	}

	
} // DatabaseAccessTestConfig
