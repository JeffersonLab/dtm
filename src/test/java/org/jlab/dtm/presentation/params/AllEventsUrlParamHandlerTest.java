package org.jlab.dtm.presentation.params;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import org.jlab.dtm.business.params.AllEventsParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.junit.jupiter.api.Test;

class AllEventsUrlParamHandlerTest {

  private static EventType type(String name) {
    EventType type = mock(EventType.class);
    when(type.getName()).thenReturn(name);
    return type;
  }

  @Test
  void convertsEveryTypeParameter() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameterValues("type")).thenReturn(new String[] {"1", "3"});

    AllEventsParams params = new AllEventsUrlParamHandler(request, null).convert();

    assertArrayEquals(
        new BigInteger[] {BigInteger.ONE, BigInteger.valueOf(3)}, params.getEventTypeIdArray());
  }

  @Test
  void namesEachSelectedTypeInTheMessage() {
    EventTypeFacade facade = mock(EventTypeFacade.class);
    EventType acc = type("Accelerator");
    EventType hallA = type("Hall A");
    when(facade.find(BigInteger.ONE)).thenReturn(acc);
    when(facade.find(BigInteger.TWO)).thenReturn(hallA);

    AllEventsParams params = new AllEventsParams();
    params.setEventTypeIdArray(new BigInteger[] {BigInteger.ONE, null, BigInteger.TWO});

    String message =
        new AllEventsUrlParamHandler(mock(HttpServletRequest.class), facade).message(params);

    assertEquals("Type \"Accelerator\", \"Hall A\"", message);
  }

  @Test
  void leavesTypeOutOfTheMessageWhenNoneIsSelected() {
    AllEventsParams params = new AllEventsParams();
    params.setEventTypeIdArray(new BigInteger[] {null});

    String message =
        new AllEventsUrlParamHandler(mock(HttpServletRequest.class), mock(EventTypeFacade.class))
            .message(params);

    assertEquals("", message);
  }
}
