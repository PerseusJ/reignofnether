package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.buildings.villagers.Blacksmith;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ForgeIronChestplate extends Ability {

    private static final UnitAction FORGE_ACTION = UnitAction.FORGE_IRON_CHESTPLATE;
    public static final ResourceCost cost = ResourceCosts.FORGE_IRON_CHESTPLATE;

    private static final int CD_MAX = 20;

    public ForgeIronChestplate() {
        super(FORGE_ACTION, CD_MAX, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        if (!(placement.getBuilding() instanceof Blacksmith)) {
            return null;
        }

        return new AbilityButton(
            "Forge Iron Chestplates",
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/iron_chestplate.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == FORGE_ACTION,
            () -> false,
            () -> placement.isBuilt && placement.getUpgradeLevel() > 0 && !UnitClientEvents.getSelectedUnits().isEmpty(),
            () -> UnitClientEvents.sendUnitCommand(FORGE_ACTION),
            () -> {},
            List.of(
                FormattedCharSequence.forward(I18n.get("ability.reignofnether.forge.iron"), Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("ability.reignofnether.forge.iron.tooltip1"), Style.EMPTY)
            ),
            this,
            placement
        );
    }
}


