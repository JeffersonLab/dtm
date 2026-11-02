package org.jlab.dtm.business.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.view.User;
import org.junit.jupiter.api.Test;

class ScheduledEmailerTest {

  @Test
  void expertGetsUsernameAtDomain() throws UserFriendlyException {
    assertEquals(
        "jdoe@example.com",
        ScheduledEmailer.expertRecipientCsv("jdoe", false, "@example.com", null));
  }

  @Test
  void missingDomainSendsNothing() {
    assertThrows(
        UserFriendlyException.class,
        () -> ScheduledEmailer.expertRecipientCsv("jdoe", false, " ", null));
  }

  @Test
  void testingSendsToTestersOnly() throws UserFriendlyException {
    List<User> testers =
        List.of(
            new User("user3", "User", "Three", "user3@example.com"),
            new User("user4", "User", "Four", null));

    assertEquals(
        "user3@example.com",
        ScheduledEmailer.expertRecipientCsv("jdoe", true, "@example.com", testers));
  }

  @Test
  void testingWithoutTestersSendsNothing() {
    assertThrows(
        UserFriendlyException.class,
        () -> ScheduledEmailer.expertRecipientCsv("jdoe", true, "@example.com", null));
    assertThrows(
        UserFriendlyException.class,
        () ->
            ScheduledEmailer.expertRecipientCsv(
                "jdoe", true, "@example.com", List.of(new User("user4", "User", "Four", ""))));
  }
}
