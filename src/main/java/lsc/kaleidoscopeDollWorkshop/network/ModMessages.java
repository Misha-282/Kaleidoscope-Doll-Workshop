package lsc.kaleidoscopeDollWorkshop.network;

import com.mojang.authlib.GameProfile;
import lsc.kaleidoscopeDollWorkshop.KaleidoscopeDollWorkshop;
import lsc.kaleidoscopeDollWorkshop.gui.ComputerScreenHandler;
import lsc.kaleidoscopeDollWorkshop.item.DollItem;
import lsc.kaleidoscopeDollWorkshop.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class ModMessages {
    public static final Identifier CRAFT_DOLL_ID = new Identifier(KaleidoscopeDollWorkshop.MOD_ID, "craft_doll");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(CRAFT_DOLL_ID, (server, player, handler, buf, responseSender) -> {
            String targetName = buf.readString();

            server.execute(() -> {
                if (player.currentScreenHandler instanceof ComputerScreenHandler screenHandler) {
                    ItemStack inputStack = screenHandler.getInventory().getStack(0);
                    ItemStack outputStack = screenHandler.getInventory().getStack(1);

                    // 1. 检查原材料 (羊毛)
                    if (inputStack.isIn(ItemTags.WOOL)) {
                        boolean canCraft = false;
                        boolean isNewStack = false;

                        // 2. 检查是否可制作或堆叠
                        if (outputStack.isEmpty()) {
                            canCraft = true;
                            isNewStack = true;
                        } else {
                            if (outputStack.getItem() == ModItems.PLAYER_DOLL && outputStack.getCount() < outputStack.getMaxCount()) {
                                GameProfile existingProfile = DollItem.getGameProfile(outputStack);
                                if (existingProfile != null && existingProfile.getName() != null && existingProfile.getName().equals(targetName)) {
                                    canCraft = true;
                                    isNewStack = false;
                                }
                            }
                        }

                        // 3. 执行制作逻辑
                        if (canCraft) {
                            inputStack.decrement(1);

                            if (isNewStack) {
                                ItemStack doll = new ItemStack(ModItems.PLAYER_DOLL);
                                GameProfile tempProfile = new GameProfile(null, targetName);
                                NbtCompound ownerTag = NbtHelper.writeGameProfile(new NbtCompound(), tempProfile);
                                doll.getOrCreateNbt().put("Owner", ownerTag);
                                screenHandler.getInventory().setStack(1, doll);

                                // 异步获取皮肤数据
                                triggerSkinFetch(screenHandler, targetName, tempProfile);
                            } else {
                                outputStack.increment(1);
                            }

                            if (screenHandler.getInventory() instanceof BlockEntity be) {
                                be.markDirty();
                            }
                        }
                    }
                }
            });
        });
    }

    // 异步皮肤加载
    private static void triggerSkinFetch(ComputerScreenHandler screenHandler, String targetName, GameProfile tempProfile) {
        SkullBlockEntity.loadProperties(tempProfile, (fullProfile) -> {
            ItemStack currentOutput = screenHandler.getInventory().getStack(1);
            if (!currentOutput.isEmpty() && currentOutput.getItem() == ModItems.PLAYER_DOLL) {
                GameProfile currentProfile = DollItem.getGameProfile(currentOutput);
                if (currentProfile != null && currentProfile.getName().equals(targetName)) {
                    NbtCompound fullProfileTag = NbtHelper.writeGameProfile(new NbtCompound(), fullProfile);
                    currentOutput.getOrCreateNbt().put("Owner", fullProfileTag);

                    if (screenHandler.getInventory() instanceof BlockEntity be) {
                        be.markDirty();
                    }
                }
            }
        });
    }
}