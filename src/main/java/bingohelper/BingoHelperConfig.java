package bingohelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(BingoHelperConfig.GROUP)
public interface BingoHelperConfig extends Config{
    String GROUP = "BingoHelper";

    @ConfigItem(
        keyName = "bingostring",
        name = "Bingo String",
        description = "This is the string of items for the bingo"
    )
    String bingostring();

    @ConfigItem(
        keyName = "usecurrenttile",
        name = "Use Current Tile",
        description = "Limits the discord messages to just items on the current tile."
    )
    boolean usecurrenttile();

    @ConfigItem(
            keyName = "currenttile",
            name = "Current Tile",
            description = "The tile that the team is on"
    )
    int currenttile();

    @ConfigItem(
            keyName = "showcurrent",
            name = "Show Current Items",
            description = "Shows the current items the team is working on "
    )
    boolean showcurrent();

    @ConfigItem(
            keyName = "dtm",
            name = "Show time",
            description = "Shows the current items the team is working on "
    )
    boolean dtm();
}
