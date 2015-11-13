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
import java.io.IOException;

import com.ibm.cds.labs.onprem.util.JSONUtil;

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * 
 * @author ptitzler
 *
 */
public class OnPremDataSourceAccessTestConfiguration {

	private static final String CONFIG_FILE = "/META-INF/rdbms_config.json";

	// list of supported data source types
	private static Map<String,DatabaseAccessTestConfig> SUPPORTED_DATA_SOURCES = new TreeMap<String,DatabaseAccessTestConfig>(String.CASE_INSENSITIVE_ORDER);
	
	private static boolean init_done = false;


	private static void init() 
	 throws OnPremDataSourceAccessTestConfigurationException{
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

 		init_done = true;						
			
		InputStream is = null;
		int invalid_connector_definition_count = 0;
		
		try {
			is = OnPremDataSourceAccessTestConfiguration.class.getResourceAsStream(CONFIG_FILE);
			if(is != null) {
				JSONObject config = null;
				// convert all property keys to lower case
				config = (JSONObject) JSONUtil.normalize((JSONObject) JSON.parse(is));

				// log to console for debug purposes
				System.out.println(config.toString());
				
				JSONArray connectorlist = (JSONArray) config.get("connectors");
				JSONObject cc = null;
				// iterate through all connector definitions
				for(int i = 0; i < connectorlist.size(); i++ ) {
				   cc = (JSONObject) connectorlist.get(i);
				   // validate that all mandatory properties are defined
				   if(isNullOrEmpty((String)cc.get("url_scheme")) || isNullOrEmpty((String)cc.get("driver")) || isNullOrEmpty((String)cc.get("query"))) {
				   	   invalid_connector_definition_count++;
				   	   System.err.print("Connector entry " + (i + 1) + ":");
					   if(isNullOrEmpty((String)cc.get("url_scheme"))) {
					   	System.err.print("Property url_scheme is empty or missing.");
					   }
					   if(isNullOrEmpty((String)cc.get("driver"))) {
					   	System.err.print("Property driver is empty or missing.");
					   }
					   if(isNullOrEmpty((String)cc.get("query"))) {
					   	System.err.print("Property query is empty or missing.");
					   }
				   	   System.err.println("");
				   }
				   else {
				   	   if(cc.get("display_name") == null)	
				   	   	 cc.put("display_name",(String)cc.get("url_scheme"));
					   SUPPORTED_DATA_SOURCES.put((String)cc.get("url_scheme"), 
							                      new DatabaseAccessTestConfig((String)cc.get("display_name"),(String)cc.get("driver"),(String)cc.get("query")));
					}
				}
				
			}
		} 
		catch(IOException ioex) {
			// log JSON parsing error to console 
			System.err.println(ioex.getMessage());
			ioex.printStackTrace(System.err);
			throw new OnPremDataSourceAccessTestConfigurationException("The configuration file " + CONFIG_FILE + " could not be processed due to a JSON parsing error: " + ioex.getMessage() + ".", ioex);
		}
		catch(Exception ex) {
			// log error to console 
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			throw new OnPremDataSourceAccessTestConfigurationException("The configuration file " + CONFIG_FILE + " could not be processed: " + ex.getMessage(), ex);
		}
		finally {
			if(is != null) {
				// cleanup
				try {
					is.close();
				}
				catch(Exception ex) {
					// ignore
				}
			}
			else {
				 // this indicates that the file could not be loaded
				throw new OnPremDataSourceAccessTestConfigurationException("The configuration file " + CONFIG_FILE + " could not be located.");	
			}

			if(invalid_connector_definition_count > 0)	 {
				throw new OnPremDataSourceAccessTestConfigurationException("The configuration file " + CONFIG_FILE + " contains " + invalid_connector_definition_count + " invalid connector definition(s).");		
			}
		}
	} // init

	/**
	 * Returns a list of data source types, for which a configuration was specified.
	 * @return an array of strings; guaranteed to be not null
	 */
	public static ArrayList<String> getSupportedDataSources() 
	 throws OnPremDataSourceAccessTestConfigurationException {
		
		if(! init_done) {
			init();
		}

		ArrayList<String> supportedDataSourceTypes = new ArrayList<String>();
		
		Iterator<DatabaseAccessTestConfig> it = SUPPORTED_DATA_SOURCES.values().iterator();
		while(it.hasNext()) {
			supportedDataSourceTypes.add(it.next().getURLDisplayName());
		}
		return supportedDataSourceTypes;
	} // getSupportedDataSources

	/**
	 * 
	 * @return
	 */
	public static DatabaseAccessTestConfig getDataSourceAccessConfig (String scheme) 
	 throws OnPremDataSourceAccessTestConfigurationException {

		if(! init_done) {
			init();
		}

		return SUPPORTED_DATA_SOURCES.get(scheme);

	} // getDataSourceAccessConfig


	private static boolean isNullOrEmpty(String input) {
		if((input == null) || (input.length() <1))
			return true;
		else
			return false;
	} // isNullOrEmpty

} // end class