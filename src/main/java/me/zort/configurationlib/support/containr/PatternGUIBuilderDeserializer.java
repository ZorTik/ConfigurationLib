package me.zort.configurationlib.support.containr;

import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.util.Placeholders;
import me.zort.containr.builder.PatternGUIBuilder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PatternGUIBuilderDeserializer implements NodeDeserializer<PatternGUIBuilder, ConfigurationSection> {

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
                builder.andMark(mark, item.getAsItem(placeholders));
            }
        }
        return builder;
    }
}
