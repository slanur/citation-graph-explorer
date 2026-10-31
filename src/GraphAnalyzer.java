import java.util.*;

//Graf analiz metriklerini hesaplayan sınıf
public class GraphAnalyzer {
    
    //Betweenness Centrality(Her düğüm için, diğer düğümler arası en kısa yollardan kaç tanesinde bulunduğunu hesaplar) hesaplar (yönsüz graf için)
    // 
    public static Map<String, Double> calculateBetweennessCentrality(Graph graph) {
        Map<String, Double> betweenness = new HashMap<>();
        List<String> nodes = new ArrayList<>(graph.getArticlesMap().keySet());
        
        // Tüm düğümler için başlangıç değeri 0
        for (String node : nodes) {
            betweenness.put(node, 0.0);
        }
        
        // Her düğüm için
        for (String source : nodes) {
            // Tek kaynak en kısa yol problemi (BFS tabanlı)
            Map<String, List<List<String>>> allPaths = new HashMap<>();
            
            for (String target : nodes) {
                if (!source.equals(target)) {
                    List<List<String>> paths = findAllShortestPaths(graph, source, target);
                    if (!paths.isEmpty()) {
                        allPaths.put(target, paths);
                    }
                }
            }
            
            // Her hedef için bulunan yollarda geçen düğümleri sayar
            for (Map.Entry<String, List<List<String>>> entry : allPaths.entrySet()) {
                List<List<String>> paths = entry.getValue();
                int totalPaths = paths.size();
                
                if (totalPaths > 0) {
                    // Her yolda geçen ara düğümleri sayar
                    Map<String, Integer> nodeCount = new HashMap<>();
                    
                    for (List<String> path : paths) {
                        // İlk ve son düğüm hariç (kaynak ve hedef)
                        for (int i = 1; i < path.size() - 1; i++) {
                            String node = path.get(i);
                            nodeCount.put(node, nodeCount.getOrDefault(node, 0) + 1);
                        }
                    }
                    
                    // Her düğümün betweenness değerini günceller
                    for (Map.Entry<String, Integer> count : nodeCount.entrySet()) {
                        String node = count.getKey();
                        double contribution = (double) count.getValue() / totalPaths;
                        betweenness.put(node, betweenness.get(node) + contribution);
                    }
                }
            }
        }
        
        return betweenness;
    }
    
    
     // Betweenness Centralityi Yalnızca subgraph'taki düğümler için hesaplar
    public static Map<String, Double> calculateBetweennessCentralityForSubgraph(Graph graph, Set<String> subgraphNodes) {
        Map<String, Double> betweenness = new HashMap<>();
        List<String> nodes = new ArrayList<>(subgraphNodes);
        
        // Tüm düğümler için başlangıç değeri 0
        for (String node : nodes) {
            betweenness.put(node, 0.0);
        }
        
        // Her düğüm çifti için (sadece subgraph içinde)
        for (int i = 0; i < nodes.size(); i++) {
            String source = nodes.get(i);
            
            for (int j = i + 1; j < nodes.size(); j++) {
                String target = nodes.get(j);
                
                // En kısa yolları bulur (tüm grafı kullan ama sadece subgraph düğümleri arasında)
                List<List<String>> paths = findAllShortestPaths(graph, source, target);
                
                if (!paths.isEmpty()) {
                    int totalPaths = paths.size();
                    Map<String, Integer> nodeCount = new HashMap<>();
                    
                    for (List<String> path : paths) {
                        // Yoldaki tüm ara düğümleri sayar (source ve target hariç)
                        for (int k = 1; k < path.size() - 1; k++) {
                            String node = path.get(k);
                            // Sadece subgraph'taki düğümleri sayar
                            if (subgraphNodes.contains(node)) {
                                nodeCount.put(node, nodeCount.getOrDefault(node, 0) + 1);
                            }
                        }
                    }
                    
                    // Her düğümün betweenness değerini günceller
                    for (Map.Entry<String, Integer> count : nodeCount.entrySet()) {
                        String node = count.getKey();
                        double contribution = (double) count.getValue() / totalPaths;
                        betweenness.put(node, betweenness.get(node) + contribution);
                    }
                }
            }
        }
        
        return betweenness;
    }
    
    
     //İki düğüm arasındaki tüm en kısa yolları bulur (yönsüz graf için)
    
    private static List<List<String>> findAllShortestPaths(Graph graph, String start, String end) {
        List<List<String>> allPaths = new ArrayList<>();
        
        // BFS ile en kısa mesafeyi bulur
        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<String>> parents = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        distances.put(start, 0);
        parents.put(start, new ArrayList<>());
        queue.offer(start);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDist = distances.get(current);
            
            // Komşuları alır (yönsüz graf için hem giden hem gelen kenarlar)
            Set<String> neighbors = new HashSet<>();
            neighbors.addAll(graph.getReferencedArticles(current));
            neighbors.addAll(graph.getCitingArticles(current));
            
            for (String neighbor : neighbors) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    parents.put(neighbor, new ArrayList<>());
                    parents.get(neighbor).add(current);
                    queue.offer(neighbor);
                } else if (distances.get(neighbor) == currentDist + 1) {
                    parents.get(neighbor).add(current);
                }
            }
        }
        
        // Eğer hedefe ulaşıldıysa, tüm yolları oluşturur
        if (distances.containsKey(end)) {
            List<String> path = new ArrayList<>();
            path.add(end);
            backtrackPaths(end, start, parents, path, allPaths);
        }
        
        return allPaths;
    }
    
    
     //Yolları geri izleyerek oluşturur
    private static void backtrackPaths(String current, String start, Map<String, List<String>> parents,
                                      List<String> path, List<List<String>> allPaths) {
        if (current.equals(start)) {
            List<String> completePath = new ArrayList<>(path);
            Collections.reverse(completePath);
            allPaths.add(completePath);
            return;
        }
        
        List<String> parentList = parents.get(current);
        if (parentList != null) {
            for (String parent : parentList) {
                path.add(parent);
                backtrackPaths(parent, start, parents, path, allPaths);
                path.remove(path.size() - 1);
            }
        }
    }
    
    
     /*K-Core Ayrisimi uygular
     graph Graf yapısı
     k Minimum derece değeri
     k-core'a ait düğüm ID'leri*/
     
    public static Set<String> kCoreDecomposition(Graph graph, int k) {
        // Yönsüz graf için derece hesaplar
        Map<String, Integer> degrees = new HashMap<>();
        
        for (String nodeId : graph.getArticlesMap().keySet()) {
            Set<String> neighbors = new HashSet<>();
            neighbors.addAll(graph.getReferencedArticles(nodeId));
            neighbors.addAll(graph.getCitingArticles(nodeId));
            degrees.put(nodeId, neighbors.size());
        }
        
        // k'dan küçük dereceli düğümleri kaldırır
        Set<String> removed = new HashSet<>();
        boolean changed = true;
        
        while (changed) {
            changed = false;
            List<String> toRemove = new ArrayList<>();
            
            for (Map.Entry<String, Integer> entry : degrees.entrySet()) {
                if (entry.getValue() < k && !removed.contains(entry.getKey())) {
                    toRemove.add(entry.getKey());
                    changed = true;
                }
            }
            
            for (String nodeId : toRemove) {
                removed.add(nodeId);
                
                // Komşuların derecelerini günceller
                Set<String> neighbors = new HashSet<>();
                neighbors.addAll(graph.getReferencedArticles(nodeId));
                neighbors.addAll(graph.getCitingArticles(nodeId));
                
                for (String neighbor : neighbors) {
                    if (!removed.contains(neighbor)) {
                        degrees.put(neighbor, degrees.get(neighbor) - 1);
                    }
                }
            }
        }
        
        // Kalan düğümler k-core'u oluşturur
        Set<String> kCore = new HashSet<>();
        for (String nodeId : degrees.keySet()) {
            if (!removed.contains(nodeId)) {
                kCore.add(nodeId);
            }
        }
        
        return kCore;
    }
    
    
     // K-core için kenarları bulur
    public static Set<String> getKCoreEdges(Graph graph, Set<String> kCoreNodes) {
        Set<String> edges = new HashSet<>();
        
        for (String nodeId : kCoreNodes) {
            Set<String> neighbors = new HashSet<>();
            neighbors.addAll(graph.getReferencedArticles(nodeId));
            neighbors.addAll(graph.getCitingArticles(nodeId));
            
            for (String neighbor : neighbors) {
                if (kCoreNodes.contains(neighbor)) {
                    // Kenar ID'si (küçük ID önce gelecek şekilde)
                    String edge;
                    if (nodeId.compareTo(neighbor) < 0) {
                        edge = nodeId + "->" + neighbor;
                    } else {
                        edge = neighbor + "->" + nodeId;
                    }
                    edges.add(edge);
                }
            }
        }
        
        return edges;
    }
}