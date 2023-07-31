package salted.buildersexpanse.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import salted.buildersexpanse.BuildersExpanse;
import salted.buildersexpanse.common.tags.BETags;

import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = BuildersExpanse.MODID)
public class HammerItem extends DiggerItem {

    private final TagKey<Block> blocks;

    public HammerItem(Tier tier, float attackDamage, float attackSpeed, Properties properties, TagKey<Block> blocks) {
        super(attackDamage, attackSpeed, tier, BETags.MINEABLE_WITH_HAMMER, properties);
        this.blocks = blocks;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack item, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entity) {
        if (level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && !state.is(this.blocks)) { return true; }

        float hardness = state.getDestroySpeed(level, pos);
        this.breakExtras(entity, getRadius(3), getDepth(1), hardness, this.blocks);
        return super.mineBlock(item, level, state, pos, entity);
    }

    //TODO: find a non-event based way to do this
    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        Level level = player.getLevel();
        ItemStack item = player.getMainHandItem();
        BlockPos pos = event.getPosition().orElseThrow();
        BlockState state = level.getBlockState(pos);

        if (isHammer(item) && canBulkMine(state, BETags.CAN_BULK_MINE)) {
            if(!player.isShiftKeyDown()) {
                int radius = getRadius(3);
                int depth = getDepth(1);
                int mined = getAmountMined(player, pos, radius, depth);

                if (mined > 1) {
                    Iterable<BlockPos> list = getBlocks(player, pos, radius, depth, getTargetedBlock(player));
                    AtomicReference<Float> tempDestroySpeed = new AtomicReference<>(0F);

                    list.forEach(listPos -> {
                        if (isMineable(player, listPos, BETags.CAN_BULK_MINE)) {
                            BlockState listState = level.getBlockState(listPos);

                            float destroySpeed = listState.getDestroySpeed(level, listPos);
                            tempDestroySpeed.updateAndGet(s -> s + destroySpeed);
                        }
                    });

                    float totalDestroySpeed = tempDestroySpeed.get();
                    float destroySpeed = event.getState().getDestroySpeed(level, pos);
                    float newSpeed = event.getOriginalSpeed();
                    newSpeed *= (float) (destroySpeed / (totalDestroySpeed * (1 - 0.1 * Math.sqrt(mined - 1))));

                    event.setNewSpeed(newSpeed);
                    return;
                }
            }
            event.setNewSpeed(event.getOriginalSpeed() - 0.75F);
            return;
        }
        //TODO: add config option to disable pickaxe buff
        boolean isPickaxe = item.getItem() instanceof PickaxeItem;
        if(isPickaxe) { event.setNewSpeed(event.getOriginalSpeed() + 0.5F);}
    }

    public void breakExtras(@NotNull LivingEntity entity, int radius, int depth, float hardness, TagKey<Block> tag) {
        Level level = entity.level;
        BlockHitResult result = getTargetedBlock(entity);
        BlockPos pos = result.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if(!entity.isShiftKeyDown() && state.is(tag)) {
            if (result.getType() != HitResult.Type.BLOCK) return;
            Iterable<BlockPos> list = getBlocks(entity, pos, radius, depth, result);

            list.forEach(listPos -> {
                BlockState listState = level.getBlockState(listPos);
                if (!listState.isAir() && hardness != -1 && isEffective(entity, listPos, tag)) {
                    if(entity instanceof Player && !level.isClientSide) {
                        harvestBlocks((Player) entity, (ServerLevel) level, listState, listPos);
                    }
                }
            });
        }
    }

    public static int getAmountMined(Player player, BlockPos pos, int radius, int depth) {
        BlockHitResult result = getTargetedBlock(player);
        Iterable<BlockPos> list = getBlocks(player, pos, radius, depth, result);
        AtomicReference<Integer> mined = new AtomicReference<>(0);
        list.forEach(listPos -> {
            if(isMineable(player, listPos, BETags.CAN_BULK_MINE)) {
                mined.updateAndGet(m -> m + 1);
            }
        });
        return mined.get();
    }

    public void harvestBlocks(@NotNull Player player, ServerLevel level, BlockState state, BlockPos pos) {
        ItemStack item = player.getMainHandItem();
        if(!player.isCreative()) {
            if(isEffective(player, pos, this.blocks)) {
                level.destroyBlock(pos, false, player);
                state.spawnAfterBreak(level, pos, item, true);
                Iterable<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, item);
                drops.forEach(items -> {
                    Block.popResourceFromFace(level, pos, getTargetedBlock(player).getDirection(), items);
                });
            }
        }
    }

    /**
     * Gets an {@link Iterable} list of blocks within a defined radius.
     * @param entity the {@link LivingEntity} we are using
     * @param pos the {@link BlockPos} of the block we are targeting
     * @param radius the radius around the targeted block
     * @param depth the depth starting from the targeted block
     * @param result the {@link BlockHitResult}
     * @return A list of blocks poses based on the {@code depth} and {@code radius}.
     */
    public static Iterable<BlockPos> getBlocks(@NotNull LivingEntity entity, @NotNull BlockPos pos, int radius, int depth, @NotNull BlockHitResult result) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int upperX = getAdjacent(x, radius), lowerX = getOpposite(upperX, radius);
        int upperZ = getAdjacent(z, radius), lowerZ = getOpposite(upperZ, radius);
        int upperY = getAdjacent(y, radius), lowerY = getOpposite(upperY, radius);
        Direction.Axis axis = entity.getDirection().getAxis();
        Direction face = result.getDirection();
        if (face.getAxis().isVertical()) {
            return BlockPos.betweenClosed(lowerX, y - depth, lowerZ, upperX, y + depth, upperZ);
        } else if (axis == Direction.Axis.X) {
            return BlockPos.betweenClosed(x - depth, lowerY, lowerZ, x + depth, upperY, upperZ);
        }
        return BlockPos.betweenClosed(lowerX, lowerY, z - depth, upperX, upperY, z + depth);
    }

    /**
     * Get the currently targeted block.
     * @param entity the {@link LivingEntity} we are using
     * @return The {@link BlockHitResult} based on where the entity is looking.
     */
    public static BlockHitResult getTargetedBlock(@NotNull LivingEntity entity) {
        double reach = entity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        return (BlockHitResult) entity.pick(reach, 0F, false);
    }

    /**
     * Checks to see if the item is an instance of the {@link HammerItem}.
     * @param item the {@link ItemStack} we are using
     * @return True if the item is a hammer.
     */
    public static boolean isHammer(@NotNull ItemStack item) { return item.getItem() instanceof HammerItem; }

    /**
     * Checks to see if the block in question has the correct tag to be bulk mined.
     * @param state the {@link BlockState} of the block in question
     * @param tag the {@link TagKey} needed
     * @return True if the block has the 'can_bulk_mine' tag.
     */
    public static boolean canBulkMine(@NotNull BlockState state, TagKey<Block> tag) { return state.is(tag); }

    /**
     * Checks to see if the item being used is the correct tool for breaking said block.
     * @param entity the {@link LivingEntity} we are using
     * @param pos the {@link BlockPos} of the block in question
     * @param tag the {@link TagKey} needed
     * @return True if the tool is the correct one for drops.
     */
    public boolean isEffective(@NotNull LivingEntity entity, BlockPos pos, TagKey<Block> tag) {
        ItemStack item = entity.getMainHandItem();
        Level level = entity.getLevel();
        BlockState state = level.getBlockState(pos);
        if(entity instanceof Player && isHammer(item)) {
            return this.isCorrectToolForDrops(item, state) && canBulkMine(state, tag);
        }
        return false;
    }

    /**
     * Less strict and static version of isEffective(). Not suitable for breaking.
     * Should only be used for getting amount of blocks mine, or the break speed.
     * @param player the {@link Player} we are using
     * @param pos the {@link BlockPos} of the block in question
     * @param tag the {@link TagKey} needed
     * @return True if the tool is the correct one for breaking.
     */
    public static boolean isMineable(@NotNull Player player, BlockPos pos, TagKey<Block> tag) {
        Level level = player.getLevel();
        BlockState state = level.getBlockState(pos);
        return canBulkMine(state, tag) && state.canHarvestBlock(level, pos, player);
    }

    /**
     * Gets the block to the side of the targeted block.
     */
    static int getAdjacent(int coord, int radius) { return coord + radius; }
    /**
     * Gets the opposite coordinate from the one gotten in getAdjacent().
     */
    static int getOpposite(int coord, int radius) { return coord - (radius * 2); }
    static int getRadius(int radius) { return radius - 2; }
    static int getDepth(int depth) { return depth - 1; }

}
