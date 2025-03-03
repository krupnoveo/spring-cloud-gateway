import org.gradle.internal.declarativedsl.parsing.main

plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.krupnoveo.edu"
version = "0.0.1-SNAPSHOT"

springBoot {
    mainClass.set("ru.krupnoveo.edu.gateway.GatewayApplication")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.0"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.projectlombok:lombok:1.18.36")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.113.Final")
    implementation("io.netty:netty-all:4.1.115.Final")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
