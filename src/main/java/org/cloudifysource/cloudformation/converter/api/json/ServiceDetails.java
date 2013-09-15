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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cloudifysource.cloudformation.converter.api.json.filetype.ScriptType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Bean which represents ServiceDetails's node.
 * 
 */
public class ServiceDetails {

	@JsonIgnore
	private String serviceName;

	@JsonProperty("numInstance")
	private Integer numInstance;

	@JsonProperty("DependsOn")
	private List<String> dependsOn;

	@JsonProperty("Instances")
	private Ref instances;

	@SuppressWarnings("rawtypes")
	@JsonProperty("Template")
	private Map template;

	@JsonProperty("lifecycle")
	private Lifecycle lifecycle;

	@JsonProperty("properties")
	private List<ServiceProperties> serviceProperties;

	@JsonProperty("monitoring")
	private Monitoring monitoring;

	@JsonProperty("customCommands")
	private Map<String, ScriptType> customCommands = new HashMap<String, ScriptType>();

	@JsonProperty("scaling")
	private ScriptType scaling;

	public void setServiceName(final String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Integer getNumInstance() {
		return numInstance;
	}

	public List<String> getDependsOn() {
		return dependsOn;
	}

	public Ref getInstances() {
		return instances;
	}

	/**
	 * Get the inline JSON template.
	 * 
	 * @return The inline JSON template.
	 * @throws IOException
	 *             Could not parse the inline JSON template.
	 */
	public String getTemplate() throws IOException {
		if (template != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
				return json;
			} catch (IOException e) {
				throw e;
			}
		}
		return null;
	}

	public Lifecycle getLifecycle() {
		return lifecycle;
	}

	public List<ServiceProperties> getServiceProperties() {
		return serviceProperties;
	}

	public Monitoring getMonitoring() {
		return monitoring;
	}

	public Map<String, ScriptType> getCustomCommands() {
		return customCommands;
	}

	public ScriptType getScaling() {
		return scaling;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
