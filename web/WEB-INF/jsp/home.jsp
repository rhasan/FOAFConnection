<%@page import="website.web.model.RelationJustification"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="website.web.model.Person" %>
<jsp:useBean id="person" scope="request" class="website.web.model.Person" />
<jsp:useBean id="friendsList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="couldBeIntroList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="couldBeIntroRelList" scope="request" class="java.util.ArrayList" />
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


	<div class="friends-block">
		<%
			if(friendsList.size()>0) {
		%>
			<h2>Knows</h2>
		<%
			}
		%>
	
	
		<ul>
			<%
				Iterator it = friendsList.iterator();
				
				while (it.hasNext()) {
					Person personItem = (Person) it.next();
			%>
			<li>
				<ul>
					<li><%=personItem.getUri() %></li>
					<%if(personItem.getName() != null) { %>
						<li><%=personItem.getName()%></li>
					<%} %>
				</ul>
			</li>
			<%
				}
			%>
		</ul>
	</div>

	<div class="couldbe-block">
		
		<%
			if(couldBeIntroList.size()>0) {
		%>
			<h2>Could be introduced to</h2>
		<%
			}
		%>
	
	
		<ul>
			<%
				Iterator it1 = couldBeIntroRelList.iterator();
				while (it1.hasNext()) {
					
					RelationJustification rj = (RelationJustification)it1.next();
					Person personItem = rj.getPerson();
					String justUri = rj.getJustificationUri();
			%>
			<li>
				<ul>
					<li><%=personItem.getUri() %></li>
					<%if(personItem.getName() != null) { %>
						<li><%=personItem.getName()%></li>
					<%} %>
					<li><a href="<%=justUri%>" target="_blank">Why</a> </li>
				</ul>
			</li>
			<%
				}
			%>
		</ul>
	</div>
</body>
</html>
