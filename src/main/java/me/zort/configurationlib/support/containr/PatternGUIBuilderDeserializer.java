package me.zort.configurationlib.support.containr;

import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.util.Placeholders;
import me.zort.containr.builder.PatternGUIBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PatternGUIBuilderDeserializer implements NodeDeserializer<PatternGUIBuilder, ConfigurationSection> {

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable PatternGUIBuilder preBuildInstance(Class<PatternGUIBuilder> deserializeInto, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        String title = ((SimpleNode<ConfigurationSection>) context.get("title")).getAsString();
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
    public PatternGUIBuilder deserialize(@NotNull PatternGUIBuilder deserializeInto, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        String title = ((SimpleNode<?>) context.get("title")).getAsString();
        String[] pattern = ((List<String>) ((SimpleNode<?>) context.get("pattern")).get()).toArray(new String[0]);

        PatternGUIBuilder builder = new PatternGUIBuilder(title, pattern);
        if (context.get("items") != null) {
            for (SectionNode<?> n : ((SectionNode<?>) context.get("items")).getNodes(NodeTypes.SECTION)) {
                String mark = n.getName();
                if (mark.length() > 1) {
                    context.getNode().debug("Mark '" + mark + "' is too long! It should be only one character long!");
                    continue;
                }

                BukkitSectionNode item = (BukkitSectionNode) n;
                builder.andMark(mark, item.getAsItem(placeholders));
            }
        }
        return builder;
    }
}
