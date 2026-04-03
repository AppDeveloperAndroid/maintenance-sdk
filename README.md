# 🚧 Maintenance SDK (Android)

A centralized maintenance handling SDK using Firebase Remote Config.
Supports warning alerts and blocking dialogs across multiple apps.


## 📦 Installation

### Step 1: Add JitPack

Add in your **settings.gradle**:

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}


### Step 2: Add Dependency

implementation 'com.github.AppDeveloperAndroid:maintenance-sdk:1.0.3'


**Application class**:

FirebaseApp.initializeApp(this);
if (FirebaseApp.getApps(this).isEmpty()) {
      throw new RuntimeException("Firebase not initialized");
}
        

**Main Activity**

MaintenanceManager.init(getApplication(), "mgl_wah");
MaintenanceManager.forceCheck();


**Requirement**

* Minimum supported SDK: **API 23**
* Requires internet connection
* Firebase Remote Config must be configured


