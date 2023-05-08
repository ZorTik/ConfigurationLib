package me.zort.configurationlib.support.containr;

import lombok.RequiredArgsConstructor;
import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.support.containr.action.ActionParser;
import me.zort.configurationlib.util.Placeholders;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.builder.PatternGUIBuilder;
import me.zort.containr.builder.SimpleElementBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class PatternGUIBuilderDeserializer implements NodeDeserializer<PatternGUIBuilder, ConfigurationSection> {

    private final Function<String, String> actionsProcessor;
    // Player is present only if %player% placeholder is present in Placeholders map
    private final BiFunction<@Nullable Player, String, String> linesProcessor;

    private boolean identityProcessors = false;

    public PatternGUIBuilderDeserializer() {
        this(Function.identity(), (p, s) -> s);
        identityProcessors = true;
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

    @SuppressWarnings("unchecked")
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
                    try {
                        ActionParser parser = new ActionParser(((List<String>) item.getSimple("onclick").get()).toArray(new String[0]));
                        handleClick = handleClick.andThen(info -> parser.run(info.getPlayer(), actionsProcessor));
                    } catch(Exception e) {
                        context.getNode().debug(e.getMessage());
                        continue;
                    }

                builder.andMark(mark, new SimpleElementBuilder()
                        .click(handleClick)
                        .item(identityProcessors ? item.getAsItem(placeholders) : item.getAsItem(s -> {
                            Object nickname = placeholders.get("%player%");
                            Player player = null;
                            if (nickname instanceof String) {
                                player = Bukkit.getPlayer((String) nickname);
                            }
                            return linesProcessor.apply(player, s);
                        }))
                        .build());
            }
        }
        return builder;
    }
}
