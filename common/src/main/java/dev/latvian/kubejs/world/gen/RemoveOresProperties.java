package dev.latvian.kubejs.world.gen;

import dev.latvian.kubejs.block.BlockStatePredicate;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * @author LatvianModder
 */
public class RemoveOresProperties {
	public GenerationStep.Decoration _worldgenLayer = GenerationStep.Decoration.UNDERGROUND_ORES;
	public BlockStatePredicate blocks = BlockStatePredicate.Simple.NONE;
	public final WorldgenEntryList biomes = new WorldgenEntryList();

	public void setWorldgenLayer(String id) {
		_worldgenLayer = GenerationStep.Decoration.valueOf(id.toUpperCase());
	}
}
