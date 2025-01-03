package ch.heigvd.dai.middlewares;

import java.util.Arrays;

import ch.heigvd.dai.models.User;
import ch.heigvd.dai.models.User.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

public class AuthMiddleware implements Handler {

  private Role[] roles;

  public AuthMiddleware() {
    this(new Role[0]);
  }

  public AuthMiddleware(Role[] roles) {
    this.roles = roles;
  }

  @Override
  public void handle(Context ctx) {
    User user = ctx.attribute("user");

    if (user == null) {
      throw new UnauthorizedResponse();
    }

    // User is logged in and we don't care about the role
    if (roles.length == 0) {
      return;
    }

    boolean isAllowed = Arrays.stream(roles).anyMatch((role) -> user.hasRole(role));

    if (!isAllowed) {
      throw new UnauthorizedResponse();
    }

  }

}
