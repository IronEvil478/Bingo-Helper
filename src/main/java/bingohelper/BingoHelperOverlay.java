package bingohelper;

import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

import javax.inject.Inject;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class BingoHelperOverlay extends OverlayPanel
{
    @Inject
    private BingoHelperConfig config;
    @Inject
    private BingoHelperOverlay()
    {
        setPosition(OverlayPosition.TOP_CENTER);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Bingo Helper overlay"));
    }

    private String eventName;
    public void setEventName(String name){
        eventName = name;
    }
    private List<String> currentItems;
    public void setCurrentItems(List<String> items){
        currentItems = items;
    }
    
    
    @Override
    public Dimension render(Graphics2D graphics)
    {
        String text = eventName;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left(text)
                .leftColor(Color.green)
                .build());

        if (config.dtm())
        {
            text = text + " " + localToGMT();
            List<LayoutableRenderableEntity> elem = panelComponent.getChildren();
            ((LineComponent) elem.get(0)).setRight(localToGMT());
        }
        if (currentItems != null) {
            String items = currentItems.toString();
            if (config.showcurrent()){
                panelComponent.getChildren().add(LineComponent.builder().right(items).rightColor(Color.white).build());
            }
        }

        panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(text) + 10, 0));
        
        return super.render(graphics);
    }

    public static String localToGMT() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date) + " UTC";
    }
}