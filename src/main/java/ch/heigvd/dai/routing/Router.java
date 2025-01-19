package ch.heigvd.dai.routing;

import static io.javalin.apibuilder.ApiBuilder.*;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.CtfsController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.controllers.TeamsCtfsController;
import ch.heigvd.dai.controllers.UsersController;
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
              "/{team-name}",
              () -> {
                TeamsController teamsController = new TeamsController();
                crud(teamsController);
                put(ctx -> teamsController.update(ctx, ctx.pathParam("team-name")));
                path(
                    "/ctfs",
                    () -> {
                      TeamsCtfsController teamsCtfsController = new TeamsCtfsController();
                      get(ctx -> teamsCtfsController.getAll(ctx, ctx.pathParam("team-name")));
                      post(ctx -> teamsCtfsController.create(ctx, ctx.pathParam("team-name")));

                      path(
                          "/{ctf-title}",
                          () -> {
                            get(
                                ctx ->
                                    teamsCtfsController.getOne(
                                        ctx,
                                        ctx.pathParam("team-name"),
                                        ctx.pathParam("ctf-title")));
                          });
                    });
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
    path(
      "/users/{user-name}",
      () -> {
        UsersController usersController = new UsersController();
        crud(usersController);
        put(ctx -> usersController.update(ctx, ctx.pathParam("id")));
      });

    // Misc
    get("/", ctx -> ctx.redirect("/swagger", HttpStatus.PERMANENT_REDIRECT));
  }
}
