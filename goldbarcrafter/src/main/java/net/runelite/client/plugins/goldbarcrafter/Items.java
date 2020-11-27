package net.runelite.client.plugins.goldbarcrafter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@AllArgsConstructor
public enum Items {
    AMULET("Amulet", ItemID.GOLD_AMULET_U,"Make <col=ff9040>Gold amulet (u)</col>",29229090, ItemID.AMULET_MOULD),
    RING("Ring",ItemID.GOLD_RING,"Make <col=ff9040>Gold ring</col>",29229063,ItemID.RING_MOULD),
    NECKLACE("Necklace",ItemID.GOLD_NECKLACE, "Make <col=ff9040>Gold necklace</col>",29229077,ItemID.NECKLACE_MOULD);

    private String name;
    private int itemId;
    private String makeString;
    private int optionId;
    private int mould;

    @Override
    public String toString()
    {
        return getName();
    }
}
