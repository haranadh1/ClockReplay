<%--<%=request.getParameter("user_name")%><br>
<%=request.getParameter("pwd")%>--%>
<jsp:include page="viewUsers.jsp"/>
<jsp:include page="closebox.jsp?msg=User has been deleted."/>
