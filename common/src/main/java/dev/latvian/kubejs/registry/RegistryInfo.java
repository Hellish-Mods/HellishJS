package dev.latvian.kubejs.registry;

import com.mojang.serialization.Codec;
import dev.latvian.kubejs.CommonProperties;
import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.KubeJSEvents;
import dev.latvian.kubejs.KubeJSRegistries;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.util.ConsoleJS;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @see RegistryInfos
 * @author ZZZank
 */
public final class RegistryInfo<T> implements Iterable<BuilderBase<? extends T>>, TypeWrapperFactory<T> {

    public static <T> RegistryInfo<T> of(ResourceKey<? extends Registry<?>> key, Class<T> type) {
		var r = RegistryInfos.MAP.get(key);

		if (r == null) {
			var reg = new RegistryInfo<>(UtilsJS.cast(key), type);
			RegistryInfos.MAP.put(key, reg);
			return reg;
		}

		return (RegistryInfo<T>) r;
	}

    static <T> RegistryInfo<T> of(Registry<?> registry, Class<T> type) {
		return of(registry.key(), type);
	}

	public static RegistryInfo<?> of(ResourceKey<? extends Registry<?>> key) {
		return of(key, Object.class);
	}

    public final ResourceKey<? extends Registry<T>> key;
	public final Class<T> objectBaseClass;
	public final Map<String, BuilderType<T>> types;
	public final Map<ResourceLocation, BuilderBase<? extends T>> objects;
	public boolean hasDefaultTags = false;
	private BuilderType<T> defaultType;
	public boolean bypassServerOnly;
	public boolean autoWrap;
	private me.shedaniel.architectury.registry.Registry<T> archRegistry;
	public String languageKeyPrefix;
	//used for backward compatibility
	public Supplier<RegistryEventJS<T>> customRegEvent;

	private RegistryInfo(ResourceKey<? extends Registry<T>> key, Class<T> objectBaseClass) {
		this.key = key;
		this.objectBaseClass = objectBaseClass;
		this.types = new LinkedHashMap<>();
		this.objects = new LinkedHashMap<>();
		this.bypassServerOnly = false;
		this.autoWrap = objectBaseClass != Codec.class && objectBaseClass != ResourceLocation.class && objectBaseClass != String.class;
		this.languageKeyPrefix = key.location().getPath().replace('/', '.');
		this.customRegEvent = null;
	}

	public RegistryInfo<T> bypassServerOnly() {
		this.bypassServerOnly = true;
		return this;
	}

	public RegistryInfo<T> customRegistryEvent(Supplier<RegistryEventJS<T>> supplier) {
		this.customRegEvent = supplier;
		return this;
	}

	public RegistryInfo<T> noAutoWrap() {
		this.autoWrap = false;
		return this;
	}

	public RegistryInfo<T> languageKeyPrefix(String prefix) {
		this.languageKeyPrefix = prefix;
		return this;
	}

	public void addType(String type, Class<? extends BuilderBase<? extends T>> builderType, BuilderFactory factory, boolean isDefault) {
		var b = new BuilderType<>(type, builderType, factory);
		types.put(type, b);

		if (isDefault) {
			if (defaultType != null) {
				ConsoleJS.STARTUP.warnf("Previous default type '%s' for registry '%s' replaced with '%s'!", defaultType.type(), key.location(), type);
			}

			defaultType = b;
		}
		RegistryInfos.WITH_TYPE.put(key, this);
	}

	public void addType(String type, Class<? extends BuilderBase<? extends T>> builderType, BuilderFactory factory) {
		addType(type, builderType, factory, type.equals("basic"));
	}

	public void addBuilder(BuilderBase<? extends T> builder) {
		if (builder == null) {
			throw new IllegalArgumentException("Can't add null builder in registry '" + key.location() + "'!");
		}
		if (CommonProperties.get().debugInfo) {
			ConsoleJS.STARTUP.info("~ " + key.location() + " | " + builder.id);
		}
		if (objects.containsKey(builder.id)) {
			throw new IllegalArgumentException("Duplicate key '" + builder.id + "' in registry '" + key.location() + "'!");
		}

		objects.put(builder.id, builder);
		RegistryInfos.ALL_BUILDERS.add(builder);
	}

	@Nullable
	public BuilderType getDefaultType() {
		if (types.isEmpty()) {
			return null;
		} else if (defaultType == null) {
			defaultType = types.values().iterator().next();
		}

		return defaultType;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof RegistryInfo ri && key.equals(ri.key);
	}

	@Override
	public String toString() {
		return key.location().toString();
	}

	public int registerArch() {
		return registerObjects(this.getArchRegistry()::registerSupplied);
	}

	public int registerObjects(RegistryCallback<T> function) {
		if (CommonProperties.get().debugInfo) {
			if (objects.isEmpty()) {
				KubeJS.LOGGER.info("Skipping {} registry", this);
			} else {
				KubeJS.LOGGER.info("Building {} objects of {} registry", objects.size(), this);
			}
		}

		if (objects.isEmpty()) {
			return 0;
		}

		int added = 0;

		for (var builder : this) {
			if (builder.dummyBuilder || (!builder.getRegistryType().bypassServerOnly && CommonProperties.get().serverOnly)) {
				continue;
			}
			function.accept(builder.id, builder::createTransformedObject);

			if (CommonProperties.get().debugInfo) {
				ConsoleJS.STARTUP.info("+ " + this + " | " + builder.id);
			}

			added++;
		}

		if (!objects.isEmpty() && CommonProperties.get().debugInfo) {
			KubeJS.LOGGER.info("Registered {}/{} objects of {}", added, objects.size(), this);
		}

		return added;
	}

	@NotNull
	@Override
	public Iterator<BuilderBase<? extends T>> iterator() {
		return objects.values().iterator();
	}

	@SuppressWarnings({"unchecked"})
	public me.shedaniel.architectury.registry.Registry<T> getArchRegistry() {
		if (archRegistry == null) {
			archRegistry = KubeJSRegistries.genericRegistry((ResourceKey<Registry<T>>) key);
		}
		return archRegistry;
	}

	public Registry<T> getVanillaRegistry() {
		return Registry.REGISTRY.get((ResourceKey) key);
	}

	public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
		return getArchRegistry().entrySet();
	}

	public ResourceLocation getId(T value) {
		return getArchRegistry().getId(value);
	}

	public T getValue(ResourceLocation id) {
		return getArchRegistry().get(id);
	}

	public boolean hasValue(ResourceLocation id) {
		return getArchRegistry().contains(id);
	}

	@Override
	public T wrap(Object o) {
		if (o == null) {
			return null;
		} else if (objectBaseClass.isInstance(o)) {
			return (T) o;
		}

		var id = UtilsJS.getMCID(o);
		var value = getValue(id);

		if (value == null) {
			var npe = new NullPointerException(String.format("No such element with id %s in registry %s!",id, this));
			ConsoleJS.STARTUP.error("Error while wrapping registry element type!", npe);
			throw npe;
		}

		return value;
	}

	public void fireRegistryEvent() {
		var event = customRegEvent == null
				? new RegistryEventJS<>(this)
				: customRegEvent.get();
		event.post(ScriptType.STARTUP, key.location().getPath() + KubeJSEvents.REGISTRY_SUFFIX);
		event.created.forEach(BuilderBase::createAdditionalObjects);
	}
}

