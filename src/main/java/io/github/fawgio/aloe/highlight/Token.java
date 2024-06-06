package io.github.fawgio.aloe.highlight;

import java.util.Objects;

public final class Token {
    private final Type type;
    private final String value;
    private int to;
    private int from;

    public Token(Type type, String value, int to, int from) {
        this.type = type;
        this.value = value;
        this.to = to;
        this.from = from;
    }

    public Token(Type type, String value) {
        this(type, value, 0, 0);
    }

    public Token from(int from) {
        this.from = from;
        return this;
    }
    public Token to(int to) {
        this.to = to;
        return this;
    }

    public Type type() {
        return type;
    }

    public String value() {
        return value;
    }

    public int to() {
        return to;
    }

    public int from() {
        return from;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Token) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.value, that.value) &&
                this.to == that.to &&
                this.from == that.from;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, to, from);
    }

    @Override
    public String toString() {
        return "Token[" +
                "type=" + type + ", " +
                "value=" + value + ", " +
                "to=" + to + ", " +
                "from=" + from + ']';
    }

    public int length() {
        return to - from + 1;
    }
}
