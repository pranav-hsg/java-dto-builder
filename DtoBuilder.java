package com.sensei.encore.util.excel;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DtoBuilder {
    public static DataFormatter dataFormatter = new DataFormatter();
    public static <T> T fromExcelRow(Row row,T dto, List<String> rowMapping)  {
        Class<?> clazz = dto.getClass();
        for (int i = 0; i < rowMapping.size(); i++) {
            String cellValue = dataFormatter.formatCellValue(row.getCell(i));
            String fieldName = rowMapping.get(i);
            try{
                Field field = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = field.getType();

                Method setterMethod = getSetterMethod(clazz, fieldName,fieldType);
                setterMethod.invoke(setterMethod,cellValue);

            }catch (NoSuchFieldException e) {
                System.out.println("Field - " + fieldName + ": Not found or cannot be accessed");
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                System.out.println("Field - " + fieldName + ": Not found or cannot be accessed");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private static Object convertValueToType(String value,Class<?> fieldType){
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(value);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType == LocalDate.class ) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(value, formatter);
        } else if (fieldType == PercentRate.class){
            
        }

        return null;
    }
    private static Method getSetterMethod(Class<?> clazz, String fieldName,Class<?> fieldType) throws NoSuchMethodException {
        String setterMethodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return clazz.getMethod(setterMethodName, fieldType);
    }
}
