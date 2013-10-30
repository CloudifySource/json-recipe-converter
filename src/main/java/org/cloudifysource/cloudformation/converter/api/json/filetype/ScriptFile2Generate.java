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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Node of the json template which represents a script to be generated.<br />
 * The node will look like:
 * 
 * <pre>
 * { "groovy", "println this is a groovy script" }.
 * </pre>
 * 
 * @author victor
 * 
 * @author victor
 * @since 2.7.0
 */
public class ScriptFile2Generate implements ScriptType {

	private String fileType;
	private ScriptType content;
	private File generated;

	public ScriptFile2Generate() {
	}

	public ScriptFile2Generate(final String fileType, final ScriptType content) {
		this.fileType = fileType;
		this.content = content;
	}

	@Override
	public String getValue() {
		if (generated == null) {
			try {
				final File createTempFile = File.createTempFile("Generated", "." + fileType);
				createTempFile.deleteOnExit();
				FileUtils.write(createTempFile, content.getValue());
				this.generated = createTempFile;
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return generated.getAbsolutePath();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
