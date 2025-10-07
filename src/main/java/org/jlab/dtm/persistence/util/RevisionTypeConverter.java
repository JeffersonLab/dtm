package org.jlab.dtm.persistence.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hibernate.envers.RevisionType;

@Converter(autoApply = true) // <-- makes it apply to all RevisionType columns
public class RevisionTypeConverter implements AttributeConverter<RevisionType, Integer> {

  @Override
  public Integer convertToDatabaseColumn(RevisionType attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.ordinal();
  }

  @Override
  public RevisionType convertToEntityAttribute(Integer dbData) {
    if (dbData == null || dbData < 0 || dbData >= RevisionType.values().length) {
      return null;
    }
    return RevisionType.values()[dbData];
  }
}
