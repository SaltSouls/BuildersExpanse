package salted.buildersexpanse.common.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import salted.buildersexpanse.BuildersExpanse;

import java.util.function.ToIntFunction;

@Mod.EventBusSubscriber(modid = BuildersExpanse.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BEBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildersExpanse.MODID);

    private static ToIntFunction<BlockState> lightLevel(int lightValue) {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    // workstations


}
