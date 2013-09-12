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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Bean which represents StackResources' node.
 * 
 */
public class StackResources {

	@SuppressWarnings("rawtypes")
	private Map<String, Map> resources = new HashMap<String, Map>();

	/**
	 * Set resources.
	 * 
	 * @param resourceName
	 *            The resource name.
	 * @param resourceJson
	 *            The JSON in map value format.
	 */
	@SuppressWarnings("rawtypes")
	@JsonAnySetter
	public void setResource(final String resourceName, final Map resourceJson) {
		resources.put(resourceName, resourceJson);
	}

	/**
	 * Get the raw JSON template of a resource.
	 * 
	 * @param resourceName
	 *            The resource to get.
	 * @return The raw JSON template of a resource.
	 * @throws IOException
	 *             If cannot retrieve the raw value..
	 */
	public String getResourceJson(final String resourceName) throws IOException {
		if (resources.containsKey(resourceName)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources.get(resourceName));
				return json;
			} catch (IOException e) {
				throw e;
			}
		}
		return null;
	}

	/**
	 * Returns a list of names of the declared volumes in EC2 instance.
	 * 
	 * @param resourceName
	 *            The name of the volume resource.
	 * @return A list of names of the declared volumes in EC2 instance.
	 */
	@SuppressWarnings("rawtypes")
	public List<String> getVolumeRef(final String resourceName) {
		List<String> volumeRefList = new ArrayList<String>();
		if (resources.containsKey(resourceName)) {
			Map map = resources.get(resourceName);
			Map propertiesNode = (Map) map.get("Properties");
			List volumes = (List) propertiesNode.get("Volumes");
			if (volumes != null) {
				for (Object volume : volumes) {
					Object volumeIdObj = ((Map) volume).get("VolumeId");
					if (volumeIdObj instanceof Map) {
						Map ref = (Map) volumeIdObj;
						String volumeId = (String) ref.get("Ref");
						volumeRefList.add(volumeId);
					} else {
						volumeRefList.add((String) volumeIdObj);
					}
				}
			}
		}
		return volumeRefList;
	}

	/**
	 * Returns <code>true</code> if the resource is found.
	 * 
	 * @param resourceName
	 *            The resource's name to search.
	 * @return Returns <code>true</code> if the resource is found.
	 */
	public boolean containsResource(final String resourceName) {
		return resources.containsKey(resourceName);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
