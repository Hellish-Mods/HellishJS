package dev.latvian.kubejs.block.events;

import dev.latvian.kubejs.block.BlockBuilder;
import dev.latvian.kubejs.registry.RegistryEventJS;
import dev.latvian.kubejs.registry.RegistryInfo;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class BlockRegistryEventJS extends RegistryEventJS<Block> {
    public BlockRegistryEventJS(RegistryInfo<Block> r) {
        super(r);
    }

	public void create(String name, Consumer<BlockBuilder> callback) {
		BlockBuilder builder = (BlockBuilder) create(name);
		callback.accept(builder);
	}

	public void addDetector(String id) {
        create(id, "detector");
	}
}