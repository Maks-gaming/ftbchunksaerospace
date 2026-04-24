package dev.mgcode.ftbchunksaerospace;

import net.neoforged.neoforge.common.ModConfigSpec;

final class FtbChunksSableAerospaceConfig {
    static final ModConfigSpec CLIENT_SPEC;
    static final Client CLIENT;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    private FtbChunksSableAerospaceConfig() {
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