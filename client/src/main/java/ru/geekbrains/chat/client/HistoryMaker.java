package ru.geekbrains.chat.client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryMaker {
    public static final int SIZE_OF_RETRIEVED_HISTORY = 100;
    String login;
    File history;
    String historyPath;

    public HistoryMaker(String login) {
        this.historyPath = "history/";
        this.login = login;
        this.history = new File(historyPath + "history_" + login + ".txt");
        if (!history.exists()) {
                File path = new File(historyPath);
                path.mkdirs();
        }
    }

    public List<String> readHistory() {
        if (!history.exists()) return List.of("No previous history");
        List<String> result = null;
        if (history.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(history))) {
                String historyString;
                List<String> historyStrings = new ArrayList<>();
                while ((historyString = reader.readLine()) != null) {
                    historyStrings.add(historyString);
                }
                if (historyStrings.size() <= SIZE_OF_RETRIEVED_HISTORY) {
                    result = historyStrings;
                }
                if (historyStrings.size() > SIZE_OF_RETRIEVED_HISTORY) {
                    int firstIndex = historyStrings.size() - SIZE_OF_RETRIEVED_HISTORY;
                    result = new ArrayList<>(100);

                    for (int counter = firstIndex - 1; counter < historyStrings.size(); counter++) {
                        result.add(historyStrings.get(counter));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("History for " + result.size());
        return result;
    }

    public void writeHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(history, true))) {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
