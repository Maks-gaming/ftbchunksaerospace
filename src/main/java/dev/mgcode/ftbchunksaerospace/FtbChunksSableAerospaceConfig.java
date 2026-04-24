package dev.mgcode.ftbchunksaerospace;

import net.neoforged.neoforge.common.ModConfigSpec;

final class FtbChunksSableAerospaceConfig {
    static final ModConfigSpec COMMON_SPEC;
    static final Common COMMON;
    static final ModConfigSpec CLIENT_SPEC;
    static final Client CLIENT;

    static {
        final ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    private FtbChunksSableAerospaceConfig() {
    }

    static boolean isFreeZone(final int worldY) {
        return COMMON.freeZoneEnabled() && worldY > COMMON.freeZoneAboveY();
    }

    static final class Common {
        private final ModConfigSpec.BooleanValue freeZoneEnabled;
        private final ModConfigSpec.IntValue freeZoneAboveY;
        private final ModConfigSpec.BooleanValue freeZoneAllowBlockUse;
        private final ModConfigSpec.BooleanValue freeZoneAllowBlockBreak;
        private final ModConfigSpec.BooleanValue freeZoneAllowBlockPlace;
        private final ModConfigSpec.BooleanValue freeZoneDenyTntIgnition;
        private final ModConfigSpec.BooleanValue freeZoneShowDenyMessage;

        private Common(final ModConfigSpec.Builder builder) {
            builder.comment("Shared protection settings.").push("freeZone");

            freeZoneEnabled = builder
                    .comment(
                            "Enable a special free-zone ruleset above the configured Y level inside foreign claimed chunks.")
                    .define("enabled", true);

            freeZoneAboveY = builder
                    .comment(
                            "Foreign claimed chunks switch to free-zone rules when Y is greater than this value.")
                    .defineInRange("freeZoneAboveY", 320, -2048, 4096);

            freeZoneAllowBlockUse = builder
                    .comment("Allow normal block/item use in the free zone. Default: true.")
                    .define("allowBlockUse", true);

            freeZoneAllowBlockBreak = builder
                    .comment("Allow breaking blocks in the free zone. Default: false.")
                    .define("allowBlockBreak", false);

            freeZoneAllowBlockPlace = builder
                    .comment("Allow placing blocks and fluids in the free zone. Default: false.")
                    .define("allowBlockPlace", false);

            freeZoneDenyTntIgnition = builder
                    .comment("Deny TNT ignition in the free zone even when normal use is allowed. Default: true.")
                    .define("denyTntIgnition", true);

            freeZoneShowDenyMessage = builder
                    .comment("Show a message when an action is blocked by free-zone rules. Default: true.")
                    .define("showDenyMessage", true);

            builder.pop();
        }

        boolean freeZoneEnabled() {
            return freeZoneEnabled.get();
        }

        int freeZoneAboveY() {
            return freeZoneAboveY.get();
        }

        boolean freeZoneAllowBlockUse() {
            return freeZoneAllowBlockUse.get();
        }

        boolean freeZoneAllowBlockBreak() {
            return freeZoneAllowBlockBreak.get();
        }

        boolean freeZoneAllowBlockPlace() {
            return freeZoneAllowBlockPlace.get();
        }

        boolean freeZoneDenyTntIgnition() {
            return freeZoneDenyTntIgnition.get();
        }

        boolean freeZoneShowDenyMessage() {
            return freeZoneShowDenyMessage.get();
        }
    }

    static final class Client {
        private final ModConfigSpec.BooleanValue enableApproachWarning;
        private final ModConfigSpec.DoubleValue approachWarningSeconds;
        private final ModConfigSpec.IntValue warningCheckIntervalTicks;
        private final ModConfigSpec.BooleanValue enableEnteredMessage;
        private final ModConfigSpec.BooleanValue enableExitedMessage;

        private Client(final ModConfigSpec.Builder builder) {
            builder.comment("Client-side closed airspace warning settings.").push("clientWarnings");

            enableApproachWarning = builder
                    .comment("Show a warning before entering another team's claimed airspace.")
                    .define("enableApproachWarning", true);

            approachWarningSeconds = builder
                    .comment(
                            "Warn when the predicted time until entering closed airspace is less than or equal to this many seconds.")
                    .defineInRange("approachWarningSeconds", 60.0D, 1.0D, 300.0D);

            warningCheckIntervalTicks = builder
                    .comment("How often the client checks for upcoming closed airspace, in ticks.")
                    .defineInRange("warningCheckIntervalTicks", 10, 1, 100);

            enableEnteredMessage = builder
                    .comment("Show a message when you enter another team's closed airspace.")
                    .define("enableEnteredMessage", true);

            enableExitedMessage = builder
                    .comment("Show a message when you leave another team's closed airspace.")
                    .define("enableExitedMessage", true);

            builder.pop();
        }

        boolean enableApproachWarning() {
            return enableApproachWarning.get();
        }

        double approachWarningSeconds() {
            return approachWarningSeconds.get();
        }

        int warningCheckIntervalTicks() {
            return warningCheckIntervalTicks.get();
        }

        boolean enableEnteredMessage() {
            return enableEnteredMessage.get();
        }

        boolean enableExitedMessage() {
            return enableExitedMessage.get();
        }
    }
}