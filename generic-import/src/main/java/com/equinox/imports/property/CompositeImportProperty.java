package com.equinox.imports.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.equinox.imports.CompositeImportPropertyComposer;
import com.equinox.imports.ImportLineColumn;
import com.equinox.imports.ImportTuple;
import com.equinox.imports.exception.ImportPropertyException;
import com.equinox.imports.exception.InvalidFormatPropertyImportException;
import com.equinox.imports.exception.NullPropertyImportException;

public class CompositeImportProperty {

	private Class<?> type;
	private String name;
	private Boolean notNull;
	private Boolean multiple;
	private CompositeImportPropertyComposer composer;
	private final List<ComponentImportProperty> components;

	public CompositeImportProperty() {
		this.components = new ArrayList<ComponentImportProperty>();
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

	public void setComposer(CompositeImportPropertyComposer composer) {
		this.composer = composer;
	}

	public void addComponentProperty(ComponentImportProperty component) {
		this.components.add(component);
	}

	public List<ComponentImportProperty> getComponents() {
		return components;
	}

	public <T> T getValue(Class<T> type, ImportTuple tuple) throws ImportPropertyException {

		if (composer == null) {
			return null;
		}

		// On construit la map des valeurs même si le tuple est null (le compositor peut très bien s'en sortir quand
		// même).
		Map<String, String> values = new HashMap<String, String>();

		for (ComponentImportProperty component : this.components) {

			String value = component.getDefaultValue();

			if (tuple != null) {
				ImportLineColumn column = tuple.get(component.getFile().getId(), component.getColumnIndex());

				if (column != null) {
					value = column.getValue();
				}
			}

			values.put(component.getName(), value);
		}

		Object toReturn = composer.compose(values);

		if (toReturn == null && this.notNull) {
			throw new NullPropertyImportException(this.name);
		}

		try {
			return type.cast(toReturn);
		} catch (ClassCastException e) {
			throw new InvalidFormatPropertyImportException(name, type.getSimpleName(), toReturn.getClass()
					.getSimpleName());
		}
	}

}
