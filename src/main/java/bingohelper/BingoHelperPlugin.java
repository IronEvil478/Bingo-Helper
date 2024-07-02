package bingohelper;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.http.api.item.ItemPrice;
import okhttp3.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
        name = "Bingo Helper"
)
public class BingoHelperPlugin extends Plugin {
    @Inject
    private BingoHelperConfig config;
    @Inject
    private ItemManager itemManager;
    @Inject
    private OkHttpClient okHttpClient;
    @Inject
    private DrawManager drawManager;
    @Inject
    private Client client;
    @Inject
    private Gson clientGson;
    @Inject
	private OverlayManager overlayManager;
    @Inject
	private BingoHelperOverlay overlay;

    @Provides
    BingoHelperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BingoHelperConfig.class);
    }

    private Event event;
    private String team;
    private boolean initialValuesLoaded = false;
    private List<String> allItems;
    private String webhook;

    @Override
    protected void startUp()
    {
        // We can only do this when logged in or we error on the players name
        if (client.getGameState() == GameState.LOGGED_IN) {
            try {
                bingoInitializor();
            } catch (Exception e) {
                // Failed to load
                return;
            }
            initialValuesLoaded = true;
        }
        
    }
    @Override
	public void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

    @Subscribe
	public void onGameTick(GameTick gameTick)
	{
        // This handles the wait for login so that we can load the players name
		if (!initialValuesLoaded)
		{
            try {
                bingoInitializor();
            } catch (Exception e) {
                // Failed to load
                return;
            }
			initialValuesLoaded = true;
		}
	}

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        try {
            bingoInitializor();
        } catch (Exception e) {
            // Failed to load
            initialValuesLoaded = false;
            return;
        }
        if (event == null) {
            log.info("No valid event found");
        }
    }

    @Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if ("currentitems".equalsIgnoreCase(commandExecuted.getCommand()))
		{
            if (event.getBoard().getTiles().size() != 0) {
			    chatMessage("test: " + event.getBoard().getTiles().get(0).getItems());
            }
		} else if ("swap".equalsIgnoreCase(commandExecuted.getCommand())) {
            overlay.setEventName("Hot swaps");
        }
	}

    @Subscribe
    public void onLootReceived(LootReceived Loot) {
        Collection<ItemStack> items = Loot.getItems();
        for (ItemStack item : items) {
            int itemId = item.getId();
            processDrop(getItemName(itemId), item.getQuantity());
        }
    }

    private void processDrop(String item, int itemCount) {
        // Determine if the Drop is part of the Bingo
        if (itemCount == 0) {return;}
        item = item.toLowerCase();
        List<String> list = List.of();
        try {
            list = getCurrentItems();
        } catch (Exception e) {
            chatMessage("Current tile is out of bounds");
        }
        
        if (!list.contains(item)) {
            // Item is not in the bingo
            log.debug("item not in bingo");
            return;
        }
        
        // Maybe find a cleaner way, this is a full notification
        chatMessage(ColorUtil.wrapWithColorTag("Bingo Item Dropped! Dropped Item: " + item, ColorUtil.fromHex("0x1FBB1F")));
        
        // Prep the Web call
        WebhookBody webhookBody = new WebhookBody();
        StringBuilder stringBuilder = new StringBuilder();
        
        // Adds name
        stringBuilder.append("\n**").append(getPlayerName()).append("**");
        // Adds the Team
        stringBuilder.append(" - **").append(team).append("**");
        // Adds the item
        stringBuilder.append(" - **").append(item).append("**");
        // Addds the current tile
        stringBuilder.append(" - **").append(getItemTile(item)).append("**");

        stringBuilder.append("\n");
        
        int itemId = getItemID(item);
        if(itemId >= 0) {
            webhookBody.getEmbeds().add(new WebhookBody.Embed(new WebhookBody.UrlEmbed(itemImageUrl(itemId))));
        }

        webhookBody.setContent(stringBuilder.toString());
        sendWebhook(webhookBody);
    }
    
    // Tooling for making things work
    // Throws exception in several places, biggest thing is the name on login screen and the Json being miss configured
    private void bingoInitializor() throws Exception
    {
        try {
            event = null;
            event = clientGson.fromJson(config.bingostring(), Event.class);
        } catch (Exception e) {
            log.info("failed to unmarshall the json string");
            return;
        }
        team = getTeam();

        List<String> list = new ArrayList<>();
        for (Tile tile : event.getBoard().getTiles()) {
            if (tile.getItems() == null) {
                continue;
            }
            for (String item : tile.getItems()) {
                list.add(item);
            }
        }
        allItems = list;
        
        webhook = event.getWebhook();

        overlayManager.add(overlay);
        overlay.setEventName(event.getName());
        overlay.setCurrentItems(getCurrentItems());
    }

    // Tooling to make things easier to read
    private static String itemImageUrl(int itemId) 
    {
        return "https://static.runelite.net/cache/item/icon/" + itemId + ".png";
    }
    private String getPlayerName()
    {
        return client.getLocalPlayer().getName();
    }
    private String getTeam() 
    {
        String team = "no team";
        if (event.getTeams().size() == 0) 
        {
            return team;
        }
        for (Team teamobj : event.getTeams()) {
            if (teamobj.getPlayers().contains(getPlayerName())) {
                return teamobj.getName();
            }
        }
        return team;
    }
    private String getItemName(int itemID) 
    {
        return itemManager.getItemComposition(itemID).getMembersName();
    }
    private int getItemID(String itemName) 
    {
        List<ItemPrice> items = itemManager.search(itemName);
        if (items.size() == 1) {
            return items.get(0).getId();
        }
        return -1;
    }
    private void chatMessage(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
	}
    private List<String> getCurrentItems() {
        if (config.usecurrenttile()) {
            return event.getBoard().getTiles().get(config.currenttile()).getItems();
        } else {
            return allItems;
        }
    }
    private int getItemTile(String item)
    {
        if (config.usecurrenttile()) {
            return config.currenttile();
        } else {
            for (int i = 0; i < event.getBoard().getTiles().size(); i++) {
                try {
                    if (event.getBoard().getTiles().get(i).getItems().contains(item)) {
                    return i;
                }
                } catch (Exception e) {
                    // Failure is ok
                }
                
            }
            return -1;
        }
    }

    // Web call processing
    private void sendWebhook(WebhookBody webhookBody)
    {
        String configUrl = webhook;
        if (Strings.isNullOrEmpty(configUrl))
        {
            return;
        }

        String[] configArray = configUrl.split(",");

        for(int i = 0; i < configArray.length; i++) {

            HttpUrl url = HttpUrl.parse(configArray[i].trim());
            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("payload_json", GSON.toJson(webhookBody));

            sendWebhookWithScreenshot(url, requestBodyBuilder);
        }
    }
    private void sendWebhookWithScreenshot(HttpUrl url, MultipartBody.Builder requestBodyBuilder) {
        drawManager.requestNextFrameListener(image ->
        {
            BufferedImage bufferedImage = (BufferedImage) image;
            byte[] imageBytes;
            try
            {
                imageBytes = convertImageToByteArray(bufferedImage);
            }
            catch (IOException e)
            {
                log.warn("Error converting image to byte array", e);
                return;
            }

            requestBodyBuilder.addFormDataPart("file", "image.png",
                    RequestBody.create(MediaType.parse("image/png"), imageBytes));
            buildRequestAndSend(url, requestBodyBuilder);
        });
    }
    private void buildRequestAndSend(HttpUrl url, MultipartBody.Builder requestBodyBuilder)
    {
        RequestBody requestBody = requestBodyBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        sendRequest(request);
    }
    private void sendRequest(Request request)
    {
        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.debug("Error submitting webhook", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                response.close();
            }
        });
    }
    private static byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
