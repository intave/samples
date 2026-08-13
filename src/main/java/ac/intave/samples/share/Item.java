package ac.intave.samples.share;

import java.util.Objects;

public final class Item {
  private final String type;
  private final int amount;
  private final ItemCategory category;
  private final boolean glowing;
  private final double baseQuality;
  private final double enchantmentQuality;

  public Item(
    String type, int amount, ItemCategory category,
    boolean glowing, double baseQuality, double enchantmentQuality
  ) {
    this.type = Objects.requireNonNull(type, "type");
    this.amount = amount;
    this.category = Objects.requireNonNull(category, "category");
    this.glowing = glowing;
    this.baseQuality = baseQuality;
    this.enchantmentQuality = enchantmentQuality;
  }

  public static Item air() {
    return new Item("AIR", 0, ItemCategory.OTHER, false, 0, 0);
  }

  public String type() {
    return type;
  }

  public int amount() {
    return amount;
  }

  public ItemCategory category() {
    return category;
  }

  public boolean glowing() {
    return glowing;
  }

  public double baseQuality() {
    return baseQuality;
  }

  public double enchantmentQuality() {
    return enchantmentQuality;
  }

  @Override
  public String toString() {
    if ("AIR".equalsIgnoreCase(type)) {
      return "Item{AIR}";
    }
    return "Item{" +
      "type='" + type + '\'' +
      ", amount=" + amount +
      ", category=" + category +
      ", glowing=" + glowing +
      ", baseQuality=" + baseQuality +
      ", enchantmentQuality=" + enchantmentQuality +
      '}';
  }
}
