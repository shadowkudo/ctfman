package ch.heigvd.dai.middlewares;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.heigvd.dai.models.Session;
import ch.heigvd.dai.models.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class SessionMiddleware implements Handler {

  private static final Logger LOG = LoggerFactory.getLogger(SessionMiddleware.class);

  @Override
  public void handle(Context ctx) {
    String token = ctx.cookie("session");

    if (token == null) {
      LOG.debug("no token");
      return;
    }

    try {
      Session session = Session.findByToken(token);

      if (session == null) {
        LOG.debug("no session");
        ctx.removeCookie("session");
        return;
      }

      if (session.getExpiresAt().before(Timestamp.valueOf(LocalDateTime.now()))) {
        // Session expired
        LOG.debug("session expired");
        ctx.removeCookie("session");
        return;
      }

      User user = User.findByName(session.getUserAccount());
      ctx.attribute("user", user);

      LOG.debug("session: " + session.getToken() + " authentified as: " + session.getUserAccount());

    } catch (SQLException ex) {
      // Continue even if the session failed but still log to the console
      LOG.error(ex.toString());
    }

  }
}
