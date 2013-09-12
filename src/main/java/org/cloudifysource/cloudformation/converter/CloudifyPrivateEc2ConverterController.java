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
package org.cloudifysource.cloudformation.converter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cloudifysource.cloudformation.converter.api.CloudifyPrivateEc2Converter;
import org.cloudifysource.cloudformation.converter.api.TemplateParserException;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackResponse;
import org.cloudifysource.cloudformation.converter.api.model.DeleteStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackResponse;
import org.cloudifysource.restclient.RestException;
import org.cloudifysource.restclient.exceptions.RestClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST Controller.
 * 
 * @author victor keophila
 * 
 */
@Controller
public class CloudifyPrivateEc2ConverterController {

	private final Logger logger = Logger.getLogger(CloudifyPrivateEc2ConverterController.class.getName());

	private CloudifyPrivateEc2Converter converterApi = new CloudifyPrivateEc2Converter();

	@Value("#{systemProperties['cloudify.user']}")
	private String user;
	@Value("#{systemProperties['cloudify.password']}")
	private String password;
	@Value("#{systemProperties['cloudify.url']}")
	private String url;
	@Value("#{systemProperties['cloudify.version']}")
	private String version;

	public CloudifyPrivateEc2ConverterController() {
		logger.info(String.format("user=%s url=%s", user, url));
	}

	/**
	 * Test method.
	 * 
	 * @return an okay status.
	 */
	@ResponseBody
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Object test() {
		final Map<String, String> response = new HashMap<String, String>();
		response.put("status", "okay");
		return response;
	}

	/**
	 * Connect to a Cloudify REST gateway.
	 * 
	 * @param user
	 *            The user to connect with (optional).
	 * @param password
	 *            The password (optional).
	 * @param url
	 *            The URL of the Cloudify REST gateway.
	 * @param version
	 *            Cloudify Version (i.e: 2.6.1)
	 * @return An Okay status
	 * @throws MalformedURLException
	 *             If the given url is incorrect.
	 * @throws RestClientException
	 *             If a problem occurs
	 * @throws RestException
	 *             If a problem occurs
	 */
	@ResponseBody
	@RequestMapping(value = "/connect", method = RequestMethod.GET)
	public Object connect(final String user, final String password, final String url, final String version)
			throws MalformedURLException, RestClientException, RestException {
		if (StringUtils.isEmpty(url) && StringUtils.isEmpty(this.url)) {
			throw new RestClientException("400", "Missing url parameter", "");
		}

		logger.info("Connect with parameters: user=" + user + " to url=" + url);

		this.initConnectionParameters(user, password, url, version);
		converterApi.connect(this.user, this.password, this.url, this.version);

		final Map<String, String> response = new HashMap<String, String>();
		response.put("status", "connected");
		return response;
	}

	private void initConnectionParameters(final String user, final String password, final String url,
			final String version) {
		if (StringUtils.isNotEmpty(user)) {
			this.user = user;
		}
		if (StringUtils.isNotEmpty(password)) {
			this.password = password;
		}
		if (StringUtils.isNotEmpty(url)) {
			this.url = url;
		}
		if (StringUtils.isNotEmpty(version)) {
			this.version = version;
		}
	}

	/**
	 * Redirect the create stack view.
	 * 
	 * @param model
	 *            Object model.
	 * @return Redirect to the CreateStack form.
	 */
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String createStackForm(final ModelMap model) {
		model.addAttribute("createStackRequest", new CreateStackRequest());
		return "createStack";
	}

	/**
	 * Create stack request.
	 * 
	 * @param request
	 *            The create request.
	 * @param result
	 *            The spring binding result.
	 * @return The deployed stack id.
	 * @throws Exception
	 *             If a problem occurs.
	 */
	@ResponseBody
	@RequestMapping(value = "/createStack", method = { RequestMethod.GET, RequestMethod.POST })
	public Object createStack(@Valid final CreateStackRequest request, final BindingResult result) throws Exception {

		if (result.hasErrors()) {
			throw new PrivateEc2RestValidationException(result);
		}

		logger.fine("CreateStack request:" + request);
		this.connect();
		final CreateStackResponse createStackResponse = converterApi.createStack(request);
		final Map<String, String> response = new HashMap<String, String>();
		response.put("StackID", createStackResponse.getStackID());
		return response;
	}

	/**
	 * Delete stack request.
	 * 
	 * @param request
	 *            The delete request.
	 * @param result
	 *            The spring binding result.
	 * @throws Exception
	 *             If a problem occurs.
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteStack", method = { RequestMethod.GET, RequestMethod.POST })
	public void deleteStack(@Valid final DeleteStackRequest request, final BindingResult result) throws Exception {
		if (result.hasErrors()) {
			throw new PrivateEc2RestValidationException(result);
		}
		logger.fine("DeleteStack request:" + request);
		this.connect();
		converterApi.deleteStack(request);
	}

	/**
	 * Describe stack request.
	 * 
	 * @param request
	 *            The describe request.
	 * @return Applications status coming from the TEST request of the management.
	 * @throws Exception
	 *             If a problem occurs.
	 */
	@ResponseBody
	@RequestMapping(value = "/describeStack", method = { RequestMethod.GET, RequestMethod.POST })
	public Object describeStack(final DescribeStackRequest request) throws Exception {
		logger.fine("DescribeStack request:" + request);
		this.connect();
		final DescribeStackResponse response = converterApi.describeStack(request);
		return response.getApplications();
	}

	private void connect() throws RestException {
		if (!converterApi.isConnected()) {
			try {
				converterApi.connect(user, password, url, version);
			} catch (Exception e) {
				throw new RestException("Unable to connect to a Cloudify REST gateway.", e);
			}
		}
	}

	/**
	 * Handle PrivateEc2RestValidationException.
	 * 
	 * @param response
	 *            The http servlet response
	 * @param e
	 *            The handled exception.
	 * @throws IOException
	 *             When a problem occrus.
	 */
	@ExceptionHandler(PrivateEc2RestValidationException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void handleValidationException(final HttpServletResponse response, final PrivateEc2RestValidationException e)
			throws IOException {
		final StringBuilder sb = new StringBuilder("Missing parameters: ");
		for (FieldError f : e.getBindingResult().getFieldErrors()) {
			sb.append(f.getField()).append(", ");
		}
		sb.setLength(sb.length() - 2);
		this.returnErrorMessage(response, sb.toString());
	}

	/**
	 * Handle MalformedURLException.
	 * 
	 * @param response
	 *            The http servlet response
	 * @param e
	 *            The handled exception.
	 * @throws IOException
	 *             When a problem occurs.
	 */
	@ExceptionHandler(MalformedURLException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public void handleMalformedURLException(final HttpServletResponse response, final Exception e) throws IOException {
		logger.log(Level.SEVERE, e.getMessage(), e);
		this.returnErrorMessage(response, e.getMessage());
	}

	/**
	 * Handle generic Exception.class.
	 * 
	 * @param response
	 *            The http servlet response
	 * @param e
	 *            The handled exception
	 * @throws IOException
	 *             When a problem occurs.
	 */
	@ExceptionHandler({ Exception.class })
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleRestException(final HttpServletResponse response, final Exception e) throws IOException {
		logger.log(Level.SEVERE, e.getMessage(), e);
		if (e instanceof RestClientException) {
			final RestClientException e2 = (RestClientException) e;
			this.returnErrorMessage(response,
					e2.getMessageFormattedText() == null ? e2.getMessageCode() : e2.getMessageFormattedText());
		} else if (e instanceof TemplateParserException) {
			final TemplateParserException e2 = (TemplateParserException) e;
			this.returnErrorMessage(response, "Couldn't parse the template: " + e2.getMessage());
		} else {
			this.returnErrorMessage(response, e.getMessage());
		}

	}

	private void returnErrorMessage(final HttpServletResponse response, final String message) throws IOException {
		final String errorMessage = String.format("{\"status\":\"error\", \"error\":\"%s\"}", message);
		final ServletOutputStream outputStream = response.getOutputStream();
		final byte[] messageBytes = errorMessage.getBytes();
		outputStream.write(messageBytes);
	}

}
