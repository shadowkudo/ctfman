package ch.heigvd.dai.models;

import java.sql.Timestamp;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Authentication {
  @JsonIgnore
  protected String passwordHash;
  protected Timestamp createdAt;
  protected Timestamp deletedAt;

  public Authentication() {

  }

  public Authentication(@Nullable String passwordHash, @Nullable Timestamp createdAt, @Nullable Timestamp deletedAt) {
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
    this.deletedAt = deletedAt;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getDeletedAt() {
    return deletedAt;
  }
}
