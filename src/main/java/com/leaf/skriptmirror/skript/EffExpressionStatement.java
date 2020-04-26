package com.leaf.skriptmirror.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;

import com.leaf.Leaf;
import com.leaf.skriptmirror.skript.reflect.ExprJavaCall;
import com.leaf.skriptmirror.util.SkriptUtil;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EffExpressionStatement extends Effect {
  static {
    Skript.registerEffect(EffExpressionStatement.class, "[(1¦await)] %~javaobject%");
  }

  private static final ExecutorService threadPool =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  private Expression<Object> arg;
  private boolean isAsynchronous;

  @Override
  protected void execute(Event e) {
    arg.getAll(e);
  }

  @Override
  protected TriggerItem walk(Event e) {
    if (isAsynchronous) {
      CompletableFuture.runAsync(() -> execute(e), threadPool)
          .thenAccept(res -> Bukkit.getScheduler().runTask(Leaf.getInstance(), () -> {
            if (getNext() != null) {
              TriggerItem.walk(getNext(), e);
            }
          }));
      return null;
    }
    return super.walk(e);
  }

  @Override
  public String toString(Event e, boolean debug) {
    return arg.toString(e, debug);
  }

  @Override
  public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed,
                      SkriptParser.ParseResult parseResult) {
    arg = SkriptUtil.defendExpression(exprs[0]);

    if (!(arg instanceof ExprJavaCall)) {
      return false;
    }

    isAsynchronous = (parseResult.mark & 1) == 1;
    return SkriptUtil.canInitSafely(arg);
  }
}
