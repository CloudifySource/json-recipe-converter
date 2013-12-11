# json-recipe-converter #

JSON recipe converter has to be used with privateEc2 driver. 

The converter is a REST API that parse and deploy recipes from CloudFormation like JSON template.

## Requirements ##
* Cloudify 2.7.0 with a bootstrapped manager with privateEc2 driver.
* Apache Tomcat 7.x.
	* set CATALINA_HOME in the variable environment.
* Maven 3.x

## Quick Testing ##

* Download the project.
* Package the project with maven: `mvn package`
* Run cargo to deploy the war: `mvn cargo:run -Dcloudify.url=http://cloudify_manager_host:8100`
* Test that application has been successfully deployed: `curl http://localhost:8080/json-recipe-converter-1.0-SNAPSHOT/test`
* You have sample DSLs in `./src/test/resources/samples`. I suggest you upload the `templateUrlApplication.zip` in some apache server and use it to install application.
* To install application : `curl "http://localhost:8080/json-recipe-converter-1.0-SNAPSHOT/createStack?stackName=myApplication&templateURL=http://url_path_to/templateUrlApplication.zip"`
* Get an application description: `curl http://localhost:8080/json-recipe-converter-1.0-SNAPSHOT/describeStack`
* Delete application : `curl http://localhost:8080/json-recipe-converter-1.0-SNAPSHOT/deleteStack?stackName=myApplication`

You can also use the from page to create stack: `http://localhost:8080/json-recipe-converter-1.0-SNAPSHOT/create`

## The API ##

### createStack ###

**Description**:

Install an application using JSON templates.

**HTTP Method**: 

`/createStack`

**Parameters**:

* `stackName`: Application's name.
* `templateURL`*: URL to the DSL files.
* `templateBody`*: DSL content.
* `timeoutInMinutes`: Timeout in minutes (**optional**).

(*) use `templateURL` or `templateBody` but not both in the same time. 

----------

#### deleteStack ####

**Description**:

Uninstall an application.

**HTTP Method**: 

`/deleteStack`

**Parameters**:

* **stackName**: Application's name.

----------

#### describeStack ####

**Description**:

Get application(s) description.

**HTTP Method**: 

`/describeStack`

**Parameters**:

* **stackName**: Application's name (**optional**).
