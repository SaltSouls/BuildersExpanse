package salted.buildersexpanse.common.items;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import salted.buildersexpanse.common.tags.BETags;

public class SawItem extends DiggerItem {
    public SawItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, tier, BETags.MINEABLE_WITH_SAW, properties);
    }
}
