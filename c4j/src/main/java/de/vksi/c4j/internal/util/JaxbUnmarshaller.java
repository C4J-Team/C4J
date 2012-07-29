package de.vksi.c4j.internal.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class JaxbUnmarshaller {
	public <T> T unmarshal(InputStream xmlStream, Class<T> xmlClass) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(xmlClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		@SuppressWarnings("unchecked")
		T unmarshal = (T) unmarshaller.unmarshal(xmlStream);
		setDefaultValues(unmarshal);
		return unmarshal;
	}

	public <T> void setDefaultValues(T xmlObject) throws JAXBException {
		try {
			for (Field field : xmlObject.getClass().getDeclaredFields())
				setDefaultValueForField(xmlObject, field);
		} catch (Exception e) {
			throw new JAXBException("Error handling default values", e);
		}
	}

	private <T> void setDefaultValueForField(T xmlObject, Field field) throws JAXBException, IllegalAccessException,
			InstantiationException {
		if (field.getAnnotation(XmlElement.class) == null)
			return;
		field.setAccessible(true);
		Object fieldValue = field.get(xmlObject);
		if (field.getType().equals(List.class)) {
			setDefaultValueForList(fieldValue);
			return;
		}
		if (fieldValue == null)
			handleFieldDefaultValue(xmlObject, field);
	}

	private <T> void handleFieldDefaultValue(T xmlObject, Field field) throws IllegalAccessException,
			InstantiationException {
		if (handleFieldDefaultValueAsXmlType(xmlObject, field))
			return;
		String defaultValue = field.getAnnotation(XmlElement.class).defaultValue();
		if ("\u0000".equals(defaultValue))
			return;
		if (handleFieldDefaultValueAsBoolean(xmlObject, field, defaultValue))
			return;
		if (handleFieldDefaultValueAsEnum(xmlObject, field, defaultValue))
			return;
	}

	private <T> boolean handleFieldDefaultValueAsXmlType(T xmlObject, Field field) throws IllegalAccessException,
			InstantiationException {
		if (field.getType().isEnum() || field.getType().getAnnotation(XmlType.class) == null)
			return false;
		field.set(xmlObject, field.getType().newInstance());
		return true;
	}

	private <T> boolean handleFieldDefaultValueAsEnum(T xmlObject, Field field, String defaultValue)
			throws IllegalAccessException {
		if (!field.getType().isEnum())
			return false;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), defaultValue.toUpperCase());
		field.set(xmlObject, enumValue);
		return true;
	}

	private <T> boolean handleFieldDefaultValueAsBoolean(T xmlObject, Field field, String defaultValue)
			throws IllegalAccessException {
		if (!field.getType().equals(Boolean.class))
			return false;
		field.set(xmlObject, Boolean.valueOf(defaultValue));
		return true;
	}

	private void setDefaultValueForList(Object fieldValue) throws JAXBException {
		if (fieldValue == null) {
			return;
		}
		List<?> listValues = (List<?>) fieldValue;
		for (Object value : listValues) {
			setDefaultValues(value);
		}
	}
}
