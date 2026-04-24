package dev.mgcode.ftbchunksaerospace;

import dev.ftb.mods.ftbchunks.client.map.MapChunk;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.MapManager;
import dev.ftb.mods.ftbchunks.client.map.MapRegion;
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Locale;
import java.util.UUID;

final class SableAerospaceClientWarnings {
    private static final String AIRSPACE_WARNING_KEY = "message.ftbchunksaerospace.airspace_warning";
    private static final String AIRSPACE_ENTERED_KEY = "message.ftbchunksaerospace.airspace_entered";
    private static final String AIRSPACE_EXITED_KEY = "message.ftbchunksaerospace.airspace_exited";
    private static final double WARNING_STEP_SECONDS = 0.25;
    private static final double MIN_WARNING_SPEED_SQUARED = 0.04;

    private AirspaceInfo trackedAirspace;

    @SubscribeEvent
    public void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.isPaused()) {
            return;
        }

        final int warningIntervalTicks = FtbChunksSableAerospaceConfig.CLIENT.warningCheckIntervalTicks();
        if (player.tickCount % warningIntervalTicks != 0) {
            return;
        }

        final SubLevel subLevel = Sable.HELPER.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) {
            trackedAirspace = null;
            return;
        }

        final AirspaceInfo currentAirspace = findForeignAirspace(player, BlockPos.containing(player.position()));
        updateTrackedAirspace(player, currentAirspace);

        final Vec3 velocity = Sable.HELPER.getVelocity(player.level(), subLevel, player.position());
        if (velocity.lengthSqr() < MIN_WARNING_SPEED_SQUARED) {
            return;
        }

        final WarningPrediction prediction = findSecondsUntilClaim(player, velocity);
        if (prediction == null) {
            return;
        }

        player.displayClientMessage(buildWarningMessage(prediction), true);
    }

    private void updateTrackedAirspace(final LocalPlayer player, final AirspaceInfo currentAirspace) {
        if (sameAirspace(trackedAirspace, currentAirspace)) {
            return;
        }

        if (trackedAirspace != null && FtbChunksSableAerospaceConfig.CLIENT.enableExitedMessage()) {
            player.displayClientMessage(buildExitedMessage(trackedAirspace), false);
        }

        if (currentAirspace != null && FtbChunksSableAerospaceConfig.CLIENT.enableEnteredMessage()) {
            player.displayClientMessage(buildEnteredMessage(currentAirspace), false);
        }

        trackedAirspace = currentAirspace;
    }

    private static boolean sameAirspace(final AirspaceInfo left, final AirspaceInfo right) {
        return left == right || left != null && right != null && left.teamId().equals(right.teamId());
    }

    private static WarningPrediction findSecondsUntilClaim(final LocalPlayer player, final Vec3 velocity) {
        final Vec3 start = player.position();
        if (findForeignAirspace(player, BlockPos.containing(start)) != null) {
            return null;
        }

        if (!FtbChunksSableAerospaceConfig.CLIENT.enableApproachWarning()) {
            return null;
        }

        final double warningLookaheadSeconds = FtbChunksSableAerospaceConfig.CLIENT.approachWarningSeconds();

        BlockPos lastChecked = null;

        for (double seconds = WARNING_STEP_SECONDS; seconds <= warningLookaheadSeconds; seconds += WARNING_STEP_SECONDS) {
            final Vec3 future = start.add(velocity.scale(seconds));
            final BlockPos futurePos = BlockPos.containing(future);
            if (futurePos.equals(lastChecked)) {
                continue;
            }

            lastChecked = futurePos;
            final AirspaceInfo futureAirspace = findForeignAirspace(player, futurePos);
            if (futureAirspace != null) {
                return new WarningPrediction(seconds, futureAirspace);
            }
        }

        return null;
    }

    private static AirspaceInfo findForeignAirspace(final LocalPlayer player, final BlockPos worldPos) {
        final MapManager mapManager = MapManager.getInstance().orElse(null);
        if (mapManager == null) {
            return null;
        }

        final MapDimension mapDimension = mapManager.getDimension(player.level().dimension());
        if (mapDimension == null) {
            return null;
        }

        final XZ chunkPos = XZ.chunkFromBlock(worldPos);
        final MapRegion region = mapDimension.getRegion(XZ.regionFromChunk(chunkPos.x(), chunkPos.z()));
        final MapChunk mapChunk = region.getChunkForAbsoluteChunkPos(chunkPos);
        if (mapChunk.isTeamMember(player)) {
            return null;
        }

        final Team team = mapChunk.getTeam().orElse(null);
        if (team == null) {
            return null;
        }

        return new AirspaceInfo(team.getId(), team.getColoredName().copy());
    }

    private static Component buildWarningMessage(final WarningPrediction prediction) {
        return Component.translatable(
                AIRSPACE_WARNING_KEY,
                Component.literal(formatSeconds(prediction.seconds())).withStyle(ChatFormatting.AQUA),
                prediction.airspace().teamName().copy())
                .withStyle(ChatFormatting.GOLD);
    }

    private static Component buildEnteredMessage(final AirspaceInfo airspace) {
        return Component.translatable(AIRSPACE_ENTERED_KEY, airspace.teamName().copy())
                .withStyle(ChatFormatting.RED);
    }

    private static Component buildExitedMessage(final AirspaceInfo airspace) {
        return Component.translatable(AIRSPACE_EXITED_KEY, airspace.teamName().copy())
                .withStyle(ChatFormatting.GREEN);
    }

    private static String formatSeconds(final double seconds) {
        if (seconds < 10.0) {
            return String.format(Locale.ROOT, "%.1f", seconds);
        }

        return Integer.toString((int) Math.ceil(seconds));
    }

    private record AirspaceInfo(UUID teamId, Component teamName) {
    }

    private record WarningPrediction(double seconds, AirspaceInfo airspace) {
    }
}