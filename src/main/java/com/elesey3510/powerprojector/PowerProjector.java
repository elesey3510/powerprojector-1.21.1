package com.elesey3510.powerprojector;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.resources.ResourceLocation;
@Mod(PowerProjector.MODID)
public class PowerProjector {
    public static final String MODID = "powerprojector";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);


    public static final DeferredHolder<Block, SpotlightBlock> SPOTLIGHT_BLOCK = BLOCKS.register("spotlight",
        () -> new SpotlightBlock(BlockBehaviour.Properties.of().strength(2.0f)));

    public static final DeferredHolder<Item, SpotlightItem> SPOTLIGHT_ITEM = ITEMS.register("spotlight",
            () -> new SpotlightItem(SPOTLIGHT_BLOCK.get(), new Item.Properties()));
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Проверяем вкладку мода Create: Power Grid
        // У Power Grid вкладка называется "powergrid:base" или "powergrid:main"
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("powergrid", "base"))
         || event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("powergrid", "main"))) {
            event.accept(SPOTLIGHT_ITEM.get());
        }
    }
    // Регистрация BlockEntity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpotlightBlockEntity>> SPOTLIGHT_BE = 
            BLOCK_ENTITIES.register("spotlight",
            () -> BlockEntityType.Builder.of(SpotlightBlockEntity::new, SPOTLIGHT_BLOCK.get()).build(null));
    
    public PowerProjector(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(PowerProjector::registerRenderers);
        modEventBus.addListener(PowerProjector::addCreative);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SPOTLIGHT_BE.get(), SpotlightRenderer::new);
    }
}