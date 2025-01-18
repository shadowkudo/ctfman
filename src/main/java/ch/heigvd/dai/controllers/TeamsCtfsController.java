package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.Ctf;
import ch.heigvd.dai.models.Team;
import ch.heigvd.dai.models.User;
import ch.heigvd.dai.utils.Validation;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import io.javalin.validation.BodyValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamsCtfsController {
  private static final Logger LOG = LoggerFactory.getLogger(CtfsController.class);

  @OpenApi(
      path = "/teams/{team-name}/ctfs",
      methods = HttpMethod.GET,
      summary = "get all the ctfs of a team",
      operationId = "getAllTeamCTFs",
      tags = {"CTF", "Team"},
      pathParams = {
        @OpenApiParam(name = "team-name", type = String.class, description = "The team name")
      },
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The list of ctfs",
            content = {@OpenApiContent(from = Ctf[].class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void getAll(Context ctx, String teamName) {
    try {
      if (!Team.exists(teamName)) {
        throw new NotFoundResponse();
      }

      List<Ctf> ctfs = Ctf.getAllByTeam(teamName);
      ctx.json(ctfs);

    } catch (SQLException e) {
      LOG.error(e.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/teams/{team-name}/ctfs",
      methods = HttpMethod.POST,
      summary = "Join a ctf as the team",
      operationId = "teamJoinCTF",
      tags = {"CTF", "Team"},
      pathParams = {
        @OpenApiParam(name = "team-name", type = String.class, description = "The team name")
      },
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
            description = "Only captain can join a CTF",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "404",
            description = "Team not found",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "409",
            description = "Team already joined",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void create(Context ctx, String teamName) {
    User user = ctx.attribute("user");

    if (!user.hasRole(User.Role.CHALLENGER)) {
      throw new ForbiddenResponse("only challengers can use this endpoint");
    }

    BodyValidator<CreateRequest> precheck =
        ctx.bodyValidator(CreateRequest.class).check(it -> it.ctf != null, "Missing ctf field");

    if (!precheck.errors().isEmpty()) {
      precheck.get();
      return;
    }

    CreateRequest req =
        ctx.bodyValidator(CreateRequest.class)
            .check(
                it -> it.ctf.map(Validation::notBlank).orElse(false),
                "Ctf field cannot be an empty string")
            .get();

    try {
      Team team = Team.getByName(teamName);

      if (team == null) {
        throw new NotFoundResponse("team not found");
      }

      if (!team.getCaptain().equals(user.getAuthentication())) {
        throw new ForbiddenResponse("user isn't the team captain");
      }

      Ctf ctf = Ctf.getByTitle(req.ctf().get());

      if (ctf == null) {
        throw new NotFoundResponse("associated ctf not found");
      }

      if (team.joinCtf(req.ctf.get()) != 1) {
        throw new InternalServerErrorResponse();
      }

      ctx.status(HttpStatus.CREATED);

    } catch (SQLException e) {
      if (e.getMessage()
          .contains(
              "duplicate key value violates unique constraint \"team_sign_up_to_ctf_pkey\"")) {
        throw new ConflictResponse("team already joind this ctf");
      }
      LOG.error(e.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApiName("TeamsCtfsUpdateRequest")
  public static record CreateRequest(
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiDescription("The ctf that the team wants to join")
          @OpenApiExample(value = "DEFCON")
          @OpenApiRequired()
          Optional<String> ctf) {}
}
