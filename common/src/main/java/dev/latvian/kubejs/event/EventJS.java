package dev.latvian.kubejs.event;

import dev.latvian.kubejs.script.ScriptType;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class EventJS {
	private boolean cancelled = false;

	public boolean canCancel() {
		return false;
	}

	public final void cancel() {
		cancelled = true;
	}

	public final boolean isCancelled() {
		return cancelled;
	}

	protected void afterPosted(boolean result) {
	}

    /**
     * @param ids event ids to be used for posting events to handlers, they will be used in input order
     * @return whether there's handlers called `event.cancel()` and this event itself can be cancelled
     */
    public final boolean post(@NotNull ScriptType type, @NotNull List<String> ids) {
        if (type != ScriptType.STARTUP && post(ScriptType.STARTUP, ids) && canCancel()) {
            return true;
        }
        val e = type.manager.get().events;
        boolean cancelled = false;
        for (val id : ids) {
            if (cancelled) {
                break; //prevent posting events after being cancelled
            }
            cancelled = e.postToHandlers(id, e.handlers(id), this);
        }
        afterPosted(cancelled);
        return cancelled;
    }

	public final boolean post(@NotNull ScriptType type, @NotNull String id) {
		return post(type, Collections.singletonList(id));
	}

	public final boolean post(ScriptType t, String id, String sub) {
        //id with sub id comes first to match original behaviour
        return post(t, Arrays.asList(id + '.' + sub, id));
	}
}