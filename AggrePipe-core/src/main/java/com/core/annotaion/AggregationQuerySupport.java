package com.core.annotaion;


import com.core.operation.ValueType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;



public class AggregationQuerySupport implements ImportBeanDefinitionRegistrar {




    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attrs = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableAggQuery.class.getName())
        );

        Set<String> baseSet = new LinkedHashSet<>();

        if(attrs !=null) {
            for(String p : attrs.getStringArray("basePackages")) {
                if(p !=null && !p.isBlank())
                    baseSet.add(p);
            }
            for(Class<?> c : attrs.getClassArray("basePackageClasses")) {
                if(c !=null)
                    baseSet.add(c.getPackageName());
            }
        }

        baseSet.add(ClassUtils.getPackageName(metadata.getClassName()));
        List<String> bases = new ArrayList<>(baseSet);


        Map<Class<?>, AggQueryMetadata> metaDataMap = scanAndBuildMetadata(bases);

        BeanDefinitionBuilder b = BeanDefinitionBuilder.rootBeanDefinition(AggQueryRegistry.class)
                .addConstructorArgValue(metaDataMap);

        String registryBeanName = BeanDefinitionReaderUtils.registerWithGeneratedName(b.getBeanDefinition(), registry);


        BeanDefinitionBuilder b2 = BeanDefinitionBuilder.rootBeanDefinition(AggQueryBindingHandler.class)
                .addConstructorArgReference(registryBeanName);

        BeanDefinitionReaderUtils.registerWithGeneratedName(b2.getBeanDefinition(), registry);
    }


    private Map<Class<?>, AggQueryMetadata> scanAndBuildMetadata(List<String> bases) {
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

                validateAnnotation(dto, fieldNames);
                AggQueryMetadata aggQueryMetadata = buildMetaData(dto);
                out.put(dto, aggQueryMetadata);
            }
        }
        return out;
    }


    private AggQueryMetadata buildMetaData(Class<?> clazz) {
        if(clazz.isRecord()) {
            List<RecordComponent> aggFields = getAggFields(clazz.getRecordComponents());

            List<Item> items = new ArrayList<>();

            for (RecordComponent aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                Item item = new Item(annotation.fieldName(), annotation.op(), annotation.type());
                items.add(item);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);

            return new AggQueryMetadata(ann.name(), ann.groupByKeys(), true, items);
        }else {
            List<Field> aggFields = getAggFields(clazz.getDeclaredFields());

            List<Item> items = new ArrayList<>();

            for (Field aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                Item item = new Item(annotation.fieldName(), annotation.op(), annotation.type());
                items.add(item);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);

            return new AggQueryMetadata(ann.name(), ann.groupByKeys(), false, items);
        }
    }


    private void validateAnnotation(Class<?> queryDto, Set<String> filedNames) {
        validateGroupByKeys(queryDto, filedNames);
        validateAggField(queryDto);
    }



    private void validateGroupByKeys(Class<?> queryDto, Set<String> filedNames) {
        AggQuery ann = queryDto.getAnnotation(AggQuery.class);
        GroupByKey[] keys = ann.groupByKeys();

        List<String> keyNames = Arrays.stream(keys).map(GroupByKey::field).toList();


        List<String> missing = keyNames.stream().filter(k -> !filedNames.contains(k)).toList();
        if(!missing.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class is missing group-by fields: %s".
                    formatted(queryDto.getName(), missing));


        List<String> dupKeys = findDuplicates(keyNames);
        if(!dupKeys.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] %s class has duplicate group-by fields : %s".
                    formatted(queryDto.getName(), dupKeys));


        List<String> hasAggFieldAnn = keyNames.stream().filter(keyName -> hasAggFieldAnnotation(queryDto, keyName)).toList();
        if(hasAggFieldAnn != null || hasAggFieldAnn.isEmpty())
            throw new BeanDefinitionValidationException("[ERROR] group-by fields must not declare @AggField annotation : %s");
    }



    private void validateAggField(Class<?> queryDto) {

        if(queryDto.isRecord()) {
            List<RecordComponent> aggFields = getAggFields(queryDto.getRecordComponents());
            if(aggFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @AggField".
                        formatted(queryDto.getName()));

            List<String> fieldsName = aggFields.stream().map(rc -> {
                AggField annotation = rc.getDeclaredAnnotation(AggField.class);
                String fieldName = annotation.fieldName();
                return fieldName.isEmpty() ? rc.getName() : fieldName;
            }).toList();


            List<String> dupFields = findDuplicates(fieldsName);
            if(!dupFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class has duplicate AggField name : %s".
                        formatted(queryDto.getName(), dupFields));


            aggFields.stream().forEach(rc -> {
                AggField annotation = rc.getDeclaredAnnotation(AggField.class);
                validateAggFieldAnnotationValueType(annotation, rc.getType());
            });
        }else {
            List<Field> aggFields = getAggFields(queryDto.getDeclaredFields());
            if(aggFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class does not declare any @AggField".
                        formatted(queryDto.getName()));

            List<String> fieldsName = aggFields.stream().map(field -> {
                AggField annotation = field.getDeclaredAnnotation(AggField.class);
                String fieldName = annotation.fieldName();
                return fieldName.isEmpty() ? field.getName() : fieldName;
            }).toList();

            List<String> dupFields = findDuplicates(fieldsName);
            if(!dupFields.isEmpty())
                throw new BeanDefinitionValidationException("[ERROR] %s class has duplicate AggField name : %s".
                        formatted(queryDto.getName(), dupFields));

            aggFields.stream().forEach(field -> {
                AggField annotation = field.getDeclaredAnnotation(AggField.class);
                validateAggFieldAnnotationValueType(annotation, field.getType());
            });
        }
    }



    private Set<String> getDeclaredFields(Class<?> clazz) {
        Set<String> names = new HashSet<>();

        if(clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                names.add(rc.getName());
            }
        }

        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if(!Modifier.isStatic(modifiers))
                names.add(field.getName());
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
        }else if(valueType == ValueType.COUNT) {
            if(fieldType != Long.class && fieldType!= Integer.class)
                throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                        formatted(valueType.name(), fieldType.getName()));
        }else {
            throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                    formatted(valueType.name(), fieldType.getName()));
        }
    }
}