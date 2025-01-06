package ch.heigvd.dai.models;

import ch.heigvd.dai.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Team extends Authentication {

  private static final Logger LOG = LoggerFactory.getLogger(Team.class);

  private String authentication;
  private String description;
  private String country;

  public Team() {
    super();
  }

  public Team(
      @NotNull String authentication, @Nullable String description, @Nullable String country) {
    this(authentication, description, country, null, null, null);
  }

  public Team(
      @NotNull String authentication,
      @Nullable String description,
      @Nullable String country,
      @Nullable String passwordHash,
      @Nullable Timestamp createdAt,
      @Nullable Timestamp deletedAt) {
    super(passwordHash, createdAt, deletedAt);
    this.authentication = authentication;
    this.description = description;
    this.country = country;
  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public static List<Team> getAll() throws SQLException {
    List<Team> teams = new ArrayList<>();
    String query =
        "SELECT authentication, description, country, created_at, deleted_at FROM team JOIN"
            + " authentication a ON team.authentication = a.identification";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet res = stmt.executeQuery();

      while (res.next()) {
        teams.add(
            new Team(
                res.getString("authentication"),
                res.getString("description"),
                res.getString("country"),
                null,
                res.getTimestamp("created_at"),
                res.getTimestamp("deleted_at")));
      }
    }

    return teams;
  }

  public static @Nullable Team getByName(String name) throws SQLException {
    String query =
        "SELECT authentication, description, country, created_at, deleted_at FROM team JOIN"
            + " authentication a ON team.authentication = a.identification WHERE authentication ="
            + " ?";

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
          null,
          res.getTimestamp("created_at"),
          res.getTimestamp("deleted_at"));
    }
  }

  public void insertWithCaptain(User user) throws SQLException {
    // create_team(authentication, password_hash, captain, description, country)
    String query = "CALL create_team(?, ?, ?, ?, ?)";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, this.authentication);
      stmt.setString(2, this.passwordHash);
      stmt.setString(3, user.getAuthentication());
      stmt.setString(4, this.description);
      stmt.setString(5, this.country);

      stmt.executeUpdate();
    }
  }
}
