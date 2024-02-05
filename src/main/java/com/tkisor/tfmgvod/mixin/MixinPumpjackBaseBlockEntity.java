package com.tkisor.tfmgvod.mixin;

import com.drmangotea.tfmg.blocks.machines.oil_processing.pumpjack.base.PumpjackBaseBlockEntity;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = PumpjackBaseBlockEntity.class, remap = false)
public abstract class MixinPumpjackBaseBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    @Shadow public BlockPos deposit;
    @Shadow public FluidTank tankInventory;
    @Shadow public int miningRate;

    public MixinPumpjackBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("RETURN"), method = "findDeposit", remap = false)
    public void findDeposit(CallbackInfo ci) {

    }

    @Inject(at = @At("RETURN"), method = "process", remap = false)
    public void process(CallbackInfo ci) {
        if (this.deposit == null) {
            if (this.tankInventory.getFluidAmount() + (this.miningRate/12) <= 8000) {
                this.tankInventory.setFluid(new FluidStack(TFMGFluids.CRUDE_OIL.getSource(), this.tankInventory.getFluidAmount() + (this.miningRate/12)));
            }
        }
    }

    /**
     * @author Tki_sor
     * @reason Overwrite the original method to add the fluid tank information to the goggle tooltip
     */
    @Overwrite
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("goggles.pumpjack_info", new Object[0]).forGoggles(tooltip);
        LangBuilder mb = Lang.translate("generic.unit.millibuckets", new Object[0]);
        Lang.translate("goggles.pumpjack.deposit_info", new Object[0]).style(ChatFormatting.GRAY).forGoggles(tooltip);
        if (this.deposit != null) {
            Lang.translate("pumpjack_deposit_amount", new Object[]{this.miningRate}).style(ChatFormatting.LIGHT_PURPLE).forGoggles(tooltip, 1);
        } else {
            Lang.translate("goggles.zero.virtual", new Object[0]).style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
        }

        LazyOptional<IFluidHandler> handler = this.getCapability(ForgeCapabilities.FLUID_HANDLER);
        Optional<IFluidHandler> resolve = handler.resolve();
        if (!resolve.isPresent()) {
            return false;
        } else {
            IFluidHandler tank = (IFluidHandler)resolve.get();
            if (tank.getTanks() == 0) {
                return false;
            } else {
                boolean isEmpty = true;

                for(int i = 0; i < tank.getTanks(); ++i) {
                    FluidStack fluidStack = tank.getFluidInTank(i);
                    if (!fluidStack.isEmpty()) {
                        Lang.fluidName(fluidStack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
                        Lang.builder().add(Lang.number((double)fluidStack.getAmount()).add(mb).style(ChatFormatting.DARK_GREEN)).text(ChatFormatting.GRAY, " / ").add(Lang.number((double)tank.getTankCapacity(i)).add(mb).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
                        isEmpty = false;
                    }
                }

                if (tank.getTanks() > 1) {
                    if (isEmpty) {
                        tooltip.remove(tooltip.size() - 1);
                    }

                    return true;
                } else if (!isEmpty) {
                    return true;
                } else {
                    Lang.translate("gui.goggles.fluid_container.capacity", new Object[0]).add(Lang.number((double)tank.getTankCapacity(0)).add(mb).style(ChatFormatting.DARK_GREEN)).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
                    return true;
                }
            }
        }
    }
}
