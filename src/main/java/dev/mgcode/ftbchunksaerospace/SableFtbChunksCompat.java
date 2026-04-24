package dev.mgcode.ftbchunksaerospace;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Vector3d;

final class SableFtbChunksCompat {
    static final String CLAIM_DENIED_KEY = "message.ftbchunksaerospace.claim_denied";
    private static final String AIRSPACE_OWNER_UNKNOWN_KEY = "message.ftbchunksaerospace.airspace_owner_unknown";

    @SubscribeEvent
    public void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final BlockPos protectedWorldPos = findProtectedWorldPos(player, event.getPos(), event.getHand(),
                Protection.EDIT_AND_INTERACT_BLOCK);
        if (protectedWorldPos == null) {
            return;
        }

        deny(player, protectedWorldPos);
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final BlockPos protectedWorldPos = findProtectedWorldPos(player, event.getPos(), InteractionHand.MAIN_HAND,
                Protection.EDIT_BLOCK);
        if (protectedWorldPos == null) {
            return;
        }

        deny(player, protectedWorldPos);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBreakBlock(final BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        final BlockPos protectedWorldPos = findProtectedWorldPos(player, event.getPos(), InteractionHand.MAIN_HAND,
                Protection.EDIT_BLOCK);
        if (protectedWorldPos == null) {
            return;
        }

        deny(player, protectedWorldPos);
        event.setCanceled(true);
    }

    private static BlockPos findProtectedWorldPos(
            final ServerPlayer player,
            final BlockPos physicalBlockPos,
            final InteractionHand hand,
            final Protection protection) {
        final SubLevel subLevel = Sable.HELPER.getContaining(player.level(), physicalBlockPos);
        if (subLevel == null) {
            return null;
        }

        final BlockPos worldPos = resolveWorldBlockPos(subLevel, physicalBlockPos);
        return isProtectedWorldPosition(player, hand, worldPos, protection) ? worldPos : null;
    }

    static boolean isProtectedWorldPosition(
            final net.minecraft.world.entity.Entity entity,
            final InteractionHand hand,
            final BlockPos worldPos,
            final Protection protection) {
        return FTBChunksAPI.api().isManagerLoaded()
                && FTBChunksAPI.api().getManager().shouldPreventInteraction(entity, hand, worldPos, protection, null);
    }

    static BlockPos resolveWorldBlockPos(final SubLevel subLevel, final BlockPos physicalBlockPos) {
        final Vector3d worldCenter = subLevel.logicalPose().transformPosition(new Vector3d(
                physicalBlockPos.getX() + 0.5,
                physicalBlockPos.getY() + 0.5,
                physicalBlockPos.getZ() + 0.5));
        return BlockPos.containing(worldCenter.x(), worldCenter.y(), worldCenter.z());
    }

    private static void deny(final ServerPlayer player, final BlockPos worldPos) {
        player.displayClientMessage(Component.translatable(CLAIM_DENIED_KEY, getOwningTeamName(player, worldPos))
                .withStyle(ChatFormatting.RED), true);
    }

    private static Component getOwningTeamName(final ServerPlayer player, final BlockPos worldPos) {
        if (!FTBChunksAPI.api().isManagerLoaded()) {
            return Component.translatable(AIRSPACE_OWNER_UNKNOWN_KEY).withStyle(ChatFormatting.GRAY);
        }

        final var claimedChunk = FTBChunksAPI.api().getManager()
                .getChunk(XZ.chunkFromBlock(worldPos).dim(player.level()));
        if (claimedChunk == null) {
            return Component.translatable(AIRSPACE_OWNER_UNKNOWN_KEY).withStyle(ChatFormatting.GRAY);
        }

        return claimedChunk.getTeamData().getTeam().getColoredName().copy();
    }
}