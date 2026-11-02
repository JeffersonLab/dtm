package org.jlab.dtm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.*;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Set;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.presentation.util.ParamConverter;

@WebServlet(
    name = "RARUpload",
    urlPatterns = {"/ajax/rar-upload"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB, then cache on disk
    maxFileSize = RARUpload.MAX_FILE_BYTES,
    maxRequestSize = RARUpload.MAX_FILE_BYTES + 1024 * 1024) // file plus form fields
public class RARUpload extends HttpServlet {
  static final long MAX_FILE_BYTES = 1024 * 1024 * 10; // 10 MB

  /** The file types RARDownload knows how to serve. */
  static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".docx", ".doc", ".txt");

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
    int status = HttpServletResponse.SC_BAD_REQUEST;
    String dirPath = getRarDir();

    // System.err.println("path: " + dirPath);

    try {
      // Check before reading any of the multipart request
      if (request.getRemoteUser() == null) {
        status = HttpServletResponse.SC_UNAUTHORIZED;
        throw new IllegalArgumentException("You must be logged in to upload a RAR");
      }

      try {
        request.getParts(); // Parses the request, so a file over the size limit fails here
      } catch (IllegalStateException e) {
        throw new IllegalArgumentException("File must be no larger than 10 MB");
      }

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

        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
          throw new IllegalArgumentException("File must be a PDF, Word (.docx, .doc) or text file");
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
      response.setStatus(status);
      response.setContentType("application/json");

      try (JsonGenerator gen = Json.createGenerator(response.getOutputStream())) {
        gen.writeStartObject();
        gen.write("error", error);
        gen.writeEnd();
      }
    }
  }

  String getRarDir() {
    return System.getenv("RAR_DIR");
  }

  String getFileExtension(String name) {
    if (name == null) {
      return null;
    }

    int i = name.lastIndexOf('.');
    String ext = i > 0 ? "." + name.substring(i + 1).toLowerCase(Locale.ROOT) : null;
    return ext;
  }
}
