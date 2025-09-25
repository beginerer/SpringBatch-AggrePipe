package com.core.support;


import com.core.AggQueryMetadata;
import com.core.ItemSpec;
import com.core.ReadItemSpec;
import com.core.ReadQueryMetadata;
import com.core.annotaion.*;
import com.core.operation.Operation;
import com.core.operation.ValueType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;



@Configuration
public class AggregationQuerySupport implements ImportAware {


    private AnnotationAttributes attrAs;

    private Map<Class<?>, AggQueryMetadata> aggQueryMetadataMap;

    private Map<Class<?>, ReadQueryMetadata> readQueryMetadataMap;






    @Override
    public void setImportMetadata(AnnotationMetadata metadata) {
        this.attrAs = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableAggQuery.class.getName()));

        Set<String> baseSet = new LinkedHashSet<>();

        if(attrAs !=null) {
            for(String p : attrAs.getStringArray("basePackages")) {
                if(p !=null && !p.isBlank())
                    baseSet.add(p);
            }
            for(Class<?> c : attrAs.getClassArray("basePackageClasses")) {
                if(c !=null)
                    baseSet.add(c.getPackageName());
            }
        }

        baseSet.add(ClassUtils.getPackageName(metadata.getClassName()));
        List<String> bases = new ArrayList<>(baseSet);

        this.aggQueryMetadataMap = scanAndBuildAggMetadata(bases);
        this.readQueryMetadataMap = scanAndBuildReadMetadata(bases);
    }


    @Bean
    public AggQueryRegistry aggQueryRegistry() {
        return new AggQueryRegistry(aggQueryMetadataMap, readQueryMetadataMap);
    }


    @Bean
    public AggQueryBindingHandler aggQueryBindingHandler(AggQueryRegistry registry) {
        return new AggQueryBindingHandler(registry);
    }

    @Bean
    public ReadQueryBindingHandler readQueryBindingHandler(AggQueryRegistry registry) {
        return new ReadQueryBindingHandler(registry);
    }



    private Map<Class<?>, AggQueryMetadata> scanAndBuildAggMetadata(List<String> bases) {
        var out = new LinkedHashMap<Class<?>, AggQueryMetadata>();
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AggQuery.class, true));
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Set<String> seen = new HashSet<>();

        for(String base : bases) {
            for (BeanDefinition bd : scanner.findCandidateComponents(base)) {
                String className = Objects.requireNonNull(bd.getBeanClassName());
                if(!seen.add(className))
                    continue;

                Class<?> dto = ClassUtils.resolveClassName(className, cl);
                Set<String> fieldNames = getDeclaredFields(dto);

                validateAggQueryAnnotation(dto, fieldNames);
                AggQueryMetadata aggQueryMetadata = buildAggQueryMetaData(dto);
                out.put(dto, aggQueryMetadata);
            }
        }
        return out;
    }

    private Map<Class<?>, ReadQueryMetadata> scanAndBuildReadMetadata(List<String> bases) {
        var out = new LinkedHashMap<Class<?>, ReadQueryMetadata>();
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ReadQuery.class, true));
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Set<String> seen = new HashSet<>();

        for(String base  : bases) {
            for(BeanDefinition bd : scanner.findCandidateComponents(base)) {
                String className = Objects.requireNonNull(bd.getBeanClassName());
                if(!seen.add(className))
                    continue;

                Class<?> dto = ClassUtils.resolveClassName(className, cl);
                Set<String> fieldNames = getDeclaredFields(dto);

                validateReadQueryAnnotation(dto, fieldNames);
                ReadQueryMetadata readQueryMetadata = buildReadQueryMetaData(dto);
                out.put(dto, readQueryMetadata);
            }
        }
        return out;
    }



    private AggQueryMetadata buildAggQueryMetaData(Class<?> clazz) {
        if(clazz.isRecord()) {
            List<RecordComponent> aggFields = getAggFields(clazz.getRecordComponents());
            List<ItemSpec> itemSpecs = new ArrayList<>();

            for (RecordComponent aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                ItemSpec itemSpec = new ItemSpec(aggField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);

            return new AggQueryMetadata(clazz.getSimpleName(), ann.groupByKeys(), true, itemSpecs);
        }else {
            List<Field> aggFields = getAggFields(clazz.getDeclaredFields());
            List<ItemSpec> itemSpecs = new ArrayList<>();

            for (Field aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                ItemSpec itemSpec = new ItemSpec(aggField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);
            return new AggQueryMetadata(clazz.getSimpleName(), ann.groupByKeys(), false, itemSpecs);
        }
    }

    public ReadQueryMetadata buildReadQueryMetaData(Class<?> clazz) {
        if(clazz.isRecord()) {
            List<RecordComponent> readFields = getReadFields(clazz.getRecordComponents());
            List<ReadItemSpec> itemSpecs = new ArrayList<>();

            for(RecordComponent readField : readFields) {
                ReadAggField annotation = readField.getDeclaredAnnotation(ReadAggField.class);
                ReadItemSpec itemSpec = new ReadItemSpec(annotation.originalFieldName(), readField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            ReadQuery ann = clazz.getDeclaredAnnotation(ReadQuery.class);
            return new ReadQueryMetadata(clazz.getSimpleName(), ann.groupByKeys(), true, itemSpecs);
        }else {
            List<Field> readFields = getReadFields(clazz.getDeclaredFields());
            List<ReadItemSpec> itemSpecs = new ArrayList<>();

            for (Field readField : readFields) {
                ReadAggField annotation = readField.getDeclaredAnnotation(ReadAggField.class);
                ReadItemSpec itemSpec = new ReadItemSpec(annotation.originalFieldName(), readField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            ReadQuery ann = clazz.getDeclaredAnnotation(ReadQuery.class);
            return new ReadQueryMetadata(clazz.getSimpleName(), ann.groupByKeys(), false, itemSpecs);
        }
    }


    private void validateAggQueryAnnotation(Class<?> queryDto, Set<String> filedNames) {
        validateAggGroupByKeys(queryDto, filedNames);
        validateAggField(queryDto);
    }

    private void validateReadQueryAnnotation(Class<?> queryDto, Set<String> filedNames) {
        validateReadGroupByKeys(queryDto, filedNames);
        validateReadField(queryDto);
        validateReadQueryAvailable(queryDto);
    }



    private void validateAggGroupByKeys(Class<?> queryClass, Set<String> filedNames) {
        AggQuery ann = queryClass.getAnnotation(AggQuery.class);
        GroupByKey[] keys = ann.groupByKeys();

        List<String> keyNames = Arrays.stream(keys).map(GroupByKey::field).toList();

        List<String> missing = keyNames.stream().filter(k -> !filedNames.contains(k)).toList();
        if(!missing.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class is missing group-by fields: %s".
                    formatted(queryClass.getName(), missing));


        List<String> dupKeys = findDuplicates(keyNames);
        if(!dupKeys.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class has duplicate group-by fields : %s".
                    formatted(queryClass.getName(), dupKeys));

        List<String> hasAggFieldAnn = keyNames.stream().filter(keyName -> hasAggFieldAnnotation(queryClass, keyName)).toList();
        if(!hasAggFieldAnn.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] group-by fields must not declare @AggField annotation : %s");


        if(queryClass.isRecord()) {
            for(GroupByKey key : keys) {
                RecordComponent rc = getGroupByField(key, queryClass.getRecordComponents());
                validateGroupByKeyAnnotationValueType(key, rc.getType());
            }
        }else {
            for(GroupByKey key : keys) {
                Field field = getGroupByField(key, queryClass.getDeclaredFields());
                validateGroupByKeyAnnotationValueType(key, field.getType());
            }
        }
    }

    private void validateReadGroupByKeys(Class<?> queryClass, Set<String> filedNames) {
        ReadQuery ann = queryClass.getDeclaredAnnotation(ReadQuery.class);
        GroupByKey[] keys = ann.groupByKeys();

        List<String> keyNames = Arrays.stream(keys).map(GroupByKey::field).toList();

        List<String> missing = keyNames.stream().filter(k -> !filedNames.contains(k)).toList();
        if(!missing.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class is missing group-by fields: %s".
                    formatted(queryClass.getName(), missing));


        List<String> dupKeys = findDuplicates(keyNames);
        if(!dupKeys.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class has duplicate group-by fields : %s".
                    formatted(queryClass.getName(), dupKeys));

        List<String> hasReadFieldAnn = keyNames.stream().filter(keyName -> hasReadFiledAnnotation(queryClass, keyName)).toList();
        if(!hasReadFieldAnn.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] group-by fields must not declare @ReadAggField annotation : %s");

        if(queryClass.isRecord()) {
            for(GroupByKey key : keys) {
                RecordComponent rc = getGroupByField(key, queryClass.getRecordComponents());
                validateGroupByKeyAnnotationValueType(key, rc.getType());
            }
        }else {
            for(GroupByKey key : keys) {
                Field field = getGroupByField(key, queryClass.getDeclaredFields());
                validateGroupByKeyAnnotationValueType(key, field.getType());
            }
        }
    }



    private void validateAggField(Class<?> queryDto) {

        if(queryDto.isRecord()) {
            List<RecordComponent> aggFields = getAggFields(queryDto.getRecordComponents());
            if(aggFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @AggField".
                        formatted(queryDto.getName()));

            aggFields.stream().forEach(rc -> {
                AggField annotation = rc.getDeclaredAnnotation(AggField.class);
                validateAggFieldAnnotationValueType(annotation, rc.getType());
            });
        }else {
            List<Field> aggFields = getAggFields(queryDto.getDeclaredFields());
            if(aggFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @AggField".
                        formatted(queryDto.getName()));


            aggFields.stream().forEach(field -> {
                AggField annotation = field.getDeclaredAnnotation(AggField.class);
                validateAggFieldAnnotationValueType(annotation, field.getType());
            });
        }
    }

    private void validateReadField(Class<?> queryDto) {
        if(queryDto.isRecord()) {
            List<RecordComponent> readFields = getReadFields(queryDto.getRecordComponents());
            if(readFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @ReadAggField".
                        formatted(queryDto.getName()));

            readFields.stream().forEach(rc -> {
                ReadAggField annotation = rc.getDeclaredAnnotation(ReadAggField.class);
                validateReadFieldAnnotationValueType(annotation, rc.getType());
            });
        }else {
            List<Field> readFields = getReadFields(queryDto.getDeclaredFields());
            if(readFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @ReadAggField".
                        formatted(queryDto.getName()));

            readFields.stream().forEach(field -> {
                ReadAggField annotation = field.getDeclaredAnnotation(ReadAggField.class);
                validateReadFieldAnnotationValueType(annotation, field.getType());
            });
        }
    }

    private void validateReadQueryAvailable(Class<?> queryDto) {
        ReadQuery annotation = queryDto.getDeclaredAnnotation(ReadQuery.class);
        String originalClassName = annotation.aggQueryClassName();

        Class<?> aggQueryClass = aggQueryMetadataMap.keySet().stream().filter(clazz -> clazz.getSimpleName().equals(originalClassName)).findAny()
                .orElseThrow(() -> new BeanDefinitionValidationException("[ERROR] %s class's original class name doesn't matches"));

        if(queryDto.isRecord()) {
            RecordComponent[] originalRecordComponents = aggQueryClass.getRecordComponents();
            for (RecordComponent readField : getReadFields(queryDto.getRecordComponents())) {
                ReadAggField ann = readField.getDeclaredAnnotation(ReadAggField.class);
                validateReadFieldAnnotationMetadata(ann, originalRecordComponents);
            }
        }else {
            Field[] OriginalFields = aggQueryClass.getDeclaredFields();
            for (Field readField : getReadFields(queryDto.getDeclaredFields())) {
                ReadAggField ann = readField.getAnnotation(ReadAggField.class);
                validateReadFieldAnnotationMetadata(ann, OriginalFields);
            }
        }
    }



    private Set<String> getDeclaredFields(Class<?> clazz) {
        Set<String> names = new HashSet<>();

        if(clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                names.add(rc.getName());
            }
        }else {
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if(!Modifier.isStatic(modifiers))
                    names.add(field.getName());
            }
        }
        return names;
    }


    private List<RecordComponent> getAggFields(RecordComponent[] fields) {
        return Arrays.stream(fields).
                filter(rc -> rc.isAnnotationPresent(AggField.class)).toList();
    }


    private List<Field> getAggFields(Field[] fields) {
        return Arrays.stream(fields).filter(field -> {
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers);
        }).filter(field -> field.isAnnotationPresent(AggField.class)).toList();
    }

    private List<RecordComponent> getReadFields(RecordComponent[] fields) {
        return Arrays.stream(fields).filter(rc -> rc.isAnnotationPresent(ReadAggField.class)).toList();
    }

    private List<Field> getReadFields(Field[] fields) {
        return Arrays.stream(fields).filter(field -> {
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers);
        }).filter(field -> field.isAnnotationPresent(ReadAggField.class)).toList();
    }


    private RecordComponent getGroupByField(GroupByKey key, RecordComponent[] fields) {
        String field = key.field();
        for(RecordComponent rc : fields) {
            if(rc.getName().equals(field))
                return rc;
        }
        throw new IllegalStateException("[ERROR] %s class does not have field. field=%s".
                formatted(field));
    }

    private Field getGroupByField(GroupByKey key, Field[] fields) {
        String field = key.field();
        for(Field f : fields) {
            if(f.getName().equals(field))
                return f;
        }
        throw new IllegalStateException("[ERROR] %s class does not have field. field=%s".
                formatted(field));
    }


    private List<String> findDuplicates(List<String> items) {
        Set<String> seen = new HashSet<>();

        return items.stream().filter( s -> !seen.add(s)).distinct().toList();
    }


    private boolean hasAggFieldAnnotation(Class<?> clazz, String name) {
        if(clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                if(rc.getName().equals(name))
                    return rc.isAnnotationPresent(AggField.class);
            }
        }else {
            for (Field f : clazz.getDeclaredFields()) {
                if(f.getName().equals(name)) {
                    int modifiers = f.getModifiers();
                    if(!Modifier.isStatic(modifiers))
                        return f.isAnnotationPresent(AggField.class);
                }
            }
        }
        return false;
    }

    private boolean hasReadFiledAnnotation(Class<?> clazz, String name) {
        if(clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                if(rc.getName().equals(name))
                    return rc.isAnnotationPresent(ReadAggField.class);
            }
        }else {
            for (Field f : clazz.getDeclaredFields()) {
                if(f.getName().equals(name)) {
                    int modifiers = f.getModifiers();
                    if(!Modifier.isStatic(modifiers))
                        return f.isAnnotationPresent(ReadAggField.class);
                }
            }
        }
        return false;
    }


    private void validateAggFieldAnnotationValueType(AggField aggField, Class<?> fieldRawType) {
        Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(fieldRawType);
        ValueType valueType = aggField.type();

        if(valueType == ValueType.LONG) {
            if(fieldType != Long.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }else if(valueType == ValueType.DOUBLE) {
            if(fieldType != Double.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        } else {
            throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                    formatted(valueType.name(), fieldType.getName()));
        }
    }

    private void validateReadFieldAnnotationValueType(ReadAggField readField, Class<?> fieldRawType) {
        Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(fieldRawType);
        ValueType valueType = readField.type();

        if(valueType == ValueType.LONG) {
            if(fieldType != Long.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }else if(valueType == ValueType.DOUBLE) {
            if(fieldType != Double.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        } else {
            throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                    formatted(valueType.name(), fieldType.getName()));
        }
    }

    private void validateGroupByKeyAnnotationValueType(GroupByKey groupByKey, Class<?> fieldRawType) {
        Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(fieldRawType);
        ValueType valueType = groupByKey.type();

        if(valueType == ValueType.LONG) {
            if(fieldType != Long.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }else if(valueType == ValueType.DOUBLE) {
            if(fieldType != Double.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }else if(valueType == ValueType.STRING) {
            if(fieldType != String.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }
    }

    private void validateReadFieldAnnotationMetadata(ReadAggField readAggField, RecordComponent[] originalRecordComponents) {
        String originalFieldName = readAggField.originalFieldName();
        Operation operation = readAggField.op();
        ValueType valueType = readAggField.type();

        RecordComponent recordComponent = Arrays.stream(originalRecordComponents).filter(rc -> rc.getName().equals(originalFieldName)).findAny()
                .orElseThrow(() -> new BeanDefinitionValidationException("[ERROR] %s fieldName doesn't exist in AggQuery class".
                        formatted(originalFieldName)));

        AggField ann = recordComponent.getDeclaredAnnotation(AggField.class);
        if(ann == null)
            throw new BeanDefinitionValidationException("[ERROR] %s field does not declare @AggField".formatted(originalFieldName));

        Operation[] aggFieldOperations = ann.op();
        ValueType aggFieldValueType = ann.type();

        if(valueType != aggFieldValueType)
            throw new BeanDefinitionValidationException("[ERROR] %s field does not matches value type. expected=%s, current=%s".
                    formatted(aggFieldOperations,valueType));

        Arrays.stream(aggFieldOperations).filter(op -> op == operation).findAny()
                .orElseThrow(() -> new BeanDefinitionValidationException("[ERROR] %s field doest not support %s operation. support operation=%s".
                        formatted(operation, aggFieldOperations)));
    }

    private void validateReadFieldAnnotationMetadata(ReadAggField readAggField, Field[] originalFields) {
        String originalFieldName = readAggField.originalFieldName();
        Operation operation = readAggField.op();
        ValueType valueType = readAggField.type();

        Field field = Arrays.stream(originalFields).filter(f -> f.getName().equals(originalFieldName)).findAny()
                .orElseThrow(() -> new BeanDefinitionValidationException("[ERROR] %s fieldName doesn't exist in AggQuery class".
                        formatted(originalFieldName)));

        AggField ann = field.getDeclaredAnnotation(AggField.class);
        if(ann == null)
            throw new BeanDefinitionValidationException("[ERROR] %s field does not declare @AggField".formatted(originalFieldName));

        Operation[] aggFieldOperations = ann.op();
        ValueType aggFieldValueType = ann.type();

        if(valueType != aggFieldValueType)
            throw new BeanDefinitionValidationException("[ERROR] %s field does not matches value type. expected=%s, current=%s".
                    formatted(aggFieldOperations,valueType));

        Arrays.stream(aggFieldOperations).filter(op -> op == operation).findAny()
                .orElseThrow(() -> new BeanDefinitionValidationException("[ERROR] %s field doest not support %s operation. support operation=%s".
                        formatted(operation, aggFieldOperations)));
    }

}