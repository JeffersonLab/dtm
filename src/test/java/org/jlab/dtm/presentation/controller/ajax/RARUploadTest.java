package org.jlab.dtm.presentation.controller.ajax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RARUploadTest {

  @TempDir Path rarDir;

  RARUpload servlet;
  IncidentFacade incidentFacade;
  HttpServletRequest request;
  HttpServletResponse response;
  ByteArrayOutputStream body;

  @BeforeEach
  void setUp() throws Exception {
    incidentFacade = mock(IncidentFacade.class);
    when(incidentFacade.find(any())).thenReturn(new Incident());

    servlet =
        new RARUpload() {
          @Override
          String getRarDir() {
            return rarDir.toString();
          }
        };
    servlet.incidentFacade = incidentFacade;

    request = mock(HttpServletRequest.class);
    when(request.getParameter("incidentId")).thenReturn("123");

    body = new ByteArrayOutputStream();
    response = mock(HttpServletResponse.class);
    when(response.getOutputStream())
        .thenReturn(
            new ServletOutputStream() {
              @Override
              public boolean isReady() {
                return true;
              }

              @Override
              public void setWriteListener(WriteListener listener) {}

              @Override
              public void write(int b) {
                body.write(b);
              }
            });
  }

  Part upload(String filename) {
    Part part = mock(Part.class);
    when(part.getSubmittedFileName()).thenReturn(filename);
    return part;
  }

  String responseBody() {
    return body.toString(StandardCharsets.UTF_8);
  }

  @Test
  void anonymousUploadIsRefusedBeforeReadingTheRequest() throws Exception {
    when(request.getRemoteUser()).thenReturn(null);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    assertTrue(responseBody().contains("logged in"));
    verify(request, never()).getParts();
    verify(request, never()).getPart(anyString());
    verify(incidentFacade, never()).rarUploaded(any(), any());
  }

  @Test
  void oversizeUploadIsRefused() throws Exception {
    when(request.getRemoteUser()).thenReturn("user1");
    when(request.getParts()).thenThrow(new IllegalStateException("too large"));

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseBody().contains("10 MB"));
    verify(incidentFacade, never()).rarUploaded(any(), any());
  }

  @Test
  void otherFileTypesAreRefused() throws Exception {
    when(request.getRemoteUser()).thenReturn("user1");
    Part part = upload("report.html");
    when(request.getPart("rar")).thenReturn(part);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(part, never()).write(anyString());
    verify(incidentFacade, never()).rarUploaded(any(), any());
  }

  @Test
  void loggedInPdfUploadIsSaved() throws Exception {
    when(request.getRemoteUser()).thenReturn("user1");
    Part part = upload("Report.PDF");
    when(request.getPart("rar")).thenReturn(part);

    servlet.doPost(request, response);

    verify(response, never()).setStatus(anyInt());
    verify(part).write(new File(rarDir.toFile(), "123.pdf").getAbsolutePath());
    verify(incidentFacade).rarUploaded(BigInteger.valueOf(123), ".pdf");
  }

  @Test
  void fileExtension() {
    assertEquals(".pdf", servlet.getFileExtension("Report.PDF"));
    assertEquals(".docx", servlet.getFileExtension("a.b.docx"));
    assertNull(servlet.getFileExtension("noextension"));
    assertNull(servlet.getFileExtension(".hidden"));
    assertNull(servlet.getFileExtension(null));
    assertFalse(RARUpload.ALLOWED_EXTENSIONS.contains(servlet.getFileExtension("x.jsp")));
  }
}
