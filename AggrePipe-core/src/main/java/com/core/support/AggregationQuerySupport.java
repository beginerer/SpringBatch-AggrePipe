package com.core.support;


import com.core.AggQueryMetadata;
import com.core.ItemSpec;
import com.core.annotaion.*;
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


    private AnnotationAttributes attrs;

    private Map<Class<?>, AggQueryMetadata> metadataMap;





    @Override
    public void setImportMetadata(AnnotationMetadata metadata) {
        this.attrs = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableAggQuery.class.getName()));

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

        this.metadataMap = scanAndBuildMetadata(bases);
    }


    @Bean
    public AggQueryRegistry aggQueryRegistry() {
        return new AggQueryRegistry(metadataMap);
    }


    @Bean
    public AggQueryBindingHandler aggQueryBindingHandler(AggQueryRegistry registry) {
        return new AggQueryBindingHandler(registry);
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
            List<ItemSpec> itemSpecs = new ArrayList<>();

            for (RecordComponent aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                ItemSpec itemSpec = new ItemSpec(aggField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);
            String className = ann.name().isBlank() ? clazz.getName() : ann.name();

            return new AggQueryMetadata(className, ann.groupByKeys(), true, itemSpecs);
        }else {
            List<Field> aggFields = getAggFields(clazz.getDeclaredFields());
            List<ItemSpec> itemSpecs = new ArrayList<>();

            for (Field aggField : aggFields) {
                AggField annotation = aggField.getDeclaredAnnotation(AggField.class);
                ItemSpec itemSpec = new ItemSpec(aggField.getName(), annotation.op(), annotation.type());
                itemSpecs.add(itemSpec);
            }
            AggQuery ann = clazz.getDeclaredAnnotation(AggQuery.class);
            String className = ann.name().isBlank() ? clazz.getName() : ann.name();

            return new AggQueryMetadata(className, ann.groupByKeys(), false, itemSpecs);
        }
    }


    private void validateAnnotation(Class<?> queryDto, Set<String> filedNames) {
        validateGroupByKeys(queryDto, filedNames);
        validateAggField(queryDto);
    }



    private void validateGroupByKeys(Class<?> queryClass, Set<String> filedNames) {
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
        } else {
            throw new BeanDefinitionValidationException("[ERROR] Unsupported valueType. valueType=%s, fieldType=%s".
                    formatted(valueType.name(), fieldType.getName()));
        }
    }
}