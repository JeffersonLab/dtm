package org.jlab.dtm.business.session;

import java.io.StringReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.enumeration.IncidentEditType;
import org.jlab.dtm.persistence.model.LogReference;
import org.jlab.dtm.presentation.util.DtmFunctions;
import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.jlog.Reference;
import org.jlab.smoothness.business.exception.InternalException;
import org.jlab.smoothness.business.service.SettingsService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * @author ryans
 */
@Stateless
public class LogbookFacade extends AbstractFacade<Object> {

  private static final Logger LOGGER = Logger.getLogger(LogbookFacade.class.getName());

  public LogbookFacade() {
    super(Object.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return null;
  }

  @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
  @PermitAll
  public void silentlyCreateIncidentELog(Incident incident, IncidentEditType type) {
    if (SettingsService.cachedSettings.is("LOGBOOK_ENABLED")) {
      try {
        createIncidentELog(incident, type);
      } catch (InternalException e) {
        LOGGER.log(Level.SEVERE, "Unable to create close incident elog", e);
      }
    }
  }

  @PermitAll
  public long createIncidentELog(Incident incident, IncidentEditType type)
      throws InternalException {
    String username = checkAuthenticated();

    if (incident == null) {
      throw new InternalException("incident must not be null");
    }

    String logbookServerUrl = System.getenv("LOGBOOK_SERVER_URL");

    if (logbookServerUrl == null || logbookServerUrl.isEmpty()) {
      throw new InternalException("LOGBOOK_SERVER_URL env not set");
    }

    List<LogReference> referenceList = getLogReferences(incident.getIncidentId());
    String body = getIncidentELogHTMLBody(incident);

    String subject = "Downtime Incident " + type + ": " + incident.getTitle();

    String logbooks = SettingsService.cachedSettings.get("LOGBOOK_LIST");

    LogEntry entry = new LogEntry(subject, logbooks);

    entry.setBody(body, Body.ContentType.HTML);

    LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
    extension.setAuthor(username);

    Reference ref = new Reference("dtm", incident.getIncidentId().toString());
    entry.addReference(ref);

    for (LogReference reference : referenceList) {
      ref = new Reference("logbook", reference.getLognumber());
      entry.addReference(ref);
    }

    Properties config = Library.getConfiguration();

    config.setProperty("SUBMIT_URL", logbookServerUrl + "/incoming");

    long logId;

    try {
      logId = entry.submitNow();
    } catch (Exception e) {
      throw new InternalException("Unable to send elog", e);
    }

    return logId;
  }

  private String getIncidentELogHTMLBody(Incident incident) throws InternalException {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.append(
        "<table style=\"border: 1px solid black; width: 900px; table-layout: fixed;\"><thead><tr><th>ID</th><th style=\"width: 500px;\">Description</th><th>Period</th><th>Cause</th></tr></thead><tbody>\n");

    builder.append("<tr style=\"border: 1px solid black;\"><td style=\"vertical-align: top;\">");
    builder.append(incident.getIncidentId());
    builder.append(
        "</td><td style=\"vertical-align: top; word-wrap: break-word;\"><b>Title:</b><br/>");
    builder.append(IOUtil.escapeXml(incident.getTitle()));
    builder.append("<br/><br/><b>Summary:</b><br/>");
    builder.append(IOUtil.escapeXml(incident.getSummary()));
    builder.append("</td><td style=\"vertical-align: top;\"><b>Duration:</b><br/>");
    builder.append(DtmFunctions.millisToHumanReadable(incident.getElapsedMillis(), true));
    builder.append("<br/><br/><b>Time Down:</b><br/>");
    builder.append(formatter.format(incident.getTimeDown()));
    builder.append("<br/><br/><b>Time Up:</b><br/>");
    if (incident.getTimeUp() != null) {
      builder.append(formatter.format(incident.getTimeUp()));
    }
    builder.append("</td><td style=\"vertical-align: top;\">");
    builder.append("<b>Category:</b><br/>");
    builder.append(incident.getSystem().getCategory().getName());
    builder.append("<br/><br/><b>System:</b><br/>");
    builder.append(incident.getSystem().getName());
    if (incident.getComponent() != null) {
      builder.append("<br/><br/><b>Component:</b><br/>");
      builder.append(incident.getComponent().getName());
    }
    builder.append("</td>");
    builder.append("</tr>\n");
    builder.append("</tbody></table>\n");

    builder.append("</div><div>\n\n<b>See:</b> <a href=\"");
    builder.append(System.getenv("FRONTEND_SERVER_URL"));
    builder.append("/dtm/incidents/");
    builder.append(incident.getIncidentId());
    builder.append("\">Downtime Manager</a></div>\n");

    return builder.toString();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<LogReference> getLogReferences(BigInteger incidentId) throws InternalException {
    List<LogReference> referenceList = new ArrayList<>();

    Map<String, List<String>> params = new HashMap<>();
    params.put("ref_type", List.of("dtm"));
    params.put("ref_id", Collections.singletonList(incidentId.toString()));
    // TODO: We may be able to remove this cache buster in the future...
    params.put("cache_buster", List.of(String.valueOf(IOUtil.randInt(1, Integer.MAX_VALUE))));
    String queryString = ServletUtil.buildQueryString(params, "UTF-8");

    String logbookServerUrl = System.getenv("LOGBOOK_SERVER_URL");

    if (logbookServerUrl == null || logbookServerUrl.isEmpty()) {
      throw new InternalException("LOGBOOK_SERVER_URL env not set");
    }

    try {
      String query = logbookServerUrl + "/references/json" + queryString;
      String jsonStr = IOUtil.doHtmlGet(query, 2000, 2000);

      LOGGER.log(Level.FINEST, "query: {0}", query);
      LOGGER.log(Level.FINEST, "jsonStr: {0}", jsonStr);

      JsonReader reader = Json.createReader(new StringReader(jsonStr));
      JsonObject obj = reader.readObject();

      String value = obj.getString("stat");
      if ("ok".equals(value)) {
        JsonArray arr = obj.getJsonArray("data");
        if (arr != null) {
          for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.getJsonObject(i);
            String lognumber = o.getString("lognumber");
            String title = o.getString("title");
            referenceList.add(new LogReference(lognumber, title));
          }
        }
      }

    } catch (Exception e) {
      throw new InternalException("Unable to obtain log numbers", e);
    }

    return referenceList;
  }
}
