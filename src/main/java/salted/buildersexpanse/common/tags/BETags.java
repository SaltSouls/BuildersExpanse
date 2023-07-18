package salted.buildersexpanse.common.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import salted.buildersexpanse.BuildersExpanse;

public class BETags {
    public static final TagKey<Block> MINEABLE_WITH_HAMMER = modBlockTag("mineable/hammer");
    public static final TagKey<Block> HAMMER_BULK_MINE = modBlockTag("hammer_bulk_mine");
    public static final TagKey<Block> MINEABLE_WITH_SAW = modBlockTag("mineable/saw");

    private static TagKey<Block> modBlockTag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(BuildersExpanse.MODID, path));
    }
}
