package dev.mgcode.ftbchunksaerospace;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.joml.Vector3d;

final class SableFtbChunksCompat {
    static final String CLAIM_DENIED_KEY = "message.ftbchunksaerospace.claim_denied";
    private static final String FREE_ZONE_BREAK_DENIED_KEY = "message.ftbchunksaerospace.free_zone_break_denied";
    private static final String FREE_ZONE_PLACE_DENIED_KEY = "message.ftbchunksaerospace.free_zone_place_denied";
    private static final String FREE_ZONE_IGNITE_DENIED_KEY = "message.ftbchunksaerospace.free_zone_ignite_denied";
    private static final String FREE_ZONE_USE_DENIED_KEY = "message.ftbchunksaerospace.free_zone_use_denied";
    private static final String AIRSPACE_OWNER_UNKNOWN_KEY = "message.ftbchunksaerospace.airspace_owner_unknown";

    @SubscribeEvent
    public void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final BlockPos worldPos = resolveWorldBlockPos(player.level(), event.getPos());
        if (worldPos == null) {
            return;
        }

        if (FtbChunksSableAerospaceConfig.isFreeZone(worldPos.getY())) {
            if (!isClaimProtectedWorldPosition(player, event.getHand(), worldPos, Protection.EDIT_AND_INTERACT_BLOCK)) {
                return;
            }

            if (isDeniedFreeZoneTntIgnition(event)) {
                denyFreeZone(player, worldPos, FREE_ZONE_IGNITE_DENIED_KEY);
                event.setCancellationResult(InteractionResult.FAIL);
                event.setUseItem(TriState.FALSE);
                event.setCanceled(true);
                return;
            }

            if (!FtbChunksSableAerospaceConfig.COMMON.freeZoneAllowBlockUse()) {
                denyFreeZone(player, worldPos, FREE_ZONE_USE_DENIED_KEY);
                event.setCancellationResult(InteractionResult.FAIL);
                event.setCanceled(true);
            }

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

        final BlockPos worldPos = resolveWorldBlockPos(player.level(), event.getPos());
        if (worldPos != null && FtbChunksSableAerospaceConfig.isFreeZone(worldPos.getY())) {
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

        final BlockPos worldPos = resolveWorldBlockPos(player.level(), event.getPos());
        if (worldPos != null && FtbChunksSableAerospaceConfig.isFreeZone(worldPos.getY())) {
            if (!FtbChunksSableAerospaceConfig.COMMON.freeZoneAllowBlockBreak()
                    && isClaimProtectedWorldPosition(player, InteractionHand.MAIN_HAND, worldPos,
                            Protection.EDIT_BLOCK)) {
                denyFreeZone(player, worldPos, FREE_ZONE_BREAK_DENIED_KEY);
                event.setCanceled(true);
            }
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
    public void onPlaceBlock(final BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final BlockPos worldPos = resolveWorldBlockPos(player.level(), event.getPos());
        if (worldPos == null || !FtbChunksSableAerospaceConfig.isFreeZone(worldPos.getY())) {
            return;
        }

        if (FtbChunksSableAerospaceConfig.COMMON.freeZoneAllowBlockPlace()) {
            return;
        }

        if (!isClaimProtectedWorldPosition(player, InteractionHand.MAIN_HAND, worldPos, Protection.EDIT_BLOCK)) {
            return;
        }

        denyFreeZone(player, worldPos, FREE_ZONE_PLACE_DENIED_KEY);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onExplosionDetonate(final ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(blockPos -> shouldProtectFromExplosion(event.getLevel(), blockPos));
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

    private static BlockPos resolveWorldBlockPos(final Level level, final BlockPos physicalBlockPos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, physicalBlockPos);
        if (subLevel == null) {
            return null;
        }

        return resolveWorldBlockPos(subLevel, physicalBlockPos);
    }

    static boolean isProtectedWorldPosition(
            final net.minecraft.world.entity.Entity entity,
            final InteractionHand hand,
            final BlockPos worldPos,
            final Protection protection) {
        if (FtbChunksSableAerospaceConfig.isFreeZone(worldPos.getY())) {
            return false;
        }

        return isClaimProtectedWorldPosition(entity, hand, worldPos, protection);
    }

    static boolean isClaimProtectedWorldPosition(
            final net.minecraft.world.entity.Entity entity,
            final InteractionHand hand,
            final BlockPos worldPos,
            final Protection protection) {

        return FTBChunksAPI.api().isManagerLoaded()
                && FTBChunksAPI.api().getManager().shouldPreventInteraction(entity, hand, worldPos, protection, null);
    }

    private static boolean shouldProtectFromExplosion(final Level level, final BlockPos physicalBlockPos) {
        final BlockPos worldPos = resolveWorldBlockPos(level, physicalBlockPos);
        if (worldPos == null) {
            return false;
        }

        final ClaimedChunk claimedChunk = getClaimedChunk(level, worldPos);
        return claimedChunk != null && !claimedChunk.getTeamData().canExplosionsDamageTerrain();
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

    private static void denyFreeZone(final ServerPlayer player, final BlockPos worldPos, final String translationKey) {
        if (!FtbChunksSableAerospaceConfig.COMMON.freeZoneShowDenyMessage()) {
            return;
        }

        player.displayClientMessage(Component.translatable(translationKey, getOwningTeamName(player, worldPos))
                .withStyle(ChatFormatting.RED), true);
    }

    private static boolean isDeniedFreeZoneTntIgnition(final PlayerInteractEvent.RightClickBlock event) {
        if (!FtbChunksSableAerospaceConfig.COMMON.freeZoneDenyTntIgnition()) {
            return false;
        }

        final BlockState blockState = event.getEntity().level().getBlockState(event.getPos());
        return blockState.is(Blocks.TNT)
                && event.getEntity().getItemInHand(event.getHand()).canPerformAction(ItemAbilities.FIRESTARTER_LIGHT);
    }

    private static Component getOwningTeamName(final ServerPlayer player, final BlockPos worldPos) {
        final ClaimedChunk claimedChunk = getClaimedChunk(player.level(), worldPos);
        if (claimedChunk == null) {
            return Component.translatable(AIRSPACE_OWNER_UNKNOWN_KEY).withStyle(ChatFormatting.GRAY);
        }

        return claimedChunk.getTeamData().getTeam().getColoredName().copy();
    }

    private static ClaimedChunk getClaimedChunk(final Level level, final BlockPos worldPos) {
        if (!FTBChunksAPI.api().isManagerLoaded()) {
            return null;
        }

        return FTBChunksAPI.api().getManager().getChunk(XZ.chunkFromBlock(worldPos).dim(level));
    }
}