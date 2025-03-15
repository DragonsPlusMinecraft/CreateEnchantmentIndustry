package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.CeiComponents;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;

public class BlazeEnchanterEditPacket implements ServerboundPacketPayload {

    private final int index;
    private final ItemStack itemStack;
    private final BlockPos blockPos;

    public static final StreamCodec<RegistryFriendlyByteBuf, BlazeEnchanterEditPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, packet -> packet.index,
            ItemStack.STREAM_CODEC, packet -> packet.itemStack,
            BlockPos.STREAM_CODEC, packet -> packet.blockPos,
            BlazeEnchanterEditPacket::new
    );

    public BlazeEnchanterEditPacket(int index, ItemStack enchantedBook, BlockPos blockPos) {
        this.index = index;
        this.itemStack = enchantedBook;
        this.blockPos = blockPos;
    }

    public void handle(ServerPlayer sender) {
        if(!(sender.level().getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity blazeEnchanter))
            return;

        ItemStack target = blazeEnchanter.targetItem;
        target.set(CeiComponents.ENCHANTING_INDEX, index);
        target.set(CeiComponents.ENCHANTING_TARGET, (CompoundTag) itemStack.save(sender.level().registryAccess()));

        if (blazeEnchanter.processingTicks > 5) {
            blazeEnchanter.processingTicks = BlazeEnchanterBlockEntity.ENCHANTING_TIME;
        }

        blazeEnchanter.notifyUpdate();
    }

    public PacketTypeProvider getTypeProvider() {
        return CeiPackets.CONFIGURE_BLAZE_ENCHANTER;
    }
}
