package photocol;

import spark.Spark;

public class Photocol {
    public static void main(String[] args) {
        Spark.get("/", (req, res) -> "Hello, world!");
    }
}
