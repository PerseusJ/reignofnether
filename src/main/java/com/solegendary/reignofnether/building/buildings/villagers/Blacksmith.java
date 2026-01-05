package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.ability.abilities.ForgeIronChestplate;
import com.solegendary.reignofnether.ability.abilities.ForgeLeatherChestplate;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.building.production.ProductionItems;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.faction.Faction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Blacksmith extends ProductionBuilding {

    public final static String buildingName = "Blacksmith";
    public final static String structureName = "blacksmith";
    public final static String upgradedStructureName = "blacksmith_armorer";
    public final static ResourceCost cost = ResourceCosts.BLACKSMITH;

    private static volatile boolean upgradeSignatureComputed = false;
    private static Set<Block> upgradedOnlyBlocks = Set.of();
    private static int baseStructureBlockCount = -1;
    private static int upgradedStructureBlockCount = -1;

    public Blacksmith() {
        super(structureName, cost, false);
        this.name = buildingName;
        this.portraitBlock = Blocks.SMITHING_TABLE;
        this.icon = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/smithing_table_front.png");

        this.buildTimeModifier = 0.85f;

        this.startingBlockTypes.add(Blocks.OAK_PLANKS);
        this.startingBlockTypes.add(Blocks.COBBLESTONE);

        this.abilities.add(new ForgeLeatherChestplate(), Keybindings.keyT);
        this.abilities.add(new ForgeIronChestplate(), Keybindings.keyY);

        this.productions.add(ProductionItems.IRON_GOLEM, Keybindings.keyQ);
        this.productions.add(ProductionItems.RESEARCH_GOLEM_SMITHING, Keybindings.keyW);
        this.productions.add(ProductionItems.RESEARCH_MILITIA_BOWS, Keybindings.keyE);
        this.productions.add(ProductionItems.RESEARCH_BLACKSMITH_ARMORER, Keybindings.keyR);
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    private static void ensureUpgradeSignatureComputed(LevelAccessor level) {
        if (upgradeSignatureComputed) {
            return;
        }
        synchronized (Blacksmith.class) {
            if (upgradeSignatureComputed) {
                return;
            }

            ArrayList<BuildingBlock> baseBlocks = BuildingBlockData.getBuildingBlocksFromNbt(structureName, level);
            ArrayList<BuildingBlock> upgradedBlocks = BuildingBlockData.getBuildingBlocksFromNbt(upgradedStructureName, level);

            baseStructureBlockCount = baseBlocks.size();
            upgradedStructureBlockCount = upgradedBlocks.size();

            Set<Block> baseBlockTypes = new HashSet<>();
            for (BuildingBlock bb : baseBlocks) {
                baseBlockTypes.add(bb.getBlockState().getBlock());
            }

            Set<Block> upgradedBlockTypes = new HashSet<>();
            for (BuildingBlock bb : upgradedBlocks) {
                upgradedBlockTypes.add(bb.getBlockState().getBlock());
            }

            upgradedBlockTypes.removeAll(baseBlockTypes);
            upgradedOnlyBlocks = Collections.unmodifiableSet(upgradedBlockTypes);
            upgradeSignatureComputed = true;
        }
    }

    @Override
    public int getUpgradeLevel(BuildingPlacement placement) {
        ensureUpgradeSignatureComputed(placement.getLevel());

        // Fast path: any block type that only exists in the upgraded structure
        if (!upgradedOnlyBlocks.isEmpty()) {
            for (BuildingBlock block : placement.getBlocks()) {
                if (upgradedOnlyBlocks.contains(block.getBlockState().getBlock())) {
                    return 1;
                }
            }
        }

        // Fallback: compare total block counts (works even if palette is identical but size differs)
        int current = placement.getBlocks().size();
        if (upgradedStructureBlockCount >= 0
            && current == upgradedStructureBlockCount
            && upgradedStructureBlockCount != baseStructureBlockCount) {
            return 1;
        }

        return 0;
    }

    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {
        ResourceLocation key = ReignOfNetherRegistries.BUILDING.getKey(this);
        String name = I18n.get("buildings." + getFaction().name().toLowerCase() + "." + key.getNamespace() + "." + key.getPath());
        return new BuildingPlaceButton(
                name,
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/smithing_table_front.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == Buildings.BLACKSMITH,
                () -> !TutorialClientEvents.isAtOrPastStage(TutorialStage.ATTACK_ENEMY_BASE),
                () -> BuildingClientEvents.hasFinishedBuilding(Buildings.BARRACKS) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                List.of(
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.blacksmith"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.blacksmith.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.blacksmith.tooltip2"), Style.EMPTY)
                ),
                this
        );
    }
}
