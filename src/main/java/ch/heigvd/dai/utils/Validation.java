package ch.heigvd.dai.utils;


public abstract class Validation {
  public static Boolean notBlank(String v) {
    return !v.isBlank();
  }
}
