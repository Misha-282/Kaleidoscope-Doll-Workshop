package lsc.kaleidoscopeDollWorkshop.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lsc.kaleidoscopeDollWorkshop.KaleidoscopeDollWorkshop;
import lsc.kaleidoscopeDollWorkshop.item.DollItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

public class DollItemModel extends GeoModel<DollItem> {
    private static final Identifier STEVE_MODEL = new Identifier(KaleidoscopeDollWorkshop.MOD_ID, "geo/steve.json");
    private static final Identifier ALEX_MODEL = new Identifier(KaleidoscopeDollWorkshop.MOD_ID, "geo/alex.json");
    private static final Identifier STEVE_TEXTURE = new Identifier("minecraft", "textures/entity/player/wide/steve.png");

    private GameProfile currentProfile;

    public void setProfile(GameProfile profile) {
        this.currentProfile = profile;
    }

    @Override
    public Identifier getModelResource(DollItem object) {
        if (currentProfile != null) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map =
                    MinecraftClient.getInstance().getSkinProvider().getTextures(currentProfile);

            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                String metadata = map.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
                if ("slim".equals(metadata)) {
                    return ALEX_MODEL;
                }
            }
        }
        return STEVE_MODEL;
    }

    @Override
    public Identifier getTextureResource(DollItem object) {
        if (currentProfile != null) {
            return MinecraftClient.getInstance().getSkinProvider().loadSkin(currentProfile);
        }
        return STEVE_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DollItem animatable) {
        return null;
    }
}