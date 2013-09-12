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
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.cloudifysource.cloudformation.converter.api.json.JsonTemplate;
import org.cloudifysource.cloudformation.converter.api.json.ServiceDetails;
import org.cloudifysource.cloudformation.converter.api.json.ServiceProperties;
import org.cloudifysource.cloudformation.converter.api.json.StackApplication;
import org.cloudifysource.cloudformation.converter.api.json.StackLayers;
import org.cloudifysource.cloudformation.converter.api.json.StackResources;
import org.cloudifysource.dsl.internal.packaging.ZipUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class convert JSON template into Cloudify groovy templates (cloud and recipes files).
 * 
 * @author victor
 * 
 */
public class CloudifyPrivateEc2TemplateConverter {

	private static final int TIMEOUT = 30000;

	private final Logger logger = Logger.getLogger(CloudifyPrivateEc2TemplateConverter.class.getName());

	private final ObjectMapper mapper;

	public CloudifyPrivateEc2TemplateConverter() {
		mapper = new ObjectMapper();
		mapper.configure(Feature.USE_ANNOTATIONS, true);
		mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Convert a JSON template into Cloudify cloudFolder.
	 * 
	 * @param stackName
	 *            The applicationName.
	 * @param templateBody
	 *            The JSON template.
	 * @return A file object of Cloudify cloudFolder.
	 * @throws TemplateParserException
	 *             If the template couldn't be parsed.
	 */
	public File convertTemplate(final String stackName, final String templateBody) throws TemplateParserException {
		final JsonTemplate template = this.parseTemplate(templateBody);
		return this.doConvertTemplate(stackName, null, template);
	}

	/**
	 * Convert a JSON template into Cloudify cloudFolder.
	 * 
	 * @param stackName
	 *            The applicationName.
	 * @param templateFile
	 *            The JSON template.
	 * @return A file object of Cloudify cloudFolder.
	 * @throws TemplateParserException
	 *             If the template couldn't be parsed.
	 */
	public File convertTemplate(final String stackName, final File templateFile) throws TemplateParserException {
		final File sourceAppDir = templateFile.getParentFile();
		final JsonTemplate template = this.parseTemplate(templateFile);
		return this.doConvertTemplate(stackName, sourceAppDir, template);
	}

	/**
	 * Convert a JSON template into Cloudify cloudFolder.
	 * 
	 * @param stackName
	 *            The applicationName.
	 * @param url
	 *            URL which point to a JSON template (file only or zip archive).
	 * @return A file object of Cloudify cloudFolder.
	 * @throws TemplateParserException
	 *             If the template couldn't be parsed.
	 * @throws ConverterException
	 *             If something is wrong with the file or zip archive.
	 */
	public File convertTemplate(final String stackName, final URL url) throws TemplateParserException,
			ConverterException {
		try {
			final String extension = FilenameUtils.getExtension(url.getFile());
			final File downloadedFile = File.createTempFile("cfnDownloaded", "." + extension);
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("downloaded templateUrl: " + downloadedFile.getAbsolutePath());
			}
			downloadedFile.deleteOnExit();
			FileUtils.copyURLToFile(url, downloadedFile, TIMEOUT, TIMEOUT);

			File templateFile = null;
			if ("template".equals(extension)) {
				templateFile = downloadedFile;
			} else if ("zip".equals(extension)) {
				final File createTempFile = File.createTempFile("extracted", "");
				createTempFile.delete();
				createTempFile.mkdir();
				createTempFile.deleteOnExit();
				ZipUtils.unzip(downloadedFile, createTempFile);
				templateFile = this.findTemplateFile(createTempFile);
			}

			if (templateFile == null) {
				throw new TemplateParserException("Couldn't find template file");
			}

			final File sourceAppDir = templateFile.getParentFile();
			final JsonTemplate template = this.parseTemplate(templateFile);
			return this.doConvertTemplate(stackName, sourceAppDir, template);
		} catch (IOException e) {
			throw new ConverterException("Couldn't download or extract file: " + e.getMessage());
		}
	}

	private File findTemplateFile(final File directory) throws TemplateParserException {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + directory.getAbsolutePath());
		}
		for (final File file : directory.listFiles()) {
			if (file.isDirectory()) {
				final File returned = this.findTemplateFile(file);
				if (returned != null && returned.getName().endsWith("-dsl.template")) {
					return returned;
				}
			} else if (file.getName().endsWith("-dsl.template")) {
				return file;
			}
		}
		return null;
	}

	private File doConvertTemplate(final String stackName, final File sourceAppDir, final JsonTemplate template)
			throws TemplateParserException {
		final StackApplication stackApplication = template.getStackApplication();

		final StackLayers layers = stackApplication.getLayers();

		final File recipeRootPath = this.createNewTempDir();
		logger.info("Recipe temp root: " + recipeRootPath);
		final File targetAppDir = new File(recipeRootPath, stackName);
		targetAppDir.mkdirs();

		// Create application groovy file
		try {
			final ApplicationTemplateGenerator appTemplateGenerator =
					new ApplicationTemplateGenerator(stackName, layers.getServices());
			appTemplateGenerator.generateFiles(targetAppDir);

			final Collection<ServiceDetails> services = layers.getServices();
			for (final ServiceDetails service : services) {
				this.generateServiceFiles(sourceAppDir, targetAppDir, service);
				this.generateCfnFile(recipeRootPath, stackName, service, stackApplication.getResources());
			}
			return recipeRootPath;
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new TemplateParserException(e.getMessage());
		}
	}

	private void generateCfnFile(final File targetPath, final String stackName, final ServiceDetails service,
			final StackResources stackResources)
			throws IOException {
		// Copy service's CFN templates
		final File cfnsPath = new File(targetPath, "cfns/" + stackName);
		cfnsPath.mkdirs();
		final File cfnFile = new File(cfnsPath, service.getServiceName() + "-cfn.template");
		if (service.getTemplate() != null) {
			final String convertedTemplate =
					this.convertProperties(service.getServiceProperties(), service.getTemplate());
			FileUtils.writeStringToFile(cfnFile, convertedTemplate);
		} else {
			final String resourceName = service.getInstances().getRefValue();
			final String resourceJson = stackResources.getResourceJson(resourceName);
			final List<String> volumeNames = stackResources.getVolumeRef(resourceName);

			final StringBuilder sb = new StringBuilder();
			sb.append("{\n\"Resources\":{\n");
			final String convertedJson = this.convertProperties(service.getServiceProperties(), resourceJson);
			sb.append("\"").append(resourceName).append("\":").append(convertedJson).append(",\n");
			for (final String volumeName : volumeNames) {
				final String volumeJson = stackResources.getResourceJson(volumeName);
				final String convertedVolumeJon = this.convertProperties(service.getServiceProperties(), volumeJson);
				sb.append("\"").append(volumeName).append("\":").append(convertedVolumeJon).append(",\n");
			}
			sb.setLength(sb.length() - 2);
			sb.append("\n}\n}");
			FileUtils.writeStringToFile(cfnFile, sb.toString());
		}
	}

	String convertProperties(final List<ServiceProperties> properties, final String template) {
		String converted = template;
		if (properties != null) {
			final Pattern p = Pattern.compile("\"#([\\w-_]+)#\"");
			Matcher m = p.matcher(converted);
			while (m.find()) {
				final String group1 = m.group(1);

				boolean foundProperties = false;
				for (final ServiceProperties serviceProperties : properties) {
					if (serviceProperties.getKey().equals(group1)) {
						converted = m.replaceFirst("\"" + serviceProperties.getValue() + "\"");
						m = p.matcher(converted);
						foundProperties = true;
						break;
					}
				}
				if (!foundProperties) {
					converted = m.replaceFirst(group1);
					m = p.matcher(converted);
				}
			}
		}
		return converted;
	}

	private File createNewTempDir() {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		final int randomInt = 1 + RandomUtils.nextInt();
		final File tempDir = new File(sysTempDir, "tempDir" + randomInt);
		tempDir.delete();
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		tempDir.deleteOnExit();
		return tempDir;
	}

	private void generateServiceFiles(final File sourceAppDir, final File targetAppDir, final ServiceDetails service)
			throws IOException {
		final File serviceFolderDest = new File(targetAppDir, service.getServiceName());
		serviceFolderDest.mkdirs();

		// Create service groovy file
		final String absolutePath = sourceAppDir == null ? null : sourceAppDir.getAbsolutePath();
		final ServiceTemplateGenerator serviceTemplateGenerator =
				new ServiceTemplateGenerator(service, absolutePath, serviceFolderDest);
		serviceTemplateGenerator.generateFiles(serviceFolderDest);
	}

	JsonTemplate parseTemplate(final File templateFile) throws TemplateParserException {
		JsonTemplate tokenResponse = null;
		try {
			tokenResponse = mapper.readValue(templateFile, JsonTemplate.class);
			return tokenResponse;
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new TemplateParserException(e.getMessage());
		}
	}

	JsonTemplate parseTemplate(final String templateBody) throws TemplateParserException {
		JsonTemplate tokenResponse = null;
		try {
			tokenResponse = mapper.readValue(templateBody, JsonTemplate.class);
			return tokenResponse;
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new TemplateParserException(e.getMessage());
		}
	}
}
