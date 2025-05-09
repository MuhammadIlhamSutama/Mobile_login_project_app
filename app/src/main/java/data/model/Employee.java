package data.model;

public class Employee {
    private int id;
    private String password;
    private String name;
    private String access_token;

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
