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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAnySetter;

/**
 * Bean which represents StackLayers's node.
 * 
 * @author victor
 * @since 2.7.0
 */
public class StackLayers {

	private Map<String, ServiceDetails> services = new HashMap<String, ServiceDetails>();

	/**
	 * Setter to add services.
	 * 
	 * @param serviceName
	 *            The serviceName.
	 * @param details
	 *            ServiceDetails's node.
	 */
	@JsonAnySetter
	public void addServices(final String serviceName, final ServiceDetails details) {
		details.setServiceName(serviceName);
		this.services.put(serviceName, details);
	}

	public Collection<ServiceDetails> getServices() {
		return services.values();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
