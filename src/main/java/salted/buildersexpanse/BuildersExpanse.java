package salted.buildersexpanse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import salted.buildersexpanse.common.registry.BEBlocks;
import salted.buildersexpanse.common.registry.BEItems;

import javax.annotation.Nonnull;

@Mod(BuildersExpanse.MODID)
public class BuildersExpanse {
    public static final String MODID = "buildersexpanse";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab(BuildersExpanse.MODID) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(BEItems.IRON_SAW.get());
        }
    };

    public BuildersExpanse() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
//        bus.addListener(CommonSetup::init);
//        bus.addListener(ClientSetup::init);
//
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfig.COMMON);
//        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfig.CLIENT);

        BEItems.ITEMS.register(bus);
        BEBlocks.BLOCKS.register(bus);
    }


//    public static void commonSetup(final @Nonnull FMLCommonSetupEvent event) { }
}
