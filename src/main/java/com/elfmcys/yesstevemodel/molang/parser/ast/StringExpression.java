package com.elfmcys.yesstevemodel.molang.parser.ast;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class StringExpression implements Expression {

    private final String name;

    private final int path;

    private ResourceLocation cachedLocation;

    public StringExpression(@NotNull String str) {
        this.name = Objects.requireNonNull(str, "value");
        this.path = StringPool.computeIfAbsent(str);
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public int getPath() {
        return this.path;
    }

    @Override
    public <R> R visit(@NotNull ExpressionVisitor<R> expressionVisitor) {
        return expressionVisitor.visitString(this);
    }

    public String toString() {
        return this.name;
    }

    @Nullable
    public ResourceLocation getResourceLocation() {
        return this.cachedLocation;
    }

    public void setResourceLocation(@Nullable ResourceLocation resourceLocation) {
        this.cachedLocation = resourceLocation;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return this.name.equals(obj);
        }
        return (obj instanceof StringExpression) && this.path == ((StringExpression) obj).path;
    }

    public int hashCode() {
        return this.name.hashCode();
    }
}