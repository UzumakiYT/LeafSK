package com.leaf.gui;

import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;

import org.bukkit.event.Event;

import com.leaf.util.ReflectionUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * This is called only when someone uses /sk reload all, so it will register {@link SkriptGUIEvent} again.
 * @author Tuke_Nuke on 03/09/2017
 */
public class TriggerUnregisterListener extends SelfRegisteringSkriptEvent {

	private Trigger t;

	public TriggerUnregisterListener() {
	}

	public void register() {
		if (t == null) //A file object, just to not use null
			t = new Trigger(new File("TuSKe"), getClass().getName(), this, new ArrayList<>());
		//Some old skript version doesn't have this method public
		ReflectionUtils.invokeMethod(SkriptEventHandler.class, null, "addSelfRegisteringTrigger", t);
	}

	@Override
	public void register(Trigger t) {

	}

	@Override
	public void unregister(Trigger t) {

	}

	@Override
	public void unregisterAll() {
		SkriptGUIEvent.getInstance().unregisterAll();
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(Event e, boolean debug) {
		return getClass().getName();
	}
}
