package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.foundation.mixin.TankSegmentAccessor;

import java.util.function.Predicate;

public class FilteringFluidTankBehaviour extends SmartFluidTankBehaviour {
    
    protected final Predicate<FluidStack> filter;
    
    public FilteringFluidTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type,
                                       Predicate<FluidStack> filter,
                                       SmartBlockEntity be,
                                       int tanks, int tankCapacity,
                                       boolean enforceVariety) {
        super(type, be, tanks, tankCapacity, enforceVariety);
        this.filter = filter;
        IFluidHandler[] handlers = new IFluidHandler[tanks];
        for (int i = 0; i < tanks; i++) {
            TankSegment tankSegment = new TankSegment(tankCapacity);
            this.tanks[i] = tankSegment;
            handlers[i] = ((TankSegmentAccessor)tankSegment).getTank();
        }
        this.capability = LazyOptional.of(() -> new InternalFluidHandler(handlers, enforceVariety));
    }
    
    public static FilteringFluidTankBehaviour single(Predicate<FluidStack> filter, SmartBlockEntity be, int capacity) {
        return new FilteringFluidTankBehaviour(TYPE, filter, be, 1, capacity, false);
    }
    
    public class InternalFluidHandler extends SmartFluidTankBehaviour.InternalFluidHandler {
        
        public InternalFluidHandler(IFluidHandler[] handlers, boolean enforceVariety) {
            super(handlers, enforceVariety);
        }
    
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!filter.test(resource))
                return 0;
            return super.fill(resource, action);
        }
        
    }
    
}
