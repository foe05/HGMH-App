<?php
/**
 * Plugin Name: HG Forms API
 * Description: Stellt REST-API-Endpunkte bereit, um formularbasierte Inhalte aus der App zu listen und zu senden.
 * Version: 0.1.0
 * Author: HG
 */

if (!defined('ABSPATH')) { exit; }

class HG_Forms_API {
    public function __construct() {
        add_action('rest_api_init', [$this, 'register_routes']);
    }

    public function register_routes() {
        register_rest_route('hg/v1', '/forms', [
            'methods' => 'GET',
            'callback' => [$this, 'list_forms'],
            'permission_callback' => function() { return current_user_can('read'); }
        ]);

        register_rest_route('hg/v1', '/forms/(?P<id>[a-zA-Z0-9_-]+)/submit', [
            'methods' => 'POST',
            'callback' => [$this, 'submit_form'],
            'permission_callback' => function() { return is_user_logged_in(); }
        ]);
    }

    public function list_forms(\WP_REST_Request $request) {
        // Placeholder: Integrieren Sie hier Ihre Formularquelle (z. B. Shortcodes, ACF, CF7, GF)
        // Für MVP: liefern wir eine statische Struktur, die pro Seite erweitert werden kann.
        $forms = [
            [
                'id' => 'example_form',
                'title' => 'Beispiel-Formular',
                'fields' => [
                    [ 'key' => 'title', 'label' => 'Titel', 'type' => 'text', 'required' => true ],
                    [ 'key' => 'description', 'label' => 'Beschreibung', 'type' => 'textarea', 'required' => false ],
                ]
            ]
        ];
        return new \WP_REST_Response($forms, 200);
    }

    public function submit_form(\WP_REST_Request $request) {
        $form_id = $request->get_param('id');
        $payload = $request->get_json_params();
        if (!$form_id || !is_array($payload)) {
            return new \WP_Error('invalid_request', 'Ungültige Daten', ['status' => 400]);
        }

        // TODO: Formular-spezifische Verarbeitung je Seite (Hook/Filter)
        $result = apply_filters('hg_forms_handle_submit', [
            'form_id' => $form_id,
            'payload' => $payload,
            'user_id' => get_current_user_id(),
        ]);

        if (is_wp_error($result)) {
            return $result;
        }

        // Beispielhafte Antwort
        return new \WP_REST_Response([
            'status' => 'ok',
            'created_at' => current_time('mysql'),
            'form_id' => $form_id,
        ], 200);
    }
}

new HG_Forms_API();

