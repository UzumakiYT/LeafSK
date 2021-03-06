package com.leaf.skriptmirror.skript.reflect.sections;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;

import org.bukkit.event.Event;

import com.leaf.skriptmirror.util.SkriptReflection;

import java.util.List;

public class Section {

  private final Trigger trigger;
  private final Object variablesMap;
  private final List<Variable<?>> argumentVariables;
  private Object[] output;

  public Section(Trigger trigger, Object variablesMap, List<Variable<?>> argumentVariables) {
    this.trigger = trigger;
    this.variablesMap = variablesMap;
    this.argumentVariables = argumentVariables;
  }

  public Section(Trigger trigger, Event event, List<Variable<?>> argumentVariables) {
    this(trigger, SkriptReflection.copyLocals(SkriptReflection.getLocals(event)), argumentVariables);
  }

  public void run(Event event, Object[][] arguments) {
    output = null;
    SectionEvent sectionEvent = new SectionEvent(event, this);
    SkriptReflection.putLocals(SkriptReflection.copyLocals(variablesMap), sectionEvent);

    for (int i = 0; i < arguments.length && i < argumentVariables.size(); i++) {
      argumentVariables.get(i).change(sectionEvent, arguments[i], Changer.ChangeMode.SET);
    }

    TriggerItem.walk(trigger, sectionEvent);
    SkriptReflection.removeLocals(sectionEvent);
  }

  public Object[] getOutput() {
    return output;
  }

  public void setOutput(Object[] output) {
    this.output = output;
  }

}
