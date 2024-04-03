

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

public class DtoBuilder {
    public static DataFormatter dataFormatter = new DataFormatter();
    public static <T> T fromExcelRow(Row row,T dto, LinkedHashMap<String, Map<String,String>> rowMapping)  {
        Class<?> clazz = dto.getClass();
        int i = 0;
        for (Map.Entry<String, Map<String,String>> entry : rowMapping.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(row.getCell(i));
            String fieldMapName = entry.getValue().get("fieldMapping");
            try{
//                String pfieldName = fieldMapName.contains(":") ? fieldMapName.substring(0,fieldMapName.lastIndexOf(":")) : fieldMapName;
//                Field field = clazz.getDeclaredField(pfieldName);
//                Class<?> fieldType = field.getType();
                setNestedValue(fieldMapName,dto,cellValue);
//                Object value = fieldMapName.contains(":") ? getNestedValue(fieldMapName,field,dto,cellValue) : convertValueToType(cellValue,fieldType);
//                Method setterMethod = getSetterMethod(clazz, fieldMapName,fieldType);
//                setterMethod.invoke(dto,value);

            }catch (NoSuchFieldException e) {
                System.out.println("Field - " + fieldMapName + ": Not found or cannot be accessed");
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                System.out.println("Field - " + fieldMapName + ": Not found or cannot be accessed");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
            i++;
        }
        return dto;
    }
    private static <T> Object setNestedValue(String fieldMapName, T dto,String cellValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<String> fields = new LinkedList<>(List.of(fieldMapName.split(":")));
        Object currentClassInstance = dto;
        while (true){
            String curFieldMapName = fields.removeFirst();
            Field curField =  currentClassInstance.getClass().getDeclaredField(curFieldMapName);
            if(fields.isEmpty()){
                Method setterMethod = getSetterMethod(currentClassInstance.getClass(), curFieldMapName,curField.getType());
                setterMethod.invoke(currentClassInstance,convertValueToType(cellValue,curField.getType()));
                break;
            }
            Method getterMethod = getGetterMethod(currentClassInstance.getClass(), curFieldMapName);
            Object getterValue = getterMethod.invoke(currentClassInstance);
            Method setterMethod = getSetterMethod(currentClassInstance.getClass(), curFieldMapName,curField.getType());
            Object nestedClassInstance = (getterValue == null) ?  curField.getType().getConstructor().newInstance() : getterValue;
            setterMethod.invoke(currentClassInstance,nestedClassInstance);
            currentClassInstance =  nestedClassInstance;
        }
        return null;
    }
    private static Object convertValueToType(String value,Class<?> fieldType) {
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
        } else if (fieldType == BigDecimal .class ) {
            return new BigDecimal(value);
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
}
