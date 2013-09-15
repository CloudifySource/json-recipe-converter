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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.cloudformation.converter.api.json.JsonTemplate;
import org.cloudifysource.cloudformation.converter.api.json.ServiceProperties;
import org.cloudifysource.cloudformation.converter.api.json.StackResources;
import org.junit.Assert;
import org.junit.Test;

public class CloudifyPrivateEc2TemplateConverterTest {

	private final Logger logger = Logger.getLogger(CloudifyPrivateEc2TemplateConverterTest.class.getName());

	private CloudifyPrivateEc2TemplateConverter converter = new CloudifyPrivateEc2TemplateConverter();

	@Test
	public void testParseTemplate() throws Exception {
		File templateFile = new File("./src/test/resources/definition-dsl.template");
		JsonTemplate parseTemplate = converter.parseTemplate(templateFile);
		logger.info(parseTemplate.toString());
	}

	@Test
	public void testParseExtendedApplicationTemplate() throws Exception {
		File templateFile =
				new File("./src/test/resources/samples/extendedApplication/extendedApplication-dsl.template");
		JsonTemplate parseTemplate = converter.parseTemplate(templateFile);
		StackResources resources = parseTemplate.getStackApplication().getResources();
		List<String> volumeRef = resources.getVolumeRef("appHost");
		for (String ref : volumeRef) {
			Assert.assertTrue("Resource " + ref + "does not exists", resources.containsResource(ref));
		}
	}

	@Test
	public void testParseTemplateBodyTemplate() throws Exception {
		File templateFile =
				new File("./src/test/resources/samples/templateBodyApplication/templateBodyApplication-dsl.template");
		JsonTemplate parseTemplate = converter.parseTemplate(templateFile);
		StackResources resources = parseTemplate.getStackApplication().getResources();
		List<String> volumeRef = resources.getVolumeRef("appHost");

		for (String ref : volumeRef) {
			Assert.assertTrue("Resource " + ref + "does not exists", resources.containsResource(ref));
		}
	}

	@Test
	public void testConvertExtendedApplicationTemplate() throws Exception {
		File convertTemplate = converter.convertTemplate("extendedApplication", new File(
				"./src/test/resources/samples/extendedApplication/extendedApplication-dsl.template"));
		logger.info(convertTemplate.getPath());
	}

	@Test
	public void testConvertTemplateBodyApplication() throws Exception {
		File convertTemplate = converter.convertTemplate("templateBody", FileUtils.readFileToString(new File(
				"./src/test/resources/samples/templateBodyApplication/templateBodyApplication-dsl.template")));

		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "cfns").exists());
		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "cfns/templateBody").exists());
		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "cfns/templateBody/simpleService1-cfn.template")
				.exists());
		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "templateBody").exists());
		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "templateBody/templateBody-application.groovy")
				.exists());
		Assert.assertTrue(new File(convertTemplate.getAbsolutePath(), "templateBody/simpleService1").exists());
		logger.info(convertTemplate.getPath());
	}

	@Test
	public void testConvertProperties() throws Exception {
		String template =
				FileUtils.readFileToString(new File("./src/test/resources/tests/convert-properties.template"));
		List<ServiceProperties> properties = new ArrayList<ServiceProperties>();
		properties.add(new ServiceProperties("imageId", "12345"));
		properties.add(new ServiceProperties("keyName", "secret"));
		properties.add(new ServiceProperties("val_1", "__value1__"));
		properties.add(new ServiceProperties("val-2", "__value2__"));

		String converted = converter.convertProperties(properties, template);

		Assert.assertTrue("imageId has not been converted", converted.contains("\"12345\""));
		Assert.assertTrue("keyName has not been converted", converted.contains("\"secret\""));
		Assert.assertTrue("val_1 has not been converted", converted.contains("\"__value1__\""));
		Assert.assertTrue("val-2 has not been converted", converted.contains("\"__value2__\""));
		Assert.assertTrue("#unknown1# has been converted", !converted.contains("#unknown1#"));
		Assert.assertTrue("#unknown2# has been converted", !converted.contains("#unknown2#"));
		Assert.assertTrue("#unknown3# has been converted", !converted.contains("#unknown3#"));
		Assert.assertTrue("unknown1 has not been found", converted.contains("unknown1"));
		Assert.assertTrue("unknown2 has not been found", converted.contains("unknown2"));
		Assert.assertTrue("unknown3 has not been found", converted.contains("unknown3"));
	}
}
