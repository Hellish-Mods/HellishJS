package dev.latvian.kubejs.integration.jei;

import dev.latvian.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.fluid.FluidStackJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.script.ScriptType;
import lombok.val;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.plugins.jei.info.IngredientInfoRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author LatvianModder
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {
	public static final ResourceLocation ID = new ResourceLocation(KubeJS.MOD_ID, "jei");
	public IJeiRuntime runtime;

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
		BuiltinKubeJSPlugin.GLOBAL.put("jeiRuntime", runtime);

		new HideJEIEventJS<>(runtime, VanillaTypes.ITEM, object -> IngredientJS.of(object)::testVanilla, stack -> !stack.isEmpty()).post(ScriptType.CLIENT, JEIIntegration.JEI_HIDE_ITEMS);

        new HideJEIEventJS<>(runtime, VanillaTypes.FLUID, object -> {
            val fs = FluidStackJS.of(object);
            return fluidStack -> fluidStack.getFluid().isSame(fs.getFluid())
                && Objects.equals(fluidStack.getTag(), fs.getNbt());}, stack -> !stack.isEmpty()).post(ScriptType.CLIENT, JEIIntegration.JEI_HIDE_FLUIDS);

		new HideCustomJEIEventJS(runtime).post(ScriptType.CLIENT, JEIIntegration.JEI_HIDE_CUSTOM);

		new RemoveJEICategoriesEvent(runtime).post(ScriptType.CLIENT, JEIIntegration.JEI_REMOVE_CATEGORIES);
		new RemoveJEIRecipesEvent(runtime).post(ScriptType.CLIENT, JEIIntegration.JEI_REMOVE_RECIPES);

		new AddJEIEventJS<>(runtime, VanillaTypes.ITEM, object -> ItemStackJS.of(object).getItemStack(), stack -> !stack.isEmpty()).post(ScriptType.CLIENT, JEIIntegration.JEI_ADD_ITEMS);
		new AddJEIEventJS<>(runtime, VanillaTypes.FLUID, object -> fromArchitectury(FluidStackJS.of(object).getFluidStack()), stack -> !stack.isEmpty()).post(ScriptType.CLIENT, JEIIntegration.JEI_ADD_FLUIDS);
	}

	private FluidStack fromArchitectury(me.shedaniel.architectury.fluid.FluidStack stack) {
		return new FluidStack(stack.getFluid(), stack.getAmount().intValue(), stack.getTag());
	}

	@Override
	public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
		new JEISubtypesEventJS(registration).post(ScriptType.CLIENT, JEIIntegration.JEI_SUBTYPES);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<IngredientInfoRecipe<?>> list = new ArrayList<>();
		new InformationJEIEventJS(registration.getIngredientManager(), list).post(ScriptType.CLIENT, JEIIntegration.JEI_INFORMATION);
		registration.addRecipes(list, VanillaRecipeCategoryUid.INFORMATION);
	}
}