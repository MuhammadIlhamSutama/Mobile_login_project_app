package data.model;

public class Client {
    private int user_id;
    private String password;
    private String username;
    private String access_token; // Menambahkan properti access_token

    public int getUser_id() {
        return user_id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getAccess_token() {
        return access_token; // Mengembalikan access_token
    }

    // Menambahkan setter jika diperlukan
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
