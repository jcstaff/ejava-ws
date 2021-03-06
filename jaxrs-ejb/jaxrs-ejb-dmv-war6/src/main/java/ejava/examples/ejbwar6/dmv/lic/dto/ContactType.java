package ejava.examples.ejbwar6.dmv.lic.dto;

import javax.xml.bind.annotation.XmlType;

/**
 * This enum defines the types of contacts that can exist.
 */
@XmlType(namespace=DrvLicRepresentation.DRVLIC_NAMESPACE, name="ContactTypeType")
public enum ContactType {
    RESIDENCE,
    WORK,
    OTHER
}
