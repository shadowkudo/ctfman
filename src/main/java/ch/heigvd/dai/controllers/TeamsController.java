package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.heigvd.dai.models.Team;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.MethodNotAllowedResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamsController implements CrudHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TeamsController.class);

  @OpenApi(
      path = "/teams",
      methods = HttpMethod.GET,
      summary = "get all teams",
      operationId = "getAllTeams",
      tags = {"Team"},
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The list of teams",
            content = {@OpenApiContent(from = Team[].class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void getAll(Context ctx) {
    try {
      // get all the teams
      List<Team> teams = Team.getAll();
      ctx.json(teams);
    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/teams",
      methods = HttpMethod.POST,
      summary = "create a team",
      operationId = "createTeam",
      tags = {"Team"},
      requestBody =
          @OpenApiRequestBody(
              description = "Username and password for authentication",
              required = true,
              content = {@OpenApiContent(from = CreateRequest.class, type = ContentType.JSON)}),
      responses = {
        @OpenApiResponse(status = "201", description = "Resource created"),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Only challengers can create a team",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "409",
            description = "A resource with this name already exists",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void create(Context ctx) {
    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only a challenger can be the captain (and create a team)
    if (!user.hasRole(User.Role.CHALLENGER)) {
      throw new ForbiddenResponse();
    }

    CreateRequest rq =
        ctx.bodyValidator(CreateRequest.class)
            .check(x -> x.name != null && !x.name.isBlank(), "name is required")
            .check(x -> x.password != null && !x.password.isBlank(), "password is required")
            .get();

    try {
      String hash = BCrypt.with(new SecureRandom()).hashToString(6, rq.password.toCharArray());
      Team team = new Team(rq.name, rq.description, rq.country, hash, null, null);
      team.insertWithCaptain(user);

      ctx.status(HttpStatus.CREATED);
    } catch (SQLException ex) {
      if (ex.getMessage()
          .contains("duplicate key value violates unique constraint \"authentication_pkey\"")) {
        throw new ConflictResponse();
      }
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/teams/{teamName}",
      methods = HttpMethod.GET,
      summary = "get a team",
      operationId = "getOneTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "teamName", type = String.class, description = "The team name")
      },
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The team details",
            content = {@OpenApiContent(from = Team.class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "404",
            description = "No team with this name",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void getOne(Context ctx, String teamName) {
    try {
      Team team = Team.getByName(teamName);

      if (team == null) {
        throw new NotFoundResponse();
      }

      ctx.status(HttpStatus.OK);
      ctx.json(team);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/teams/{teamName}",
      methods = {HttpMethod.PUT, HttpMethod.PATCH},
      summary = "update a team",
      operationId = "updateTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "teamName", type = String.class, description = "The team name")
      })
  public void update(Context ctx, String id) {
    throw new MethodNotAllowedResponse();
  }

  @OpenApi(
      path = "/teams/{teamName}",
      methods = HttpMethod.DELETE,
      summary = "delete a team",
      operationId = "deleteTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "teamName", type = String.class, description = "The team name")
      })
  public void delete(Context ctx, String id) {
    throw new MethodNotAllowedResponse();
  }

  public static record CreateRequest(
      String name, String password, String description, String country) {}
}
