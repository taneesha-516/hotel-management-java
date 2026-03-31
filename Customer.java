class Customer {
    private String customerId;
    private String name;
    private String contactNumber;

    public Customer(String customerId, String name, String contactNumber) {
        this.customerId = customerId;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Contact: %s", customerId, name, contactNumber);
    }
}
