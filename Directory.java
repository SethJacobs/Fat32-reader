import java.util.ArrayList;

public class Directory {
    Directory parent;
    int start;
    ArrayList<Directory> children = new ArrayList<>();

    public Directory (Directory parent, int start, String name) {
        this.parent = parent;
        this.start = start;
    }

    public void addChild(Directory child) {
        children.add(child);
    }
    
}
