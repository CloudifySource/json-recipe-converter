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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackResponse;
import org.cloudifysource.cloudformation.converter.api.model.DeleteStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackResponse;
import org.cloudifysource.dsl.Application;
import org.cloudifysource.dsl.internal.CloudifyConstants;
import org.cloudifysource.dsl.internal.DSLException;
import org.cloudifysource.dsl.internal.DSLReader;
import org.cloudifysource.dsl.internal.DSLUtils;
import org.cloudifysource.dsl.internal.packaging.Packager;
import org.cloudifysource.dsl.internal.packaging.ZipUtils;
import org.cloudifysource.dsl.rest.request.InstallApplicationRequest;
import org.cloudifysource.dsl.rest.response.InstallApplicationResponse;
import org.cloudifysource.dsl.rest.response.UninstallApplicationResponse;
import org.cloudifysource.restclient.ErrorStatusException;
import org.cloudifysource.restclient.RestException;
import org.cloudifysource.restclient.exceptions.RestClientException;

/**
 * API implementation. This method calls Cloudify REST API to createStack, deleteStack and describeStack.
 * 
 * @author victor
 * 
 */
public class CloudifyPrivateEc2Converter implements ConverterAPI {

	private static final int DEFAULT_TIMEOUT_IN_MINUTES = 15;

	private final Logger logger = Logger.getLogger(CloudifyPrivateEc2Converter.class.getName());

	private CloudifyPrivateEc2TemplateConverter converter = new CloudifyPrivateEc2TemplateConverter();
	private RestFacade restFacade = new RestFacade();

	private boolean connected;

	public void setRestFacade(final RestFacade restFacade) {
		this.restFacade = restFacade;
	}

	@Override
	public void connect(final String user, final String password, final String urlString)
			throws RestClientException, MalformedURLException, RestException {
		this.connect(user, password, urlString, null);
	}

	@Override
	public void connect(final String user, final String password, final String urlString, final String version)
			throws RestClientException, MalformedURLException, RestException {
		this.restFacade.connect(user, password, urlString, version);
		connected = true;
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public CreateStackResponse createStack(final CreateStackRequest createStackRequest) throws Exception {
		final String stackName = createStackRequest.getStackName();
		final Integer timeoutInMinutes = createStackRequest.getTimeoutInMinutes();

		final File rootFolder = this.convertCfnTemplateToCloudifyTemplate(createStackRequest);

		final File applicationFile = new File(rootFolder, stackName);
		final Application application = this.createApplicationEntity(applicationFile);
		final File zipFile = Packager.packApplication(application, applicationFile, null);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("application zip file: " + zipFile);
		}
		final String applicationFileKey = this.restFacade.uploadToRepo(zipFile);

		final File cloudConfigtempFile = new File(rootFolder, CloudifyConstants.SERVICE_CLOUD_CONFIGURATION_FILE_NAME);
		cloudConfigtempFile.deleteOnExit();
		ZipUtils.zip(new File(rootFolder, "/cfns"), cloudConfigtempFile);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("cloudConfiguration zip file: " + cloudConfigtempFile);
		}
		final String cloudConfigurationFileKey = this.restFacade.uploadToRepo(cloudConfigtempFile);

		final InstallApplicationRequest installRequest = new InstallApplicationRequest();
		installRequest.setApplicationName(stackName);
		installRequest.setApplcationFileUploadKey(applicationFileKey);
		installRequest.setCloudConfigurationUploadKey(cloudConfigurationFileKey);
		if (timeoutInMinutes != null) {
			installRequest.setTimeoutInMillis(TimeUnit.MINUTES.toMillis(timeoutInMinutes));
		}
		final InstallApplicationResponse installResponse =
				this.restFacade.installApplication(stackName, installRequest);
		final String deploymentID = installResponse.getDeploymentID();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("deploymentID: " + deploymentID);
		}
		return new CreateStackResponse(deploymentID);
	}

	private File convertCfnTemplateToCloudifyTemplate(final CreateStackRequest createStackRequest)
			throws TemplateParserException, ConverterException {

		if (createStackRequest.getTemplateFile() != null) {
			return converter.convertTemplate(createStackRequest.getStackName(), createStackRequest.getTemplateFile());
		} else if (StringUtils.isNotEmpty(createStackRequest.getTemplateBody())) {
			return converter.convertTemplate(createStackRequest.getStackName(), createStackRequest.getTemplateBody());
		} else if (StringUtils.isNotEmpty(createStackRequest.getTemplateURL())) {
			try {
				return converter.convertTemplate(createStackRequest.getStackName(),
						new URL(createStackRequest.getTemplateURL()));
			} catch (MalformedURLException e) {
				throw new ConverterException(e.getMessage());
			}
		}
		throw new ConverterException("No template defined in the request: " + createStackRequest);
	}

	private Application createApplicationEntity(final File applicationFile) throws DSLException {
		final DSLReader dslReader = new DSLReader();
		final File dslFile = DSLReader.findDefaultDSLFile(DSLUtils.APPLICATION_DSL_FILE_NAME_SUFFIX, applicationFile);
		dslReader.setDslFile(dslFile);
		dslReader.setCreateServiceContext(false);
		dslReader.addProperty(DSLUtils.APPLICATION_DIR, dslFile.getParentFile().getAbsolutePath());
		final Application application = dslReader.readDslEntity(Application.class);
		return application;
	}

	@Override
	public void deleteStack(final DeleteStackRequest deleteStackRequest) throws RestClientException {
		final UninstallApplicationResponse uninstallReponse =
				this.restFacade.uninstallApplication(deleteStackRequest.getStackName(), DEFAULT_TIMEOUT_IN_MINUTES);
		logger.fine("Deleted stack with id = " + uninstallReponse.getDeploymentID());
	}

	@Override
	@SuppressWarnings("unchecked")
	public DescribeStackResponse describeStack(final DescribeStackRequest describeStackRequest)
			throws RestClientException {
		final String stackName = describeStackRequest.getStackName();
		try {
			DescribeStackResponse response = null;
			if (stackName == null) {
				final List<Object> objectsList =
						(List<Object>) this.restFacade.get("/service/applications/description");
				response = new DescribeStackResponse(objectsList);
			} else {
				final List<Object> objectsList =
						(List<Object>) this.restFacade.get(String.format(
								"/service/applications/%s/services/description", stackName));
				response = new DescribeStackResponse(objectsList);
			}
			return response;
		} catch (final ErrorStatusException e) {
			throw new RestClientException(e.getReasonCode(), e.getMessage(), null);
		}
	}

}
