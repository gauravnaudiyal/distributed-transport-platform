package com.global_traffic_bs.journey_compatibility_svc.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Multi-Database Configuration
 * Supports two independent PostgreSQL databases:
 * 1. Primary DB: Local/internal database for route capacity policies and audit logs
 * 2. Booking DB: External Azure PostgreSQL database for booking records
 */
@Configuration
public class MultiDatabaseConfig {

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String ddlAuto;

    /**
     * PRIMARY DATABASE CONFIGURATION
     * Used for: RouteCapacity, BookingRecord (legacy), internal audit logs
     */

    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = {"primaryEntityManagerFactory", "entityManagerFactory"})
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.global_traffic_bs.journey_compatibility_svc.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = {"primaryTransactionManager", "transactionManager"})
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * BOOKING DATABASE CONFIGURATION
     * Used for: Booking entity (external Azure PostgreSQL)
     * Connection details from environment variables:
     *   - BOOKING_DB_URL: bkdemo123-postgres.postgres.database.azure.com
     *   - BOOKING_DB_USERNAME: bookingadmin
     *   - BOOKING_DB_PASSWORD: (provided at runtime)
     */

    @Bean(name = "bookingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.booking")
    public DataSource bookingDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bookingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bookingEntityManagerFactory(
            @Qualifier("bookingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.global_traffic_bs.journey_compatibility_svc.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPersistenceUnitName("booking");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "bookingTransactionManager")
    public PlatformTransactionManager bookingTransactionManager(
            @Qualifier("bookingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

