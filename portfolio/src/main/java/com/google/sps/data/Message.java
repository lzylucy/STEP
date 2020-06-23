package com.google.sps.data;
 
/** An item containing visitor information and comment. */
public final class Message {
    
  public Message(long id, String name, String job, String comment, long timestamp) {
    this.id = id;
    this.name = name;
    this.job = job;
    this.comment = comment;
    this.timestamp = timestamp;
  }

  private final long id;
  private final String name;
  private final String job;
  private final String comment;
  private final long timestamp;
}
