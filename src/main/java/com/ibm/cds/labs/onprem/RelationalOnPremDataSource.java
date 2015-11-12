/*-------------------------------------------------------------------------------
 Copyright IBM Corp. 2015
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-------------------------------------------------------------------------------*/
package com.ibm.cds.labs.onprem;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Implements the connectivity test for relational data sources.
 * @author ptitzler
 *
 */
public class RelationalOnPremDataSource extends OnPremDataSource {

	// list of supported data source types
	private static final Map<String,DatabaseAccessTestConfig> SUPPORTED_DATA_SOURCES;
	
	static {
		/*
		 *  Initialization: load the data source configuration information from /META-INF/rdbms_config.json.
		 *  The configuration file is expected to contain the following JSON
		   				   		"connectors": [
   		  							{
   		  								"url_scheme": "JDBC_URL_SCHEME",
  	 	  								"display_name": "DATA_SOURCE_TYPE_DISPLAY_NAME",
   		  								"driver" : "JDBC_CLASS_NAME",
   		   								"query" : "CURRENT_DATE_QUERY"   		
   		  							}, ...
 		  						]			 
          
		 * Example configuration for DB2 and MySQL:
				   "connectors": [
   									{
   										"url_scheme": "db2",
  	 									"display_name": "DB2",
   										"driver" : "com.ibm.db2.jcc.DB2Driver",
   										"query" : "SELECT current date FROM sysibm.sysdummy1"   		
   									},
   								    {
  			 							"url_scheme": "mysql",
   			 							"display_name": "MySQL",
   			 							"driver" : "com.mysql.jdbc.Driver",
   			 							"query" : "SELECT current_date"
  									} 
 								]			 
		 *
		 */
		SUPPORTED_DATA_SOURCES = new TreeMap<String,DatabaseAccessTestConfig>(String.CASE_INSENSITIVE_ORDER);
			
		InputStream is = null;
		
		try {
			is = RelationalOnPremDataSource.class.getResourceAsStream("/META-INF/rdbms_config.json");
			if(is != null) {
				JSONObject config = null;
				config = (JSONObject) JSON.parse(is);
				// log to console for debug purposes
				
				System.out.println(config.toString());
				
				JSONArray connectorlist = (JSONArray) config.get("connectors");
				JSONObject cc = null;
				for(int i = 0; i < connectorlist.size(); i++ ) {
				   cc = (JSONObject) connectorlist.get(i);
				   SUPPORTED_DATA_SOURCES.put((String)cc.get("url_scheme"), 
						                      new DatabaseAccessTestConfig((String)cc.get("display_name"),(String)cc.get("driver"),(String)cc.get("query")));
				}
				
			}
			else {
				System.err.println("Resource /META-INF/config.json was not found");
			}
			
		} 
		catch(Exception ex) {
			// log error to console 
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
		}
		finally {
			if(is != null) {
				try {
					is.close();
				}
				catch(Exception ex) {
					// ignore
				}
			}
		}
		
	}
	
	private DatabaseAccessTestConfig accessTestConfig = null; 
	private String jdbcURL = null;
	private String opUser = null;
	private String opPassword = null;

	private Connection connection = null;

	/**
	 * Returns a list of data source types, for which a configuration was specified.
	 * @return an array of strings; guaranteed to be not null
	 */
	public static ArrayList<String> getSupportedDataSources() {
		
		ArrayList<String> supportedDataSourceTypes = new ArrayList<String>();
		
		Iterator<DatabaseAccessTestConfig> it = SUPPORTED_DATA_SOURCES.values().iterator();
		while(it.hasNext()) {
			supportedDataSourceTypes.add(it.next().getURLDisplayName());
		}
		return supportedDataSourceTypes;
	} // getSupportedDataSources
		
	/**
	 * 
	 * @param scheme
	 * @param URL
	 * @param user
	 * @param password
	 * @throws OnPremDataSourceNotSupportedException
	 */
	public RelationalOnPremDataSource(String scheme, String URL, String user, String password) 
	 throws OnPremDataSourceNotSupportedException {
				
		accessTestConfig = SUPPORTED_DATA_SOURCES.get(scheme);
	
		if(accessTestConfig == null) {
			throw new OnPremDataSourceNotSupportedException(scheme);
		}
		
		jdbcURL = URL;
		opUser = user;
		opPassword = password;
	} // constructor

	/**
	 * Connect to the relational data source
	 * @throws OnPremResourceAccessTestException if a problem was encountered
	 */
	public void connect() 
	 throws OnPremDataSourceAccessTestException {

		try {
			// load the JDBC driver
			Class.forName(accessTestConfig.getJDBCDriverClassName());			
			// try to establish a connection
			connection = DriverManager.getConnection(jdbcURL,opUser,opPassword);
		}
		catch(Exception ex) {
			throw new OnPremDataSourceAccessTestException("Failed to connect to on-premises "+ accessTestConfig.getURLDisplayName() + " database.",ex);
		}

	} // connect

    /**
     * Disconnect from the relational data source. Note that no error is raised if a problem was encountered.
     */
	public void disconnect() {

		try {

			if(connection!= null) {
				// free resources
				connection.close();
			}

		}
		catch(Exception ex) {
			// do not propagate error; it is not relevant
			System.err.println(ex.getMessage());
			ex.printStackTrace();						
		}

		connection = null;

	} // disconnect

	/**
	 * 
	 * @return the first column of the dummy query that was executed, expressed as a String
	 * @throws OnPremResourceAccessTestException
	 */
	public String runQuery() 
		throws OnPremDataSourceAccessTestException {

		if(connection == null)	{
			throw new OnPremDataSourceAccessTestException("Cannot execute test query because no database connection exists.");
		}

		// return data structure
		String result = null;
		ResultSet resultset = null;

		try {

			// create and run a dummy statement
			Statement statement = connection.createStatement();
			statement.execute(accessTestConfig.getTestQuery());
		
			// fetch the result set
			resultset = statement.getResultSet();
			if(resultset.next()) {
				result = resultset.getString(1);
			}
		}
		catch(SQLException sqlex) {
			throw new OnPremDataSourceAccessTestException("Failed to execute test query on on-premises " + accessTestConfig.getURLDisplayName() + " database.",sqlex);
		}
		finally {
			// clean up
			try {
				resultset.close();
			}
			catch(SQLException sqlex) {
				// ignore
			}
			resultset = null;
		}

		// return query result	
		return result;

	} // runQuery

} // RelationalOnPremResource