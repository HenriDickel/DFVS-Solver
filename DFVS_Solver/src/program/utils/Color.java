package program.utils;

public enum Color {
    WHITE("\u001B[0m"),
    RED("\u001B[31m"),
    YELLOW("\u001B[33m"),
    PURPLE("\u001B[35m");

    private final String name;

    private Color(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
