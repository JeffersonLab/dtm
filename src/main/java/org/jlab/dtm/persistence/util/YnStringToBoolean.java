package org.jlab.dtm.persistence.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * TODO: this should be part of smoothness weblib.  Also used by BAM.
 */
@Converter
public class YnStringToBoolean implements AttributeConverter<Boolean, String> {
    /**
     * Converts the value stored in the entity attribute into the
     * data representation to be stored in the database.
     *
     * @param attribute the entity attribute value to be converted
     * @return the converted data to be stored in the database
     * column
     */
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        String result = null;

        if(attribute != null) {
            result = attribute ? "Y" : "N";
        }

        return result;
    }

    /**
     * Converts the data stored in the database column into the
     * value to be stored in the entity attribute.
     * Note that it is the responsibility of the converter writer to
     * specify the correct <code>dbData</code> type for the corresponding
     * column for use by the JDBC driver: i.e., persistence providers are
     * not expected to do such type conversion.
     *
     * @param dbData the data from the database column to be
     *               converted
     * @return the converted value to be stored in the entity
     * attribute
     */
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        Boolean result = null;

        if(dbData != null) {
            result = dbData.equals("Y");
        }

        return result;
    }
}
