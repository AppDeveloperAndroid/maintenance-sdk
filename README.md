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
}
``` 

### Step 3: Base Activity or Main Activity
```java
MaintenanceManager.init(getApplication(), AppId);
MaintenanceManager.forceCheck();
```

## ⚠️ Requirement

* Minimum supported SDK: **API 23**


