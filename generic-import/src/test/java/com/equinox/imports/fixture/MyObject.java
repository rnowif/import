package com.equinox.imports.fixture;

public class MyObject {

    private String stringField;
    private Integer integerField;

    public MyObject() {

    }

    public MyObject(String stringField, Integer integerField) {
        this.stringField = stringField;
        this.integerField = integerField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyObject myObject = (MyObject) o;

        if (stringField != null ? !stringField.equals(myObject.stringField) : myObject.stringField != null)
            return false;
        return integerField != null ? integerField.equals(myObject.integerField) : myObject.integerField == null;

    }

    @Override
    public int hashCode() {
        int result = stringField != null ? stringField.hashCode() : 0;
        result = 31 * result + (integerField != null ? integerField.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MyObject{" +
                "stringField='" + stringField + '\'' +
                ", integerField=" + integerField +
                '}';
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }
}
