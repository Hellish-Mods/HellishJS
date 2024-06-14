package dev.latvian.kubejs.item;

import com.google.gson.JsonElement;
import dev.latvian.kubejs.fluid.FluidStackJS;
import dev.latvian.kubejs.item.events.KubeJSItemEventHandler;
import net.minecraft.world.item.ItemStack;

public class DummyFluidItemStackJS extends ItemStackJS {
	public final FluidStackJS fluid;

	public DummyFluidItemStackJS(FluidStackJS f) {
		super(new ItemStack(KubeJSItemEventHandler.DUMMY_FLUID_ITEM.get()));
		fluid = f;
	}

	@Override
	public boolean areItemsEqual(ItemStack other) {
		return false;
	}

	@Override
	public boolean areItemsEqual(ItemStackJS other) {
		return other instanceof DummyFluidItemStackJS && fluid.getFluid() == ((DummyFluidItemStackJS) other).fluid.getFluid();
	}

	@Override
	public JsonElement toRawResultJson() {
		return fluid.toJson();
	}

	@Override
	public JsonElement toJson() {
		return fluid.toJson();
	}

	@Override
	public FluidStackJS getFluidStack() {
		return fluid;
	}
}
