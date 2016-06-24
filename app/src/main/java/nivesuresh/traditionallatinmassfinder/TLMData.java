package nivesuresh.traditionallatinmassfinder;

/**
 * Created by nivesuresh on 6/24/16.
 */
public class TLMData {

    private String affiliation;
    private String churchName;
    private String country;
    private String email;
    private String latitude;
    private String longitude;
    private String phone;
    private String stateProvince;
    private String times;
    private String town;
    private String website;
    private String zipPostalCode;

    public TLMData(String affiliation, String churchName, String country, String email, String latitude, String longitude,
                       String phone, String stateProvince, String times, String town, String website, String zipPostalCode){
        this.affiliation = affiliation;
        this.churchName = churchName;
        this.country = country;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.stateProvince = stateProvince;
        this.times = times;
        this.town = town;
        this.website = website;
        this.zipPostalCode = zipPostalCode;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getChurchName() {
        return churchName;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPhone() {
        return phone;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public String getTimes() {
        return times;
    }

    public String getTown() {
        return town;
    }

    public String getWebsite() {
        return website;
    }

    public String getZipPostalCode() {
        return zipPostalCode;
    }
}