# HGAM Mobile App

Eine moderne Android-App für das Wildtierbestand-Management von Hegegemeinschaften mit direkter WordPress-Integration.

## 🚀 Features

- **Wildtier-Erfassung** mit vollständiger Validierung
- **OCR-Scanner** für Wildursprungsscheine mit ML Kit
- **Gastformular** für externe Meldungen
- **Push-Notifications** mit Firebase
- **Export-Funktionen** für Obmänner (CSV/PDF)
- **WordPress-Integration** mit vollständiger REST API
- **Rollenbasierte Berechtigungen** (Nutzer vs. Obmänner)
- **Moderne UI** mit Material Design 3

## 📱 APK Download

### GitHub Actions (Empfohlen)
1. Gehe zu [Actions](https://github.com/dein-username/HGMH-App/actions)
2. Wähle den neuesten erfolgreichen Build
3. Lade das `app-debug` oder `app-release` APK herunter

### Manueller Build
```bash
git clone https://github.com/dein-username/HGMH-App.git
cd HGMH-App/android-app
./gradlew assembleDebug
```

## 🛠️ Entwicklung

### Voraussetzungen
- Android Studio Arctic Fox oder neuer
- JDK 17
- Android SDK API 24+

### Setup
1. Repository klonen
2. Android Studio öffnen
3. Projekt importieren (`android-app` Ordner)
4. Gradle Sync abwarten
5. App auf Gerät/Emulator installieren

### Build-Befehle
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Tests ausführen
./gradlew test

# Lint-Checks
./gradlew lintDebug
```

## 🌐 WordPress Plugin

Das WordPress Plugin befindet sich im `wp-plugin` Ordner und bietet:
- REST API für mobile App
- Session-basierte Authentifizierung
- OCR-Service Integration
- Push-Notification Support
- Export-Funktionen

### Installation
1. Plugin in WordPress hochladen
2. Aktivieren
3. API-Endpunkte sind unter `/wp-json/hgam/v1/` verfügbar

## 📋 Workflows

### Vollständiger Build & Test
- Läuft bei Push auf `main`/`develop`
- Code Quality Checks
- Unit Tests
- Instrumented Tests
- Security Scan
- APK Upload

### Quick Build
- Manueller Trigger über GitHub Actions
- Schneller Build ohne Tests
- Ideal für schnelle APK-Generierung

### Release
- Automatisch bei GitHub Release
- Erstellt signierte APK
- Upload zu GitHub Release

## 🔧 Konfiguration

### Firebase Setup
1. Firebase-Projekt erstellen
2. `google-services.json` in `android-app/app/` platzieren
3. Push-Notifications konfigurieren

### WordPress API
- Base URL in `NetworkModule.kt` anpassen
- API-Endpunkte in `HGAMApiService.kt` konfigurieren

## 📄 Lizenz

GPL v2 oder später

## 🤝 Support

Bei Fragen oder Problemen:
- GitHub Issues erstellen
- Dokumentation prüfen
- Code-Review durchführen

---

**Entwickelt für:** Deutsche Hegegemeinschaften  
**Technologie:** Kotlin, Jetpack Compose, WordPress REST API  
**Status:** Produktionsbereit ✅
