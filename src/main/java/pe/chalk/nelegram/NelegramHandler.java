package pe.chalk.nelegram;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;
import pe.chalk.nelegram.event.TelegramMessageEvent;
import pe.chalk.telegram.handler.UpdateHandler;
import pe.chalk.telegram.method.MeGetter;
import pe.chalk.telegram.method.TextMessageSender;
import pe.chalk.telegram.type.Update;
import pe.chalk.telegram.type.message.*;
import pe.chalk.telegram.type.user.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2016-08-04
 */
public class NelegramHandler extends ConsoleCommandSender implements Listener, UpdateHandler {
    private User me;

    public NelegramHandler() {
        this.me = new MeGetter().get(Nelegram.getBot());
    }

    @Override
    public String getName() {
        return this.me.getUsername();
    }

    @Override
    public void sendMessage(String message) {
        final String text = TextFormat.clean(this.getServer().getLanguage().translateString(message).trim());
        for (String line: text.split("\n")) new TextMessageSender(Nelegram.getTarget(), line).send(Nelegram.getBot());
    }

    @Override
    public void handleMessage(List<Update> updates) {
        updates.parallelStream().map(Update::getMessage).filter(Objects::nonNull).map(TelegramMessageEvent::new).forEach(this.getServer().getPluginManager()::callEvent);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onTelegramMessage(TelegramMessageEvent event) {
        final Message message = event.getMessage();
        if (!message.getChat().getId().equals(Nelegram.getTarget())) return;
        if (!message.hasFrom() || !message.getFrom().hasUsername()) return;

        final String text = this.getServer().getLanguage().translateString("chat.type.text", new String[]{ message.getFrom().getUsername(), TextFormat.clean(this.visualizeMessage(message)) });
        final List<CommandSender> recipients = this.getServer().getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_USERS).stream().filter(v -> v instanceof CommandSender && !(v instanceof NelegramHandler)).map(CommandSender.class::cast).collect(Collectors.toList());

        this.getServer().broadcastMessage(text, recipients);
    }
    
    private String visualizeMessage(Message message) {
        if (Objects.isNull(message)) return "";

        else if (message instanceof TextMessage)     return ((TextMessage) message).getText();
        else if (message instanceof PhotoMessage)    return "(Photo)";
        else if (message instanceof VideoMessage)    return "(Video)";
        else if (message instanceof AudioMessage)    return "(Audio)";
        else if (message instanceof DocumentMessage) return "(Document)";
        else if (message instanceof ContactMessage)  return "(Contact)";
        else if (message instanceof LocationMessage) return "(Location)";

        else return "(Unknown)";
    }
}