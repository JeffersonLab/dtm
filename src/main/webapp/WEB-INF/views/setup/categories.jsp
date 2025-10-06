<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<jsp:useBean id="categoryList" scope="request" type="java.util.List"/>
<jsp:useBean id="typeList" scope="request" type="java.util.List"/>
<c:set var="title" value="Categories"/>
<s:setup-page title="${title}">
    <jsp:attribute name="stylesheets">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-plugins/jstree/3.3.8/themes/classic/style.min.css"/>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/jstree/3.3.8/themes/classic/style.min.css"/>
            </c:otherwise>
        </c:choose>
        <style>
            #root-table ul {
                margin: 0;
                padding-left: 1em;
            }
            .small-icon {
                display: inline-block;
                height: 16px;
                width: 16px;
                vertical-align: top;
            }
            .small-icon.CATEGORY {
                background: url("../resources/img/category.png") 0 0;
            }

            .small-icon.SYSTEM {
                background: url("../resources/img/system.png") 0 0;
            }

            #tree-widget {
                width: 100%;
                display: table;
                /*border-top: 1px solid black;*/
            }

            #tree-nodes {
                display: table-cell;
                vertical-align: top;
            }

            #tree-keys {
                width: 200px;
                padding: 0 15px;
                display: table-cell;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="${cdnContextPath}/jquery-plugins/jstree/3.3.8/jstree.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jstree/3.3.8/jstree.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script>
            $(function () {
                $("#tree").jstree({
                    core: {
                        multiple: false,
                        themes: {
                            theme: "classic",
                            dots: true,
                            icons: true
                        }
                    },
                    state: {key: 'setup'},
                    types: {
                        "#": {
                            "max_children": 1
                        },
                        "CATEGORY": {
                            "icon": "../resources/img/category.png"
                        },
                        "SYSTEM": {
                            "icon": "../resources/img/system.png"
                        },
                        "COMPONENT": {
                            "icon": "../resources/img/component.png"
                        },
                        "GROUP": {
                            "icon": "../resources/img/group.png"

                        },
                        "default": {
                            "icon": "../resources/img/file.png"
                        }
                    },
                    plugins: ["types", "state", "conditionalselect"],
                    conditionalselect: function (node) {
                        return false;
                    }
                });
            });

            // conditional select
            (function ($, undefined) {
                "use strict";
                $.jstree.defaults.conditionalselect = function () {
                    return true;
                };
                $.jstree.plugins.conditionalselect = function (options, parent) {
// own function
                    this.activate_node = function (obj, e) {
                        if (this.settings.conditionalselect.call(this, this.get_node(obj))) {
                            parent.activate_node.call(this, obj, e);
                        }
                    };
                };
            })(jQuery);
        </script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <h3>All Categories/Systems</h3>
            <div id="tree-widget">
                <div id="tree-nodes">
                    <div id="tree">
                        <ul class="category-list">
                            <c:set var="parent" value="${root}" scope="request"/>
                            <jsp:include page="/WEB-INF/includes/category-tree-node.jsp"/>
                        </ul>
                    </div>
                </div>
                <div id="tree-keys">
                    <fieldset>
                        <legend>Node Key</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <span class="small-icon CATEGORY"></span>
                                </div>
                                <div class="li-value">
                                    Category
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <span class="small-icon SYSTEM"></span>
                                </div>
                                <div class="li-value">
                                    System
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                </div>
            </div>
            <h3>Alpha Categories</h3>
            <p>Categories are hierarchical, but many reports benefit from use of a subset of "alpha categories", which are a carefully chosen set that still captures all downtime, but is "flat", i.e. no alpha category is a subset of another alpha category, and therefore the alpha set is smaller than the full set.</p>
            <p>It's up to the admins to ensure they don't define alpha categories such that one duplicates downtime by another or leaves out a branch that would miss downtime.  Including the top-level category, for example, would immediately disqualify all other categories.  Any category that has one or more systems as a sibling is not a candidate for an alpha category (as the parent category would be needed to capture the downtime in the systems).</p>
            <table id="alpha-table" class="data-table stripped-table">
                <thead>
                <tr>
                    <th>Category Name</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="category" items="${categoryList}">
                    <tr>
                        <td><c:out value="${category.name}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <h3>Type Roots</h3>
            <p>Each Event Type has one or more root categories assigned that limits which components operators can select.</p>
            <table id="root-table" class="data-table stripped-table">
                <thead>
                <tr>
                    <th>Event Type</th>
                    <th>Category</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="type" items="${typeList}">
                    <tr>
                        <td><c:out value="${type.name}"/></td>
                        <td>
                            <ul>
                            <c:forEach items="${type.categoryList}" var="category">
                                <li><c:out value="${category.name}"/></li>
                            </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <h3>Beam Transport</h3>
            The category named "Beam Transport" is special.  It should be configured as an Alpha Category.  The NPES schedule provides an allowance for beam transport, often referred to as tuning.    The Joule report contains a metric named Tuning and Restore, of which Beam Transport indirectly makes up a portion.
        </section>
    </jsp:body>         
</s:setup-page>