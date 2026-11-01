package com.linhagens.entity;

import com.linhagens.Linhagens;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<ImpEntity> IMP = Registry.register(Registries.ENTITY_TYPE, Linhagens.id("imp_infernal"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ImpEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f)).fireImmune().build());

    public static final EntityType<ShadowSwordsmanEntity> SHADOW_SWORDSMAN = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("espadachim_sombrio"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ShadowSwordsmanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build());

    public static final EntityType<AuroraGuardianEntity> AURORA_GUARDIAN = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("aurora_guardian"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, AuroraGuardianEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 2.7f)).build());

    public static final EntityType<OcultistaEntity> OCULTISTA = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ocultista_diablos"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, OcultistaEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).fireImmune().build());

    public static final EntityType<IceDragonEntity> ICE_DRAGON = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ice_dragon"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, IceDragonEntity::new)
                    .dimensions(EntityDimensions.fixed(4.0f, 3.6f)).trackRangeBlocks(96).build());

    public static final EntityType<IceDragonEntity> ICE_DRAGON_BABY = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ice_dragon_baby"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, IceDragonEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 1.4f)).build());

    public static void init() {
        FabricDefaultAttributeRegistry.register(IMP, ImpEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SHADOW_SWORDSMAN, ShadowSwordsmanEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(AURORA_GUARDIAN, AuroraGuardianEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(OCULTISTA, OcultistaEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ICE_DRAGON, IceDragonEntity.createBossAttributes());
        FabricDefaultAttributeRegistry.register(ICE_DRAGON_BABY, IceDragonEntity.createBabyAttributes());

        // Imps e Ocultistas nascem no Nether; Espadachins Sombrios no escuro do Overworld.
        // (Dragões de Gelo só aparecem dentro do Castelo de Gelo.)
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.MONSTER, IMP, 20, 2, 4);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.MONSTER, OCULTISTA, 8, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, SHADOW_SWORDSMAN, 6, 1, 2);
    }
}
