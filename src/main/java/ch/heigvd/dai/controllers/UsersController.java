package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.javalin.openapi.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsersController implements CrudHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UsersController.class);

    @OpenApi(
            path = "/users",
            methods = HttpMethod.GET,
            summary = "get all users",
            operationId = "getAllUsers",
            tags = { "User" },
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = { @OpenApiContent(from = User[].class, type = ContentType.JSON) })
            }
    )
    public void getAll(Context ctx) {
        LOG.info("Je suis dans getAll (simple)");
        try {
            List<User> users = User.getAll(User.Role.ALL);
            ctx.json(users);
        } catch (SQLException e) {
            LOG.error(e.toString());
            throw new InternalServerErrorResponse();
        }
    }

    @OpenApi(
            path = "/users",
            methods = HttpMethod.POST,
            summary = "create a user",
            operationId = "createUser",
            tags = { "User" },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = @OpenApiContent(from = CreateRequest.class, type = ContentType.JSON)
            ),
            responses = {
                    @OpenApiResponse(
                      status = "201",
                      description = "Resource created")
            }
        )
    public void create(Context ctx) {
      LOG.info("Je suis dans create");
      User user = ctx.attribute("user");
      User newUser;
      CreateRequest request =
        ctx.bodyValidator(CreateRequest.class)
          .check(x -> x.name != null
            && !x.name.isBlank(), "can't create user with empty auth")
          .check(x -> x.password != null
            && !x.password.isBlank(), "can't register user with an empty password")
          .get();
      LOG.info("requete acceptée");

      if (user == null) {
        if (request.role != User.Role.CHALLENGER) throw new BadRequestResponse();
      } else if (!user.hasRole(User.Role.ADMIN)) throw new BadRequestResponse();
      LOG.info("requete valide");
      /* Encrypt Password*/
      String hash = BCrypt.with(new SecureRandom()).hashToString(6, request.password.toCharArray());
      /* Create new User */
      newUser = new User(
        request.name,
        request.email,
        hash,
        request.role);
      try {
        LOG.info("try - catch");
        if (newUser.hasRole(User.Role.CHALLENGER)) newUser.insert_challenger();
        else newUser.insert_manager(request.role);
        LOG.info("Tout bon");
        ctx.status(HttpStatus.CREATED);
      } catch (SQLException e) {
        throw new InternalServerErrorResponse();
      }
    }

    @OpenApi(path = "/users/{id}",
            methods = HttpMethod.GET,
            summary = "get a user",
            operationId = "getOneUser",
            tags = { "User" }, responses = {
            @OpenApiResponse(status = "200", content = { @OpenApiContent(from = User.class) })
    })
    public void getOne(Context ctx, String id) {
        LOG.info("Je suis dans getOne");
        throw new MethodNotAllowedResponse();
    }

    @OpenApi(path = "/users/{id}", methods = { HttpMethod.PUT,
            HttpMethod.PATCH }, summary = "update a user", operationId = "updateUser", tags = { "User" })
    public void update(Context ctx, String id) {
        LOG.info("Je suis dans update");
        throw new MethodNotAllowedResponse();
    }

    @OpenApi(path = "/users/{id}", methods = HttpMethod.DELETE, summary = "delete a user", operationId = "deleteUser",
            tags = { "User" })
    public void delete(Context ctx, String id) {
        LOG.info("Je suis dans delete");
        throw new MethodNotAllowedResponse();
    }

    public static record CreateRequest(
      String name, String password, String email, User.Role role) {}

}
