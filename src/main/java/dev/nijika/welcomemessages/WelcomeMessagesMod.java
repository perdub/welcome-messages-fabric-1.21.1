package dev.nijika.welcomemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class WelcomeMessagesMod implements ModInitializer {
    public static final String MOD_ID = "welcome_messages";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("welcome-messages");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private volatile List<CompiledRule> rules = List.of();

    @Override
    public void onInitialize() {
        loadConfig();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sendWelcomeMessage(handler.player);
        });

        LOGGER.info("Welcome Messages loaded from {}", CONFIG_FILE.toAbsolutePath());
    }

    private void sendWelcomeMessage(ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();

        for (CompiledRule rule : rules) {
            if (!rule.pattern.matcher(playerName).matches()) {
                continue;
            }

            if (rule.messages.isEmpty()) {
                return;
            }

            String rawMessage = rule.messages.get(
                    ThreadLocalRandom.current().nextInt(rule.messages.size())
            );

            String formattedMessage = rawMessage.replace("{player}", playerName);
            player.sendMessage(Text.literal(colorize(formattedMessage)), false);
            return;
        }
    }

    public void loadConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (Files.notExists(CONFIG_FILE)) {
                WelcomeConfig defaults = WelcomeConfig.createDefault();
                writeConfig(defaults);
                compileRules(defaults);
                LOGGER.info("Created default welcome messages config at {}", CONFIG_FILE.toAbsolutePath());
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                WelcomeConfig config = GSON.fromJson(reader, WelcomeConfig.class);
                if (config == null) {
                    throw new JsonParseException("Config JSON is empty");
                }

                config.normalize();
                compileRules(config);
                LOGGER.info("Loaded {} welcome message rule(s)", rules.size());
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.error("Failed to read {}. Keeping the previous configuration.", CONFIG_FILE.toAbsolutePath(), e);
        }
    }

    private void compileRules(WelcomeConfig config) {
        List<CompiledRule> compiled = new ArrayList<>();

        for (WelcomeConfig.Rule rule : config.rules) {
            try {
                Pattern pattern = Pattern.compile(rule.pattern);
                compiled.add(new CompiledRule(pattern, List.copyOf(rule.messages)));
            } catch (PatternSyntaxException e) {
                LOGGER.error("Invalid regex '{}'; this rule will be ignored.", rule.pattern, e);
            }
        }

        rules = List.copyOf(compiled);
    }

    private void writeConfig(WelcomeConfig config) throws IOException {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }

    private static String colorize(String input) {
        StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (current == '&' && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));
                if (isLegacyColorCode(code)) {
                    output.append('\u00A7').append(code);
                    i++;
                    continue;
                }
            }
            output.append(current);
        }
        return output.toString();
    }

    private static boolean isLegacyColorCode(char code) {
        return (code >= '0' && code <= '9')
                || (code >= 'a' && code <= 'f')
                || "klmnor".indexOf(code) >= 0;
    }

    private record CompiledRule(Pattern pattern, List<String> messages) {
    }
}
