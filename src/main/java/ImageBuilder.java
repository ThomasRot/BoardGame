import util.U;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ImageBuilder {
    public static final int OFFSET = 10;
    public static final int CARD_WIDTH = 730;
    public static final int DIE_WIDTH = 300;
    public static final int CARD_HEIGHT = CARD_WIDTH * 29 / 20;
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color DARKBLUE = new Color(0, 0, 120);
    public List<List<String>> matrix = new ArrayList<>();
    private List<Integer> highs = new ArrayList<>();
    public List<String> dies = new ArrayList<>();

    public void addImages(Collection<String> images) {
        addImages(images.toArray(String[]::new));
    }

    public void addImages(int high, Collection<String> images) {
        addImages(high, images.toArray(String[]::new));
    }

    public void addImages(String... images) {
        addImages(-1, images);
    }

    public void addImages(int high, String... images) {
        U.Log(images, high);
        final var row = new ArrayList<String>();
        for (var im : images) {
            var fileName = Arrays.stream(Objects.requireNonNull(new File(Bot.game.getClass().getSimpleName()).listFiles()))
                    .map(File::getName)
                    .filter(s -> s.startsWith(im))
                    .findAny().orElse("err.png");
            row.add(Bot.game.getClass().getSimpleName() + "/" + fileName);
        }
        matrix.add(row);
        highs.add(high);
        U.Log(matrix);
    }

    public void addDies(String dieString) {
        dies.addAll(split(dieString, 3));
    }

    private List<String> split(String dieString, int length) {
        List<String> dies = new ArrayList<>();
        for (int i = 0; i < dieString.length(); i+= length) {
            dies.add(dieString.substring(i, Math.min(dieString.length(), i + length)));
        }
        return dies;
    }


    public byte[] get() {
        BufferedImage img = dies.isEmpty() ? loadImages() : loadDies();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    public BufferedImage loadDies() {
        var img = new BufferedImage((DIE_WIDTH + 10) * dies.size() + 10, DIE_WIDTH + 20, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        U.enumerate(dies).forEach(die -> {
            DiceGame.drawDie(g, die.val(), die.i() * (DIE_WIDTH + 10) + 10, 10);
        });
        return img;
    }

    private BufferedImage loadImages() {
        var localScale = 2.0 / (matrix.size() + matrix.get(0).size());
        var localCardWidth = (int) (CARD_WIDTH * localScale);
        var localCardHeight = (int) (CARD_HEIGHT * localScale);
        var img = new BufferedImage((localCardWidth + OFFSET) * matrix.get(0).size() + OFFSET,
                (localCardHeight + OFFSET) * matrix.size() + OFFSET,
                BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setColor(BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        U.enumerate(matrix).forEach(row -> {
            U.enumerate(row.val()).forEach(ele -> {
                BufferedImage card = readImage(ele.val());
                AffineTransform atc = new AffineTransform();
                atc.translate(ele.i() * (localCardWidth + OFFSET) + OFFSET, row.i() * (localCardHeight + OFFSET) + OFFSET);
                atc.scale(localScale, localScale);
                g.drawImage(card, atc, null);
            });
        });
        return img;
    }



    private static BufferedImage readImage(String s) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


}
