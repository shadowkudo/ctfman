package ch.heigvd.dai.controllers;

import io.javalin.openapi.OpenApiExample;
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

  @OpenApiExample("Forbidden")
  public String getTitle() {
    return title;
  }

  @OpenApiExample("403")
  public int getStatus() {
    return status;
  }

  @OpenApiExample("https://javalin.io/documentation#forbiddenresponse")
  public String getType() {
    return type;
  }

  public Map<String, String> getDetails() {
    return details;
  }
}
