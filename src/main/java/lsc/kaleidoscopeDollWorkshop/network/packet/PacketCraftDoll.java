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
    private final boolean craftAll; // 是否执行批量合成

    public PacketCraftDoll(String targetName, boolean craftAll) {
        this.targetName = targetName;
        this.craftAll = craftAll;
    }

    public PacketCraftDoll(FriendlyByteBuf buf) {
        this.targetName = buf.readUtf();
        this.craftAll = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(targetName);
        buf.writeBoolean(craftAll);
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

        // 验证输入槽是否有羊毛
        if (!inputStack.is(ItemTags.WOOL)) return;

        int craftAmount = 0;
        boolean isNewStack = false;

        // 计算合成数量
        if (outputStack.isEmpty()) {
            isNewStack = true;
            // 如果批量制作，则数量为输入数量与堆叠上限的较小值；否则为1
            craftAmount = craftAll ? Math.min(inputStack.getCount(), ModItems.PLAYER_DOLL.get().getMaxStackSize()) : 1;
        } else if (outputStack.getItem() == ModItems.PLAYER_DOLL.get()) {
            GameProfile profile = DollItem.getGameProfile(outputStack);
            // 验证输出槽物品的NBT数据与目标玩家名是否一致
            if (profile != null && targetName.equals(profile.getName())) {
                int space = outputStack.getMaxStackSize() - outputStack.getCount();
                if (space > 0) {
                    // 如果批量制作，取输入数量与剩余空间的较小值；否则为1
                    craftAmount = craftAll ? Math.min(inputStack.getCount(), space) : 1;
                }
            }
        }

        // 执行物品消耗与生成
        if (craftAmount > 0) {
            inputStack.shrink(craftAmount);

            if (isNewStack) {
                ItemStack doll = new ItemStack(ModItems.PLAYER_DOLL.get(), craftAmount);
                GameProfile temp = new GameProfile(null, targetName);

                CompoundTag nbt = new CompoundTag();
                nbt.put("Owner", NbtUtils.writeGameProfile(new CompoundTag(), temp));
                doll.setTag(nbt);

                menu.blockEntity.getItemHandler().setStackInSlot(1, doll);
                // 异步更新皮肤数据
                updateSkinDataAsync(menu, temp);
            } else {
                outputStack.grow(craftAmount);
            }
        }
    }

    private void updateSkinDataAsync(ComputerMenu menu, GameProfile tempProfile) {
        SkullBlockEntity.updateGameprofile(tempProfile, (fullProfile) -> {
            ItemStack current = menu.blockEntity.getItemHandler().getStackInSlot(1);
            // 确保更新时物品仍然是对应的玩偶
            if (!current.isEmpty() && current.getItem() == ModItems.PLAYER_DOLL.get()) {
                CompoundTag fullTag = NbtUtils.writeGameProfile(new CompoundTag(), fullProfile);
                current.getOrCreateTag().put("Owner", fullTag);
                menu.blockEntity.setChanged();
            }
        });
    }
}