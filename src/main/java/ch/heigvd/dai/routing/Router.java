package ch.heigvd.dai.routing;

import static io.javalin.apibuilder.ApiBuilder.*;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.CtfsController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.middlewares.AuthMiddleware;
import ch.heigvd.dai.middlewares.SessionMiddleware;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

public class Router implements EndpointGroup {

  @Override
  public void addEndpoints() {

    // Global middlewares
    before(new SessionMiddleware());

    final AuthMiddleware authMiddleware = new AuthMiddleware();

    // Auth
    final AuthController authController = new AuthController();
    post("/login", authController::login);
    post("/logout", authController::logout);
    get("/profile", authController::profile);

    // Resources
    path(
        "/teams",
        () -> {
          before(authMiddleware);
          path(
              "/{teamName}",
              () -> {
                TeamsController teamsController = new TeamsController();
                crud(teamsController);
                put(ctx -> teamsController.update(ctx, ctx.pathParam("teamName")));
              });
        });

    path(
        "/ctfs",
        () -> {
          before(authMiddleware);
          path(
              "/{ctf-title}",
              () -> {
                CtfsController ctfController = new CtfsController();
                crud(ctfController);
                put(ctx -> ctfController.update(ctx, ctx.pathParam("ctf-title")));
              });
        });

    // Misc
    get("/", ctx -> ctx.redirect("/swagger", HttpStatus.PERMANENT_REDIRECT));
  }
}
