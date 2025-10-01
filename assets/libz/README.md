# LibZ
LibZ is a library for a few mods made by Globox_Z.

### Installation
LibZ is a library built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately; all other dependencies are installed with the mod.

### License
LibZ is licensed under MIT.

### For Mod Developers
Bring in the library as a dependency:

Be sure to add a maven like the modrinth maven to your `build.gradle`:
```groovy
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    modApi ("maven.modrinth:libz:${libz_version}") {
		exclude(group: "net.fabricmc.fabric-api")
	}
}
```

Set the required version for libz in the `gradle.properties`:
```
    libz_version=...
```

For the required version check out the [versions](https://modrinth.com/mod/libz/versions) tab on Modrinth.