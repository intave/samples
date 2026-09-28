/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Updates the recorded player's sidebar scoreboard title, one line, or both.
 * A null title leaves the title unchanged. A null line and text leave all lines
 * unchanged. Line positions are zero-based, ordered from top to bottom.
 */
public final class ScoreboardEvent extends Event {
  @SerializedName("title")
  private String title;
  @SerializedName("line")
  private Integer line;
  @SerializedName("text")
  private String text;

  public ScoreboardEvent() {
  }

  /** Updates only the title. */
  public ScoreboardEvent(String title) {
    this.title = Objects.requireNonNull(title, "title");
  }

  /** Updates only the given line. */
  public ScoreboardEvent(int line, String text) {
    if (line < 0) {
      throw new IllegalArgumentException("line must be non-negative");
    }
    this.line = line;
    this.text = Objects.requireNonNull(text, "text");
  }

  /** Updates the given line and the title together. */
  public ScoreboardEvent(int line, String text, String title) {
    this(line, text);
    this.title = Objects.requireNonNull(title, "title");
  }

  /** Display title, preserved verbatim; null leaves it unchanged, empty blanks it. */
  public String title() {
    return title;
  }

  /** Zero-based display position, with zero at the top; null means no line update. */
  public Integer line() {
    return line;
  }

  /** Display text, preserved verbatim; an empty string sets a blank line. */
  public String text() {
    return text;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
