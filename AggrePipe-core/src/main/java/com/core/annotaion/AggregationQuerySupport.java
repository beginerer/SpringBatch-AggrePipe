package com.core.annotaion;


import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import java.lang.reflect.Field;
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


        BeanDefinitionBuilder b = BeanDefinitionBuilder.rootBeanDefinition(AggQueryBindingHandler.class)
                .addConstructorArgValue(bases);

        BeanDefinitionReaderUtils.registerWithGeneratedName(b.getBeanDefinition(), registry);
    }



    private Map<Class<?>, AggMeta> scanAndBuildMetadata(List<String> bases) {
        var out = new LinkedHashMap<Class<?>, AggMeta>();
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AggQuery.class, true));
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        for(String base : bases) {
            scanner.findCandidateComponents(base).forEach(bd -> {
                Class<?> dto = ClassUtils.resolveClassName(Objects.requireNonNull(bd.getBeanClassName()), cl);
                Set<String> fieldNames = getDeclaredFields(dto);

            });
        }
    }


    private void validateAnnotation(Class<?> queryDto, Set<String> filedNames) {

        validateGroupByKeys(queryDto, filedNames);
        validateAggField(queryDto, filedNames);

    }



    private void validateGroupByKeys(Class<?> queryDto, Set<String> filedNames) {
        AggQuery ann = queryDto.getAnnotation(AggQuery.class);
        GroupByKey[] keys = ann.groupByKeys();


        List<String> keyNames = Arrays.stream(keys).map(GroupByKey::field).toList();

        List<String> missing = keyNames.stream().filter(k -> !filedNames.contains(k)).toList();

        if(!missing.isEmpty())
            throw new BeanDefinitionStoreException("[ERROR] %s class is missing group-by fields: %s".
                    formatted(queryDto.getName(), missing));


        keyNames.forEach(key -> );




    }


    private void validateAggField(Class<?> queryDto, Set<String> fieldNames) {

    }


    // 1. group by에 포함된 요소는, 필드에 정의되어있어야함
    // 2. group by에 포함되는 요소는, @AggField가 선언되면 안됨
    // 3. @AggFiled는 커스텀 이름을 정의할 수있는데, 이런 경우에는 어덯게 처리할건지
    private Set<String> getDeclaredFields(Class<?> clazz) {
        Set<String> names = new HashSet<>();

        if(clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                names.add(rc.getName());
            }
        }

        for (Field field : clazz.getDeclaredFields()) {
            names.add(field.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        return names;
    }



}
