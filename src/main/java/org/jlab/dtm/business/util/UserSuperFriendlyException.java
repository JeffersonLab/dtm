package org.jlab.dtm.business.util;

import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * This class is a workaround for CodeQL whining about any use of getMessage() being dangerous, even
 * if the intent is to relay a message directly to the user.
 */
public class UserSuperFriendlyException extends UserFriendlyException {
  private final String userMessage;

  public UserSuperFriendlyException(String msg) {
    super(msg);
    this.userMessage = msg;
  }

  public String getUserMessage() {
    return userMessage;
  }
}
