import java.util.List;

public class Kartograph extends Game {
    int pointer = 0;
    public final static List<String> Seasons = List.of("Frühling", "Sommer", "Herbst", "Winter");
    public final static List<String> AtoD = List.of("AEval", "BEval", "CEval", "DEval");
    public List<String> evals;

    public Kartograph(String spring, String summer, String fall, String winter) {
        super();
        evals = List.of(spring, summer, fall, winter);
    }

    public void next() {
        pointer = (pointer + 1) % 4;
    }
}
