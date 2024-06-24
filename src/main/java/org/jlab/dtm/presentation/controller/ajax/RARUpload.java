package org.jlab.dtm.presentation.controller.ajax;

import java.io.*;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.presentation.util.ParamConverter;

@WebServlet(
    name = "RARUpload",
    urlPatterns = {"/ajax/rar-upload"})
@MultipartConfig()
// @MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1 MB, then cache on disk
//        maxFileSize = 1024 * 1024 * 10, // 10 MB
//        maxRequestSize = 1024 * 1024 * 10) // 10 MB
public class RARUpload extends HttpServlet {
  @EJB IncidentFacade incidentFacade;

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String error = null;
    String dirPath = System.getenv("RAR_DIR");

    // System.err.println("path: " + dirPath);

    try {

      if (dirPath == null) {
        throw new RuntimeException("Internal Server Configuration Error: RAR directory undefined");
      }

      File uploadDir = new File(dirPath);
      if (!uploadDir.exists()) {
        throw new RuntimeException("Server RAR directory unavailable!");
      }

      BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

      // System.err.println("incidentId: " + incidentId);

      if (incidentId == null) {
        throw new IllegalArgumentException("Incident ID required");
      }

      Incident incident = incidentFacade.find(incidentId);

      if (incident == null) {
        throw new IllegalArgumentException("Incident not found");
      }

      Part file = request.getPart("rar");

      if (file != null) {
        String submittedFilename = file.getSubmittedFileName();
        String type = file.getContentType();

        // System.err.println("filename: " + submittedFilename);
        // System.err.println("type: " + type);

        String ext = getFileExtension(submittedFilename);

        if (ext == null) {
          throw new RuntimeException("File must have an extension");
        }

        String filename = incidentId + ext;

        File path = new File(dirPath + File.separator + filename);

        if (path.exists()) {
          boolean worked = path.delete();

          if (!worked) {
            throw new RuntimeException("Unable to delete old version of uploaded file");
          }
        }

        file.write(path.getAbsolutePath());

        incidentFacade.rarUploaded(incidentId, ext);
      } else {
        throw new IllegalArgumentException("no file found");
      }
    } catch (Exception e) {
      e.printStackTrace();
      error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    if (error != null) {
      response.setStatus(400);

      try (JsonGenerator gen = Json.createGenerator(response.getOutputStream())) {
        gen.writeStartObject();
        gen.write("error", error);
        gen.writeEnd();
      }
    }
  }

  String getFileExtension(String name) {
    int i = name.lastIndexOf('.');
    String ext = i > 0 ? "." + name.substring(i + 1) : null;
    return ext;
  }
}
