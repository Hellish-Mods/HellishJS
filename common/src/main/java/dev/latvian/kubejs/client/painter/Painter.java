package dev.latvian.kubejs.client.painter;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.kubejs.KubeJSEvents;
import dev.latvian.kubejs.client.painter.screen.ScreenPaintEventJS;
import dev.latvian.kubejs.client.painter.screen.ScreenPainterObject;
import dev.latvian.kubejs.client.painter.world.WorldPainterObject;
import dev.latvian.kubejs.net.PainterUpdatedEventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.unit.FixedUnit;
import dev.latvian.mods.rhino.util.unit.MutableUnit;
import dev.latvian.mods.rhino.util.unit.Unit;
import dev.latvian.mods.rhino.util.unit.UnitStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class Painter {
	public static final transient Painter INSTANCE = new Painter();
	public static final Random RANDOM = new Random();
	// /kubejs painter add @p {Text:{id:'text', text:'Test', x: 30, y: 40, scale: 2.0}}

	public static final int DRAW_ALWAYS = 0;
	public static final int DRAW_INGAME = 1;
	public static final int DRAW_GUI = 2;

	public static final int CENTER = 0;
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	public static final int TOP = -1;
	public static final int BOTTOM = 1;

	private final Object lock;
	private final Map<String, Supplier<PainterObject>> objectRegistry;
	private final PainterObjectStorage storage;
	private ScreenPainterObject[] screenObjects;
	private WorldPainterObject[] worldObjects;
	public final UnitStorage unitStorage;
	public final MutableUnit deltaUnit;
	public final MutableUnit screenWidthUnit;
	public final MutableUnit screenHeightUnit;
	public final MutableUnit mouseXUnit;
	public final MutableUnit mouseYUnit;

	private Painter() {
		lock = new Object();
		objectRegistry = new HashMap<>();
		storage = new PainterObjectStorage();
		screenObjects = null;
		worldObjects = null;
		unitStorage = new UnitStorage();
		unitStorage.setVariable("delta", deltaUnit = new MutableUnit(1F));
		unitStorage.setVariable("screenW", screenWidthUnit = new MutableUnit(1F));
		unitStorage.setVariable("screenH", screenHeightUnit = new MutableUnit(1F));
		unitStorage.setVariable("mouseX", mouseXUnit = new MutableUnit(0F));
		unitStorage.setVariable("mouseY", mouseYUnit = new MutableUnit(0F));
	}

	@HideFromJS
	public void registerObject(String name, Supplier<PainterObject> supplier) {
		objectRegistry.put(name, supplier);
	}

	@Nullable
	public PainterObject make(String type) {
		Supplier<PainterObject> supplier = objectRegistry.get(type);
		return supplier == null ? null : supplier.get();
	}

	@Nullable
	public PainterObject getObject(String key) {
		synchronized (lock) {
			return storage.getObject(key);
		}
	}

	public void paint(CompoundTag root) {
		synchronized (lock) {
			storage.handle(root);
			screenObjects = null;
			worldObjects = null;
			new PainterUpdatedEventJS().post(KubeJSEvents.CLIENT_PAINTER_UPDATED);
		}
	}

	public void clear() {
		synchronized (lock) {
			storage.clear();
			screenObjects = null;
			worldObjects = null;
			new PainterUpdatedEventJS().post(KubeJSEvents.CLIENT_PAINTER_UPDATED);
		}
	}

	@HideFromJS
	public ScreenPainterObject[] getScreenObjects() {
		if (screenObjects == null) {
			synchronized (lock) {
				screenObjects = storage.createScreenObjects();
			}
		}

		return screenObjects;
	}

	@HideFromJS
	public WorldPainterObject[] getWorldObjects() {
		if (worldObjects == null) {
			synchronized (lock) {
				worldObjects = storage.createWorldObjects();
			}
		}

		return worldObjects;
	}

	public void setVariable(String key, Unit variable) {
		Unit original = unitStorage.getVariable(key);

		if (original instanceof MutableUnit) {
			((MutableUnit) original).set(variable.get());
		} else if (variable instanceof FixedUnit) {
			unitStorage.setVariable(key, new MutableUnit(variable.get()));
		} else {
			unitStorage.setVariable(key, variable);
		}
	}

    public void inGameScreenDraw(PoseStack matrices, float delta) {
        var mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.renderDebug || mc.screen != null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        //RenderSystem.disableLighting();

        var event = new ScreenPaintEventJS(mc, matrices, delta);
        deltaUnit.set(delta);
        screenWidthUnit.set(event.width);
        screenHeightUnit.set(event.height);
        //mouse is always at the center of screen when playing
        mouseXUnit.set(event.width / 2F);
        mouseYUnit.set(event.height / 2F);
        event.post(KubeJSEvents.CLIENT_PAINT_SCREEN);

        for (var object : getScreenObjects()) {
            if (object.visible && (object.draw == Painter.DRAW_ALWAYS || object.draw == Painter.DRAW_INGAME)) {
                object.preDraw(event);
            }
        }

        for (var object : getScreenObjects()) {
            if (object.visible && (object.draw == Painter.DRAW_ALWAYS || object.draw == Painter.DRAW_INGAME)) {
                object.draw(event);
            }
        }
    }

    public void guiScreenDraw(Screen screen, PoseStack matrices, int mouseX, int mouseY, float delta) {
        var mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.disableLighting();

        var event = new ScreenPaintEventJS(mc, screen, matrices, mouseX, mouseY, delta);
        deltaUnit.set(delta);
        screenWidthUnit.set(event.width);
        screenHeightUnit.set(event.height);
        mouseXUnit.set(mouseX);
        mouseYUnit.set(mouseY);

//        event.resetShaderColor();
        event.post(KubeJSEvents.CLIENT_PAINT_SCREEN);

        for (var object : getScreenObjects()) {
            if (object.visible && (object.draw == Painter.DRAW_ALWAYS || object.draw == Painter.DRAW_GUI)) {
                object.preDraw(event);
            }
        }

        for (var object : getScreenObjects()) {
            if (object.visible && (object.draw == Painter.DRAW_ALWAYS || object.draw == Painter.DRAW_GUI)) {
                object.draw(event);
            }
        }
    }
}
