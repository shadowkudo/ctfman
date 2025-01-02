package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.Session;
import ch.heigvd.dai.models.User;
import io.javalin.http.*;
import io.javalin.openapi.ContentType;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class AuthController {
  // TODO: check if there is anything more secure/better for the token
  private static final SecureRandom rnd = new SecureRandom();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

  private static String generateToken() {
    byte[] randomBytes = new byte[32];
    rnd.nextBytes(randomBytes);
    return encoder.encodeToString(randomBytes);
  }

  public AuthController() {
  }

  @OpenApi(path = "/login", methods = HttpMethod.POST, summary = "login as an user", operationId = "login", tags = {
      "Auth" }, requestBody = @OpenApiRequestBody(description = "Username and password for authentication", required = true, content = {
          @OpenApiContent(from = LoginRequest.class, type = ContentType.JSON)
      }), responses = {
          @OpenApiResponse(status = "200", description = "login successful", headers = {

          }),
          @OpenApiResponse(status = "403", description = "login failed, invalid username or password")
      })
  public void login(Context ctx) {
    LoginRequest rq = ctx.bodyAsClass(LoginRequest.class);

    if (rq.username == null || rq.password == null) {
      throw new UnauthorizedResponse();
    }

    System.out.println("trying to login with: " + rq.username + " and " + rq.password);

    try {

      User user = User.findByName(rq.username);

      if (user == null) {
        System.out.println("user not found");
      } else {
        System.out.println("user found with password: " + user.getPasswordHash());
      }

      // TODO: use salt
      if (user == null || !BCrypt.verifyer().verify(rq.password.toCharArray(), user.getPasswordHash()).verified) {
        throw new UnauthorizedResponse();
      }

      Timestamp expires = Timestamp.valueOf(LocalDateTime.now().plus(30, ChronoUnit.DAYS));

      while (true) {
        Session session = new Session(generateToken(), rq.username, expires);
        try {

          if (session.insert() == 1) {
            Cookie cookie = new Cookie("session", session.getToken());
            cookie.setPath("/");
            cookie.setSameSite(SameSite.STRICT);
            ctx.cookie(cookie);
            ctx.status(HttpStatus.NO_CONTENT);
            return;
          }
        } catch (SQLException ex) {
          throw ex;
        }
      }
    } catch (SQLException ex) {
      // TODO: logging
      System.err.println(ex);
      throw new InternalServerErrorResponse();
    }

  }

  public void logout(Context ctx) {
    ctx.removeCookie("session");
    ctx.status(HttpStatus.NO_CONTENT);
  }

  public record LoginRequest(String username, String password) {
  }

}
