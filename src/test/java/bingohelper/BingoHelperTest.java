package bingohelper;

import bingohelper.BingoHelperPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BingoHelperTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BingoHelperPlugin.class);
		RuneLite.main(args);
	}
}