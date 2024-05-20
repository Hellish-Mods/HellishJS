package dev.latvian.kubejs.recipe;

import com.google.gson.JsonObject;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.item.ingredient.TagIngredientJS;
import dev.latvian.kubejs.recipe.minecraft.CustomRecipeJS;
import dev.latvian.kubejs.util.ConsoleJS;
import dev.latvian.kubejs.util.ListJS;
import dev.latvian.kubejs.util.MapJS;
import dev.latvian.kubejs.util.WrappedJS;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class RecipeFunction extends BaseFunction implements WrappedJS {
	private static final Pattern SKIP_ERROR = Pattern.compile("at dev.latvian.kubejs.recipe.RecipeFunction.call");

	private final RecipeEventJS event;
	public final ResourceLocation typeID;
	public final RecipeTypeJS type;

	public RecipeFunction(RecipeEventJS e, ResourceLocation id, @Nullable RecipeTypeJS t) {
		event = e;
		typeID = id;
		type = t;
	}

	@Override
	public RecipeJS call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args0) {
		return createRecipe(args0);
	}

	public RecipeJS createRecipe(Object[] args0) {
		try {
			if (type == null) {
				throw new RecipeExceptionJS("Unknown recipe type!");
			}

			ListJS args = ListJS.of(args0);

			if (args == null || args.isEmpty()) {
				throw new RecipeExceptionJS("Recipe requires at least one argument!");
			} else if (type.isCustom() && args.size() != 1) {
				throw new RecipeExceptionJS("Custom recipe has to use a single json object argument!");
			}

			if (args.size() == 1) {
				MapJS map = MapJS.of(args.get(0));

				if (map != null) {
					RecipeJS recipe = type.factory.get();
					recipe.type = type;
					recipe.json = ((MapJS) normalize(map)).toJson();

					if (!(recipe instanceof CustomRecipeJS)) {
						recipe.serializeInputs = true;
						recipe.serializeOutputs = true;
						recipe.deserializeJson();
					}

					return event.addRecipe(recipe, type, args);
				}
			}

			RecipeJS recipe = type.factory.get();
			recipe.type = type;
			recipe.json = new JsonObject();
			recipe.serializeInputs = true;
			recipe.serializeOutputs = true;
			recipe.create(args);
			return event.addRecipe(recipe, type, args);
		} catch (RecipeExceptionJS ex) {
			ex.error();
			ConsoleJS.SERVER.error("Failed to create recipe for type '" + typeID + "'", ex, SKIP_ERROR);
		} catch (Exception ex) {
			ConsoleJS.SERVER.printStackTrace(ex, SKIP_ERROR);
		}

		return new CustomRecipeJS();
	}

	private Object normalize(Object o) {
		if (o instanceof ItemStackJS) {
			return ((ItemStackJS) o).toResultJson();
		} else if (o instanceof IngredientJS) {
			return ((IngredientJS) o).toJson();
		} else if (o instanceof String s) {

			if (s.length() >= 4 && s.startsWith("#") && s.indexOf(':') != -1) {
				return TagIngredientJS.createTag(s.substring(1)).toJson();
			}

			return o;
		} else if (o instanceof ListJS) {
			ListJS list = new ListJS();

			for (Object o1 : (ListJS) o) {
				list.add(normalize(o1));
			}

			return list;
		} else if (o instanceof MapJS) {
			MapJS map = new MapJS();

			for (Map.Entry<String, Object> entry : ((MapJS) o).entrySet()) {
				map.put(entry.getKey(), normalize(entry.getValue()));
			}

			return map;
		}

		return o;
	}

	@Override
	public String toString() {
		return typeID.toString();
	}
}