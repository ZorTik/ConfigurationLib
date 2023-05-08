package me.zort.configurationlib.support.containr;

import lombok.AllArgsConstructor;
import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.support.containr.action.ActionParser;
import me.zort.configurationlib.util.Placeholders;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.builder.PatternGUIBuilder;
import me.zort.containr.builder.SimpleElementBuilder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class PatternGUIBuilderDeserializer implements NodeDeserializer<PatternGUIBuilder, ConfigurationSection> {

    private final Function<String, String> processor;

    public PatternGUIBuilderDeserializer() {
        this(Function.identity());
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
                        handleClick = handleClick.andThen(info -> parser.run(info.getPlayer(), processor));
                    } catch(Exception e) {
                        context.getNode().debug(e.getMessage());
                        continue;
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
