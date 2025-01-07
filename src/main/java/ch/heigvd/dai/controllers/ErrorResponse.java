package ch.heigvd.dai.controllers;

import java.util.Map;

/**
 * @see
 *     https://github.com/javalin/javalin-samples/blob/main/javalin6/javalin-openapi-example/src/main/java/io/javalin/example/java/ErrorResponse.java
 */
class ErrorResponse {
  private String title;
  private int status;
  private String type;
  private Map<String, String> details;

  public ErrorResponse() {}

  public ErrorResponse(String title, int status, String type, Map<String, String> details) {
    this.title = title;
    this.status = status;
    this.type = type;
    this.details = details;
  }

  public String getTitle() {
    return title;
  }

  public int getStatus() {
    return status;
  }

  public String getType() {
    return type;
  }

  public Map<String, String> getDetails() {
    return details;
  }
}
