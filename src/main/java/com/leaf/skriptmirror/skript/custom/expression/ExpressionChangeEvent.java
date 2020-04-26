package com.leaf.skriptmirror.skript.custom.expression;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.leaf.skriptmirror.skript.custom.CustomSyntaxEvent;

public class ExpressionChangeEvent extends CustomSyntaxEvent {
  private final static HandlerList handlers = new HandlerList();
  private final Object[] delta;

  public ExpressionChangeEvent(Event event, Expression<?>[] expressions, int matchedPattern,
                               SkriptParser.ParseResult parseResult, Object[] delta) {
    super(event, expressions, matchedPattern, parseResult);
    this.delta = delta;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public Object[] getDelta() {
    return delta;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
