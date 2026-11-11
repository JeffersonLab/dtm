package org.jlab.dtm.presentation.params;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.junit.jupiter.api.Test;

class IncidentDowntimeReportUrlParamHandlerTest {

  private static IncidentDowntimeReportUrlParamHandler handlerWithSort(String sort) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("sort")).thenReturn(sort);
    return new IncidentDowntimeReportUrlParamHandler(
        request, new Date(), new Date(), null, null, null);
  }

  @Test
  void sortsByDurationWhenSortIsMissing() {
    assertTrue(handlerWithSort(null).convert().isSortByDuration());
  }

  @Test
  void sortsByDurationWhenAsked() {
    assertTrue(handlerWithSort("DURATION").convert().isSortByDuration());
  }

  @Test
  void sortsByTimeDownWhenAsked() {
    assertFalse(handlerWithSort("TIME_DOWN").convert().isSortByDuration());
  }

  @Test
  void rejectsSortKeysTheReportDoesNotOffer() {
    assertThrows(RuntimeException.class, () -> handlerWithSort("RAR_UPLOADED").convert());
  }

  @Test
  void defaultsSortByDuration() {
    IncidentDowntimeReportParams params = handlerWithSort(null).defaults();
    assertTrue(params.isSortByDuration());
  }
}
