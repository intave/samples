package ac.intave.samples.share;

public final class HitboxSize {
  private final float width;
  private final float height;

  public HitboxSize(float width, float height) {
    this.width = width;
    this.height = height;
  }

  public static HitboxSize of(float width, float height) {
    return new HitboxSize(width, height);
  }

  public static HitboxSize zero() {
    return new HitboxSize(0, 0);
  }

  public static HitboxSize playerDefault() {
    return new HitboxSize(0.6f, 1.8f);
  }

  public float width() {
    return width;
  }

  public float height() {
    return height;
  }

  public HitboxSize scaled(double scale) {
    return new HitboxSize(width * (float) scale, height * (float) scale);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof HitboxSize)) {
      return false;
    }
    HitboxSize other = (HitboxSize) object;
    return Float.compare(width, other.width) == 0 && Float.compare(height, other.height) == 0;
  }

  @Override
  public int hashCode() {
    int result = Float.hashCode(width);
    return 31 * result + Float.hashCode(height);
  }

  @Override
  public String toString() {
    return "(" + width + ", " + height + ")";
  }
}
