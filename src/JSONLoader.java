import java.io.*;
import java.util.*;
import java.util.regex.*;


//JSON dosyasını okuyarak Graf oluşturan sınıf
 
public class JSONLoader {

     //JSON dosyasından graf yükler
    public static Graph loadGraph(String filePath) throws Exception {
        Graph graph = new Graph();
        
        System.out.println("JSON dosyası okunuyor: " + filePath);
        
        // Dosyayı okur
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String jsonContent = content.toString().trim();
        
        // JSON array'i parse eder
        if (!jsonContent.startsWith("[") || !jsonContent.endsWith("]")) {
            throw new Exception("JSON dosyası bir array ile başlamalı");
        }
        
        // Her bir makale nesnesini bulur ve parse eder
        List<Map<String, Object>> articles = parseJSONArray(jsonContent);
        
        System.out.println("Toplam " + articles.size() + " makale bulundu.");
        
        // Tüm makaleleri ekler
        for (Map<String, Object> jsonArticle : articles) {
            String id = extractId(jsonArticle);
            if (id == null || id.isEmpty()) {
                continue;
            }
            
            Article article = new Article(id);
            
            // DOI
            if (jsonArticle.containsKey("doi")) {
                article.setDoi(jsonArticle.get("doi").toString());
            }
            
            // Title
            if (jsonArticle.containsKey("title")) {
                article.setTitle(jsonArticle.get("title").toString());
            }
            
            // Year
            if (jsonArticle.containsKey("year")) {
                Object yearObj = jsonArticle.get("year");
                try {
                    article.setYear(Integer.parseInt(yearObj.toString()));
                } catch (NumberFormatException e) {
                    article.setYear(0);
                }
            }
            
            // Authors
            if (jsonArticle.containsKey("authors")) {
                Object authorsObj = jsonArticle.get("authors");
                if (authorsObj instanceof List<?>) {
                    List<?> authorsList = (List<?>) authorsObj;
                    List<String> authors = new ArrayList<>();
                    for (Object authorObj : authorsList) {
                        if (authorObj instanceof String) {
                            authors.add((String) authorObj);
                        }
                    }
                    article.setAuthors(authors);
                }
            }
            
            // Referenced works
            if (jsonArticle.containsKey("referenced_works")) {
                Object refsObj = jsonArticle.get("referenced_works");
                if (refsObj instanceof List<?>) {
                    List<?> refsList = (List<?>) refsObj;
                    List<String> references = new ArrayList<>();
                    for (Object refObj : refsList) {
                        if (refObj instanceof String) {
                            String refId = extractIdFromUrl((String) refObj);
                            if (refId != null && !refId.isEmpty()) {
                                references.add(refId);
                            }
                        }
                    }
                    article.setReferencedWorks(references);
                }
            }
            
            graph.addArticle(article);
        }
        
        System.out.println("Makaleler graf'a eklendi. Referans ilişkileri oluşturuluyor...");
        
        //Referans ilişkilerini ekler
        int edgeCount = 0;
        for (Article article : graph.getAllArticles()) {
            for (String refId : article.getReferencedWorks()) {
                if (graph.getArticle(refId) != null) {
                    graph.addEdge(article.getId(), refId);
                    edgeCount++;
                }
            }
        }
        
        System.out.println("Graf oluşturuldu: " + graph.getTotalArticles() + " düğüm, " + edgeCount + " kenar");
        
        return graph;
    }
    
    
     // JSON array'i basit parsing ile liste haline getirir
    private static List<Map<String, Object>> parseJSONArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Array parantezlerini kaldırır
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
        
        // Her bir objeyi bulur
        int braceCount = 0;
        int startIndex = -1;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '{') {
                if (braceCount == 0) {
                    startIndex = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && startIndex != -1) {
                    String objStr = json.substring(startIndex, i + 1);
                    Map<String, Object> obj = parseJSONObject(objStr);
                    if (obj != null) {
                        result.add(obj);
                    }
                    startIndex = -1;
                }
            }
        }
        
        return result;
    }
   
    private static Map<String, Object> parseJSONObject(String json) {
        Map<String, Object> result = new HashMap<>();
        
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        // Key-value çiftlerini manuel olarak bulur 
        int i = 0;
        while (i < json.length()) {
            // Key'i bul
            int keyStart = json.indexOf("\"", i);
            if (keyStart == -1) break;
            int keyEnd = json.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;
            
            String key = json.substring(keyStart + 1, keyEnd);
            
            // ':' karakterini bulur
            int colonIndex = json.indexOf(":", keyEnd);
            if (colonIndex == -1) break;
            
            // Value başlangıcını bulur
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                valueStart++;
            }
            
            if (valueStart >= json.length()) break;
            
            char firstChar = json.charAt(valueStart);
            
            // String değer
            if (firstChar == '"') {
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueEnd != -1) {
                    String value = json.substring(valueStart + 1, valueEnd);
                    result.put(key, value);
                    i = valueEnd + 1;
                }
            }
            // Array değer
            else if (firstChar == '[') {
                int bracketCount = 1;
                int valueEnd = valueStart + 1;
                while (valueEnd < json.length() && bracketCount > 0) {
                    if (json.charAt(valueEnd) == '[') bracketCount++;
                    else if (json.charAt(valueEnd) == ']') bracketCount--;
                    valueEnd++;
                }
                
                String arrayContent = json.substring(valueStart, valueEnd);
                List<String> list = parseJSONArray2(arrayContent);
                result.put(key, list);
                i = valueEnd;
            }
            // Object değer
            else if (firstChar == '{') {
                int braceCount = 1;
                int valueEnd = valueStart + 1;
                while (valueEnd < json.length() && braceCount > 0) {
                    if (json.charAt(valueEnd) == '{') braceCount++;
                    else if (json.charAt(valueEnd) == '}') braceCount--;
                    valueEnd++;
                }
                i = valueEnd;
            }
            // Sayı veya diğer değerler
            else {
                int valueEnd = valueStart;
                while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
                    valueEnd++;
                }
                
                String value = json.substring(valueStart, valueEnd).trim();
                try {
                    result.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
                i = valueEnd;
            }
        }
        
        return result;
    }
    
     // String array parsing
    private static List<String> parseJSONArray2(String json) {
        List<String> result = new ArrayList<>();
        
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
        
        // String değerleri bulur
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        
        return result;
    }
    
     // JSON nesnesinden ID'yi bulur
    private static String extractId(Map<String, Object> jsonArticle) {
        if (jsonArticle.containsKey("id")) {
            String id = jsonArticle.get("id").toString();
            return extractIdFromUrl(id);
        }
        
        if (jsonArticle.containsKey("doi")) {
            return extractIdFromUrl(jsonArticle.get("doi").toString());
        }
        
        return null;
    }
    
     // URL veya DOI string'inden ID'yi bulur
    private static String extractIdFromUrl(String urlOrId) {
        if (urlOrId == null || urlOrId.isEmpty()) {
            return null;
        }
        
        if (urlOrId.contains("/")) {
            String[] parts = urlOrId.split("/");
            return parts[parts.length - 1];
        }
        
        return urlOrId;
    }
}