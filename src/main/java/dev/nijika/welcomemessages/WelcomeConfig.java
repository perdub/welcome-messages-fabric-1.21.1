package dev.nijika.welcomemessages;

import java.util.ArrayList;
import java.util.List;

public final class WelcomeConfig {
    public List<Rule> rules = new ArrayList<>();

    public static WelcomeConfig createDefault() {
        WelcomeConfig config = new WelcomeConfig();

        Rule defaultRule = new Rule();
        defaultRule.pattern = ".*";
        defaultRule.messages = new ArrayList<>(List.of(
                "&7Добро пожаловать, &d{player}&7!",
                "&7С возвращением, &d{player}&7!"
        ));

        config.rules.add(defaultRule);
        return config;
    }

    public void normalize() {
        if (rules == null) {
            rules = new ArrayList<>();
        }

        rules.removeIf(rule -> rule == null || rule.pattern == null || rule.pattern.isBlank());

        for (Rule rule : rules) {
            if (rule.messages == null) {
                rule.messages = new ArrayList<>();
            }
            rule.messages.removeIf(message -> message == null || message.isBlank());
        }
    }

    public static final class Rule {
        public String pattern = ".*";
        public List<String> messages = new ArrayList<>();
    }
}
