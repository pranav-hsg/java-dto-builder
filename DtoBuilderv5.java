package com.sensei.encore.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Slf4j
public class DtoBuilder {
    public static DataFormatter dataFormatter = new DataFormatter();

    public static <T> T fromExcelRow(Row row,T dto, LinkedHashMap<String, Map<String,String>> rowMapping)  {
        return fromExcelRow(row,dto,rowMapping,null);
    }
    public static <T> T fromExcelRow(Row row,T dto, LinkedHashMap<String, Map<String,String>> rowMapping,ConvertValueToType convertValueToType)  {
        int i = 0;
        for (Map.Entry<String, Map<String,String>> entry : rowMapping.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(row.getCell(i));
            String fieldMapName = entry.getValue().get("fieldMapping");
            try{
                setNestedValue(fieldMapName,dto,cellValue,convertValueToType);
            }catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                log.error("An Error Occurred while handling field "+fieldMapName,e);
            }
            i++;
        }
        return dto;
    }
    private static <T> Object setNestedValue(String fieldMapName, T dto,String cellValue,ConvertValueToType convertValueToTypePassed) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Object currentClassInstance = dto;
        List<String> fields = new LinkedList<>(List.of(fieldMapName.split(":")));
        while (true){
            String curFieldMapName = fields.removeFirst();
            Field curField =  currentClassInstance.getClass().getDeclaredField(curFieldMapName);
            Method setterMethod = getSetterMethod(currentClassInstance.getClass(), curFieldMapName,curField.getType());
            if(fields.isEmpty()) {
                setterMethod.invoke(currentClassInstance, convertValueToTypePassed == null ? convertValueToType(cellValue, curField.getType()) : convertValueToTypePassed.apply(cellValue,curField.getType()));
                break;
            }
            Method getterMethod = getGetterMethod(currentClassInstance.getClass(), curFieldMapName);
            Object getterValue = getterMethod.invoke(currentClassInstance);
            Object nestedClassInstance = (getterValue == null) ?  curField.getType().getConstructor().newInstance() : getterValue;
            setterMethod.invoke(currentClassInstance,nestedClassInstance);
            currentClassInstance =  nestedClassInstance;
        }
        return null;
    }
    private static Object convertValueToType(String value,Class<?> fieldType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            if (fieldType == String.class) {
                return value;
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(value);
            } else if (fieldType == Double.class || fieldType == double.class) {
                return Double.parseDouble(value);
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (fieldType == LocalDate.class) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(value, formatter);
            } else if (fieldType == BigDecimal.class) {
                return new BigDecimal(value);
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                Method valueOf = fieldType.getMethod("valueOf", String.class);
                return valueOf.invoke(fieldType, value);
            }
        }catch( RuntimeException e){
            log.error("Error occurred while parsing value ",e);
        }
        return null;
    }
    private static Method getSetterMethod(Class<?> clazz, String fieldName,Class<?> fieldType) throws NoSuchMethodException {
        String setterMethodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return clazz.getMethod(setterMethodName, fieldType);
    }
    private static Method getGetterMethod(Class<?> clazz, String fieldName) throws NoSuchMethodException {
        String getterMethodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return clazz.getMethod(getterMethodName);
    }
    private static String removeLeftmostValue(String input) {
        int indexOfColon = input.indexOf(":");
        if (indexOfColon >= 0) {
            return input.substring(indexOfColon + 1);
        } else {
            return input;
        }
    }

    @FunctionalInterface
    public interface ConvertValueToType {
        Object apply(String value,Class<?> fieldType) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException;
    }
}
