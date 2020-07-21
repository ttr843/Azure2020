package ru.itis.telegrambot.bot;


import javassist.bytecode.ByteArray;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 18.07.2020
 * TelegramBot
 *
 * @author Ruslan Pashin
 * High school ITIS
 * @version v1.0
 */
public class TelegramBot extends TelegramLongPollingBot {

    // имя бота, которое мы указали при создании аккаунта у BotFather
    // и токен, который получили в результате
    private static final String BOT_NAME = "InformationOrganizations_bot";
    private static final String BOT_TOKEN = "1390845811:AAGpXh2mcgEDIzhbfnngEkkviaQfDL3UGW4";


    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        if (message.equals("/start")) {
            sendMsg(update.getMessage().getChatId().toString(), "please write ORGN or INN ");
        } else {
            try {
                String input = update.getMessage().getText();
                URL url = new URL("http://telegramBotDanil.azurewebsites.net/api/daDataFunction?input=" + input);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                        StandardCharsets.UTF_8));
                String line = reader.readLine();
                String[] strings = line.trim().split(",");
                String out = "";
                for (String x: strings){
                    if (!x.contains("null")){
                        out = out + x +",";
                    }
                }
                if(out.length()>4096){
                    out = out.substring(0,4095);
                }
                out = out.replace(':',' ');
                out = out.replace(',',' ');
                out = out.replace('\\',' ');
                out = out.replace('\"',' ');
                out = out.replace('{',' ');
                out = out.replace('}',' ');
                out = out.replace('[',' ');
                out = out.replace(']',' ');
                System.out.println(out);
                sendMsg(update.getMessage().getChatId().toString(),out);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }

}
