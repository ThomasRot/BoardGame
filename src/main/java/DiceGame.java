import util.UPair;

import java.awt.*;
import java.util.List;


public class DiceGame extends Game {
    public static void drawDie(Graphics2D g, String die, int x, int y) {
        final var colors = lookupColor(die.replaceAll("\\d", ""));
        g.setColor(colors.first);
        g.fillRect(x, y, ImageBuilder.DIE_WIDTH, ImageBuilder.DIE_WIDTH);
        g.setColor(colors.second);
        lookUpSpots(Integer.parseInt(die.substring(2, 3))).forEach(i -> {
            var spotX = (int) (x + ImageBuilder.DIE_WIDTH * 0.125 * (i % 7 + 1));
            var spotY = (int) (y + ImageBuilder.DIE_WIDTH * 0.125 * (i / 7 + 1));
            fillCircle(g, spotX, spotY, (int) (0.13 * ImageBuilder.DIE_WIDTH));
        });
    }

    private static void fillCircle(Graphics2D g, int spotX, int spotY, int width) {
        g.fillOval(spotX - width / 2, spotY - width / 2, width, width);
    }

    private static List<Integer> lookUpSpots(int i) {
        return switch (i) {
            case 1 -> List.of(24);
            case 2 -> List.of(12, 36);
            case 3 -> List.of(12, 24, 36);
            case 4 -> List.of(8, 12, 36, 40);
            case 5 -> List.of(8, 12, 24, 36, 40);
            case 6 -> List.of(8, 12, 22, 26, 36, 40);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private static UPair<Color, Color> lookupColor(String co) {
        return switch (co) {
            case "bl" -> UPair.of(new Color(0, 0, 140), Color.WHITE);
            case "re", "ro" -> UPair.of(new Color(142, 0, 0), Color.WHITE);
            case "gr" -> UPair.of(new Color(2, 92, 0), Color.WHITE);
            case "ye", "ge" -> UPair.of(new Color(205, 174, 0), Color.BLACK);
            case "br" -> UPair.of(new Color(57, 32, 0), Color.WHITE);
            case "sc", "nw" -> UPair.of(new Color(0, 0, 0), Color.WHITE);
            case "wh", "we" -> UPair.of(new Color(255, 255, 255), Color.BLACK);
            case "ws", "sw" -> UPair.of(new Color(50, 50, 50), Color.WHITE);
            case "or" -> UPair.of(new Color(194, 117, 0), Color.BLACK);
            case "pu", "li", "vi" -> UPair.of(new Color(83, 0, 94), Color.WHITE);
            case "tu", "tü" -> UPair.of(new Color(0, 241, 183), Color.BLACK);
            default -> throw new IllegalStateException("Unexpected value: " + co);
        };
    }
}
