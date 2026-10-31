import java.util.ArrayList;
import java.util.List;


//Makale bilgilerini tutan sınıf
 
public class Article {
    private String id;
    private String doi;
    private String title;
    private int year;
    private List<String> authors;
    private List<String> referencedWorks;
    private int citationCount; //atıf sayısı
    
    public Article(String id) {
        this.id = id;
        this.authors = new ArrayList<>();
        this.referencedWorks = new ArrayList<>();
        this.citationCount = 0;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getDoi() {
        return doi;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getYear() {
        return year;
    }
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public List<String> getReferencedWorks() {
        return referencedWorks;
    }
    
    public int getCitationCount() {
        return citationCount;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setDoi(String doi) {
        this.doi = doi;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    
    public void addAuthor(String author) {
        this.authors.add(author);
    }
    
    public void setReferencedWorks(List<String> referencedWorks) {
        this.referencedWorks = referencedWorks;
    }
    
    public void addReferencedWork(String workId) {
        this.referencedWorks.add(workId);
    }
    
    public void incrementCitationCount() {
        this.citationCount++;
    }
    
    public void setCitationCount(int count) {
        this.citationCount = count;
    }
    
    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", citations=" + citationCount +
                '}';
    }
}