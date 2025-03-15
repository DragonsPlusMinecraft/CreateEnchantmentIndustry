package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.CeiComponents;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;

public class EnchantingGuideEditPacket implements ServerboundPacketPayload {

    private final int index;
    private final ItemStack itemStack;

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingGuideEditPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, packet -> packet.index,
            ItemStack.OPTIONAL_STREAM_CODEC, packet -> packet.itemStack,
            EnchantingGuideEditPacket::new
    );

    public EnchantingGuideEditPacket(int index, ItemStack enchantedBook) {
        this.index = index;
        itemStack = enchantedBook;
    }

    public void handle(ServerPlayer sender) {
        ItemStack mainHandItem = sender.getMainHandItem();
        if (!CeiItems.ENCHANTING_GUIDE.isIn(mainHandItem))
            return;

        mainHandItem.set(CeiComponents.ENCHANTING_INDEX, index);
        mainHandItem.set(CeiComponents.ENCHANTING_TARGET, (CompoundTag) itemStack.saveOptional(sender.level().registryAccess()));

        sender.getCooldowns()
                .addCooldown(mainHandItem.getItem(), 5);
    }

    public PacketTypeProvider getTypeProvider() {
        return CeiPackets.CONFIGURE_ENCHANTING_GUIDE_FOR_BLAZE;
    }
}
