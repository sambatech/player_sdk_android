# SambaPlayer SDK (Android)

## Como instalar em sua app:

1) Adicionar o repositório Maven e a dependência (build.gradle):
```java
repositories {
    maven {
        url 'http://arch2.sambatech.com.br:8081/nexus/content/repositories/sambaplayer-android-sdk'
    }
}
...
dependencies {
    compile 'com.sambatech.player:sdk-android:1.0.0-alpha'
}
```

2) Instanciar o SambaPlayer (*interface* de SambaPlayerView) em um View:
```xml
<com.sambatech.player.SambaPlayerView
    android:id="@+id/samba_player"/>
```

3) Habilitar permissão para internet (AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

Para maiores informações, favor consultar nossa página [Wiki](https://github.com/sambatech/player_androidsdk/wiki).
Para informações sobre o JavaDoc favor consultar a nossa página no [SambaDev][http://dev.sambatech.com/documentation/androidsdk/index.html]
