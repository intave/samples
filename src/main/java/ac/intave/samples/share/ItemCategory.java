package ac.intave.samples.share;

public enum ItemCategory {
  SWORD(0, new double[]{6.4, 6.4, 8.0, 9.6, 11.2, 12.8}),
  PICKAXE(1, new double[]{2.4, 2.4, 3.6, 4.8, 6.0, 7.2, 8.4}),
  SHOVEL(2, new double[]{2.5, 2.5, 3.5, 4.5, 5.5, 10.4}),
  AXE(3, new double[]{5.6, 7.0, 7.2, 8.1, 9.0, 10.0}),
  BOW(4),
  HELMET(5),
  CHESTPLATE(6),
  LEGGINGS(7),
  BOOTS(8),
  BLOCK(9),
  OTHER(10);

  private final int id;
  private final double[] attackDamage;

  ItemCategory(int id) {
    this(id, new double[]{1, 1, 1, 1, 1, 1});
  }

  ItemCategory(int id, double[] attackDamage) {
    this.id = id;
    this.attackDamage = attackDamage;
  }

  public int id() {
    return id;
  }

  public double attackDamage(int materialIndex) {
    if (materialIndex < 0 || materialIndex >= attackDamage.length) {
      return 0;
    }
    return attackDamage[materialIndex];
  }

  public boolean isArmor() {
    return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
  }

  public boolean isTool() {
    return this == PICKAXE || this == SHOVEL || this == AXE;
  }

  public boolean isSword() {
    return this == SWORD;
  }

  public boolean isBlock() {
    return this == BLOCK;
  }
}
