package com.leaf.skriptmirror.skript.custom;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;

import com.leaf.skriptmirror.JavaType;
import com.leaf.skriptmirror.LibraryLoader;
import com.leaf.skriptmirror.util.SkriptMirrorUtil;
import com.leaf.skriptmirror.util.SkriptReflection;
import com.leaf.skriptmirror.util.SkriptUtil;

import org.bukkit.event.Event;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

public class CustomImport {
  private static Pattern IMPORT_STATEMENT =
      Pattern.compile("(" + SkriptMirrorUtil.PACKAGE + ")(?:\\s+as (" + SkriptMirrorUtil.IDENTIFIER + "))?");

  static {
    CustomSyntaxSection.register("Import", SectionImport.class, "import");
    Skript.registerEffect(EffImport.class, "import <" + IMPORT_STATEMENT.pattern() + ">");

    //noinspection unchecked
    Skript.registerExpression(ImportHandler.class, JavaType.class, ExpressionType.SIMPLE);
    Optional<ExpressionInfo<?, ?>> info = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(Skript.getExpressions(), Spliterator.ORDERED), false)
        .filter(i -> i.c == ImportHandler.class)
        .findFirst();

    if (info.isPresent()) {
      thisInfo = info.get();
    } else {
      Skript.warning("Could not find custom import class. Custom imports will not work.");
    }
  }

  private static SyntaxElementInfo<?> thisInfo;
  // Most scripts are associated with files, but according to Skript, file-less configs may also be loaded.
  private static Map<File, Map<String, JavaType>> imports = new HashMap<>();

  public static class SectionImport extends SelfRegisteringSkriptEvent {
    @Override
    public void register(Trigger t) {
    }

    @Override
    public void unregister(Trigger t) {
      imports.remove(t.getScript());
    }

    @Override
    public void unregisterAll() {
      imports.clear();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      File currentScript = SkriptUtil.getCurrentScript();
      SectionNode node = ((SectionNode) SkriptLogger.getNode());

      if (node.getKey().toLowerCase().startsWith("on ")) {
        return false;
      }

      node.forEach(subNode -> registerImport(subNode.getKey(), currentScript));
      SkriptUtil.clearSectionNode(node);
      return true;
    }


    @Override
    public String toString(Event e, boolean debug) {
      return "import";
    }
  }

  private static void registerImport(String rawStatement, File script) {
    Matcher statement = IMPORT_STATEMENT.matcher(ScriptLoader.replaceOptions(rawStatement));
    if (!statement.matches()) {
      Skript.warning(rawStatement + " is an invalid import statement.");
      return;
    }

    String cls = statement.group(1);
    Class<?> javaClass;

    try {
      javaClass = LibraryLoader.getClassLoader().loadClass(cls);
    } catch (ClassNotFoundException ex) {
      Skript.warning(cls + " refers to a non-existent class.");
      return;
    }

    String importName = statement.group(2);

    if (importName == null) {
      importName = javaClass.getSimpleName();
    }

    imports.computeIfAbsent(script, s -> new HashMap<>())
        .compute(importName,
            (name, oldClass) -> {
              if (oldClass != null) {
                Skript.warning(
                    String.format("%s is already mapped to %s. It will not be remapped to %s.",
                        name, oldClass.getJavaClass(), javaClass)
                );
                return oldClass;
              }
              return new JavaType(javaClass);
            });

    updateImports();
  }

  private static void updateImports() {
    String[] patterns = imports.values().stream()
        .flatMap(m -> m.keySet().stream())
        .distinct()
        .toArray(String[]::new);
    SkriptReflection.setPatterns(thisInfo, patterns);
  }

  public static class ImportHandler extends SimpleExpression<JavaType> {
    private JavaType type;

    @Override
    protected JavaType[] get(Event e) {
      return new JavaType[]{type};
    }

    @Override
    public boolean isSingle() {
      return true;
    }

    @Override
    public Class<? extends JavaType> getReturnType() {
      return JavaType.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
      return type.getJavaClass().getName();
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
      Map<String, JavaType> localImports = imports.get(SkriptUtil.getCurrentScript());

      if (localImports != null) {
        type = localImports.get(parseResult.expr);
      }

      return type != null;
    }
  }

  public static class EffImport extends Effect {
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed,
                        final SkriptParser.ParseResult parseResult) {
      if (!ScriptLoader.isCurrentEvent(EffectCommandEvent.class)) {
        return false;
      }

      registerImport(parseResult.regexes.get(0).group(), null);

      return true;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
      return "import";
    }

    @Override
    protected void execute(Event e) {
    }
  }

  public static JavaType lookup(File script, String identifier) {
    Map<String, JavaType> localImports = imports.get(script);

    if (localImports == null) {
      return null;
    }

    return localImports.get(identifier);
  }
}
