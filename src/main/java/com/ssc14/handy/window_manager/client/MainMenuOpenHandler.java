package com.ssc14.handy.window_manager.client;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.window.Window;
import com.ssc14.handy.window_manager.window.WindowManager;
import com.ssc14.handy.window_manager.window.WindowProperties;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public class MainMenuOpenHandler {
    private static final Identifier HOTBAR_LOCATION =
            Identifier.parse("handy:ui/hotbar.xml");
    private static boolean initialized = false;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (initialized || !(event.getNewScreen() instanceof TitleScreen)) {
            return;
        }

        initialized = true;

        WindowManager.INSTANCE.addStandardWindow(MainMenuOpenHandler::createHotbar);
    }

    private static Window createHotbar(){
        var xml = XmlUtils.loadXml(HOTBAR_LOCATION);
        if (xml == null) {
            throw new IllegalStateException(
                    "Failed to load " + HOTBAR_LOCATION
            );
        }
        var ui = UI.of(xml);

        Window window = WindowManager.createWindowFromMui(ModularUI.of(ui), 0, 0, new WindowProperties().autoSetup(false).immovable());

        window.layout(layoutStyle -> layoutStyle
                .positionType(TaffyPosition.ABSOLUTE)
                .width(200)
                .height(50)
                .bottom(10)
                .alignSelf(AlignItems.CENTER)
        );

        window.setAllowHitTest(false);
        window.getChildren().getFirst().setAllowHitTest(false);

        return window;
    }
}