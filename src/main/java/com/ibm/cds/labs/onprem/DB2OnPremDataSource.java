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

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.Exception;

/**
 * Implements the connectivity test for a DB2 resource.
 * @author ptitzler
 *
 */
public class DB2OnPremDataSource extends OnPremDataSource {

	// dummy query that will be run against a DB2 data source
	private static final String QUERY = "SELECT current date FROM sysibm.sysdummy1";

	private String jdbcURL = null;
	private String opUser = null;
	private String opPassword = null;

	private Connection connection = null;

	/**
	 * Simple constructor
	 * @param URL
	 * @param user
	 * @param password
	 */
	public DB2OnPremDataSource(String URL, String user, String password) {
		jdbcURL = URL;
		opUser = user;
		opPassword = password;
	} // constructor

	/**
	 * Connect to the DB2 data source
	 * @throws OnPremResourceAccessTestException if a problem was encountered
	 */
	public void connect() 
	 throws OnPremDataSourceAccessTestException {

		try {
			// load the DB2 database driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");			
			// try to establish a connection
			connection = DriverManager.getConnection(jdbcURL,opUser,opPassword);
		}
		catch(Exception ex) {
			throw new OnPremDataSourceAccessTestException("Failed to connect to on-premise DB2 database.",ex);
		}

	} // connect

    /**
     * Disconnect from the DB2 data source. Note that no error is raised if a problem was encountered.
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
			statement.execute(QUERY);
		
			// fetch the result set
			resultset = statement.getResultSet();
			if(resultset.next()) {
				result = resultset.getString(1);
			}
		}
		catch(SQLException sqlex) {
			throw new OnPremDataSourceAccessTestException("Failed to execute test query on-premise DB2.",sqlex);
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

} // DB2OnPremResource