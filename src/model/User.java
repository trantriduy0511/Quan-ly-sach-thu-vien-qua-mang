// Class cho người dùng

package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
 	
