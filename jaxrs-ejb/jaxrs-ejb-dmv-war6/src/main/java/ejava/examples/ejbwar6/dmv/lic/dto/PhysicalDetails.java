package ejava.examples.ejbwar6.dmv.lic.dto;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents the physical details used to decribe a resident
 * or driver.
 */
@XmlType(name="PhysicalDetailsType", namespace=DrvLicRepresentation.DRVLIC_NAMESPACE, propOrder={
        "sex", "height", "weight", "hairColor", "eyeColor"
})
public class PhysicalDetails {
    @XmlType(namespace=DrvLicRepresentation.DRVLIC_NAMESPACE)
    public enum HairColor{ BROWN, BLONDE, GREY, BALD };
    @XmlType(namespace=DrvLicRepresentation.DRVLIC_NAMESPACE)
    public enum EyeColor { GREEN, BLUE, BROWN };
    @XmlType(namespace=DrvLicRepresentation.DRVLIC_NAMESPACE)
    public enum Sex { M, F };
    private Sex sex;
    private Integer height;
    private Integer weight;
    private HairColor hairColor;
    private EyeColor eyeColor;

    public Sex getSex() {
        return sex;
    }
    public void setSex(Sex sex) {
        this.sex = sex;
    }
    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Integer getWeight() {
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    public HairColor getHairColor() {
        return hairColor;
    }
    public void setHairColor(HairColor hairColor) {
        this.hairColor = hairColor;
    }
    public EyeColor getEyeColor() {
        return eyeColor;
    }
    public void setEyeColor(EyeColor eyeColor) {
        this.eyeColor = eyeColor;
    }
    
    @XmlTransient
    public boolean isComplete() {
        return sex != null && height != null && weight != null && 
                hairColor != null && eyeColor != null;
    }
}
