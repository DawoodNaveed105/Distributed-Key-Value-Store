package com.example.demo1;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    public String getClusterStatus(String node) {
        return sendGetRequest(node, "/data/cluster/status");
    }

    public String getLocalKeys(String node) {
        return sendGetRequest(node, "/data/local/keys");
    }

    public String putKey(String node, String key, String value) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
            String url = "http://" + node + "/data?key=" + encodedKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(value.getBytes(StandardCharsets.UTF_8));
            }

            return readResponse(conn);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getKey(String node, String key) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
            return sendGetRequest(node, "/data/" + encodedKey);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String deleteKey(String node, String key) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
            String url = "http://" + node + "/data/" + encodedKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("DELETE");

            return readResponse(conn);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String replicateKey(String node, String key, String value) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
            String url = "http://" + node + "/data/internal/replicate?key=" + encodedKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(value.getBytes(StandardCharsets.UTF_8));
            }

            return readResponse(conn);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String deleteReplication(String node, String key) {
        try {
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
            String url = "http://" + node + "/data/internal/replicate/" + encodedKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("DELETE");

            return readResponse(conn);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String sendGetRequest(String node, String path) {
        try {
            String url = "http://" + node + path;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            return readResponse(conn);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();

        response.append("HTTP Status: ").append(responseCode).append(" ").append(conn.getResponseMessage()).append("\n\n");

        InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        }

        // Try to format JSON if it looks like JSON
        String responseStr = response.toString();
        if (responseStr.contains("{") && responseStr.contains("}")) {
            return formatJson(responseStr);
        }

        return responseStr;
    }

    private String formatJson(String jsonString) {
        // Simple JSON formatting
        int indent = 0;
        StringBuilder formatted = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : jsonString.toCharArray()) {
            switch (ch) {
                case '{':
                case '[':
                    formatted.append(ch).append("\n");
                    indent += 2;
                    formatted.append(" ".repeat(indent));
                    break;

                case '}':
                case ']':
                    formatted.append("\n");
                    indent -= 2;
                    formatted.append(" ".repeat(indent)).append(ch);
                    break;

                case ',':
                    formatted.append(ch).append("\n").append(" ".repeat(indent));
                    break;

                case ':':
                    formatted.append(ch).append(" ");
                    break;

                case '"':
                    formatted.append(ch);
                    inQuotes = !inQuotes;
                    break;

                default:
                    formatted.append(ch);
                    break;
            }
        }

        return formatted.toString();
    }
}
