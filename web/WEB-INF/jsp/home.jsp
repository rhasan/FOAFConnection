<%@ page import="java.util.Iterator" %>
<%@ page import="website.web.model.Person" %>
<jsp:useBean id="person" scope="request" class="website.web.model.Person" />
<jsp:useBean id="friendsList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="errorMessage" scope="request" class="java.lang.String" />

<html>
   <head>
      <title>FOAF Connection</title>
   </head>
   <body>
      <h1>FOAF Connection</h1>
${errorMessage}
	<form method="post">
	   <input type="text" name="uri" value="${person.uri}">
	   
	   <input type="submit" name="submit" value="Submit" />
	</form>
	<%@ include file="person.jsp" %>

	<ul>
		<% 
      Iterator it = friendsList.iterator();
      while (it.hasNext())
      {
         Person personItem = (Person) it.next();
   %>
		<li><%=personItem.getName()%>
		</li>
	<% } %>
	</ul>
</body>
</html>
