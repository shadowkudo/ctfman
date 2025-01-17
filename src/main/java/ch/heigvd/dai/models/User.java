package ch.heigvd.dai.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.heigvd.dai.db.DB;

import static org.eclipse.jetty.http.HttpParser.LOG;

public class User extends Authentication {

  private String authentication;
  private String primaryContact;
  private Boolean isChallenger = false;
  private Boolean isAdmin = false;
  private Boolean isModerator = false;
  private Boolean isAuthor = false;

  // TODO: photo

  public User(String name, String primaryContact) {
    this(name, primaryContact, null, null, null, null, null, null, null);
  }

  public User(String name, String primaryContact, String passwordHash, Role role) {
    super(passwordHash, new Timestamp(System.currentTimeMillis()), null);
    this.authentication = name;
    this.primaryContact = primaryContact;
    switch (role) {
      case ADMIN: isAdmin = true; break;
      case MODERATOR: isModerator = true; break;
      case AUTHOR: isAuthor = true; break;
      case CHALLENGER: isChallenger = true; break;
      case null, default: isChallenger = true; break;
    }
  }

  public User(String name, String primaryContact, String passwordHash, Timestamp createdAt, Timestamp deletedAt,
      Boolean isChallenger, Boolean isAdmin, Boolean isModerator, Boolean isAuthor) {
    super(passwordHash, createdAt, deletedAt);
    this.authentication = name;
    this.primaryContact = primaryContact;
    this.isChallenger = isChallenger;
    this.isAdmin = isAdmin;
    this.isModerator = isModerator;
    this.isAuthor = isAuthor;
  }

  /**
   * Find a user by name.
   * This includes information on whether the user is a
   * challenger/admin/moderator/author
   */
  public static @Nullable User findByName(@NotNull String name) throws SQLException {

    String query = "SELECT * FROM user_role_view WHERE authentication = ?";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet res = stmt.executeQuery();

      if (res.next()) {
        return new User(
            res.getString("authentication"),
            res.getString("primary_contact"),
            res.getString("password_hash"),
            res.getTimestamp("created_at"),
            res.getTimestamp("deleted_at"),
            res.getBoolean("is_challenger"),
            res.getBoolean("is_admin"),
            res.getBoolean("is_moderator"),
            res.getBoolean("is_author"));
      }

    }

    return null;
  }

  public static List<User> getAll(Role role) throws SQLException {
    List<User> list = new ArrayList<>();
    /* Create the query to get the role asked */
    String query = switch (role) {
        case ADMIN -> "SELECT au.identification " +
                " FROM admin a " +
                " JOIN authentication au ON a.manager = au.identification " +
                " WHERE au.deleted_at IS NULL;";
        case MODERATOR -> "SELECT au.identification " +
                " FROM moderator m " +
                " JOIN authentication au ON m.manager = au.identification " +
                " WHERE au.deleted_at IS NULL;";
        case AUTHOR -> "SELECT au.identification " +
                " FROM author a " +
                " JOIN authentication au ON a.manager = au.identification " +
                " WHERE au.deleted_at IS NULL;";
        case CHALLENGER -> "SELECT au.identification " +
                " FROM challenger ch " +
                " JOIN authentication au ON ch.user_account = au.identification " +
                " WHERE au.deleted_at IS NULL;";
        case ALL -> "SELECT au.identification" +
                " FROM user_account ua" +
                " JOIN authentication au ON au.identification = ua.authentication" +
                " WHERE au.deleted_at IS NULL;";
        default -> throw new NotFoundResponse();
    };

      try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet res = stmt.executeQuery();

        while (res.next()) list.add(findByName(res.getString("identification")));
    }
    return list;
  }

  public void insert_challenger() throws SQLException {
    String query = "CALL create_challenger(?,?,?,NULL,NULL,NULL,NULL)";
    try (Connection conn = DB.getConnection()){
    PreparedStatement stmt = DB.getConnection().prepareStatement(query);
    stmt.setString(1, this.authentication);
    stmt.setString(2, this.passwordHash);
    stmt.setString(3, this.primaryContact);
    LOG.info(stmt.toString());

      stmt.executeUpdate();
    }
  }

  public void insert_manager(Role role) throws SQLException {
    String query = "CALL create_manager(?,?,?,NULL,?,?,?)";
    try (Connection conn = DB.getConnection()){
      PreparedStatement stmt = DB.getConnection().prepareStatement(query);
      stmt.setString(1, this.authentication);
      stmt.setString(2, this.passwordHash);
      stmt.setString(3, this.primaryContact);
      switch (role) {
        case ADMIN:
          stmt.setBoolean(4, true);
          stmt.setBoolean(5, false);
          stmt.setBoolean(6, false);
          break;
        case MODERATOR:
          stmt.setBoolean(4, false);
          stmt.setBoolean(5, true);
          stmt.setBoolean(6, false);
          break;
        case AUTHOR:
          stmt.setBoolean(4, false);
          stmt.setBoolean(5, false);
          stmt.setBoolean(6, true);
      }
      LOG.info(stmt.toString());

      stmt.executeUpdate();
    }

  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }

  public String getPrimaryContact() {
    return primaryContact;
  }

  public void setPrimaryContact(String primaryContact) {
    this.primaryContact = primaryContact;
  }

  public Boolean getIsChallenger() {
    return isChallenger;
  }

  public void setIsChallenger(Boolean isChallenger) {
    this.isChallenger = isChallenger;
  }

  public Boolean getIsAdmin() { return isAdmin; }

  public void setIsAdmin(Boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public Boolean getIsModerator() {
    return isModerator;
  }

  public void setIsModerator(Boolean isModerator) {
    this.isModerator = isModerator;
  }

  public Boolean getIsAuthor() {
    return isAuthor;
  }

  public void setIsAuthor(Boolean isAuthor) {
    this.isAuthor = isAuthor;
  }

  public Boolean hasRole(Role role) {
    return switch (role) {
      case ADMIN -> isAdmin;
      case AUTHOR -> isAuthor;
      case CHALLENGER -> isChallenger;
      case MODERATOR -> isModerator;
      case ALL -> isChallenger;
    };
  }

  public static enum Role {
    CHALLENGER, ADMIN, MODERATOR, AUTHOR, ALL
  }

}
