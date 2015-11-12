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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;


@Path("/meta")
public class OnPremDataSourceAccessMetaData {

	/**
	 * This method returns information about the data source types that are supported by this application.
	 * @return A JSON string {"version":"STRING_VALUE","supported_on_prem_resource_types":["STRING_VALUE",...]}
	 */
	@GET
	public String getMeta() {

		JSONObject metadata = new JSONObject();
		metadata.put("version", "0.1");
		JSONArray supportedDataSources = new JSONArray();
		supportedDataSources.addAll(RelationalOnPremDataSource.getSupportedDataSources());
		metadata.put("supported_on_prem_resource_types", supportedDataSources);
		return metadata.toString();
		
	} // getMeta	
	
} // class