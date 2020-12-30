
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import util.U;


import java.io.ByteArrayInputStream;

public class Bot {
    public static Game game;
    public static final String CARD_PNG = "card.png";
    DiscordClient client;

    Bot(String discordAPI) {
        client = new DiscordClientBuilder(discordAPI).build();
    }

    public static void main(String[] args) {
        var s = new Bot(args[0]);
        s.getCommands(s.client);
        s.client.login().block();
    }

    private void getCommands(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(event -> {
                    var msgText = event.getMessage().getContent().orElse(null);
                    if (msgText == null) return;
                    var parts = msgText.split(" ");
                    final var channel = event.getMessage().getChannel().block();
                    assert channel != null;
                    var ib = new ImageBuilder();
                    U.Log(msgText);
                    try {
                        if (parts[0].startsWith(".k")) {
                            game = new Kartograph(parts[1], parts[2], parts[3], parts[4]);
                            channel.createMessage("Spiel erstellt :partying_face:").block();
                            return;
                        }
                        if (game instanceof Kartograph) {
                            var kar = (Kartograph) game;
                            if (parts[0].startsWith("§n")) {
                                kar.next();
                                ib.addImages(kar.pointer, Kartograph.Seasons);
                                ib.addImages(Kartograph.AtoD);
                                ib.addImages(kar.evals);
                            } else if (parts[0].startsWith("§s")) {
                                ib.addImages(kar.pointer, Kartograph.Seasons);
                                ib.addImages(Kartograph.AtoD);
                                ib.addImages(kar.evals);
                            } else if (parts[0].startsWith("+")){
                                ib.addImages(parts[0].substring(1));
                            }
                            if (!ib.matrix.isEmpty()) {
                                channel.createMessage((messageCreateSpec) ->
                                        messageCreateSpec.addFile(CARD_PNG, new ByteArrayInputStream(ib.get()))).block();
                                event.getMessage().delete().block();
                            }
                        }
                        if (parts[0].startsWith(".d")) {
                            game = new DiceGame();
                            channel.createMessage("Spiel erstellt :partying_face:").block();
                            return;
                        }
                        if (game instanceof DiceGame) {
                            if (parts[0].startsWith("+")) {
                                ib.addDies(parts[0].substring(1));
                            }
                            if (!ib.dies.isEmpty()) {
                                channel.createMessage((messageCreateSpec) ->
                                        messageCreateSpec.addFile(CARD_PNG, new ByteArrayInputStream(ib.get()))).block();
                                event.getMessage().delete().block();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }
}
