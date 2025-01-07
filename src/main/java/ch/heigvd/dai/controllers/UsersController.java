package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.javalin.openapi.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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
            path = "/groups/{roles}",
            methods = HttpMethod.GET,
            summary = "get all users from same group",
            operationId = "getUsersByGroups",
            tags = { "User" },
            pathParams = {
                    @OpenApiParam(name = "role", type = User.Role.class, description = "The role")
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = { @OpenApiContent(from = User[].class, type = ContentType.JSON) })
            }
    )
    public void getGroups(Context ctx, User.Role role) {
        LOG.info("Je suis dans getGroups");
        try {
            List<User> users = User.getAll(role);
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
                tags = { "User" })
    public void create(Context ctx) {
            LOG.info("Je suis dans create");
            throw new MethodNotAllowedResponse();
    }

    @OpenApi(path = "/users/{id}", methods = HttpMethod.GET, summary = "get a user", operationId = "getOneUser",
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
}
