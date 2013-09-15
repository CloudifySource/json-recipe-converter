<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ page isErrorPage="true" %>
<html>
<head>
<meta charset="utf-8" />
<title>${title}</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/lib/bootstrap/css/bootstrap.css"
	rel="stylesheet" />
</head>
<body>
${response.getStatus()}
	<div class="well well-large">${message}</div>
</body>
</html>