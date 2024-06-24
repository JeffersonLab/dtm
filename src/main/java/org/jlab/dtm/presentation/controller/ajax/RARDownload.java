package org.jlab.dtm.presentation.controller.ajax;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.presentation.util.ParamConverter;

@WebServlet(
    name = "RARDownload",
    urlPatterns = {"/ajax/rar-download"})
public class RARDownload extends HttpServlet {
  @EJB IncidentFacade incidentFacade;

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String error = null;
    String dirPath = System.getenv("RAR_DIR");

    // System.err.println("path: " + dirPath);

    if (dirPath == null) {
      throw new RuntimeException("Internal Server Configuration Error: RAR directory undefined");
    }

    File uploadDir = new File(dirPath);
    if (!uploadDir.exists()) {
      throw new RuntimeException("Server RAR directory unavailable!");
    }

    BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

    if (incidentId == null) {
      throw new RuntimeException("Incident ID must not be empty");
    }

    Incident incident = incidentFacade.find(incidentId);

    if (incident == null) {
      throw new RuntimeException("Incident not found with ID " + incidentId);
    }

    String ext = incident.getRarExt();

    if (ext == null) {
      throw new RuntimeException("No file for this incident");
    }

    String filename = incidentId + ext;

    File path = new File(dirPath + File.separator + filename);

    if (!path.exists()) {
      throw new RuntimeException("File missing from filesystem!");
    }

    String contentType = "application/octet-stream";

    switch (ext) {
      case ".pdf":
        contentType = "application/pdf";
        break;
      case ".docx":
        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        break;
      case ".doc":
        contentType = "application/msword";
        break;
      case ".txt":
        contentType = "text/plain";
        break;
    }

    response.setContentType(contentType);
    response.setHeader("content-disposition", "attachment;filename=\"" + filename + "\"");

    OutputStream out = response.getOutputStream();

    try (FileInputStream in = new FileInputStream(path)) {
      FileChannel channel = in.getChannel();
      byte[] buffer = new byte[256 * 1024];
      ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

      for (int length = 0; (length = channel.read(byteBuffer)) != -1; ) {
        out.write(buffer, 0, length);
        byteBuffer.clear();
      }
    }
  }
}
