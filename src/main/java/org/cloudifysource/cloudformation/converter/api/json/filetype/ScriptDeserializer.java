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
package org.cloudifysource.cloudformation.converter.api.json.filetype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * A deserializer which handle property values of Amazon CloudFormation.<br />
 * It create a specific bean for :
 * <ul>
 * <li><code>Fn::Base64</code></li>
 * <li><code>Fn::Join</code></li>
 * <li><code>Ref</code></li>
 * <li><code>basic string values</code></li>
 * </ul>
 * The other type of value is simply saved as a raw in a {@link StringValue}.
 * 
 * @author victor
 * 
 */
public class ScriptDeserializer extends JsonDeserializer<ScriptType> {

	@Override
	public ScriptType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		final ObjectCodec oc = jp.getCodec();
		final JsonNode node = oc.readTree(jp);
		return this.functionValue(node, ctxt);
	}

	private ScriptType functionValue(final JsonNode root, final DeserializationContext ctxt) throws IOException {

		final Iterator<String> fieldNames = root.getFieldNames();

		while (fieldNames.hasNext()) {
			final String next = fieldNames.next();

			if ("groovy".equals(next) || "bat".equals(next) || "sh".equals(next)) {
				final JsonNode jsonNode = root.get(next);
				final ScriptType value = this.functionValue(jsonNode, ctxt);
				return new ScriptFile2Generate(next, value);
			} else if ("Fn::Join".equals(next)) {
				final JsonNode joinNode = root.get(next);
				final Iterator<JsonNode> elements = joinNode.getElements();

				final JsonNode separatorNode = elements.next();
				final String separator = separatorNode.getTextValue();

				final JsonNode toJoinNodes = elements.next();
				final Iterator<JsonNode> iterator = toJoinNodes.iterator();
				final List<ScriptType> toJoinList = new ArrayList<ScriptType>();
				while (iterator.hasNext()) {
					final JsonNode node = iterator.next();
					toJoinList.add(this.functionValue(node, ctxt));
				}

				return new JoinFunction(separator, toJoinList);
			} else {
				return new StringContent(root.toString());
			}
		}
		return new StringContent(root.getTextValue());
	}
}
