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
 * Wraps the standard Java exception class
 * @author ptitzler
 *
 */
public class OnPremDataSourceNotSupportedException extends java.lang.Exception {

	private static final long serialVersionUID = 1L;

	public OnPremDataSourceNotSupportedException(String message) {
		super(message);
	}

	public OnPremDataSourceNotSupportedException(String message, Throwable throwable) {
		super(message, throwable);
	}
} // class