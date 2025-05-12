package data.model;

public class Employee {
    private int id;
    private String uuid; // ✅ Tambahkan ini
    private String name;
    private String rfid_tag;
    private String password;
    private String access_token;

    public Employee() {}

    public Employee(String name, String rfid_tag, String password) {
        this.name = name;
        this.rfid_tag = rfid_tag;
        this.password = password;
    }

    // ✅ Tambahkan getter dan setter untuk UUID
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // Getters & setters lainnya
    public int getId() { return id; }
    public String getName() { return name; }
    public String getRfid_tag() { return rfid_tag; }
    public String getPassword() { return password; }
    public String getAccess_token() { return access_token; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRfid_tag(String rfid_tag) { this.rfid_tag = rfid_tag; }
    public void setPassword(String password) { this.password = password; }
    public void setAccess_token(String access_token) { this.access_token = access_token; }
}
