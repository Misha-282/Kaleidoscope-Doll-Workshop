package lsc.kaleidoscopeDollWorkshop.network.packet;

import com.mojang.authlib.GameProfile;
import lsc.kaleidoscopeDollWorkshop.item.DollItem;
import lsc.kaleidoscopeDollWorkshop.menu.ComputerMenu;
import lsc.kaleidoscopeDollWorkshop.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCraftDoll {
    private final String targetName;

    public PacketCraftDoll(String targetName) {
        this.targetName = targetName;
    }

    public PacketCraftDoll(FriendlyByteBuf buf) {
        this.targetName = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(targetName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof ComputerMenu menu) {
                handleCrafting(menu);
            }
        });
        return true;
    }

    private void handleCrafting(ComputerMenu menu) {
        ItemStack inputStack = menu.blockEntity.getItemHandler().getStackInSlot(0);
        ItemStack outputStack = menu.blockEntity.getItemHandler().getStackInSlot(1);

        // 验证输入材料
        if (!inputStack.is(ItemTags.WOOL)) return;

        boolean canCraft = false;
        boolean isNewStack = false;

        // 验证输出槽是否可用
        if (outputStack.isEmpty()) {
            canCraft = true;
            isNewStack = true;
        } else if (outputStack.getItem() == ModItems.PLAYER_DOLL.get()) {
            GameProfile profile = DollItem.getGameProfile(outputStack);
            if (profile != null && targetName.equals(profile.getName()) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                canCraft = true;
            }
        }

        if (canCraft) {
            inputStack.shrink(1);
            if (isNewStack) {
                ItemStack doll = new ItemStack(ModItems.PLAYER_DOLL.get());
                GameProfile temp = new GameProfile(null, targetName);

                CompoundTag nbt = new CompoundTag();
                nbt.put("Owner", NbtUtils.writeGameProfile(new CompoundTag(), temp));
                doll.setTag(nbt);

                menu.blockEntity.getItemHandler().setStackInSlot(1, doll);
                updateSkinDataAsync(menu, temp);
            } else {
                outputStack.grow(1);
            }
        }
    }

    private void updateSkinDataAsync(ComputerMenu menu, GameProfile tempProfile) {
        SkullBlockEntity.updateGameprofile(tempProfile, (fullProfile) -> {
            ItemStack current = menu.blockEntity.getItemHandler().getStackInSlot(1);
            if (!current.isEmpty() && current.getItem() == ModItems.PLAYER_DOLL.get()) {
                CompoundTag fullTag = NbtUtils.writeGameProfile(new CompoundTag(), fullProfile);
                current.getOrCreateTag().put("Owner", fullTag);
                menu.blockEntity.setChanged();
            }
        });
    }
}