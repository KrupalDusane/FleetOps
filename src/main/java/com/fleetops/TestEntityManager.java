package com.fleetops;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;

@Component
public class TestEntityManager implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== ENTITIES FOUND ===");
        for (EntityType<?> entity : em.getMetamodel().getEntities()) {
            System.out.println(entity.getName() + " -> " + entity.getJavaType().getName());
        }
        System.out.println("======================");
    }
}
