package org.jlab.dtm.business.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ejb.EJBAccessException;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Date;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class IncidentFacadeOperabilityReviewTest {

  private static final BigInteger INCIDENT_ID = BigInteger.ONE;
  private static final BigInteger GROUP_ID = BigInteger.TEN;

  private final EntityManager em = mock(EntityManager.class);

  private IncidentFacade facade(String username, String role) {
    IncidentFacade facade =
        new IncidentFacade() {
          @Override
          protected EntityManager getEntityManager() {
            return em;
          }
        };
    facade.context = AbstractFacadeTest.callerContext(username);
    facade.groupFacade = mock(ResponsibleGroupFacade.class);
    when(facade.groupFacade.find(GROUP_ID)).thenReturn(new Workgroup(GROUP_ID, "Group"));
    AbstractFacadeTest.setEffectiveRole(role);
    return facade;
  }

  /** An incident with no ID yet, so saving repairers does not clear rows in the database. */
  private Incident incident(String reviewedUsername, Date eventTimeUp) {
    Event event = new Event();
    event.setTimeUp(eventTimeUp);
    Incident incident = new Incident();
    incident.setEvent(event);
    incident.setReviewedUsername(reviewedUsername);
    incident.setResolution("original");
    when(em.find(Incident.class, INCIDENT_ID)).thenReturn(incident);
    return incident;
  }

  @AfterEach
  void clearAuditContext() {
    AbstractFacadeTest.TestAuditContext.install(null);
  }

  @Test
  void operatorSavesSolutionAndRepairers() throws UserFriendlyException {
    Incident incident = incident(null, null);

    facade("user1", "OPERATOR")
        .editOperabilityReview(INCIDENT_ID, "fixed", new BigInteger[] {GROUP_ID}, " ");

    assertEquals("fixed", incident.getResolution());
    assertNull(incident.getReviewedUsername());
    assertEquals(1, incident.getRepairedByList().size());
    assertEquals(GROUP_ID, incident.getRepairedByList().get(0).getRepairedBy().getWorkgroupId());
  }

  @Test
  void operatorCannotSetReviewer() {
    Incident incident = incident(null, null);

    assertThrows(
        UserFriendlyException.class,
        () ->
            facade("user1", "OPERATOR").editOperabilityReview(INCIDENT_ID, "fixed", null, "jdoe"));
    assertEquals("original", incident.getResolution());
  }

  @Test
  void operatorCannotEditReviewedIncident() {
    incident("jdoe", null);

    assertThrows(
        EJBAccessException.class,
        () -> facade("user1", "OPERATOR").editOperabilityReview(INCIDENT_ID, "fixed", null, null));
  }

  @Test
  void operatorCannotEditEventClosedLongAgo() {
    incident(null, new Date(System.currentTimeMillis() - Event.OPERATOR_EDIT_MILLIS - 60000));

    assertThrows(
        EJBAccessException.class,
        () -> facade("user1", "OPERATOR").editOperabilityReview(INCIDENT_ID, "fixed", null, null));
  }

  @Test
  void reviewerSetsReviewerOnReviewedIncident() throws UserFriendlyException {
    Incident incident = incident("jdoe", null);

    facade("jdoe", "REVIEWER").editOperabilityReview(INCIDENT_ID, "fixed", null, "user3");

    assertEquals("fixed", incident.getResolution());
    assertEquals("user3", incident.getReviewedUsername());
  }

  @Test
  void anonymousIsRefused() {
    incident(null, null);

    assertThrows(
        EJBAccessException.class,
        () ->
            facade("anonymous", "REVIEWER")
                .editOperabilityReview(INCIDENT_ID, "fixed", null, null));
  }

  @Test
  void missingIncidentIsReported() {
    assertThrows(
        UserFriendlyException.class,
        () ->
            facade("jdoe", "REVIEWER").editOperabilityReview(BigInteger.TWO, "fixed", null, null));
  }
}
