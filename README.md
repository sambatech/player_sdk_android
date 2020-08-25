## Features

- Reprodução vídeos VOD PROGRESSIVE e HLS
- SambaPlayer API
- Suporte ao Samba Player Analytics
- Suporte a advertising DFP
- Player nativo do Android
- Download de videos para assistir offline

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
    compile 'com.sambatech.player:sdk-android:v0.13.0-beta'
    //compile 'com.sambatech.player:sdk-android:0.+' // para utilizar a versão mais atual
}
```
_Para verificar todas as versões disponíveis, favor consultar nossa página de [releases](https://github.com/sambatech/player_sdk_android/releases)._

2) Instanciar o SambaPlayer em um View:
```xml
<com.sambatech.player.SambaPlayer
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

Para maiores informações, favor consultar nossa página [Wiki](https://github.com/sambatech/player_sdk_android/wiki).

Para informações sobre o JavaDoc favor consultar a nossa página no [SambaDev](http://dev.sambatech.com/documentation/androidsdk/index.html)

## Deploy

1) Atualizar `versionName` no arquivo `sambaplayersdk/build.gradle` subindo a versão.

2) Após o merge em master, gerar uma release com a nova versão

3) Reexecutar o workflow de deploy para publicar no Bintray

**OBS:** É necessário reexecutar o workflow porque a geração da release e publicação no GitHub não está automatizada circleci.

#### Atenção :warning:

O JFrog Bintray permite a inclusão de novas releases dentro de uma versão somente no período de 1 ano. Após este prazo, é necessário criar uma nova versão e atualizar no arquivo `assets/publish.sh`.

Exemplo: Ao tentar publicar na versão **beta3** o seguinte erro é apresentado:

```bash
Info] Verifying repository maven exists...
[Info] Verifying package sdk-android exists...
[Info] Collecting files for upload...
[Info] [Thread 1] Uploading artifact: sdk-android-0.14.5-beta.pom
[Info] [Thread 0] Uploading artifact: sdk-android-0.14.5-beta.aar
[Error] [Thread 1] Bintray response: 403 Forbidden
Forbidden!
[Error] [Thread 0] Bintray response: 403 Forbidden
Forbidden!
[Error] Failed uploading 2 artifacts.
{
  "status": "failure",
  "totals": {
    "success": 0,
    "failure": 2
  }
}

```

Assim, na interface do JFrog Bintray é necessário criar uma nova versão.

**Versão atual:** beta4
**Data de criação:** 11/02/2020
**Data de expiração:** 11/02/2021
