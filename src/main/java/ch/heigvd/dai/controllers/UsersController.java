package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.javalin.openapi.ContentType;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsersController implements CrudHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UsersController.class);

  @OpenApi(
      path = "/users",
      methods = HttpMethod.GET,
      summary = "get all users",
      operationId = "getAllUsers",
      tags = {"User"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The list of active users ",
            content = {@OpenApiContent(from = User[].class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
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
      tags = {"User"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
      requestBody =
          @OpenApiRequestBody(
              required = true,
              content = @OpenApiContent(from = CreateRequest.class, type = ContentType.JSON)),
      responses = {
        @OpenApiResponse(status = "201", description = "User created"),
        @OpenApiResponse(
            status = "400",
            description = "Unlogged users can only create CHALLENGER account",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Only admin can create a user when logged",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void create(Context ctx) {
    User user = ctx.attribute("user");
    User newUser;
    CreateRequest request =
        ctx.bodyValidator(CreateRequest.class)
            .check(x -> x.name != null && !x.name.isBlank(), "can't create user with empty auth")
            .check(
                x -> x.password != null && !x.password.isBlank(),
                "can't register user with an empty password")
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

  @OpenApi(
      path = "/users/{user-name}",
      methods = HttpMethod.GET,
      summary = "get a user",
      operationId = "getOneUser",
      tags = {"User"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
      pathParams = {
        @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      },
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The user details",
            content = @OpenApiContent(from = User.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "404",
            description = "User not found",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
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
      methods = {HttpMethod.PUT, HttpMethod.PATCH},
      summary = "update a user",
      operationId = "updateUser",
      tags = {"User"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
      pathParams = {
        @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      },
      requestBody =
          @OpenApiRequestBody(
              description =
                  "When using PUT, all fields are required. When using PATCH, fields can be omitted"
                      + " in which case they will keep their current value",
              required = true,
              content = {@OpenApiContent(from = UpdateRequest.class, type = ContentType.JSON)}),
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "Successful changes",
            content = @OpenApiContent(from = User.class, type = ContentType.JSON)),
        @OpenApiResponse(status = "400", description = "Invalid request"),
        @OpenApiResponse(
            status = "401",
            description = "Only authenticated user can modifiy user",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "No permission to modify the user",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "404",
            description = "The user to modify was not found",
            content =
                @OpenApiContent(
                    from = ErrorResponse.class,
                    type = io.javalin.http.ContentType.JSON))
      })
  public void update(Context ctx, String name) {
    boolean isPatch = ctx.method().equals(HandlerType.PATCH);
    User currentUser = ctx.attribute("user");
    if (currentUser == null) throw new UnauthorizedResponse();
    User userToModifiy;
    try {
      userToModifiy = User.findByName(name);
      if (userToModifiy == null) throw new NotFoundResponse();
    } catch (SQLException e) {
      LOG.error(e.toString());
      throw new InternalServerErrorResponse();
    }
    if (!currentUser.equals(userToModifiy) && !currentUser.hasRole(User.Role.ADMIN))
      throw new ForbiddenResponse();
    UpdateRequest ur;
    if (!isPatch) {
      ur =
          ctx.bodyValidator(UpdateRequest.class)
              .check(
                  x -> x.password != null && !x.password.isBlank(),
                  "Password can't be empty on PUT")
              .check(x -> x.email != null && !x.email.isBlank(), "Email can't be empty on PUT")
              .get();
    } else ur = ctx.bodyValidator(UpdateRequest.class).get();
    if (ur.password != null) {
      userToModifiy.setPasswordHash(
          BCrypt.with(new SecureRandom()).hashToString(6, ur.password.toCharArray()));
    }
    if (ur.email != null) userToModifiy.setPrimaryContact(ur.email);
    try {
      if (!userToModifiy.update()) throw new InternalServerErrorResponse();
      ctx.json(userToModifiy);
    } catch (SQLException e) {
      LOG.error(e.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/users/{user-name}",
      methods = HttpMethod.DELETE,
      summary = "delete a user",
      operationId = "deleteUser",
      tags = {"User"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
      pathParams = {
        @OpenApiParam(name = "user-name", type = String.class, description = "The user name")
      },
      responses = {
        @OpenApiResponse(status = "204", description = "The user has been successfully deleted"),
        @OpenApiResponse(
            status = "401",
            description = "Need to be connected to delete",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Admin permission or self account deleting",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "404",
            description = "The user to delete wasn't found",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void delete(Context ctx, String name) {
    User currentUser = ctx.attribute("user");
    if (currentUser == null) throw new UnauthorizedResponse();
    User userToModifiy;
    try {
      userToModifiy = User.findByName(name);
      if (userToModifiy == null) throw new NotFoundResponse();
      // NOTE: not quite sure about this condition
      if (!currentUser.equals(userToModifiy) && !currentUser.hasRole(User.Role.ADMIN))
        throw new ForbiddenResponse();
      // NOTE: this is a soft delete, not an actual delete
      if (!userToModifiy.delete()) {
        throw new InternalServerErrorResponse();
      }
      ctx.status(HttpStatus.NO_CONTENT);
    } catch (SQLException e) {
      LOG.error(e.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApiName("UserCreateRequest")
  public static record CreateRequest(
      @OpenApiExample("hackerman") @OpenApiRequired String name,
      @OpenApiExample("4 super Str0ng P@s$w0rd") @OpenApiRequired String password,
      @OpenApiExample("hackerman@matrix.gov") @OpenApiRequired String email,
      @OpenApiPropertyType(definedBy = User.Role.class)
          @OpenApiExample("challenger")
          @OpenApiRequired
          @OpenApiDescription(
              "the role of the user (one of [challenger, admin, moderator, author, all] where all"
                  + " means every role except challenger)")
          User.Role role) {}

  @OpenApiName("UserUpdateRequest")
  public static record UpdateRequest(
      @OpenApiExample("I forgor💀")
          @OpenApiDescription(
              "The new password to use (no question asked, we won't even invalidate the sessions)")
          String password,
      @OpenApiExample("anon@hotmail.com") String email) {}
}
