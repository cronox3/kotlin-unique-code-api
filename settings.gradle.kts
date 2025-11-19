rootProject.name = "hash-api"
```

### **3. `Main.kt`** (el código del artifact de arriba)

### **4. `Procfile`** (para Railway)
```
web: ./gradlew run
```

### **5. `.gitignore`**
```
.gradle/
        build/
*.class
*.jar
    .idea/
```

## Estructura final:
```
tu-proyecto/
├── Main.kt
├── build.gradle.kts
├── settings.gradle.kts
├── Procfile
└── .gitignore