package com.ssc14.handy.window_manager.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.debug.FidgetToyItem;
import com.ssc14.handy.window_manager.item.ItemEntityRegistry;
import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;

@EventBusSubscriber(modid = Handy.MOD_ID)
public final class HandyCommands {
    private HandyCommands() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("handy_registry")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> {
                            int count = ServerItemRegistry.INSTANCE.getNonEmptySetCount();
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Server item registry contains " + count + " items."),
                                    false
                            );
                            return count;
                        })
                        .then(Commands.literal("verbose").executes(context -> {
                            var counts = ServerItemRegistry.INSTANCE.getLiveReferenceCounts();
                            var source = context.getSource();
                            source.sendSuccess(() -> Component.literal("Server item registry:"), false);
                            for (var entry : counts.entrySet()) {
                                String line = entry.getKey() + ": " + entry.getValue();
                                source.sendSuccess(() -> Component.literal(line), false);
                            }
                            return counts.size();
                        }))
                        .then(Commands.literal("item_entities")
                                .executes(context -> {
                                    int entries = ItemEntityRegistry.getEntryCount();
                                    int live = ItemEntityRegistry.getLiveEntityCount();
                                    int nullEntries = ItemEntityRegistry.getNullEntryCount();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "ItemEntity registry: " + entries + " IDs, "
                                                            + live + " loaded entities, "
                                                            + nullEntries + " null entries."
                                            ),
                                            false
                                    );
                                    return entries;
                                })
                                .then(Commands.literal("verbose").executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    Map<UUID, ItemEntity> entries = ItemEntityRegistry.getDebugEntries();

                                    source.sendSuccess(
                                            () -> Component.literal("ItemEntity registry:"),
                                            false
                                    );

                                    for (var entry : entries.entrySet()) {
                                        String line = formatItemEntityEntry(entry.getKey(), entry.getValue());
                                        source.sendSuccess(() -> Component.literal(line), false);
                                    }

                                    return entries.size();
                                }))
                        ).then(Commands.literal("fidget_toy").executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var entries = ServerItemRegistry.INSTANCE.getDebugEntries();

                            int uuidCount = 0;
                            int stackCount = 0;

                            source.sendSuccess(
                                    () -> Component.literal("Registered FidgetToy ItemStacks:"),
                                    false
                            );

                            for (var entry : entries.entrySet()) {
                                List<ItemStack> stacks = entry.getValue().stream()
                                        .filter(stack -> stack.getItem() instanceof FidgetToyItem)
                                        .toList();

                                if (stacks.isEmpty()) {
                                    continue;
                                }

                                uuidCount++;

                                String header = entry.getKey().toString();
                                source.sendSuccess(
                                        () -> Component.literal(header),
                                        false
                                );

                                for (ItemStack stack : stacks) {
                                    String line = "  ItemStack@"
                                            + Integer.toHexString(System.identityHashCode(stack))
                                            + " count=" + stack.getCount()
                                            + " clicks=" + FidgetToyItem.getClicks(stack);

                                    source.sendSuccess(
                                            () -> Component.literal(line),
                                            false
                                    );

                                    stackCount++;
                                }
                            }

                            String summary = "Total: " + uuidCount
                                    + " UUIDs, " + stackCount + " ItemStacks.";

                            source.sendSuccess(
                                    () -> Component.literal(summary),
                                    false
                            );

                            return stackCount;
                        }))
        );
    }

    private static String formatItemEntityEntry(UUID instanceId, ItemEntity entity) {
        if (entity == null) {
            return instanceId + ": null (not currently loaded)";
        }

        return instanceId
                + ": entity=" + entity.getUUID()
                + ", entityId=" + entity.getId()
                + ", dimension=" + entity.level().dimension()
                + ", pos=" + String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)",
                entity.getX(), entity.getY(), entity.getZ())
                + ", stack=" + entity.getItem().getItem()
                + " x" + entity.getItem().getCount();
    }
}