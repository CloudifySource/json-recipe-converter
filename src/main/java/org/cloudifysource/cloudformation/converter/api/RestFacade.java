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
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.cloudifysource.dsl.rest.request.InstallApplicationRequest;
import org.cloudifysource.dsl.rest.response.InstallApplicationResponse;
import org.cloudifysource.dsl.rest.response.UninstallApplicationResponse;
import org.cloudifysource.restclient.ErrorStatusException;
import org.cloudifysource.restclient.GSRestClient;
import org.cloudifysource.restclient.RestClient;
import org.cloudifysource.restclient.RestException;
import org.cloudifysource.restclient.exceptions.RestClientException;

/**
 * Class which communicates with Cloudify REST Gateway.
 * 
 */
public class RestFacade {
    private final Logger logger = Logger.getLogger(RestFacade.class.getName());

    private static final String SERVICE_CONTROLLER_URL = "/service/";

    private RestClient newRestClient;
    private GSRestClient client;

    /**
     * Connect the Cloudify REST Gateway.
     * 
     * @param user
     *            The user if security is enabled.
     * @param password
     *            The password if security is enabled.
     * @param urlString
     *            The url to Cloudify REST Gateway.
     * @param gsVersion
     *            Cloudify version.
     * @throws RestException
     *             If a REST error occurs
     * @throws RestClientException
     *             If a REST error occurs
     * @throws MalformedURLException
     *             If the url is malformed.
     */
    public void connect(final String user, final String password, final String urlString, final String gsVersion)
            throws RestException, RestClientException,
            MalformedURLException {
        if (urlString == null) {
            throw new IllegalStateException("Cannot conncet to a cloudify manager (url=null).");
        }
        final URL url = new URL(urlString.startsWith("http") ? urlString : "http://" + urlString);

        final String version = StringUtils.isBlank(gsVersion)
                ? "2.6.1" : gsVersion; // PlatformVersion.getVersionNumber();
        final String versionNumber = version + "-Cloudify-ga"; // PlatformVersion.getVersion();
        logger.fine(String.format("Connected to REST using url=%s, user=%s, version=%s", urlString, user, version));
        this.client = new GSRestClient(user, password, url, versionNumber);
        this.newRestClient = new RestClient(url, user, password, version);
        client.get(SERVICE_CONTROLLER_URL + "testrest");
        if (StringUtils.isNotEmpty(user) || StringUtils.isNotEmpty(password)) {
            this.reconnect(user, password);
        }

    }

    private void reconnect(final String username, final String password) throws RestException {
        try {
            client.setCredentials(username, password);
            newRestClient.setCredentials(username, password);
            // test connection
            client.get(SERVICE_CONTROLLER_URL + "testlogin");
            newRestClient.connect();
        } catch (final Exception e) {
            throw new RestException(e);
        }
    }

    /**
     * uploads a file to repository using the pre-configured client.
     * 
     * @param client
     *            .
     * @param file
     *            .
     * @param displayer
     *            .
     * @return the returned upload key
     * @throws RestClientException .
     * @throws CLIException .
     */
    String uploadToRepo(final File file) throws RestClientException, ConverterException {
        if (file != null) {
            if (!file.isFile()) {
                throw new ConverterException(file.getAbsolutePath() + " is not a file or is missing");
            }
            return newRestClient.upload(null, file).getUploadKey();
        }
        return null;
    }

    /**
     * Calls installApplication.
     * 
     * @param stackName
     *            The applicationName.
     * @param installRequest
     *            The installRequest.
     * @return Cloudify InstallApplicationResponse.
     * @throws RestClientException
     *             If an error occurs.
     */
    public InstallApplicationResponse installApplication(final String stackName,
            final InstallApplicationRequest installRequest) throws RestClientException {
        return newRestClient.installApplication(stackName, installRequest);

    }

    /**
     * Calls uninstallApplication.
     * 
     * @param applicationName
     *            The application name to uninstall.
     * @param timeOutInMinutes
     *            The timeout in minutes.
     * @return The Cloudify UninstallApplicationResponse.
     * @throws RestClientException
     *             If an error occurs.
     */
    public UninstallApplicationResponse uninstallApplication(final String applicationName, final int timeOutInMinutes)
            throws RestClientException {
        return newRestClient.uninstallApplication(applicationName, timeOutInMinutes);
    }

    /**
     * Calls Cloudify REST Gateway using relativeUrl.
     * 
     * @param relativeUrl
     *            A REST request.
     * @return The REST response.
     * @throws ErrorStatusException
     *             If an error occurs.
     */
    public Object get(final String relativeUrl) throws ErrorStatusException {
        return client.get(relativeUrl);
    }
}
