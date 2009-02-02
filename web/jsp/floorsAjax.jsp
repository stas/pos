<%--
    Openbravo POS is a point of sales application designed for touch screens.
    Copyright (C) 2007 Openbravo, S.L.
    http://sourceforge.net/projects/openbravopos

    This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
   --%>
<%-- 
    Document   : floorsAjax
    Created on : Nov 19, 2008, 8:46:14 AM
    Author     : jaroslawwozniak
--%>
<%@page pageEncoding="UTF-8"
        import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<span id="ble">
    <logic:present name="places">
        <form action="showPlace.do" method="get">
            <input type="hidden" name="floorId" value="javascript:getFloorId();" />
            <% ArrayList places = (ArrayList) request.getSession().getAttribute("places");%>
            <c:forEach var="place" items="${places}">
                <c:set var="var" value="false" />
                <c:forEach var="busy" items="${busy}">
                    <c:if test="${place.id == busy.id}">
                        <button name="id" value="${place.id}" type="submit" class="busy">${place.name}</button>
                        <c:set var="var" value="true" />
                    </c:if>
                </c:forEach>
                <c:if test="${var == false}">
                    <button name="id" value="${place.id}" type="submit" class="floor">${place.name}</button>
                </c:if>
            </c:forEach>
        </form>
    </logic:present>
</span>