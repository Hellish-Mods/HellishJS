package dev.latvian.kubejs.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.kubejs.bindings.TextWrapper;
import dev.latvian.kubejs.util.JSObjectType;
import dev.latvian.kubejs.util.ListJS;
import dev.latvian.kubejs.util.MapJS;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.kubejs.util.WrappedJS;
import dev.latvian.mods.rhino.annotations.typing.JSInfo;
import dev.latvian.mods.rhino.mod.util.JsonSerializable;
import dev.latvian.mods.rhino.mod.util.color.Color;
import dev.latvian.mods.rhino.mod.wrapper.ColorWrapper;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public abstract class Text implements Iterable<Text>, Comparable<Text>, JsonSerializable, WrappedJS {
    @JSInfo("Returns a Component based on the input")
	public static Component componentOf(@Nullable Object o) {
		if (o == null) {
			return new TextComponent("null");
		} else if (o instanceof Component) {
			return (Component) o;
		} else if (o instanceof CharSequence) {
			return new TextComponent(o.toString());
		} else if (o instanceof StringTag) {
			String s = ((StringTag) o).getAsString();

			if (s.startsWith("{") && s.endsWith("}")) {
				try {
					return Component.Serializer.fromJson(s);
				} catch (Exception ex) {
					return new TextComponent("Error: " + ex);
				}
			} else {
				return new TextComponent(s);
			}
		}

		return of(o).component();
	}

	public static Text of(@Nullable Object o) {
		return ofWrapped(UtilsJS.wrap(o, JSObjectType.ANY));
	}

	private static Text ofWrapped(@Nullable Object o) {
		if (o == null) {
			return new TextString("null");
		} else if (o instanceof CharSequence || o instanceof Number || o instanceof Character) {
			return new TextString(o.toString());
		} else if (o instanceof Enum) {
			return new TextString(((Enum) o).name());
		} else if (o instanceof Text) {
			return (Text) o;
		} else if (o instanceof ListJS) {
			Text text = new TextString("");

			for (Object e1 : (ListJS) o) {
				text.append(ofWrapped(e1));
			}

			return text;
		} else if (o instanceof MapJS map && (map.containsKey("text") || map.containsKey("translate"))) {
            Text text;

            if (map.containsKey("text")) {
                text = new TextString(map.get("text").toString());
            } else { //map.containsKey("translate")
                Object[] with;

                if (map.containsKey("with")) {
                    ListJS a = map.getOrNewList("with");
                    with = new Object[a.size()];
                    int i = 0;

                    for (Object e1 : a) {
                        with[i] = e1;

                        if (with[i] instanceof MapJS || with[i] instanceof ListJS) {
                            with[i] = ofWrapped(e1);
                        }

                        i++;
                    }
                } else {
                    with = new Object[0];
                }

                text = new TextTranslate(map.get("translate").toString(), with);
            }

            if (map.containsKey("color")) {
                text.color = ColorWrapper.of(map.get("color")).getRgbKJS();
            }

            text.bold = (Boolean) map.getOrDefault("bold", null);
            text.italic = (Boolean) map.getOrDefault("italic", null);
            text.underlined = (Boolean) map.getOrDefault("underlined", null);
            text.strikethrough = (Boolean) map.getOrDefault("strikethrough", null);
            text.obfuscated = (Boolean) map.getOrDefault("obfuscated", null);
            text.insertion = (String) map.getOrDefault("insertion", null);
            text.font = map.containsKey("font") ? new ResourceLocation(map.get("font").toString()) : null;
            text.click = map.containsKey("click") ? TextWrapper.clickEventOf(map.get("click")) : null;
            text.hover = map.containsKey("hover") ? HoverEvent.deserialize(UtilsJS.cast(map.get("hover"))) : null;

            text.siblings = null;

            if (map.containsKey("extra")) {
                for (Object e : map.getOrNewList("extra")) {
                    text.append(ofWrapped(e));
                }
            }
            return text;

        }

		return new TextString(o.toString());
	}

	public static Text join(Text separator, Iterable<Text> texts) {
		Text text = new TextString("");
		boolean first = true;

		for (Text t : texts) {
			if (first) {
				first = false;
			} else {
				text.append(separator);
			}

			text.append(t);
		}

		return text;
	}

	public static Text read(FriendlyByteBuf buffer) {
		return Text.of(buffer.readComponent());
	}

	private int color = -1;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private String insertion;
	private ResourceLocation font;
	private ClickEvent click;
	private HoverEvent hover;
	private List<Text> siblings;

	public abstract MutableComponent rawComponent();

	public abstract Text rawCopy();

	@Override
	public abstract JsonElement toJson();

	public final Component component() {
		MutableComponent component = rawComponent();

		if (hasStyle()) {
			component.setStyle(createStyle());
		}

		for (Text text : getSiblings()) {
			component.append(text.component());
		}

		return component;
	}

	public final String getString() {
		return component().getString();
	}

	public final Text copy() {
		Text t = rawCopy();
		t.color = color;
		t.bold = bold;
		t.italic = italic;
		t.underlined = underlined;
		t.strikethrough = strikethrough;
		t.obfuscated = obfuscated;
		t.insertion = insertion;
		t.font = font;
		t.click = click;
		t.hover = hover;

		for (Text child : getSiblings()) {
			t.append(child.copy());
		}

		return t;
	}

	public final JsonObject createStyleJson() {
		JsonObject json = new JsonObject();

		if (color != -1) {
			json.addProperty("color", String.format("#%06X", color));
		}

		if (bold != null) {
			json.addProperty("bold", bold);
		}

		if (italic != null) {
			json.addProperty("italic", italic);
		}

		if (underlined != null) {
			json.addProperty("underlined", underlined);
		}

		if (strikethrough != null) {
			json.addProperty("strikethrough", strikethrough);
		}

		if (obfuscated != null) {
			json.addProperty("obfuscated", obfuscated);
		}

		if (insertion != null) {
			json.addProperty("insertion", insertion);
		}

		if (font != null) {
			json.addProperty("font", font.toString());
		}

		if (click != null) {
			JsonObject o = new JsonObject();
            o.addProperty("action", click.getAction().getName());
            o.addProperty("value", click.getValue());
			json.add("clickEvent", o);
		}

		if (hover != null) {
			json.add("hoverEvent", hover.serialize());
		}

		return json;
	}

	public boolean hasStyle() {
        return color != -1 || bold != null || italic != null || underlined != null || strikethrough != null
            || obfuscated != null || insertion != null || font != null || click != null || hover != null;
    }

	public Style createStyle() {
		return new Style.Serializer().deserialize(createStyleJson(), Style.class, null);
	}

	public JsonObject getStyleAndSiblingJson() {
		JsonObject json = createStyleJson();

		if (!getSiblings().isEmpty()) {
			JsonArray array = new JsonArray();

			for (Text child : getSiblings()) {
				array.add(child.toJson());
			}

			json.add("extra", array);
		}

		return json;
	}

	@Override
	public final Iterator<Text> iterator() {
		if (getSiblings().isEmpty()) {
			return Collections.singleton(this).iterator();
		}

		List<Text> list = new ArrayList<>();
		list.add(this);

		for (Text child : getSiblings()) {
			for (Text part : child) {
				list.add(part);
			}
		}

		return list.iterator();
	}

	public final Text color(Color c) {
		color = c.getRgbKJS() & 0xFFFFFF;
		return this;
	}

	public final Text black() {
		return color(ColorWrapper.BLACK);
	}

	public final Text darkBlue() {
		return color(ColorWrapper.DARK_BLUE);
	}

	public final Text darkGreen() {
		return color(ColorWrapper.DARK_GREEN);
	}

	public final Text darkAqua() {
		return color(ColorWrapper.DARK_AQUA);
	}

	public final Text darkRed() {
		return color(ColorWrapper.DARK_RED);
	}

	public final Text darkPurple() {
		return color(ColorWrapper.DARK_PURPLE);
	}

	public final Text gold() {
		return color(ColorWrapper.GOLD);
	}

	public final Text gray() {
		return color(ColorWrapper.GRAY);
	}

	public final Text darkGray() {
		return color(ColorWrapper.DARK_GRAY);
	}

	public final Text blue() {
		return color(ColorWrapper.BLUE);
	}

	public final Text green() {
		return color(ColorWrapper.GREEN);
	}

	public final Text aqua() {
		return color(ColorWrapper.AQUA);
	}

	public final Text red() {
		return color(ColorWrapper.RED);
	}

	public final Text lightPurple() {
		return color(ColorWrapper.LIGHT_PURPLE);
	}

	public final Text yellow() {
		return color(ColorWrapper.YELLOW);
	}

	public final Text white() {
		return color(ColorWrapper.WHITE);
	}

	public final Text noColor() {
		color = -1;
		return this;
	}

	public final Text bold(@Nullable Boolean value) {
		bold = value;
		return this;
	}

	public final Text bold() {
		return bold(true);
	}

	public final Text italic(@Nullable Boolean value) {
		italic = value;
		return this;
	}

	public final Text italic() {
		return italic(true);
	}

	public final Text underlined(@Nullable Boolean value) {
		underlined = value;
		return this;
	}

	public final Text underlined() {
		return underlined(true);
	}

	public final Text strikethrough(@Nullable Boolean value) {
		strikethrough = value;
		return this;
	}

	public final Text strikethrough() {
		return strikethrough(true);
	}

	public final Text obfuscated(@Nullable Boolean value) {
		obfuscated = value;
		return this;
	}

	public final Text obfuscated() {
		return obfuscated(true);
	}

	public final Text insertion(@Nullable String value) {
		insertion = value;
		return this;
	}

	public final Text font(@Nullable String value) {
		font = value == null || value.isEmpty() ? null : new ResourceLocation(value);
		return this;
	}

	public final Text click(@Nullable Object o) {
		click = TextWrapper.clickEventOf(o);
		return this;
	}

	public final Text hover(Object o) {
        hover = TextWrapper.hoverEventOf(o);
        return this;
	}

	public final Text append(Object sibling) {
		if (siblings == null) {
			siblings = new LinkedList<>();
		}

		siblings.add(of(sibling));
		return this;
	}

	public final List<Text> getSiblings() {
		return siblings == null ? Collections.emptyList() : siblings;
	}

	public final boolean hasSiblings() {
		return siblings != null && !siblings.isEmpty();
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Text t) {
            if (color == t.color
                && bold == t.bold
                && italic == t.italic
                && underlined == t.underlined
                && strikethrough == t.strikethrough
                && obfuscated == t.obfuscated) {
                return Objects.equals(insertion, t.insertion)
                    && Objects.equals(font, t.font)
                    && Objects.equals(click, t.click)
                    && Objects.equals(hover, t.hover)
                    && Objects.equals(siblings, t.siblings);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            color,
            bold,
            italic,
            underlined,
            strikethrough,
            obfuscated,
            insertion,
            font,
            click,
            hover,
            siblings
        );
    }

	@Override
	public String toString() {
		return component().getString();
	}

	@Override
	public int compareTo(Text other) {
		return toString().compareTo(other.toString());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeComponent(component());
	}
}