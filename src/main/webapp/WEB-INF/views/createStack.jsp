<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<html>
<head>
	<meta charset="utf-8" />
	<title>CreateStack</title> 
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
	<script src="${pageContext.request.contextPath}/resources/lib/bootstrap/js/bootstrap.min.js"></script>
	<script src="${pageContext.request.contextPath}/resources/js/jquery-1.9.1.min.js"></script>
	<script>
		$('.dropdown-toggle').dropdown();
	</script>
	<h3>Create Stack Form</h3>
	<form:form  id="createStack" class="form-horizontal" action="${pageContext.request.contextPath}/createStack" method="post" commandName="createStackRequest" >
		<div class="control-group">
			<label class="control-label" for="stackName">Stack name :</label>
			<div class="controls">
				<form:input type="text" id="stackName" path="stackName" class="input-xxlarge" required="required" placeHolder="Stack name" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="timeoutInMinutes">Timeout (min) :</label>
			<div class="controls">
				<form:input type="text" id="timeoutInMinutes" path="timeoutInMinutes" class="input-xxlarge" placeHolder="15" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="templateURL">Template URL :</label>
			<div class="controls">
				<form:input type="text" id="templateURL" path="templateURL" class="input-xxlarge" placeHolder="URL of the template" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="templateBody">Template body :</label>
			<div class="controls">
				<form:textarea id="templateBody" path="templateBody" class="input-xxlarge" cols="240" rows="20"></form:textarea>
			</div>
		</div>
		<div class="control-group">
			<div class="controls">
				<button type="submit">Create Stack</button>
			</div>
		</div>
	</form:form>
</body>
</html>