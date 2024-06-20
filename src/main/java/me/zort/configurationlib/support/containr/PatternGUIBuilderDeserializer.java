package me.zort.configurationlib.support.containr;

import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.util.Placeholders;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.builder.SimpleElementBuilder;
import me.zort.containr.builder.PatternGUIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternGUIBuilderDeserializer implements NodeDeserializer<PatternGUIBuilder, ConfigurationSection> {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[(.+)\\]\\s(.+)");
    private static final Map<String, Action> ACTIONS = new ConcurrentHashMap<>();

    static {
        ACTIONS.put("player", (info, value) -> {
            info.getPlayer().performCommand(value);
        });
        ACTIONS.put("console", (info, value) -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), value);
        });
        ACTIONS.put("message", (info, value) -> {
            info.getPlayer().sendMessage(value);
        });
        ACTIONS.put("close", (info, value) -> {
            info.getPlayer().closeInventory();
        });
        ACTIONS.put("broadcast", (info, value) -> {
            Bukkit.broadcastMessage(value);
        });
    }

    interface Action {
        void run(ContextClickInfo info, String value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable PatternGUIBuilder preBuildInstance(Class<PatternGUIBuilder> deserializeInto, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        String title = placeholders.replace(((SimpleNode<ConfigurationSection>) context.get("title")).getAsString());
        title = ChatColor.translateAlternateColorCodes('&', title);
        List<String> pattern = ((List<String>) ((SimpleNode<ConfigurationSection>) context.get("pattern")).get());
        String[] patternArray = new String[pattern.size()];
        int i = 0;
        for (String s : pattern) {
            patternArray[i] = s;
            i++;
        }
        return new PatternGUIBuilder(title, patternArray);
    }

    @Override
    public PatternGUIBuilder deserialize(@NotNull PatternGUIBuilder builder, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        if (context.get("items") != null) {
            for (SectionNode<?> n : ((SectionNode<?>) context.get("items")).getNodes(NodeTypes.SECTION)) {
                String mark = n.getName();
                if (mark.length() > 1) {
                    context.getNode().debug("Mark '" + mark + "' is too long! It should be only one character long!");
                    continue;
                }

                BukkitSectionNode item = (BukkitSectionNode) n;
                Consumer<ContextClickInfo> handleClick = (info) -> {};

                if (item.has("onclick"))
                    for (String actionString : (List<String>) item.getSimple("onclick").get()) {
                        Matcher matcher = ACTION_PATTERN.matcher(actionString);
                        if (!matcher.matches()) {
                            context.getNode().debug("Invalid action string: " + actionString);
                            continue;
                        }
                        String action = matcher.group(1);
                        String value = matcher.group(2);
                        Action actionObject = ACTIONS.get(action);
                        if (actionObject == null) {
                            context.getNode().debug("Invalid action: " + action);
                            continue;
                        }
                        handleClick = handleClick.andThen(info -> actionObject.run(info, value));
                    }

                builder.andMark(mark, new SimpleElementBuilder()
                        .click(handleClick)
                        .item(item.getAsItem(placeholders))
                        .build());
            }
        }
        return builder;
    }
}
