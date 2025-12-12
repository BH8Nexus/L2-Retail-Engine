package l2r.gameserver.model.actor.Custom.ProtectionSystemChat;

import l2r.gameserver.model.Player;
import l2r.gameserver.tables.AdminTable;

/**
 * This class is created in order to find evil words so to speak in real time
 * Based on the existing technology of the United States that always analyzes telephone conversations, mails, messages, etc. to find
 * words and phrases that can be how to kill the president
 *
 * In this case I am going to analyze words spoken in trade channel or in wisp only, which are the important channels, for when they say
 * words like exploit, bug, l2walker, walker, l2net, bot, etc. In the case of finding one, notify the gms, that simple
 * in this case the gm knows that they are talking about bugs or not, and can snoop him to find out more
 *
 * @Flash
 */
public class MessageProtection
{
    /**
     * If an evil word is found in the sentence, gms is notified. We also use the log to write to the console
     *
     * @param activeChar
     * @param target
     * @param message
     * @param channel
     */
    public void analizeMessage(Player activeChar, Player target, String message, String channel)
    {
        if (activeChar == null)
            return;

        // If the speaker is a GM then it should not be analyzed
        if (activeChar.getAccessLevel().getLevel() > 0)
            return;

        // If the target is a GM then it is because he is talking to one, therefore it is not necessary to analyze it
        if (target != null && target.getAccessLevel().getLevel() > 0)
            return;

        if (message == null || message.isEmpty())
            return;

        final String lowMessage = message.toLowerCase();
        final String[] text = lowMessage.split(" ");

        for (String word : text)
        {
            if (word.startsWith("hack")	|| word.startsWith("cheat") || word.startsWith("exploit") || word.equalsIgnoreCase("bug")
                    || (word.startsWith("bot") && !word.startsWith("bota") && !word.startsWith("boton") && !word.startsWith("botell") && !word.startsWith("bottle"))
                    // Programas ilegales
                    || word.startsWith("l2walker") || word.startsWith("l2net")
                    || word.startsWith("wally") || word.equalsIgnoreCase("waldo")
                    || word.startsWith("radar") || word.startsWith("autopot") || word.startsWith("l2control") || word.startsWith("l2divine")
                    || word.startsWith("l2superman") || word.startsWith("l2sniper")  || word.startsWith("l2control")
                    || word.startsWith("adrenaline") || word.startsWith("l2radar") || word.startsWith("l2tower")
                    // Venta Adena
                    || word.startsWith("buy adena")  || word.startsWith("sell adena") || word.startsWith("adena sell")
                    || word.contains("usd") || word.startsWith("euro") || word.startsWith("pesos") || word.startsWith("dolar") || word.startsWith("dollar")
                    || word.startsWith("skype") ||  word.startsWith("ts") ||  word.startsWith("rc")
                    // Paginas
                    || ((word.contains("www.") || word.contains(".com") || word.contains(".ru")) ))//&& !word.contains(Config.SERVER_NAME.toLowerCase())))
            {
                AdminTable.broadcastSecurityToGMs("Player " + activeChar.getName() + " used the word " + word + " in the " + channel + " channel. Full message: " + message);
                return;
            }
        }

        // Then we look for complete phrases that do not enter or cannot be formed with simple words
        if (lowMessage.contains("buy adena") || lowMessage.contains("sell adena") || lowMessage.contains("adena sell"))
            AdminTable.broadcastSecurityToGMs("Player " + activeChar.getName() + " used a dangerous phrase in the " + channel + " channel. Full message: " + message);
    }

    public static MessageProtection getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final MessageProtection _instance = new MessageProtection();
    }
}
