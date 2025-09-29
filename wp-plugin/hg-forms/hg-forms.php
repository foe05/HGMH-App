<?php
/**
 * Plugin Name: HGAM Mobile API
 * Description: REST-API-Endpunkte für die HGAM Mobile App - Wildtierbestand-Management
 * Version: 1.0.0
 * Author: HG
 * Requires at least: 5.0
 * Tested up to: 6.4
 * Requires PHP: 7.4
 * License: GPL v2 or later
 */

if (!defined('ABSPATH')) {
    exit;
}

// Plugin constants
define('HGAM_API_VERSION', '1.0.0');
define('HGAM_API_NAMESPACE', 'hgam/v1');

// Include required files
require_once plugin_dir_path(__FILE__) . 'includes/class-hgam-stammdaten.php';
require_once plugin_dir_path(__FILE__) . 'includes/class-hgam-erfassungen.php';
require_once plugin_dir_path(__FILE__) . 'includes/class-hgam-ocr.php';
require_once plugin_dir_path(__FILE__) . 'includes/class-hgam-notifications.php';
require_once plugin_dir_path(__FILE__) . 'includes/class-hgam-gastmeldungen.php';

class HGAM_Mobile_API {
    
    public function __construct() {
        add_action('rest_api_init', array($this, 'register_routes'));
        add_action('init', array($this, 'init'));
    }
    
    public function init() {
        // Initialize plugin
        new HGAM_Stammdaten();
        new HGAM_Erfassungen();
        new HGAM_OCR();
        new HGAM_Notifications();
        new HGAM_Gastmeldungen();
    }

    public function register_routes() {
        // Authentication routes
        register_rest_route(HGAM_API_NAMESPACE, '/auth/login', array(
            'methods' => 'POST',
            'callback' => array($this, 'login'),
            'permission_callback' => '__return_true'
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/auth/logout', array(
            'methods' => 'POST',
            'callback' => array($this, 'logout'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/auth/status', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_auth_status'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        // Stammdaten routes
        register_rest_route(HGAM_API_NAMESPACE, '/wildarten', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_wildarten'),
            'permission_callback' => '__return_true'
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/kategorien', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_kategorien'),
            'permission_callback' => '__return_true'
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/jagdgebiete', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_jagdgebiete'),
            'permission_callback' => '__return_true'
        ));
        
        // Erfassungen routes
        register_rest_route(HGAM_API_NAMESPACE, '/erfassungen', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_erfassungen'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/erfassungen/(?P<id>\d+)', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_erfassung'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/erfassungen', array(
            'methods' => 'POST',
            'callback' => array($this, 'create_erfassung'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/erfassungen/(?P<id>\d+)', array(
            'methods' => 'PUT',
            'callback' => array($this, 'update_erfassung'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/erfassungen/(?P<id>\d+)', array(
            'methods' => 'DELETE',
            'callback' => array($this, 'delete_erfassung'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        // Gastmeldung route
        register_rest_route(HGAM_API_NAMESPACE, '/gastmeldung', array(
            'methods' => 'POST',
            'callback' => array($this, 'submit_gastmeldung'),
            'permission_callback' => '__return_true'
        ));
        
        // OCR route
        register_rest_route(HGAM_API_NAMESPACE, '/ocr/analyze', array(
            'methods' => 'POST',
            'callback' => array($this, 'analyze_ocr'),
            'permission_callback' => '__return_true'
        ));
        
        // Notifications routes
        register_rest_route(HGAM_API_NAMESPACE, '/notifications/register', array(
            'methods' => 'POST',
            'callback' => array($this, 'register_notification'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/notifications/history', array(
            'methods' => 'GET',
            'callback' => array($this, 'get_notification_history'),
            'permission_callback' => array($this, 'check_auth_permission')
        ));
        
        // Export routes (nur für Obmänner)
        register_rest_route(HGAM_API_NAMESPACE, '/export/csv', array(
            'methods' => 'GET',
            'callback' => array($this, 'export_csv'),
            'permission_callback' => array($this, 'check_obmann_permission')
        ));
        
        register_rest_route(HGAM_API_NAMESPACE, '/export/pdf', array(
            'methods' => 'GET',
            'callback' => array($this, 'export_pdf'),
            'permission_callback' => array($this, 'check_obmann_permission')
        ));
    }
    
    // Authentication callbacks
    public function login($request) {
        $username = sanitize_text_field($request->get_param('username'));
        $password = $request->get_param('password');
        
        $user = wp_authenticate($username, $password);
        
        if (is_wp_error($user)) {
            return new WP_Error('login_failed', 'Ungültige Anmeldedaten', array('status' => 401));
        }
        
        // Create session token
        $session_token = wp_generate_password(32, false);
        
        // Store session (simplified - in production use proper session management)
        set_transient('hgam_session_' . $session_token, $user->ID, DAY_IN_SECONDS);
        
        return array(
            'success' => true,
            'sessionToken' => $session_token,
            'user' => array(
                'id' => $user->ID,
                'username' => $user->user_login,
                'roles' => $user->roles,
                'jagdgebiete' => get_user_meta($user->ID, 'jagdgebiete', true) ?: array(),
                'hegegemeinschaftId' => get_user_meta($user->ID, 'hegegemeinschaft_id', true) ?: 0
            )
        );
    }
    
    public function logout($request) {
        $session_token = $request->get_header('Authorization');
        if ($session_token) {
            delete_transient('hgam_session_' . $session_token);
        }
        
        return array('success' => true);
    }
    
    public function get_auth_status($request) {
        $user = $this->get_current_user_from_request($request);
        
        if (!$user) {
            return array('isAuthenticated' => false, 'user' => null);
        }
        
        return array(
            'isAuthenticated' => true,
            'user' => array(
                'id' => $user->ID,
                'username' => $user->user_login,
                'roles' => $user->roles,
                'jagdgebiete' => get_user_meta($user->ID, 'jagdgebiete', true) ?: array(),
                'hegegemeinschaftId' => get_user_meta($user->ID, 'hegegemeinschaft_id', true) ?: 0
            )
        );
    }
    
    // Permission callbacks
    public function check_auth_permission($request) {
        return $this->get_current_user_from_request($request) !== null;
    }
    
    public function check_obmann_permission($request) {
        $user = $this->get_current_user_from_request($request);
        return $user && in_array('pr25_obmann', $user->roles);
    }
    
    private function get_current_user_from_request($request) {
        $session_token = $request->get_header('Authorization');
        if (!$session_token) {
            return null;
        }
        
        $user_id = get_transient('hgam_session_' . $session_token);
        if (!$user_id) {
            return null;
        }
        
        return get_user_by('id', $user_id);
    }
    
    // API callbacks - implemented with separate classes
    public function get_wildarten($request) {
        $stammdaten = new HGAM_Stammdaten();
        return $stammdaten->get_wildarten();
    }
    
    public function get_kategorien($request) {
        $stammdaten = new HGAM_Stammdaten();
        return $stammdaten->get_kategorien();
    }
    
    public function get_jagdgebiete($request) {
        $stammdaten = new HGAM_Stammdaten();
        $user_id = $this->get_current_user_from_request($request);
        return $stammdaten->get_jagdgebiete($user_id ? $user_id->ID : null);
    }
    
    public function get_erfassungen($request) {
        $erfassungen = new HGAM_Erfassungen();
        $user_id = $this->get_current_user_from_request($request);
        return $erfassungen->get_erfassungen($user_id ? $user_id->ID : null);
    }
    
    public function get_erfassung($request) {
        $erfassungen = new HGAM_Erfassungen();
        $user_id = $this->get_current_user_from_request($request);
        $id = (int) $request->get_param('id');
        return $erfassungen->get_erfassung($id, $user_id ? $user_id->ID : null);
    }
    
    public function create_erfassung($request) {
        $erfassungen = new HGAM_Erfassungen();
        $user_id = $this->get_current_user_from_request($request);
        $data = $request->get_json_params();
        return $erfassungen->create_erfassung($data, $user_id ? $user_id->ID : null);
    }
    
    public function update_erfassung($request) {
        $erfassungen = new HGAM_Erfassungen();
        $user_id = $this->get_current_user_from_request($request);
        $id = (int) $request->get_param('id');
        $data = $request->get_json_params();
        return $erfassungen->update_erfassung($id, $data, $user_id ? $user_id->ID : null);
    }
    
    public function delete_erfassung($request) {
        $erfassungen = new HGAM_Erfassungen();
        $user_id = $this->get_current_user_from_request($request);
        $id = (int) $request->get_param('id');
        return $erfassungen->delete_erfassung($id, $user_id ? $user_id->ID : null);
    }
    
    public function submit_gastmeldung($request) {
        $gastmeldungen = new HGAM_Gastmeldungen();
        $data = $request->get_json_params();
        return $gastmeldungen->submit_gastmeldung($data);
    }
    
    public function analyze_ocr($request) {
        $ocr = new HGAM_OCR();
        $files = $request->get_file_params();
        
        if (empty($files['image'])) {
            return new WP_Error('no_image', 'Kein Bild hochgeladen', array('status' => 400));
        }
        
        $image_data = file_get_contents($files['image']['tmp_name']);
        $ocr_result = $ocr->analyze_image($image_data);
        $processed_result = $ocr->process_ocr_result($ocr_result);
        
        return $processed_result;
    }
    
    public function register_notification($request) {
        $notifications = new HGAM_Notifications();
        $user_id = $this->get_current_user_from_request($request);
        $data = $request->get_json_params();
        
        if (!$user_id) {
            return new WP_Error('unauthorized', 'Nicht autorisiert', array('status' => 401));
        }
        
        return $notifications->register_token(
            $user_id->ID,
            $data['fcm_token'],
            $data['device_id']
        );
    }
    
    public function get_notification_history($request) {
        $notifications = new HGAM_Notifications();
        $user_id = $this->get_current_user_from_request($request);
        
        if (!$user_id) {
            return new WP_Error('unauthorized', 'Nicht autorisiert', array('status' => 401));
        }
        
        return $notifications->get_notification_history($user_id->ID);
    }
    
    public function export_csv($request) {
        // TODO: Implement CSV export
        return new WP_Error('not_implemented', 'CSV Export noch nicht implementiert', array('status' => 501));
    }
    
    public function export_pdf($request) {
        // TODO: Implement PDF export
        return new WP_Error('not_implemented', 'PDF Export noch nicht implementiert', array('status' => 501));
    }
}

// Initialize the plugin
new HGAM_Mobile_API();