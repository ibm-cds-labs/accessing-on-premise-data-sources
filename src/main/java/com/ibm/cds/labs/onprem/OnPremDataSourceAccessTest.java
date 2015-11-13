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

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.ibm.cds.labs.onprem.util.JSONUtil;
import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;


@Path("/test")
public class OnPremDataSourceAccessTest {
			
	/**
	 * This method determines which user-provided services have been bound to the sample application and tries to connect
	 * to the underlying on-premises data sources. The results are returned to the caller for each supported data source. If a fatal error is encountered, the optional error
	 * property will be set.
	 * @return A JSON string {"services":[{"svc_name":"STRING_VALUE","on_prem_resource_type":"STRING_VALUE","success":"BOOLEAN_VALUE","output":"STRING_VALUE"},...],
	 *                        "error":"STRING_VALUE"} 
	 *         All properties are mandatory, with the exception of the on_prem_resource_type. This property is not set if the type cannot be determined.
	 */
	@GET
	public String runTests() {

		JSONObject services = null;
		JSONArray up_services = null;
		String message = null;
		JSONObject testresult = new JSONObject();
		JSONArray svclist = new JSONArray();

		try {
			// Fetch 'VCAP_SERVICES' variable, which contains all the credentials of services bound to this application.
			// all keys in the services JSONObject are normalized to contain only lower-case characters to make lookup easier 
			services = (JSONObject) JSONUtil.normalize(JSON.parse(System.getenv("VCAP_SERVICES")));

			if(services != null) {					
				
				// check whether user-provided services were bound to this application
				up_services = (JSONArray) services.get("user-provided");
				if(up_services != null)  {

					JSONObject svc = null;
					JSONObject svcresult = null;
					JSONObject credentials = null;
					String jdbcurl = null;

					// process each bound user-provided service
					for(Object serviceobj: up_services) {
						svcresult = new JSONObject();
						svc = (JSONObject) serviceobj;
						svcresult.put("svc_name", (String)svc.get("name"));
						 try {
							 credentials =  (JSONObject)svc.get("credentials");
							 
							// verify that this user-define service was defined for a DB2 data source
							 jdbcurl = (String)credentials.get("jdbcurl");
							 if((jdbcurl == null)|| (jdbcurl.length() < 5)) {
								 if(jdbcurl == null)
									 jdbcurl="";
								 // the JDBCURL property value is either not set or appears to be incorrect
								 svcresult.put("success","false");
								 svcresult.put("output","Test was skipped. The user-provided service does not define the jdbcUrl property or the property value "+ jdbcurl + " is invalid.");
							 }
							 else {
 								 // the jdbcUrl property is  defined for this user-provided service
								 OnPremDataSource resource = null;
								 
								 // the jdbcUrl property is set; it should look as follows: jdbc:<driver>://...
								 // determine whether the driver is supported
								 URI uri = URI.create(jdbcurl.substring(5));
								 
								 // save the URI scheme - it identifies the resource's type
								 svcresult.put("on_prem_resource_type", uri.getScheme().toUpperCase());
								 
								 // throws OnPremDataSourceNotSupportedException if no suitable test driver is found 
								 resource = new RelationalOnPremDataSource(uri.getScheme().toLowerCase(), (String)credentials.get("jdbcurl"), (String)credentials.get("user"), (String)credentials.get("password"));							 
								 								 
								 // connect to the resource
								 resource.connect();
								 // run the dummy query
								 String result = resource.runQuery();
								
								 if(result != null) {
									 // a result was returned; it appears that the on-premises resource is accessible
									 svcresult.put("success","true");
									 svcresult.put("output","The test query executed successfully on the on-premises database.");	
								 }
								 else {
									 // no result was returned; it appears that the on-premises resource cannot be accessed properly
									 svcresult.put("success","false");
									 svcresult.put("output","The test query did not return a result from the on-premises database.");									
								 }
								 // disconnect from the data source to release all allocated resources
								 resource.disconnect();
								 
							 } // the jdbcUrl property is  defined for this user-provided service
						 } // try
						 // do not catch OnPremDataSourceAccessTestConfigurationException here; it is a fatal error that needs to be processed at a higher level
						 catch(OnPremDataSourceNotSupportedException opdsnsex) {
							// the resource type cannot be processed
							 svcresult.put("success","false");
							 svcresult.put("output","Test was skipped. The user-provided service references a JDBC data source of type "+ opdsnsex.getMessage() + ", which is currently not supported by this utility.");									 							 
						 }
						 catch(OnPremDataSourceAccessTestException opex) {
							 	// a problem was encountered while trying to connect to the data source or running the dummy query
								System.err.println(opex.getMessage());
								message = "Test failed: " + opex.getMessage();
								if(opex.getCause() != null) {
									message = message + " (root cause: " + opex.getCause().getMessage() + ")" ;
								}			
								svcresult.put("success","false");
								svcresult.put("output",message);									
						} // catch
						 
						// add the test result to the output list 
						svclist.add(svcresult);
					} // for
				} // if user-provided services are defined
			} // if(services)
		} // try
		catch(OnPremDataSourceAccessTestConfigurationException opdsatce) {
			// fatal error thrown by OnPremDataSourceAccessTestConfiguration; a problem was encountered while trying to load/process the configuration file 
			testresult.put("error", opdsatce.getMessage());
		}
		catch(Exception ex) {
			// an unexpected error occurred; dump information to console
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			// set error status
			testresult.put("error", "The following error occurred: " + ex.getMessage());
		}
		
		// attach the list of processed services
		testresult.put("services", svclist);
		
		// return the result
		return testresult.toString();

	} // runTests	
	
} // class