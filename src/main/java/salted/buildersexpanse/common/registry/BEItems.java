package salted.buildersexpanse.common.registry;


import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import salted.buildersexpanse.BuildersExpanse;
import salted.buildersexpanse.common.items.HammerItem;
import salted.buildersexpanse.common.items.SawItem;
import salted.buildersexpanse.common.tags.BETags;

public class BEItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BuildersExpanse.MODID);

    // used for base items or to get creative tab
    private static Item.Properties genericItem() {
        return new Item.Properties().tab(BuildersExpanse.CREATIVE_TAB);
    }

    /**
     * properties for {@link HammerItem}
     */
    private static Item.Properties genericHammer(Tiers tier) {
        return genericItem().durability((int) (tier.getUses() * 1.5));
    }
    private static final TagKey<Block> bulkMineTag = BETags.MINEABLE_WITH_HAMMER;
    private static final int radius = 3;
    private static final int depth = 1;

    /**
     * properties for {@link SawItem}
     */
    private static Item.Properties genericSaw(Tiers tier) {
        return genericItem().durability((int) (tier.getUses() * 1.5));
    }


    // hammers
    public static final RegistryObject<Item> WOODEN_HAMMER = ITEMS.register("wooden_hammer",
            () -> new HammerItem(Tiers.WOOD, 7.0F, -3.6F, genericHammer(Tiers.STONE), bulkMineTag, radius, depth));
    public static final RegistryObject<Item> STONE_HAMMER = ITEMS.register("stone_hammer",
            () -> new HammerItem(Tiers.STONE, 7.0F, -3.6F, genericHammer(Tiers.STONE), bulkMineTag, radius, depth));
    public static final RegistryObject<Item> IRON_HAMMER = ITEMS.register("iron_hammer",
            () -> new HammerItem(Tiers.IRON, 7.0F, -3.6F, genericHammer(Tiers.IRON), bulkMineTag, radius, depth));
    public static final RegistryObject<Item> GOLDEN_HAMMER = ITEMS.register("golden_hammer",
            () -> new HammerItem(Tiers.GOLD, 7.0F, -3.6F, genericHammer(Tiers.GOLD), bulkMineTag, radius, depth));
    public static final RegistryObject<Item> DIAMOND_HAMMER = ITEMS.register("diamond_hammer",
            () -> new HammerItem(Tiers.DIAMOND, 7.0F, -3.6F, genericHammer(Tiers.DIAMOND), bulkMineTag, radius, depth));
    public static final RegistryObject<Item> NETHERITE_HAMMER = ITEMS.register("netherite_hammer",
            () -> new HammerItem(Tiers.NETHERITE, 7.0F, -3.6F, genericHammer(Tiers.NETHERITE), bulkMineTag, radius, depth));

    // saws
    public static final RegistryObject<Item> IRON_SAW = ITEMS.register("iron_saw",
            () -> new SawItem(Tiers.IRON, 2.0F, -2.6F, genericSaw(Tiers.IRON)));
    public static final RegistryObject<Item> GOLDEN_SAW = ITEMS.register("golden_saw",
            () -> new SawItem(Tiers.GOLD, 2.0F, -2.6F, genericSaw(Tiers.GOLD)));
    public static final RegistryObject<Item> DIAMOND_SAW = ITEMS.register("diamond_saw",
            () -> new SawItem(Tiers.DIAMOND, 2.0F, -2.6F, genericSaw(Tiers.DIAMOND)));
    public static final RegistryObject<Item> NETHERITE_SAW = ITEMS.register("netherite_saw",
            () -> new SawItem(Tiers.NETHERITE, 2.0F, -2.6F, genericSaw(Tiers.NETHERITE)));

}
