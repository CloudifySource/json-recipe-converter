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
package org.cloudifysource.cloudformation.converter.api.model;

import java.io.File;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The bean for CreateStack request.
 * 
 */
public class CreateStackRequest {

	@NotNull
	private String stackName;
	private String templateBody;
	private String templateURL;
	private File templateFile;
	private Integer timeoutInMinutes;

	public CreateStackRequest() {
	}

	public String getStackName() {
		return stackName;
	}

	public void setStackName(final String stackName) {
		this.stackName = stackName;
	}

	public String getTemplateBody() {
		return templateBody;
	}

	public void setTemplateBody(final String templateBody) {
		this.templateBody = templateBody;
	}

	public String getTemplateURL() {
		return templateURL;
	}

	public void setTemplateURL(final String templateURL) {
		this.templateURL = templateURL;
	}

	public File getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(final File templateFile) {
		this.templateFile = templateFile;
	}

	public Integer getTimeoutInMinutes() {
		return timeoutInMinutes;
	}

	public void setTimeoutInMinutes(final Integer timeoutInMinutes) {
		this.timeoutInMinutes = timeoutInMinutes;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
