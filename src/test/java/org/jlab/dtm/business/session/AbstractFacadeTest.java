package org.jlab.dtm.business.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.SessionContext;
import jakarta.persistence.EntityManager;
import java.security.Principal;
import java.util.Date;
import org.jlab.smoothness.presentation.filter.AuditContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AbstractFacadeTest {

  static class TestFacade extends AbstractFacade<Object> {
    TestFacade() {
      super(Object.class);
    }

    @Override
    protected EntityManager getEntityManager() {
      return null;
    }
  }

  static SessionContext callerContext(String username) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    SessionContext context = mock(SessionContext.class);
    when(context.getCallerPrincipal()).thenReturn(principal);
    return context;
  }

  /** AuditContext is normally set by a Smoothness filter for each request. */
  static class TestAuditContext extends AuditContext {
    static void install(AuditContext context) {
      setCurrentInstance(context);
    }
  }

  static void setEffectiveRole(String role) {
    AuditContext context = new AuditContext();
    context.putExtra("EffectiveRole", role);
    TestAuditContext.install(context);
  }

  @AfterEach
  void clearAuditContext() {
    TestAuditContext.install(null);
  }

  @Test
  void reviewerPasses() {
    TestFacade facade = new TestFacade();
    facade.context = callerContext("jdoe");
    setEffectiveRole("REVIEWER");

    assertDoesNotThrow(facade::checkReviewer);
  }

  @Test
  void operatorIsRefused() {
    TestFacade facade = new TestFacade();
    facade.context = callerContext("user1");
    setEffectiveRole("OPERATOR");

    assertThrows(EJBAccessException.class, facade::checkReviewer);
  }

  @Test
  void anonymousIsRefusedEvenWithReviewerRole() {
    TestFacade facade = new TestFacade();
    facade.context = callerContext("anonymous");
    setEffectiveRole("REVIEWER");

    assertThrows(EJBAccessException.class, facade::checkReviewer);
  }

  @Test
  void monthlySavesRequireReviewer() {
    setEffectiveRole("OPERATOR");

    MonthlyNoteFacade noteFacade = new MonthlyNoteFacade();
    noteFacade.context = callerContext("user1");
    assertThrows(
        EJBAccessException.class, () -> noteFacade.save(new Date(), "note", 90f, 95f, 92f));

    CategoryMonthlyGoalFacade goalFacade = new CategoryMonthlyGoalFacade();
    goalFacade.context = callerContext("user1");
    assertThrows(
        EJBAccessException.class,
        () -> goalFacade.save(new Date(), new Long[] {1L}, new Float[] {90f}));
  }

  @Test
  void recordingAnUploadedRarRequiresLogin() {
    setEffectiveRole("OPERATOR");

    IncidentFacade incidentFacade = new IncidentFacade();
    incidentFacade.context = callerContext("anonymous");

    assertThrows(
        EJBAccessException.class,
        () -> incidentFacade.rarUploaded(java.math.BigInteger.ONE, ".pdf"));
  }
}
