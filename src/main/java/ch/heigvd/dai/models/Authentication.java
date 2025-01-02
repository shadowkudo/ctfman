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

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Timestamp deletedAt) {
    this.deletedAt = deletedAt;
  }

}
