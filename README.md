# 🚧 Maintenance SDK (Android)

A centralized maintenance handling SDK using Firebase Remote Config.
Supports warning alerts and blocking dialogs across multiple apps.


## 📦 Installation

### Step 1: Add JitPack

Add in your **settings.gradle**:
```gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```


### Step 2: Add Dependency
Add in your **module: build.gradle**:
```gradle
dependencies {
    implementation 'com.github.AppDeveloperAndroid:maintenance-sdk:1.0.3'
    implementation 'com.google.firebase:firebase-config'
    implementation 'com.google.firebase:firebase-analytics'
}
apply plugin: 'com.google.gms.google-services'
```

Add in your **project: build.gradle**:
```gradle
dependencies {
    classpath 'com.google.gms:google-services:4.3.15'
}
```

### Step 3: Application class
```java
FirebaseApp.initializeApp(this);
if (FirebaseApp.getApps(this).isEmpty()) {
    throw new RuntimeException("Firebase not initialized");
}
```      

### Step 4: Main Activity
```java
MaintenanceManager.init(getApplication(), "mgl_wah");
MaintenanceManager.forceCheck();
```

## ⚠️ Requirement

* Minimum supported SDK: **API 23**
* Requires internet connection
* Firebase Remote Config must be configured


