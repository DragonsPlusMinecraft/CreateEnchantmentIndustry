package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class BlazeEnchanterEditPacket extends SimplePacketBase {

    private final int index;
    private final ItemStack itemStack;
    private final BlockPos blockPos;


    public BlazeEnchanterEditPacket(int index, ItemStack enchantedBook, BlockPos blockPos) {
        this.index = index;
        itemStack = enchantedBook;
        this.blockPos = blockPos;
    }

    public BlazeEnchanterEditPacket(FriendlyByteBuf buffer) {
        index = buffer.readInt();
        itemStack = buffer.readItem();
        blockPos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeItem(itemStack);
        buffer.writeBlockPos(blockPos);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> {
                    ServerPlayer sender = context.getSender();
                    if(!(sender.level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity blazeEnchanter))
                        return;

                    CompoundTag tag = blazeEnchanter.targetItem.getOrCreateTag();
                    tag.putInt("index", index);
                    tag.put("target", itemStack.serializeNBT());
                    tag.remove("blockPos");

                    if(blazeEnchanter.processingTicks>5){
                        blazeEnchanter.processingTicks = BlazeEnchanterBlockEntity.ENCHANTING_TIME;
                    }

                    blazeEnchanter.notifyUpdate();
                });
        return true;
    }
}
