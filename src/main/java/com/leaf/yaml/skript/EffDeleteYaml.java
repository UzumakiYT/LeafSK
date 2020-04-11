package com.leaf.yaml.skript;

import javax.annotation.Nullable;
import org.bukkit.event.Event;
import com.leaf.Leaf;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Delete YAML")
@Description("Deletes a YAML file and removes it from memory.")
@Examples({
		"delete yaml \"config\""
})
@Since("1.1.5")
public class EffDeleteYaml extends Effect {

	static {
		Skript.registerEffect(EffDeleteYaml.class, "delete [y[a]ml] %strings%");
	}

	private Expression<String> file;

	@Override
	protected void execute(@Nullable Event event) {
		for (String name : this.file.getAll(event)) {
			if (!Leaf.YAML_STORE.containsKey(name))
				continue;
			Leaf.YAML_STORE.get(name).getFile().delete();
			Leaf.YAML_STORE.remove(name);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "delete yaml " + this.file.toString(event, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		file = (Expression<String>) exprs[0];
		return true;
	}
}
