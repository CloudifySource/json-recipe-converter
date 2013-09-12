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

import java.net.MalformedURLException;

import org.cloudifysource.cloudformation.converter.api.model.CreateStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackResponse;
import org.cloudifysource.cloudformation.converter.api.model.DeleteStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackResponse;
import org.cloudifysource.restclient.RestException;
import org.cloudifysource.restclient.exceptions.RestClientException;

/**
 * API contract.
 * 
 * @author victor
 * 
 */
public interface ConverterAPI {

	/**
	 * Connect to Cloudify REST Gateway.
	 * 
	 * @param user
	 *            The user if security is enabled.
	 * @param password
	 *            The password if security is enabled.
	 * @param urlString
	 *            Cloudify REST URL.
	 * @throws RestClientException
	 *             If it couldn't connect.
	 * @throws RestException
	 *             If it couldn't connect.
	 * @throws MalformedURLException
	 *             If the given URL is wrong.
	 */
	void connect(String user, String password, String urlString) throws RestClientException, MalformedURLException,
			RestException;

	/**
	 * Connect to Cloudify REST Gateway.
	 * 
	 * @param user
	 *            The user if security is enabled.
	 * @param password
	 *            The password if security is enabled.
	 * @param urlString
	 *            Cloudify REST URL.
	 * @param version
	 *            Cloudify version.
	 * @throws RestClientException
	 *             If it couldn't connect.
	 * @throws RestException
	 *             If it couldn't connect.
	 * @throws MalformedURLException
	 *             If the given URL is wrong.
	 */
	void connect(String user, String password, String urlString, String version) throws RestClientException,
			MalformedURLException, RestException;

	/**
	 * Create application from a JSON template. It will convert the template into Cloudify groovy template and calls
	 * Cloudify REST installApplication request.
	 * 
	 * @param createStackRequest
	 *            The request. The <code>stackName</code> and a <code>template</code> parameter must be provided.
	 * @return Response with the deployment id.
	 * 
	 * @throws Exception
	 *             If an error occurs when calling Cloudify REST api.
	 */
	CreateStackResponse createStack(CreateStackRequest createStackRequest) throws Exception;

	/**
	 * Delete an application.
	 * 
	 * @param deleteStackRequest
	 *            The request.
	 * @throws RestClientException
	 *             If an error occurs.
	 */
	void deleteStack(DeleteStackRequest deleteStackRequest) throws RestClientException;

	/**
	 * Get a description of a specific or all applications.
	 * 
	 * @param describeStackRequest
	 *            The request.
	 * @return The description of a specific or all applications.
	 * @throws RestClientException
	 *             If an error occurs.
	 */
	DescribeStackResponse describeStack(DescribeStackRequest describeStackRequest) throws RestClientException;
}
