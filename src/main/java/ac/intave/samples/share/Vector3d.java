package ac.intave.samples.share;

public final class Vector3d {
  public static final Vector3d ZERO = new Vector3d(0, 0, 0);
  public static final Vector3d X_AXIS = new Vector3d(1, 0, 0);
  public static final Vector3d Y_AXIS = new Vector3d(0, 1, 0);
  public static final Vector3d Z_AXIS = new Vector3d(0, 0, 1);

  private final double x;
  private final double y;
  private final double z;

  public Vector3d(double x, double y, double z) {
    this.x = x == -0.0D ? 0.0D : x;
    this.y = y == -0.0D ? 0.0D : y;
    this.z = z == -0.0D ? 0.0D : z;
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  public double z() {
    return z;
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public Vector3d scale(double factor) {
    return new Vector3d(x * factor, y * factor, z * factor);
  }

  public Vector3d add(Vector3d other) {
    return new Vector3d(x + other.x, y + other.y, z + other.z);
  }

  public Vector3d subtract(Vector3d other) {
    return new Vector3d(x - other.x, y - other.y, z - other.z);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof Vector3d)) {
      return false;
    }
    Vector3d other = (Vector3d) object;
    return Double.compare(x, other.x) == 0
      && Double.compare(y, other.y) == 0
      && Double.compare(z, other.z) == 0;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(x);
    result = 31 * result + Double.hashCode(y);
    return 31 * result + Double.hashCode(z);
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ", " + z + ")";
  }
}
