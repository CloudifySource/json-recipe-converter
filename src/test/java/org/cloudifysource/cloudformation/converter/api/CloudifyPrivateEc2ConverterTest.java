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
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.CreateStackResponse;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackRequest;
import org.cloudifysource.cloudformation.converter.api.model.DescribeStackResponse;
import org.cloudifysource.dsl.rest.request.InstallApplicationRequest;
import org.cloudifysource.dsl.rest.response.ApplicationDescription;
import org.cloudifysource.dsl.rest.response.InstallApplicationResponse;
import org.cloudifysource.restclient.ErrorStatusException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:description-context.xml" })
public class CloudifyPrivateEc2ConverterTest {

    @Resource(name = "descriptionResults")
    private Map<String, Object> descriptionResults;

    @InjectMocks
    private CloudifyPrivateEc2Converter converter = new CloudifyPrivateEc2Converter();

    @Mock
    private RestFacade restFacade;

    @Before
    public void initMocks() throws ErrorStatusException {
        MockitoAnnotations.initMocks(this);
        String[] keys = { "/service/applications/description",
                "/service/applications/simpleApplication/services/description",
                "/service/applications/extendedApplication/services/description" };
        for (String key : keys) {
            Mockito.when(restFacade.get(key)).thenReturn(descriptionResults.get(key));
        }
    }

    @Test
    public void testCreateStackWithTemplateBody() throws Exception {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setStackName("stackName");
        createStackRequest.setTemplateBody(FileUtils.readFileToString(
                new File("./src/test/resources/samples/templateBodyApplication/templateBodyApplication-dsl.template")));
        createStackRequest.setTimeoutInMinutes(60);
        this.doCreateStackRequest(createStackRequest);
    }

    @Test
    public void testCreateStackWithTemplateFile() throws Exception {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setStackName("stackName");
        createStackRequest.setTemplateFile(new File(
                "./src/test/resources/samples/extendedApplication/extendedApplication-dsl.template"));
        createStackRequest.setTimeoutInMinutes(60);
        this.doCreateStackRequest(createStackRequest);
    }

    @Test(expected = ConverterException.class)
    public void testCreateStackWithTemplateUrlSimpleApplication() throws Exception {
        WireMockServer wireMockServer = new WireMockServer(8089);
        try {
            wireMockServer.start();
            WireMock.configureFor("localhost", 8089);

            WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/simpleApplication")).willReturn(WireMock.aResponse()
                    .withBodyFile("./src/test/resources/samples/simpleApplication/simpleApplication-dsl.template")));

            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setStackName("stackName");
            createStackRequest.setTemplateURL("http://localhost:8089/simpleApplication");
            createStackRequest.setTimeoutInMinutes(60);
            this.doCreateStackRequest(createStackRequest);
        } finally {
            wireMockServer.stop();
        }
    }

    @Test
    public void testCreateStackWithTemplateUrlFile() throws Exception {
        WireMockServer wireMockServer = new WireMockServer(8089);
        try {
            wireMockServer.start();
            WireMock.configureFor("localhost", 8089);

            WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/templateBodyApplication-dsl.template")).willReturn(
                    WireMock.aResponse()
                            .withBodyFile("../samples/templateBodyApplication/templateBodyApplication-dsl.template")));

            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setStackName("stackName");
            createStackRequest.setTemplateURL("http://localhost:8089/templateBodyApplication-dsl.template");
            createStackRequest.setTimeoutInMinutes(60);
            this.doCreateStackRequest(createStackRequest);
        } finally {
            wireMockServer.stop();
        }
    }

    @Test
    public void testCreateStackWithTemplateUrlZip() throws Exception {
        WireMockServer wireMockServer = new WireMockServer(8089);
        try {
            wireMockServer.start();
            WireMock.configureFor("localhost", 8089);

            WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/templateUrlApplication.zip")).willReturn(
                    WireMock.aResponse()
                            .withBodyFile("../samples/templateUrlApplication.zip")));

            CreateStackRequest createStackRequest = new CreateStackRequest();
            createStackRequest.setStackName("stackName");
            createStackRequest.setTemplateURL("http://localhost:8089/templateUrlApplication.zip");
            createStackRequest.setTimeoutInMinutes(60);
            this.doCreateStackRequest(createStackRequest);
        } finally {
            wireMockServer.stop();
        }
    }

    private void doCreateStackRequest(final CreateStackRequest createStackRequest) throws Exception {
        InstallApplicationResponse response = new InstallApplicationResponse();
        String deploymentID = "someDeploymentID";
        response.setDeploymentID(deploymentID);
        Mockito.when(
                restFacade.installApplication(Mockito.eq(createStackRequest.getStackName()),
                        Mockito.any(InstallApplicationRequest.class))).thenReturn(
                response);
        Mockito.when(restFacade.uploadToRepo(Mockito.any(File.class))).thenReturn("applicationKey",
                "cloudConfigurationKey");

        CreateStackResponse createStack = converter.createStack(createStackRequest);

        Assert.assertEquals(deploymentID, createStack.getStackID());
        Mockito.verify(restFacade, Mockito.times(2)).uploadToRepo(Mockito.argThat(new ArgumentMatcher<File>() {
            @Override
            public boolean matches(final Object argument) {
                if (argument instanceof File) {
                    File file = (File) argument;
                    Assert.assertEquals("zip", FilenameUtils.getExtension(file.getName()));
                    return true;
                }
                return false;
            }
        }));
        Mockito.verify(restFacade, Mockito.times(1)).installApplication(Mockito.eq(createStackRequest.getStackName()),
                Mockito.argThat(new ArgumentMatcher<InstallApplicationRequest>() {
                    @Override
                    public boolean matches(final Object argument) {
                        if (argument instanceof InstallApplicationRequest) {
                            InstallApplicationRequest request = (InstallApplicationRequest) argument;
                            Assert.assertEquals(createStackRequest.getStackName(), request.getApplicationName());
                            Assert.assertEquals("applicationKey", request.getApplcationFileUploadKey());
                            Assert.assertEquals("cloudConfigurationKey", request.getCloudConfigurationUploadKey());
                            Assert.assertEquals(60L * 60L * 1000L, request.getTimeoutInMillis());
                            return true;
                        }
                        return false;
                    }
                }));
    }

    @Test
    public void testDescribeStackSimpleApplication() throws Exception {
        DescribeStackRequest describeStackRequest = new DescribeStackRequest("simpleApplication");
        DescribeStackResponse describeStack = converter.describeStack(describeStackRequest);
        Assert.assertNotNull(describeStack.getApplications());
        Assert.assertFalse(describeStack.getApplications().isEmpty());
        Assert.assertEquals(1, describeStack.getApplications().size());
        Object object = describeStack.getApplications().get(0);
        ApplicationDescription application = this.getApplicationDescription(object);
        Assert.assertEquals("simpleApplication", application.getApplicationName());
    }

    @Test
    public void testDescribeStackAllApplications() throws Exception {
        DescribeStackRequest describeStackRequest = new DescribeStackRequest();
        DescribeStackResponse describeStack = converter.describeStack(describeStackRequest);
        Assert.assertNotNull(describeStack.getApplications());
        Assert.assertFalse(describeStack.getApplications().isEmpty());
        Assert.assertEquals(2, describeStack.getApplications().size());
        for (Object object : describeStack.getApplications()) {
            Assert.assertTrue(object.toString().contains("simpleApplication")
                    || object.toString().contains("extendedApplication"));
        }
    }

    private ApplicationDescription getApplicationDescription(final Object object) {
        final ObjectMapper map = new ObjectMapper();
        final ApplicationDescription applicationDescription = map.convertValue(object, ApplicationDescription.class);
        return applicationDescription;
    }

}
