package com.google.sps.data;
 
/** An item containing visitor information and comment. */
public final class Message {
 
  private final String name;
  private final String job;
  private final String comment; 
 
  public Message(String name, String job, String comment) {
    this.name = name;
    this.job = job;
    this.comment = comment;
  }
}
