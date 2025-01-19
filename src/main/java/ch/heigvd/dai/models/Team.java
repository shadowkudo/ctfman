package ch.heigvd.dai.models;

import ch.heigvd.dai.db.DB;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.openapi.OpenApiExample;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Team extends Authentication {

  private String authentication;
  @JsonIgnore private String oldAuthentication;
  private String description;
  private String country;
  private String captain;

  public Team() {
    super();
  }

  public Team(
      @NotNull String authentication,
      @Nullable String description,
      @Nullable String country,
      @Nullable String captain) {
    this(authentication, description, country, captain, null, null, null);
  }

  public Team(
      @NotNull String authentication,
      @Nullable String description,
      @Nullable String country,
      @Nullable String captain,
      @Nullable String passwordHash,
      @Nullable Timestamp createdAt,
      @Nullable Timestamp deletedAt) {
    super(passwordHash, createdAt, deletedAt);
    this.authentication = authentication;
    this.description = description;
    this.country = country;
    this.captain = captain;
    this.oldAuthentication = authentication;
  }

  @OpenApiExample("Team 1")
  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }

  @OpenApiExample("Lorem ipsum dolor sit amet.")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @OpenApiExample("Switzerland")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  @OpenApiExample("user1")
  public String getCaptain() {
    return captain;
  }

  public void setCaptain(String captain) {
    this.captain = captain;
  }

  public static boolean exists(String name) throws SQLException {
    String query = "SELECT authentication FROM team WHERE authentication = ? LIMIT 1";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet res = stmt.executeQuery();

      return res.next();
    }
  }

  public int joinCtf(String ctf) throws SQLException {
    String query = "INSERT INTO team_sign_up_to_ctf(team, ctf) VALUES(?, ?)";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, oldAuthentication);
      stmt.setString(2, ctf);
      return stmt.executeUpdate();
    }
  }

  public static List<Team> getAll() throws SQLException {
    return getAll(true);
  }

  public static List<Team> getAll(boolean ignoreDeleted) throws SQLException {
    List<Team> teams = new ArrayList<>();
    String query = "SELECT * FROM team_with_captain_view";

    if (ignoreDeleted) {
      query = query + " WHERE deleted_at IS NULL";
    }

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet res = stmt.executeQuery();

      while (res.next()) {
        teams.add(
            new Team(
                res.getString("authentication"),
                res.getString("description"),
                res.getString("country"),
                res.getString("captain"),
                null,
                res.getTimestamp("created_at"),
                res.getTimestamp("deleted_at")));
      }
    }

    return teams;
  }

  public static @Nullable Team getByName(String name) throws SQLException {
    return getByName(name, true);
  }

  public static @Nullable Team getByName(String name, boolean ignoreDeleted) throws SQLException {
    String query = "SELECT * FROM team_with_captain_view WHERE authentication = ?";

    if (ignoreDeleted) {
      query = query + " AND deleted_at IS NULL";
    }

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet res = stmt.executeQuery();

      if (!res.next()) {
        return null;
      }

      return new Team(
          res.getString("authentication"),
          res.getString("description"),
          res.getString("country"),
          res.getString("captain"),
          res.getString("password_hash"),
          res.getTimestamp("created_at"),
          res.getTimestamp("deleted_at"));
    }
  }

  public void insert() throws SQLException {
    // create_team(authentication, password_hash, captain, description, country)
    String query = "CALL create_team(?, ?, ?, ?, ?)";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, this.authentication);
      stmt.setString(2, this.passwordHash);
      stmt.setString(3, this.captain);
      stmt.setString(4, this.description);
      stmt.setString(5, this.country);

      stmt.executeUpdate();
    }
  }

  public boolean update() throws SQLException {
    String updateTeam = "UPDATE team SET description = ?, country = ? WHERE authentication = ?";
    String updateAuth = "UPDATE authentication SET identification = ? WHERE identification = ?";

    try (Connection conn = DB.getConnection()) {
      conn.setAutoCommit(false);

      PreparedStatement stmt = conn.prepareStatement(updateTeam);
      stmt.setString(1, this.description);
      stmt.setString(2, this.country);
      stmt.setString(3, this.oldAuthentication);

      if (stmt.executeUpdate() != 1) {
        conn.rollback();
        return false;
      }

      stmt = conn.prepareStatement(updateAuth);
      stmt.setString(1, this.authentication);
      stmt.setString(2, this.oldAuthentication);

      if (stmt.executeUpdate() != 1) {
        conn.rollback();
        return false;
      }

      conn.commit();
    }

    return true;
  }

  public boolean delete() throws SQLException {

    String query = "UPDATE authentication SET deleted_at = NOW() WHERE identification = ?";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, oldAuthentication);

      if (stmt.executeUpdate() != 1) {
        return false;
      }
    }

    return true;
  }
}
