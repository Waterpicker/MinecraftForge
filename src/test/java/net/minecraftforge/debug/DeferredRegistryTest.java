/*
 * Minecraft Forge - Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug;

import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@SuppressWarnings("unused")
@Mod(DeferredRegistryTest.MODID)
public class DeferredRegistryTest {
    static final String MODID = "deferred_registry_test";
    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<Custom> CUSTOMS = DeferredRegister.create(new ResourceLocation(MODID, "test_registry"), MODID);

    private static final RegistryObject<Block> BLOCK = BLOCKS.register("test", () -> new Block(Block.Properties.of(Material.STONE)));
    private static final RegistryObject<Item>  ITEM  = ITEMS .register("test", () -> new BlockItem(BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
    private static final RegistryObject<Custom> CUSTOM = CUSTOMS.register("test", () -> new Custom(){});

    private static final TagKey<Custom> CUSTOM_TAG_KEY = CUSTOMS.createOptionalTagKey("test_tag", Set.of(CUSTOM));
    private static final Supplier<IForgeRegistry<Custom>> CUSTOM_REG = CUSTOMS.makeRegistry(Custom.class, () ->
        new RegistryBuilder<Custom>().disableSaving().setMaxID(Integer.MAX_VALUE - 1).hasTags()
            .onAdd((owner, stage, id, obj, old) -> LOGGER.info("Custom Added: " + id + " " + obj.foo()))
    );

    public DeferredRegistryTest() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CUSTOMS.register(modBus);
        modBus.addListener(this::gatherData);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        if (event.includeClient()) {
            gen.addProvider(new BlockStateProvider(gen, MODID, event.getExistingFileHelper()) {
                @Override
                protected void registerStatesAndModels() {
                    ModelFile model = models().cubeAll(BLOCK.get().getRegistryName().getPath(), mcLoc("block/furnace_top"));
                    simpleBlock(BLOCK.get(), model);
                    simpleBlockItem(BLOCK.get(), model);
                }
            });
        }
    }

    public static class Custom extends ForgeRegistryEntry<Custom> {
        public String foo() {
            return this.getClass().getName();
        }
    }
}
