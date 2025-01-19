package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.javalin.openapi.ContentType;
import io.javalin.validation.BodyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
          description = "The list of active users ",
          content = { @OpenApiContent(from = User[].class, type = ContentType.JSON) }),
        @OpenApiResponse(
          status = "401",
          description = "User not authenticated",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        ),
        @OpenApiResponse(
          status = "500",
          description = "The server encountered an issue",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        )
      }
    )
    public void getAll(Context ctx) {
      User currentUser = ctx.attribute("user");
      if (currentUser == null) throw new UnauthorizedResponse();
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
          description = "User created"
        ),
        @OpenApiResponse(
          status = "400",
          description = "Unlogged users can only create CHALLENGER account",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        ),
        @OpenApiResponse(
          status = "403",
          description = "Only admin can create a user when logged",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        ),
        @OpenApiResponse(
          status = "500",
          description = "The server encountered an issue",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        )
      }
    )
    public void create(Context ctx) {
      User user = ctx.attribute("user");
      User newUser;
      CreateRequest request =
        ctx.bodyValidator(CreateRequest.class)
          .check(x -> x.name != null
            && !x.name.isBlank(), "can't create user with empty auth")
          .check(x -> x.password != null
            && !x.password.isBlank(), "can't register user with an empty password")
          .get();

      if (user == null) {
        if (request.role != User.Role.CHALLENGER) throw new BadRequestResponse();
      } else if (!user.hasRole(User.Role.ADMIN)) throw new UnauthorizedResponse();
      /* Encrypt Password*/
      String hash = BCrypt.with(new SecureRandom()).hashToString(6, request.password.toCharArray());
      /* Create new User */
      newUser = new User(request.name, request.email, hash, request.role);
      try {
        if (newUser.hasRole(User.Role.CHALLENGER)) newUser.insert_challenger();
        else newUser.insert_manager(request.role);
        ctx.status(HttpStatus.CREATED);
      } catch (SQLException e) {
        LOG.error(e.toString());
        throw new InternalServerErrorResponse();
      }
    }

    @OpenApi(path = "/users/{user-name}",
      methods = HttpMethod.GET,
      summary = "get a user",
      operationId = "getOneUser",
      tags = { "User" },
      pathParams = {
        @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      },
      responses = {
        @OpenApiResponse(
          status = "200",
          description = "The user details",
          content = @OpenApiContent(from = User.class, type = ContentType.JSON)
        ),
        @OpenApiResponse(
          status = "404",
          description = "User not found",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        ),
        @OpenApiResponse(
          status = "500",
          description = "The server encountered an issue",
          content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)
        )
    })
    public void getOne(Context ctx, String name) {
        try {
          User user = User.findByName(name);
          if (user == null) throw new NotFoundResponse();
          ctx.status(HttpStatus.OK);
          ctx.json(user);
        } catch (SQLException e) {
          LOG.error(e.toString());
          throw new InternalServerErrorResponse();
        }
    }

    @OpenApi(
      path = "/users/{user-name}",
      methods = { HttpMethod.PUT, HttpMethod.PATCH },
      summary = "update a user",
      operationId = "updateUser",
      tags = { "User" },
      pathParams = {
      @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      },
      requestBody =
      @OpenApiRequestBody(
        description =
          "When using PUT, all fields are required. When using PATCH, fields can be omitted"
            + " in which case they will keep their current value",
        required = true,
        content = {@OpenApiContent(from = UpdateRequest.class, type = ContentType.JSON)})
    )
    public void update(Context ctx, String name) {
      boolean isPatch = ctx.method().equals(HandlerType.PATCH);
      User currentUser = ctx.attribute("user");
      if (currentUser == null) throw new UnauthorizedResponse();
      User userToModifiy;
      try {
        userToModifiy = User.findByName(name);
        if (userToModifiy == null) throw new NotFoundResponse();
      } catch (SQLException e) { throw new InternalServerErrorResponse(); }

      if (!currentUser.equals(userToModifiy) && !currentUser.hasRole(User.Role.ADMIN)) throw new UnauthorizedResponse();

      UpdateRequest ur;
      if (!isPatch) {
        ur = ctx.bodyValidator(UpdateRequest.class)
          .check(x -> x.password != null
            && !x.password.isBlank(), "Password can't be empty on PUT")
          .check(x -> x.email != null
            && !x.email.isBlank(), "Email can't be empty on PUT")
          .get();
      } else ur = ctx.bodyValidator(UpdateRequest.class).get();

      if (ur.password != null) {
        String hash = BCrypt.with(
          new SecureRandom())
          .hashToString(6, ur.password.toCharArray());
      }

      if (ur.email != null) {
        userToModifiy.setPrimaryContact(ur.email); }

      try {
        if (!userToModifiy.update()) throw new InternalServerErrorResponse();
        ctx.json(userToModifiy);
      } catch (SQLException e) {
        throw new InternalServerErrorResponse();
      }
    }

    @OpenApi(
      path = "/users/{user-name}",
      methods = HttpMethod.DELETE,
      summary = "delete a user",
      operationId = "deleteUser",
      tags = { "User" },
      pathParams = {
        @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      }
    )
    public void delete(Context ctx, String name) {
      User currentUser = ctx.attribute("user");
      if (currentUser == null) throw new UnauthorizedResponse();
      User userToModifiy;
      try {
        userToModifiy = User.findByName(name);
        if (userToModifiy == null) throw new NotFoundResponse();
        if (!currentUser.equals(userToModifiy) && !currentUser.hasRole(User.Role.ADMIN)) throw new UnauthorizedResponse();
        if (!userToModifiy.delete()) {
          throw new InternalServerErrorResponse();
        }
        ctx.status(HttpStatus.NO_CONTENT);
      } catch (SQLException e) { throw new InternalServerErrorResponse(); }
    }

    public static record CreateRequest(
      String name, String password, String email, User.Role role) {}

    public static record UpdateRequest(
      String password, String email
    ) {}

}
