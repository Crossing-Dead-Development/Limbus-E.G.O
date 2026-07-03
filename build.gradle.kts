plugins {
    id("java")
}
group = "me.yisang"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper", "paper-api", "1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
