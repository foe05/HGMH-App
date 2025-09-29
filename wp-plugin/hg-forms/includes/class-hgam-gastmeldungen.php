<?php
/**
 * HGAM Gastmeldungen API
 * Handles guest submissions and OCR integration
 */

if (!defined('ABSPATH')) {
    exit;
}

class HGAM_Gastmeldungen {
    
    private $ocr;
    
    public function __construct() {
        $this->ocr = new HGAM_OCR();
    }
    
    /**
     * Submit guest report
     */
    public function submit_gastmeldung($data) {
        // Validate required fields
        $required_fields = array('melder_name', 'melder_email', 'wus_nummer', 'wildart', 'fundort', 'datum');
        foreach ($required_fields as $field) {
            if (empty($data[$field])) {
                return new WP_Error('missing_field', "Feld '$field' ist erforderlich", array('status' => 400));
            }
        }
        
        // Validate email
        if (!is_email($data['melder_email'])) {
            return new WP_Error('invalid_email', 'Ungültige E-Mail-Adresse', array('status' => 400));
        }
        
        // Validate WUS number format
        if (!preg_match('/^\d{7}$/', $data['wus_nummer'])) {
            return new WP_Error('invalid_wus', 'WUS-Nummer muss 7-stellig sein', array('status' => 400));
        }
        
        // Store gastmeldung
        $gastmeldung_id = $this->store_gastmeldung($data);
        
        if (is_wp_error($gastmeldung_id)) {
            return $gastmeldung_id;
        }
        
        // Send notification to obmänner
        $this->notify_obmaenner($data, $gastmeldung_id);
        
        return array(
            'success' => true,
            'message' => 'Gastmeldung erfolgreich übermittelt',
            'gastmeldung_id' => $gastmeldung_id
        );
    }
    
    /**
     * Store gastmeldung in database
     */
    private function store_gastmeldung($data) {
        global $wpdb;
        $gastmeldungen_table = $wpdb->prefix . 'hgam_gastmeldungen';
        
        // Check if table exists
        if ($wpdb->get_var("SHOW TABLES LIKE '$gastmeldungen_table'") != $gastmeldungen_table) {
            $this->create_gastmeldungen_table();
        }
        
        $insert_data = array(
            'melder_name' => sanitize_text_field($data['melder_name']),
            'melder_email' => sanitize_email($data['melder_email']),
            'melder_telefon' => sanitize_text_field($data['melder_telefon'] ?? ''),
            'wus_nummer' => sanitize_text_field($data['wus_nummer']),
            'wildart' => sanitize_text_field($data['wildart']),
            'fundort' => sanitize_text_field($data['fundort']),
            'datum' => sanitize_text_field($data['datum']),
            'bemerkungen' => sanitize_textarea_field($data['bemerkungen'] ?? ''),
            'ocr_data' => !empty($data['ocr_data']) ? json_encode($data['ocr_data']) : null,
            'status' => 'pending',
            'created_at' => current_time('mysql')
        );
        
        $result = $wpdb->insert($gastmeldungen_table, $insert_data);
        
        if ($result === false) {
            return new WP_Error('database_error', 'Fehler beim Speichern der Gastmeldung', array('status' => 500));
        }
        
        return $wpdb->insert_id;
    }
    
    /**
     * Notify obmänner about new gastmeldung
     */
    private function notify_obmaenner($data, $gastmeldung_id) {
        $obmaenner = get_users(array('role' => 'pr25_obmann'));
        
        foreach ($obmaenner as $obmann) {
            $subject = 'Neue Gastmeldung: ' . $data['wildart'] . ' (WUS: ' . $data['wus_nummer'] . ')';
            $message = $this->format_gastmeldung_email($data, $gastmeldung_id);
            
            wp_mail($obmann->user_email, $subject, $message);
            
            // Also send push notification if FCM token exists
            $fcm_token = get_user_meta($obmann->ID, 'fcm_token', true);
            if (!empty($fcm_token)) {
                $notifications = new HGAM_Notifications();
                $notifications->send_notification(
                    $obmann->ID,
                    'Neue Gastmeldung',
                    $data['wildart'] . ' von ' . $data['melder_name'],
                    'gastmeldung',
                    'gastmeldung:' . $gastmeldung_id
                );
            }
        }
    }
    
    /**
     * Format gastmeldung email
     */
    private function format_gastmeldung_email($data, $gastmeldung_id) {
        $message = "Neue Gastmeldung eingegangen:\n\n";
        $message .= "ID: " . $gastmeldung_id . "\n";
        $message .= "Melder: " . $data['melder_name'] . "\n";
        $message .= "E-Mail: " . $data['melder_email'] . "\n";
        $message .= "Telefon: " . ($data['melder_telefon'] ?? 'Nicht angegeben') . "\n";
        $message .= "WUS-Nummer: " . $data['wus_nummer'] . "\n";
        $message .= "Wildart: " . $data['wildart'] . "\n";
        $message .= "Fundort: " . $data['fundort'] . "\n";
        $message .= "Datum: " . $data['datum'] . "\n";
        $message .= "Bemerkungen: " . ($data['bemerkungen'] ?? 'Keine') . "\n";
        
        if (!empty($data['ocr_data'])) {
            $message .= "\nOCR-Daten:\n";
            $message .= "Vertrauen: " . ($data['ocr_data']['confidence'] ?? 'N/A') . "\n";
            $message .= "Roh-Text: " . ($data['ocr_data']['raw_text'] ?? 'N/A') . "\n";
        }
        
        $message .= "\nBitte prüfen Sie die Meldung im WordPress-Admin.";
        
        return $message;
    }
    
    /**
     * Create gastmeldungen table
     */
    private function create_gastmeldungen_table() {
        global $wpdb;
        
        $table_name = $wpdb->prefix . 'hgam_gastmeldungen';
        
        $charset_collate = $wpdb->get_charset_collate();
        
        $sql = "CREATE TABLE $table_name (
            id mediumint(9) NOT NULL AUTO_INCREMENT,
            melder_name varchar(255) NOT NULL,
            melder_email varchar(255) NOT NULL,
            melder_telefon varchar(50),
            wus_nummer varchar(7) NOT NULL,
            wildart varchar(100) NOT NULL,
            fundort varchar(255) NOT NULL,
            datum date NOT NULL,
            bemerkungen text,
            ocr_data longtext,
            status varchar(50) DEFAULT 'pending',
            created_at datetime DEFAULT CURRENT_TIMESTAMP,
            updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            PRIMARY KEY (id),
            KEY wus_nummer (wus_nummer),
            KEY status (status),
            KEY created_at (created_at)
        ) $charset_collate;";
        
        require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
        dbDelta($sql);
    }
    
    /**
     * Get gastmeldungen for admin
     */
    public function get_gastmeldungen($status = null, $limit = 50) {
        global $wpdb;
        $gastmeldungen_table = $wpdb->prefix . 'hgam_gastmeldungen';
        
        $query = "SELECT * FROM $gastmeldungen_table WHERE 1=1";
        $params = array();
        
        if ($status) {
            $query .= " AND status = %s";
            $params[] = $status;
        }
        
        $query .= " ORDER BY created_at DESC LIMIT %d";
        $params[] = $limit;
        
        $results = $wpdb->get_results($wpdb->prepare($query, $params));
        
        $gastmeldungen = array();
        foreach ($results as $row) {
            $gastmeldungen[] = array(
                'id' => (int) $row->id,
                'melder_name' => $row->melder_name,
                'melder_email' => $row->melder_email,
                'melder_telefon' => $row->melder_telefon,
                'wus_nummer' => $row->wus_nummer,
                'wildart' => $row->wildart,
                'fundort' => $row->fundort,
                'datum' => $row->datum,
                'bemerkungen' => $row->bemerkungen,
                'ocr_data' => $row->ocr_data ? json_decode($row->ocr_data, true) : null,
                'status' => $row->status,
                'created_at' => $row->created_at,
                'updated_at' => $row->updated_at
            );
        }
        
        return $gastmeldungen;
    }
}
