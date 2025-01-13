package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {
  // TODO: check if there is anything more secure/better for the token
  private static final SecureRandom rnd = new SecureRandom();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
  private static final TemporalAmount VALIDITY_PERIOD = Duration.ofDays(30);

  private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

  private static String generateToken() {
    byte[] randomBytes = new byte[32];
    rnd.nextBytes(randomBytes);
    return encoder.encodeToString(randomBytes);
  }

  public AuthController() {}

  @OpenApi(
      path = "/login",
      methods = HttpMethod.POST,
      summary = "login as an user",
      operationId = "login",
      tags = {"Auth"},
      requestBody =
          @OpenApiRequestBody(
              description = "Username and password for authentication",
              required = true,
              content = {@OpenApiContent(from = LoginRequest.class, type = ContentType.JSON)}),
      responses = {
        @OpenApiResponse(
            status = "204",
            description = "login successful",
            headers = {
              // TODO: document session header
            }),
        @OpenApiResponse(status = "401", description = "login failed, invalid username or password")
      })
  public void login(Context ctx) {
    LoginRequest rq = ctx.bodyAsClass(LoginRequest.class);

    if (rq.username == null || rq.password == null) {
      throw new UnauthorizedResponse();
    }

    try {

      User user = User.findByName(rq.username);

      // WARN: Passwords aren't hashed which is a security risk.
      if (user == null
          || !BCrypt.verifyer()
              .verify(rq.password.toCharArray(), user.getPasswordHash())
              .verified) {
        throw new UnauthorizedResponse();
      }

      Timestamp expires = Timestamp.valueOf(LocalDateTime.now().plus(VALIDITY_PERIOD));

      while (true) {
        Session session = new Session(generateToken(), rq.username, expires);
        try {

          if (session.insert() == 1) {
            Cookie cookie = new Cookie("session", session.getToken());
            cookie.setPath("/");
            cookie.setSameSite(SameSite.NONE);
            cookie.setSecure(true);
            ctx.cookie(
                cookie.getName()
                    + "="
                    + cookie.getValue()
                    + "Path="
                    + cookie.getPath()
                    + "SameStie=None; Secure; Partitioned;");
            // ctx.cookie(cookie);
            ctx.status(HttpStatus.NO_CONTENT);
            return;
          }
        } catch (SQLException ex) {
          throw ex;
        }
      }
    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/logout",
      methods = HttpMethod.POST,
      summary = "logout",
      operationId = "logout",
      tags = {"Auth"},
      responses = {
        @OpenApiResponse(
            status = "204",
            description = "Logged out",
            headers = {})
      })
  public void logout(Context ctx) {
    ctx.removeCookie("session");
    ctx.status(HttpStatus.NO_CONTENT);
  }

  @OpenApi(
      path = "/profile",
      methods = HttpMethod.GET,
      summary = "get profile",
      operationId = "profile",
      tags = {"Auth"},
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "Profile details",
            content = {@OpenApiContent(from = User.class)})
      })
  public void profile(Context ctx) {
    User user = ctx.attribute("user");

    if (user == null) {
      ctx.status(HttpStatus.UNAUTHORIZED);
      return;
    }

    ctx.json(user);
  }

  public record LoginRequest(String username, String password) {}
}
