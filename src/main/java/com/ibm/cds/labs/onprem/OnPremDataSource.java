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

/**
 * An abstract wrapper for an on-prem data source test provider.
 * @author ptitzler
 *
 */
public abstract class OnPremDataSource {
	
	/**
	 * Connect to a data source.
	 * @throws OnPremResourceAccessTestException if a problem was encountered
	 */
	protected abstract void connect() 
	 throws OnPremDataSourceAccessTestException;

    /**
     * Disconnect from the data source. Note that no error is raised if a problem was encountered.
     */
	protected abstract void disconnect();
	
	/**
	 * Run a dummy query on the data source. This dummy query should not depend on any user-created objects. 
	 * @return the first column of the dummy query that was executed, expressed as a String
	 * @throws OnPremResourceAccessTestException
	 */
	protected abstract String runQuery() 
		throws OnPremDataSourceAccessTestException;
	
} // class
