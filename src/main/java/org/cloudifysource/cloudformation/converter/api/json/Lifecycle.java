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
import org.cloudifysource.cloudformation.converter.api.json.filetype.ScriptType;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Bean which represents the Lifecycle's node.
 * 
 * @author victor
 * @since 2.7.0
 */
public class Lifecycle {

	@JsonProperty("postStart")
	private ScriptType postStart;

	@JsonProperty("preStop")
	private ScriptType preStop;

	@JsonProperty("stop")
	private ScriptType stop;

	@JsonProperty("postStop")
	private ScriptType postStop;

	public ScriptType getPostStart() {
		return postStart;
	}

	public ScriptType getPreStop() {
		return preStop;
	}

	public ScriptType getStop() {
		return stop;
	}

	public ScriptType getPostStop() {
		return postStop;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
