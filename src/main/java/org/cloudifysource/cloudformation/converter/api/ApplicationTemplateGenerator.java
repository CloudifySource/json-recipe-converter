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
package org.cloudifysource.cloudformation.converter.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.cloudformation.converter.api.json.ServiceDetails;

/**
 * Generates Cloudify application template from the DSL.
 * 
 * @author victor
 * 
 */
public class ApplicationTemplateGenerator extends GroovyTemplateGenerator {

	private String applicationName;
	private Collection<ServiceDetails> services;

	public ApplicationTemplateGenerator(final String applicationName, final Collection<ServiceDetails> services) {
		this.applicationName = applicationName;
		this.services = services;
	}

	@Override
	public File generateFiles(final File destinationFolder) throws IOException {
		if (!destinationFolder.isDirectory()) {
			throw new IllegalArgumentException("The path '" + destinationFolder.getAbsolutePath()
					+ "' is not a folder.");
		}
		final File appGroovyFile = new File(destinationFolder, applicationName + "-application.groovy");
		final String appGroovy = this.generate();
		FileUtils.write(appGroovyFile, appGroovy);
		return appGroovyFile;
	}

	@Override
	protected void doGenerate() throws IOException {
		openBrace("application");
		appendKeyValue("name", applicationName);

		for (ServiceDetails service : services) {
			openBrace("service");
			appendKeyValue("name", service.getServiceName());
			appendKeyList("dependsOn", service.getDependsOn());
			closeBrace();
		}
		closeBrace();
	}

	private void appendKeyList(final String key, final List<String> values) {
		if (values != null && !values.isEmpty()) {
			appendTab();
			builder.append(key).append(" = ").append("[");
			for (String value : values) {
				builder.append("\"").append(value).append("\",");
			}
			builder.setLength(builder.length() - 1);
			builder.append("]\n");
		}
	}

	private void appendKeyValue(final String key, final String value) {
		if (value != null) {
			appendTab();
			builder.append(key).append(" = ").append("\"").append(value).append("\"").append("\n");
		}
	}
}