1) Adicionar o repositório Maven e a dependência (build.gradle):
```java
repositories {
    maven {
        url 'https://dl.bintray.com/sambatech/maven'
    }
}
...
dependencies {
    compile 'com.sambatech.player:sdk-android:0.2.0-beta'
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
4) (opcional) Adicionar listeners:
```java
SambaEventBus.subscribe(new SambaPlayerListener() {
	@Override
	public void onLoad(SambaEvent e) {...}

	@Override
	public void onPlay(SambaEvent e) {...}

	@Override
	public void onPause(SambaEvent e) {...}

	@Override
	public void onStop(SambaEvent e) {...}

	@Override
	public void onFinish(SambaEvent e) {...}

	@Override
	public void onFullscreen(SambaEvent e) {...}

	@Override
	public void onFullscreenExit(SambaEvent e) {...}
});
```
5) Recuperar a instância do SambaPlayer, efetuar requisição da mídia, aguardar resposta da API (via callback) e reproduzir:
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
5.1) (opcional) É possível pré-carregar os dados das mídias utilizando uma sobrecarga do método `requestMedia`, entretanto deve-se considerar o fator "demora ao abrir aplicativo X melhor performance ao reproduzir". Para isso, basta recuperar a instância do SambaPlayer, efetuar requisição de N mídias, aguardar resposta da API (via callback), exibi-las em um View (e.g. ListView) e reproduzir:
```java
player = (SambaPlayer)findViewById(R.id.samba_player);
SambaApi api = new SambaApi(this);
api.requestMedia(new SambaMediaRequest[] {
	new SambaMediaRequest("986e07f70986265468eae1377424d171", "a7dd940fb617b7af746da3ed42c019e5"),
	new SambaMediaRequest("dc6d5bfa19c79d8f7903db43024bea3e", "ac1309d58f045e11375d9190dd055699"),
	...
}, new SambaApiCallback() {
	@Override
	public void onMediaListResponse(SambaMedia[] mediaList) {
		// adicionar a um ListView/ArrayAdapter, etc.
		ListView list = (ListView)findViewById(R.id.list);

		list.setAdapter(new ArrayAdapter<SambaMedia>(getContext(), 0, mediaList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_list_item, parent, false);

				SambaMedia media = this.getItem(position);
				// preencher view do item da lista
				...

				return convertView;
			}
		});

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				player.setMedia((SambaMedia)parent.getAdapter().getItem(position));
				player.play();
			}
		});
	}
});
```
5.2) (opcional-beta) Recuperar a instância do SambaPlayer, prover os dados da mídia e reproduzir:
```java
player = (SambaPlayer)findViewById(R.id.samba_player);
SambaMedia media = new SambaMedia();
media.url = "http://url_da_midia...";
media.title = "Título da mídia";
media.theme = 0xFF996633;
...
player.setMedia(media);
player.play();
```