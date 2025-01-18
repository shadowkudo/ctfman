package ch.heigvd.dai.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.heigvd.dai.models.Team;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import io.javalin.validation.BodyValidator;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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
      Team team =
          new Team(rq.name, rq.description, rq.country, user.getAuthentication(), hash, null, null);
      team.insert();

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
      path = "/teams/{team-name}",
      methods = HttpMethod.GET,
      summary = "get a team",
      operationId = "getOneTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "team-name", type = String.class, description = "The team name")
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
      path = "/teams/{team-name}",
      methods = {HttpMethod.PUT, HttpMethod.PATCH},
      summary = "update a team",
      operationId = "updateTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "team-name", type = String.class, description = "The team name")
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
            description = "Update successful",
            content = {@OpenApiContent(from = Team.class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "User isn't captain of the team",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void update(Context ctx, String teamName) {
    boolean isPatch = ctx.method().equals(HandlerType.PATCH);
    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only a challenger can be the captain (and create a team)
    if (!user.hasRole(User.Role.CHALLENGER)) {
      throw new ForbiddenResponse();
    }

    BodyValidator<UpdateRequest> validator = ctx.bodyValidator(UpdateRequest.class);

    if (isPatch) { // PATCH
      // name: omit/not blank
      validator
          .check(
              it -> it.name == null || it.name.isPresent() && !it.name.get().isBlank(),
              "PATCH: name should either be omitted or contain a value")
          // password omit/ not blank
          .check(
              it -> it.password == null || it.password.isPresent() && !it.password.get().isBlank(),
              "PATCH: password should either be omitted or contain a value");
      // description omit/nullable (no check)
      // country omit/nullable (no check)
    } else { // PUT
      validator
          .check(
              it -> it.name != null && it.name.isPresent() && !it.name.get().isBlank(),
              "PUT: name is required")
          .check(
              it -> it.password != null && it.password.isPresent() && !it.password.get().isBlank(),
              "PUT: password is required")
          .check(it -> it.description != null, "PUT: description is required (nullable)")
          .check(it -> it.country != null, "PUT: country is required (nullable)");
    }

    UpdateRequest rq = validator.get();

    LOG.debug(rq.toString());

    // Check that the user is the team captain
    try {
      Team team = Team.getByName(teamName);

      if (team == null) {
        throw new NotFoundResponse();
      }

      if (!team.getCaptain().equals(user.getAuthentication())) {
        throw new ForbiddenResponse();
      }

      if (rq.name != null) {
        team.setAuthentication(rq.name.orElseThrow(() -> new InternalServerErrorResponse()));
      }

      if (rq.password != null) {
        String hash =
            BCrypt.with(new SecureRandom())
                .hashToString(
                    6,
                    rq.password.orElseThrow(() -> new InternalServerErrorResponse()).toCharArray());
        team.setPasswordHash(hash);
      }

      if (rq.description != null) {
        team.setDescription(rq.description.orElse(null));
      }

      if (rq.country != null) {
        team.setCountry(rq.country.orElse(null));
      }

      if (!team.update()) {
        throw new InternalServerErrorResponse();
      }

      ctx.json(team);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/teams/{team-name}",
      methods = HttpMethod.DELETE,
      summary = "delete a team",
      operationId = "deleteTeam",
      tags = {"Team"},
      pathParams = {
        @OpenApiParam(name = "team-name", type = String.class, description = "The team name")
      },
      responses = {
        @OpenApiResponse(status = "204", description = "Successfully deleted resource"),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Admin permissions required",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void delete(Context ctx, String teamName) {

    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only an admin can delete the team for now
    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    try {
      Team team = Team.getByName(teamName);

      if (team == null) {
        throw new NotFoundResponse();
      }

      if (!team.delete()) {
        throw new InternalServerErrorResponse();
      }

      ctx.status(HttpStatus.NO_CONTENT);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApiName("TeamCreateRequest")
  public static record CreateRequest(
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "Team 1")
          @OpenApiRequired()
          String name,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "password")
          @OpenApiRequired()
          String password,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "description")
          @OpenApiRequired()
          @OpenApiNullable()
          String description,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "Switzerland")
          @OpenApiRequired()
          @OpenApiNullable()
          String country) {}

  @OpenApiName("TeamUpdateRequest")
  public static record UpdateRequest(
      @OpenApiPropertyType(definedBy = String.class) @OpenApiExample(value = "Team 1")
          Optional<String> name,
      @OpenApiPropertyType(definedBy = String.class) @OpenApiExample(value = "password")
          Optional<String> password,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "description")
          @OpenApiNullable()
          Optional<String> description,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "Switzerland")
          @OpenApiNullable()
          Optional<String> country) {}
}
