//package com.api.multitenancy;
//
//import javax.sql.DataSource;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//@EnableJpaRepositories(
//    basePackages = "com.api.user",
//    entityManagerFactoryRef = "entityManagerFactory", // ✅ Sin prefijo
//    transactionManagerRef = "transactionManager"      // ✅ Sin prefijo
//)
//public class MultitenancyConfig {
//
//    @Primary
//    @Bean(name = "dataSource")
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Primary
//    @Bean(name = "entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("dataSource") DataSource dataSource) {
//        
//        return builder
//                .dataSource(dataSource)
//                .packages("com.api.user") // Paquete donde está User.java
//                .persistenceUnit("default")
//                .build();
//    }
//
//    @Primary
//    @Bean(name = "transactionManager")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("entityManagerFactory") 
//            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
//        
//        return new JpaTransactionManager(entityManagerFactory.getObject());
//    }
//}