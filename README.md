## Features

Features | Suporte
--- | ---
Reprodução vídeos VOD PROGRESSIVE e HLS | x
SambaPlayer API | x
Suporte ao Samba Player Analytics | x
Suporte a advertising DFP | x
Player nativo do Android | x

# SambaPlayer SDK (Android)

Instalação do Java, Android SDK

1) Faça o download e instalação do último [SDK Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

2) Faça o download e instalação do último [Android SDK](http://developer.android.com/sdk/installing/index.html)

## Como rodar a APP:

1) Clone o projeto

2) Rode em seu Android SDK Studio

## Como instalar o SDK:

1) Adicionar o repositório Maven e a dependência (build.gradle):
```java
repositories {
    jcenter()
}
...
dependencies {
    compile 'com.sambatech.player:sdk-android:0.2.1-beta'
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

4) Recuperar a instância do SambaPlayer, efetuar requisição da mídia, aguardar resposta da API (via callback) e reproduzir:
```java
player = (SambaPlayer)findViewById(R.id.samba_player);
SambaApi api = new SambaApi(this);
api.requestMedia(new SambaMediaRequest("34f07cf52fd85ccfc41a39bcf499e83b", "0632f26a442ba9ba3bb9067a45e239e2"), new SambaApiCallback() {
	@Override
	public void onMediaResponse(SambaMedia media) {
		player.setMedia(media);
		player.play();
	}
});
```

Para maiores informações, favor consultar nossa página [Wiki](https://github.com/sambatech/player_androidsdk/wiki).

Para informações sobre o JavaDoc favor consultar a nossa página no [SambaDev](http://dev.sambatech.com/documentation/androidsdk/index.html)
