package program.utils;

public enum Dataset {
    DATASET_1("dataset_1"),
    DATASET_2("dataset_2"),
    DATASET_3("dataset_3");

    private final String name;

    private Dataset(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
