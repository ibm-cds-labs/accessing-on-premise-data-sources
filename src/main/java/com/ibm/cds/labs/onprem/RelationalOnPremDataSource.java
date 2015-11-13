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

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Implements the connectivity test for relational data sources.
 * @author ptitzler
 *
 */
public class RelationalOnPremDataSource extends OnPremDataSource {
		
	private DatabaseAccessTestConfig accessTestConfig = null; 
	private String jdbcURL = null;
	private String opUser = null;
	private String opPassword = null;

	private Connection connection = null;
		
	/**
	 * Constructor.
	 * @param scheme - the scheme component of the JDBC URL (jdbc:<scheme>//...)
	 * @param URL - the JDBC URL to be used to connect to the on-premises data source
	 * @param user - the user id to be used to connect to the on-premises data source
	 * @param password - user id's password
	 * @throws OnPremDataSourceNotSupportedException
	 */
	public RelationalOnPremDataSource(String scheme, String URL, String user, String password) 
	 throws OnPremDataSourceAccessTestConfigurationException, OnPremDataSourceNotSupportedException {
		
		// returns null if no configuration is defined for the specified scheme
		accessTestConfig = OnPremDataSourceAccessTestConfiguration.getDataSourceAccessConfig(scheme);
	
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
		catch(ClassNotFoundException cnfex) {
			throw new OnPremDataSourceAccessTestException("Unable to load the JDBC driver " + accessTestConfig.getJDBCDriverClassName() + " for on-premises "+ accessTestConfig.getURLDisplayName() + " database.",cnfex);
		}
		catch(SQLException sqlex) {
			throw new OnPremDataSourceAccessTestException("Failed to connect to on-premises "+ accessTestConfig.getURLDisplayName() + " database.",sqlex);
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
	 * @return the first result set column of the dummy query that was executed
	 * @throws OnPremResourceAccessTestException, if a problem was encountered
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