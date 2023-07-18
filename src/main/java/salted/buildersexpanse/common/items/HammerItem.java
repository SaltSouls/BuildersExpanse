package salted.buildersexpanse.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import salted.buildersexpanse.BuildersExpanse;
import salted.buildersexpanse.common.tags.BETags;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Mod.EventBusSubscriber(modid = BuildersExpanse.MODID)
public class HammerItem extends DiggerItem {

    public HammerItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, tier, BETags.MINEABLE_WITH_HAMMER, properties);
    }

    private static final Set<UUID> breakers = new HashSet<>();
    public static boolean isHammer(ItemStack item) { return item.getItem() instanceof HammerItem; }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.getLevel();
        ItemStack item = player.getMainHandItem();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        TagKey<Block> bulkMine = BETags.HAMMER_BULK_MINE;

        if (isHammer(item) && canBulkMine(state, bulkMine)) {
            float hardness = state.getDestroySpeed(level, pos);
            breakExtras(player, hardness, bulkMine);
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        Level level = player.getLevel();
        ItemStack item = player.getMainHandItem();

        if (isHammer(item)) {
            BlockPos pos = event.getPosition().orElseThrow();
            float speed = event.getState().getDestroySpeed(level, pos);
            int mined = getAmountMined(player, pos, getRadius(3), getDepth(1));

            if (mined > 1) { event.setNewSpeed((speed / mined) + (mined - ((float) mined / 2))); }
        }
    }

    private static void breakExtras(Player player, float hardness, TagKey<Block> tag) {
        if(!breakers.add(player.getUUID())) return;
        if(!player.isShiftKeyDown()) try {
            breakRadius(player, getRadius(3), getDepth(1), hardness, tag);
        } catch (Exception e) { e.printStackTrace(); }
        breakers.remove(player.getUUID());
    }

    public static void breakRadius(Player player, int radius, int depth, float hardness, TagKey<Block> tag) {
        Level level = player.level;
        BlockHitResult result = getTargetedBlock(player);
        BlockPos pos = result.getBlockPos();

        if (result.getType() != HitResult.Type.BLOCK) return;
        Iterable<BlockPos> list = getBlocks(player, pos, radius, depth, result);
        for (BlockPos listPos : list) {
            BlockState state = level.getBlockState(listPos);
            float stateHardness = state.getDestroySpeed(level, listPos);
            if (!state.isAir() && stateHardness != -1 && stateHardness <= hardness * 3F && isEffective(player, listPos, tag)) {
                ItemStack item = player.getMainHandItem();
                tryHarvestBlock((ServerPlayer) player, listPos);
                item.setDamageValue(item.getDamageValue() - 1);
            }
        }
    }

    public static BlockHitResult getTargetedBlock(LivingEntity entity) {
        double reach = entity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        return (BlockHitResult) entity.pick(reach, 0F, false);
    }

    public static int getAmountMined(Player player, BlockPos pos, int radius, int depth) {
        BlockHitResult result = getTargetedBlock(player);
        Iterable<BlockPos> list = getBlocks(player, pos, radius, depth, result);
        return (int) StreamSupport.stream(list.spliterator(), false).count();
    }

    public static Iterable<BlockPos> getBlocks(Player player, BlockPos pos, int radius, int depth, BlockHitResult result) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int upperX = getAdjacent(x, radius), lowerX = getOpposite(upperX, radius);
        int upperZ = getAdjacent(z, radius), lowerZ = getOpposite(upperZ, radius);
        int upperY = getAdjacent(y, radius), lowerY = getOpposite(upperY, radius);

        Direction.Axis axis = player.getDirection().getAxis();
        Direction face = result.getDirection();
        if (face.getAxis().isVertical()) {
            return BlockPos.betweenClosed(lowerX, y - depth, lowerZ, upperX, y + depth, upperZ);
        } else if (axis == Direction.Axis.X) {
            return BlockPos.betweenClosed(x - depth, lowerY, lowerZ, x + depth, upperY, upperZ);
        }
        return BlockPos.betweenClosed(lowerX, lowerY, z - depth, upperX, upperY, z + depth);
    }

    public static void tryHarvestBlock(ServerPlayer player, BlockPos pos) { player.gameMode.destroyBlock(pos); }

    static boolean isEffective(@NotNull Player player, BlockPos pos, TagKey<Block> tag) {
        Level level = player.getLevel();
        BlockState state = level.getBlockState(pos);
        return state.canHarvestBlock(level, pos, player) && canBulkMine(state, tag);
    }

    public static boolean canBulkMine(BlockState state, TagKey<Block> tag) { return state.is(tag); }

    /*
        Helps with getting the upper and lower bounds for the AOE
     */
    static int getAdjacent(int coord, int radius) { return coord + radius; }
    static int getOpposite(int coord, int radius) { return coord - (radius * 2); }
    static int getRadius(int radius) { return radius - 2; }
    static int getDepth(int depth) { return depth - 1; }

}
