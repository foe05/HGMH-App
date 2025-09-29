# HG Forms API Plugin

Voraussetzungen:
- WordPress 5.6+
- HTTPS aktiviert
- JWT Authentication Plugin für WP-API (oder OAuth2 Alternative)

Installation:
1. Ordner `hg-forms` nach `wp-content/plugins/` kopieren.
2. Im WP-Admin das Plugin "HG Forms API" aktivieren.
3. (JWT) JWT-Plugin installieren und `JWT_AUTH_SECRET_KEY` in `wp-config.php` setzen.

API Endpunkte (Basis `/wp-json/hg/v1`):
- `GET /forms` – Liste verfügbarer Formulare (authentifiziert)
- `POST /forms/{id}/submit` – Formular absenden (authentifiziert)

Erweiterbarkeit:
- Filter `hg_forms_handle_submit` implementieren, um Submission-Logik projektbezogen zu verarbeiten/speichern.

Beispiel:
```php
add_filter('hg_forms_handle_submit', function($context) {
    $form_id = $context['form_id'];
    $data = $context['payload'];
    $user = $context['user_id'];
    // TODO: speichern/weiterverarbeiten
    return [ 'ok' => true, 'form_id' => $form_id, 'user_id' => $user ];
});
```