/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package org.cloudifysource.cloudformation.converter.api.json;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAnySetter;

/**
 * The root node of the template.
 * 
 * @author victor
 * @since 2.7.0
 */
public class JsonTemplate {

	private String applicationName;
	private StackApplication stackApplication;

	/**
	 * Setter for applications node.
	 * 
	 * @param applicationName
	 *            The application name.
	 * @param value
	 *            The StackApplication's node.
	 */
	@JsonAnySetter
	public void setApplication(final String applicationName, final StackApplication value) {
		this.applicationName = applicationName;
		this.stackApplication = value;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public StackApplication getStackApplication() {
		return stackApplication;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
