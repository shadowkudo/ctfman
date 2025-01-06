package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.MethodNotAllowedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import java.sql.SQLException;
import java.util.List;

public class UsersController implements CrudHandler {

    @OpenApi(path = "/users", methods = HttpMethod.GET, summary = "get all users", operationId = "getAllUsers", tags = {
            "User" }, responses = {
            @OpenApiResponse(status = "200", content = { @OpenApiContent(from = User[].class) })
    })
    public void getAll(Context ctx) { throw new MethodNotAllowedResponse(); }

    @OpenApi(path = "/users", methods = HttpMethod.POST, summary = "create a user", operationId = "createUser",
            tags = { "User" })
    public void create(Context ctx) { throw new MethodNotAllowedResponse(); }

    @OpenApi(path = "/users/{id}", methods = HttpMethod.GET, summary = "get a user", operationId = "getOneUser",
            tags = { "User" }, responses = {
            @OpenApiResponse(status = "200", content = { @OpenApiContent(from = User.class) })
    })
    public void getOne(Context ctx, String id) { throw new MethodNotAllowedResponse(); }

    @OpenApi(path = "/users/{id}", methods = { HttpMethod.PUT,
            HttpMethod.PATCH }, summary = "update a user", operationId = "updateUser", tags = { "User" })
    public void update(Context ctx, String id) {
        throw new MethodNotAllowedResponse();
    }

    @OpenApi(path = "/users/{id}", methods = HttpMethod.DELETE, summary = "delete a user", operationId = "deleteUser",
            tags = { "User" })
    public void delete(Context ctx, String id) {
        throw new MethodNotAllowedResponse();
    }
}
