package lsc.kaleidoscopeDollWorkshop.client;

import lsc.kaleidoscopeDollWorkshop.client.gui.ComputerScreen;
import lsc.kaleidoscopeDollWorkshop.client.renderer.DollBlockEntityRenderer;
import lsc.kaleidoscopeDollWorkshop.registry.ModBlockEntities;
import lsc.kaleidoscopeDollWorkshop.registry.ModMenuTypes;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientModEvents {

    // 注册 GUI 屏幕与菜单类型的绑定
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPUTER_MENU.get(), ComputerScreen::new);
    }

    // 注册方块实体渲染器
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DOLL_BE.get(), DollBlockEntityRenderer::new);
    }
}