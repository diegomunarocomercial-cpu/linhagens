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
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<DemonioMonstroEntity> DEMONIO = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("demonio_monstro"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DemonioMonstroEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 2.5f)).fireImmune().build());

    public static final EntityType<OcultistaEntity> OCULTISTA = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ocultista_diablos"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, OcultistaEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).fireImmune().build());

    public static final EntityType<SamuelEntity> SAMUEL = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("samuel"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SamuelEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build());

    public static final EntityType<CacadorMarcadosEntity> CACADOR = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("cacador_marcados"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CacadorMarcadosEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build());

    public static final EntityType<IceDragonEntity> ICE_DRAGON = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ice_dragon"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, IceDragonEntity::new)
                    .dimensions(EntityDimensions.fixed(4.0f, 3.6f)).trackRangeBlocks(96).build());

    public static final EntityType<IceDragonEntity> ICE_DRAGON_BABY = Registry.register(Registries.ENTITY_TYPE,
            Linhagens.id("ice_dragon_baby"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, IceDragonEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 1.4f)).build());

    public static void init() {
        FabricDefaultAttributeRegistry.register(DEMONIO, DemonioMonstroEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(OCULTISTA, OcultistaEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SAMUEL, SamuelEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CACADOR, CacadorMarcadosEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ICE_DRAGON, IceDragonEntity.createBossAttributes());
        FabricDefaultAttributeRegistry.register(ICE_DRAGON_BABY, IceDragonEntity.createBabyAttributes());

        // Ocultistas nascem no Nether; Monstros Demônios nascem só no Reino Profano (bioma "void" recolorido).
        // Dragões de Gelo só aparecem no Castelo de Gelo; o Caçador de Marcados só aparece ao lado de um piano.
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.MONSTER, OCULTISTA, 8, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.THE_VOID), SpawnGroup.MONSTER, DEMONIO, 100, 1, 3);
    }
}
