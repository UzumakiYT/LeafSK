package com.leaf.skriptmirror;

public class Null {
  private static Null instance = new Null();

  private Null() {}

  public static Null getInstance() {
    return instance;
  }


}
