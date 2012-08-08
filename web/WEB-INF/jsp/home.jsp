<jsp:useBean id="person" scope="request" class="website.web.model.Person" />
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
   </body>
</html>
