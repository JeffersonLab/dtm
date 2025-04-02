<%@tag description="App Style Tag" pageEncoding="UTF-8"%>
<script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/dtm.js"></script>
<script>
    jlab.logbookEnabled = ${settings.is('LOGBOOK_ENABLED') ? 'true' : 'false'};
</script>