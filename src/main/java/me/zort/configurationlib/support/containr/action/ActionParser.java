package me.zort.configurationlib.support.containr.action;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ActionParser {

    private static final Pattern pattern = Pattern.compile("\\[(.+)\\]\\s?(.+)?");
    private final ParseResult[] results;

    public ActionParser(String[] actionStrings) throws Exception {
        results = new ParseResult[actionStrings.length];
        for (int i = 0; i < actionStrings.length; i++) {
            String str = actionStrings[i];

            Matcher matcher = pattern.matcher(str);
            if (!matcher.matches()) throw new Exception("Invalid action string: " + str);
            String action = matcher.group(1);
            if (Action.Registry.get(action) == null) throw new Exception("Invalid action: " + action);
            String value = matcher.groupCount() > 1 ? matcher.group(2) : "";

            results[i] = new ParseResult(Action.Registry.get(action), value);
        }
    }

    public void run(Player player) {
        run(player, Function.identity());
    }

    public void run(Player player, @NotNull Function<String, String> formatter) {
        for (ParseResult result : results) {
            result.action.run(player, formatter.apply(result.value));
        }
    }

    @AllArgsConstructor
    private static class ParseResult {
        private final Action action;
        private final String value;
    }

}
