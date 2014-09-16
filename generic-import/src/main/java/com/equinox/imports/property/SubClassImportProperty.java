package com.equinox.imports.property;

import java.util.ArrayList;
import java.util.List;

public class SubClassImportProperty {

	private Class<?> type;
	private String name;
	private Boolean notNull;
	private Boolean multiple;
	private final List<ImportProperty> properties;
	private final List<SubClassImportProperty> subClassProperties;

	public SubClassImportProperty() {
		this.properties = new ArrayList<ImportProperty>();
		this.subClassProperties = new ArrayList<SubClassImportProperty>();
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public Boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public List<ImportProperty> getProperties() {
		return properties;
	}

	public void addProperty(ImportProperty property) {
		this.properties.add(property);
	}

	public void addSubClassProperty(SubClassImportProperty property) {
		this.subClassProperties.add(property);
	}

	public List<SubClassImportProperty> getSubClassProperties() {
		return subClassProperties;
	}

}
