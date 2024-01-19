package wbs.buildwands.shapes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShapeManager {
    private static final Map<String, WandShape> registered = new HashMap<>();

    static {
        register(new LineWandShape());
        register(new DiscWandShape());
        register(new SquareWandShape());
    }

    public static void register(@NotNull WandShape type) {
        registered.put(stripSyntax(type.getName()), type);
    }

    @Nullable
    public static WandShape getShape(String name) {
        return registered.get(stripSyntax(name));
    }

    public static String stripSyntax(String toStrip) {
        return toStrip.toLowerCase().replaceAll("$[^a-z0-9]+", "_");
    }

    public static List<String> getShapeNames() {
        return registered.values().stream()
                .map(WandShape::getName)
                .collect(Collectors.toList());
    }
}
