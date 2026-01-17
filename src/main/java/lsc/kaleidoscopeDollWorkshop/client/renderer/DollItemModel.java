package lsc.kaleidoscopeDollWorkshop.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lsc.kaleidoscopeDollWorkshop.KaleidoscopeDollWorkshop;
import lsc.kaleidoscopeDollWorkshop.item.DollItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

public class DollItemModel extends GeoModel<DollItem> {
    @SuppressWarnings("removal")
    private static final ResourceLocation STEVE_MODEL = new ResourceLocation(KaleidoscopeDollWorkshop.MOD_ID, "geo/steve.json");
    @SuppressWarnings("removal")
    private static final ResourceLocation ALEX_MODEL = new ResourceLocation(KaleidoscopeDollWorkshop.MOD_ID, "geo/alex.json");
    @SuppressWarnings("removal")
    private static final ResourceLocation STEVE_TEXTURE = new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    private GameProfile currentProfile;

    public void setProfile(GameProfile profile) {
        this.currentProfile = profile;
    }

    @Override
    public ResourceLocation getModelResource(DollItem object) {
        if (currentProfile != null) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map =
                    Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(currentProfile);

            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                String metadata = map.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
                if ("slim".equals(metadata)) return ALEX_MODEL;
            }
        }
        return STEVE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DollItem object) {
        if (currentProfile != null) {
            return Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(currentProfile);
        }
        return STEVE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DollItem animatable) {
        return null;
    }
}