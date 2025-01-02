package ch.heigvd.dai.middlewares;

import java.sql.SQLException;

import ch.heigvd.dai.models.Session;
import ch.heigvd.dai.models.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class AuthMiddleware implements Handler {

  @Override
  public void handle(Context ctx) {
    String token = ctx.cookie("session");

    if (token == null) {
      System.err.println("no token");
      return;
    }

    try {
      Session session = Session.findByToken(token);

      if (session == null) {
        System.err.println("no session");
        ctx.removeCookie("session");
        return;
      }

      User user = User.findByName(session.getUserAccount());
      ctx.attribute("user", user);

    } catch (SQLException ex) {
      System.err.println(ex);
    }

  }
}
