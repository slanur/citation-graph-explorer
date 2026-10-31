import java.util.*;


//Graf yapısını yöneten sınıf
 
public class Graph {
    private Map<String, Article> articles; // ID -> Article mapping
    private Map<String, List<String>> adjacencyList; // Yönlü kenarlar (ID -> referans verdiği ID'ler)
    private Map<String, List<String>> reverseAdjacencyList; // Ters yönlü kenarlar (ID -> kendisine referans veren ID'ler)
    
    public Graph() {
        this.articles = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.reverseAdjacencyList = new HashMap<>();
    }
    
    //Garfa makale ekleme
    public void addArticle(Article article) {
        articles.put(article.getId(), article);
        adjacencyList.putIfAbsent(article.getId(), new ArrayList<>());
        reverseAdjacencyList.putIfAbsent(article.getId(), new ArrayList<>());
    }
    
    /**
     * İki makale arasında referans ilişkisi ekler
     * @param fromId Referans veren makale
     * @param toId Referans verilen makale
     */
    public void addEdge(String fromId, String toId) {
        if (articles.containsKey(fromId) && articles.containsKey(toId)) {
            adjacencyList.get(fromId).add(toId);
            reverseAdjacencyList.get(toId).add(fromId);
            articles.get(toId).incrementCitationCount();
        }
    }
    
    
     //Makaleye referans veren makaleleri döndürür
    public List<String> getCitingArticles(String articleId) {
        return reverseAdjacencyList.getOrDefault(articleId, new ArrayList<>());
    }
    //makalenin referans verdiği makaleleri döndürür
    public List<String> getReferencedArticles(String articleId) {
        return adjacencyList.getOrDefault(articleId, new ArrayList<>());
    }
    
    //Makale nesnesi ıd ile dondurur
    public Article getArticle(String id) {
        return articles.get(id);
    }
    
    //Tüm makaleleri döndürür
    public Collection<Article> getAllArticles() {
        return articles.values();
    }
    
    // toplam makale sayisi
    public int getTotalArticles() {
        return articles.size();
    }
    
    //Toplam kenar (referans) sayısı
    public int getTotalEdges() {
        int total = 0;
        for (List<String> refs : adjacencyList.values()) {
            total += refs.size();
        }
        return total;
    }
    
    //Toplam verilen referans sayısı
    public int getTotalGivenReferences() {
        return getTotalEdges();
    }
    
    // Toplam alınan referans sayısı (aynı değer olmalı)
    public int getTotalReceivedReferences() {
        int total = 0;
        for (Article article : articles.values()) {
            total += article.getCitationCount();
        }
        return total;
    }
    
    // En çok referans alan makaleyi bulur
    public Article getMostCitedArticle() {
        Article mostCited = null;
        int maxCitations = 0;
        
        for (Article article : articles.values()) {
            if (article.getCitationCount() > maxCitations) {
                maxCitations = article.getCitationCount();
                mostCited = article;
            }
        }
        return mostCited;
    }
    
    //En çok referans veren makaleyi bulur
    public Article getMostReferencingArticle() {
        Article mostReferencing = null;
        int maxReferences = 0;
        
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            if (entry.getValue().size() > maxReferences) {
                maxReferences = entry.getValue().size();
                mostReferencing = articles.get(entry.getKey());
            }
        }
        return mostReferencing;
    }
    
    //makalenin H-Index değerini hesaplar
    public int calculateHIndex(String articleId) {
        List<String> citingArticles = getCitingArticles(articleId);
        
        if (citingArticles.isEmpty()) {
            return 0;
        }
        
        // Her bir atıf yapan makalenin kendi atıf sayısını alır
        List<Integer> citationCounts = new ArrayList<>();
        for (String citingId : citingArticles) {
            Article citingArticle = getArticle(citingId);
            if (citingArticle != null) {
                citationCounts.add(citingArticle.getCitationCount());
            }
        }
        
        // Büyükten küçüğe sıralar
        citationCounts.sort(Collections.reverseOrder());
        
        // H-index hesapla
        int hIndex = 0;
        for (int i = 0; i < citationCounts.size(); i++) {
            if (citationCounts.get(i) >= (i + 1)) {
                hIndex = i + 1;
            } else {
                break;
            }
        }
        
        return hIndex;
    }
    
    //makalenin H-Core listesini döndürür
    public List<Article> getHCore(String articleId) {
        int hIndex = calculateHIndex(articleId);
        List<String> citingArticles = getCitingArticles(articleId);
        
        // Atıf yapan makaleleri atıf sayısına göre sıralar
        List<Article> sortedCiting = new ArrayList<>();
        for (String citingId : citingArticles) {
            Article article = getArticle(citingId);
            if (article != null) {
                sortedCiting.add(article);
            }
        }
        
        sortedCiting.sort((a, b) -> Integer.compare(b.getCitationCount(), a.getCitationCount()));
        
        // İlk h-index kadar makaleyi döndür
        List<Article> hCore = new ArrayList<>();
        for (int i = 0; i < Math.min(hIndex, sortedCiting.size()); i++) {
            hCore.add(sortedCiting.get(i));
        }
        
        return hCore;
    }
    
    //H-Median değerini hesaplar
    public double calculateHMedian(String articleId) {
        List<Article> hCore = getHCore(articleId);
        
        if (hCore.isEmpty()) {
            return 0;
        }
        
        List<Integer> citations = new ArrayList<>();
        for (Article article : hCore) {
            citations.add(article.getCitationCount());
        }
        
        citations.sort(Integer::compareTo);
        
        int size = citations.size();
        if (size % 2 == 0) {
            return (citations.get(size / 2 - 1) + citations.get(size / 2)) / 2.0;
        } else {
            return citations.get(size / 2);
        }
    }
    
    public Map<String, Article> getArticlesMap() {
        return articles;
    }
    
    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }
    
    public Map<String, List<String>> getReverseAdjacencyList() {
        return reverseAdjacencyList;
    }
}