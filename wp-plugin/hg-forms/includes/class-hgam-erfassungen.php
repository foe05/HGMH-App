<?php
/**
 * HGAM Erfassungen API
 * Handles CRUD operations for wildlife captures
 */

if (!defined('ABSPATH')) {
    exit;
}

class HGAM_Erfassungen {
    
    private $stammdaten;
    
    public function __construct() {
        $this->stammdaten = new HGAM_Stammdaten();
    }
    
    /**
     * Get all erfassungen for the current user
     */
    public function get_erfassungen($user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        // Check if table exists
        if ($wpdb->get_var("SHOW TABLES LIKE '$erfassungen_table'") != $erfassungen_table) {
            return $this->get_fallback_erfassungen();
        }
        
        // Get user's jagdgebiete for filtering
        $user_jagdgebiete = get_user_meta($user_id, 'jagdgebiete', true);
        $user_roles = wp_get_current_user()->roles;
        
        $query = "
            SELECT e.*, w.name as wildart_name, w.code as wildart_code,
                   k.name as kategorie_name, k.code as kategorie_code,
                   j.name as jagdgebiet_name, j.code as jagdgebiet_code,
                   u.display_name as erfasser_name
            FROM $erfassungen_table e
            LEFT JOIN {$wpdb->prefix}ahgmh_wildart w ON e.wildart_id = w.id
            LEFT JOIN {$wpdb->prefix}ahgmh_kategorie k ON e.kategorie_id = k.id
            LEFT JOIN {$wpdb->prefix}ahgmh_jagdgebiet j ON e.jagdgebiet_id = j.id
            LEFT JOIN {$wpdb->users} u ON e.erfasser_id = u.ID
            WHERE 1=1
        ";
        
        $params = array();
        
        // Filter by user permissions
        if (!in_array('administrator', $user_roles) && !in_array('pr25_obmann', $user_roles)) {
            // Regular users only see their own erfassungen
            $query .= " AND e.erfasser_id = %d";
            $params[] = $user_id;
        } elseif (!empty($user_jagdgebiete)) {
            // Obmänner see erfassungen from their assigned jagdgebiete
            $placeholders = implode(',', array_fill(0, count($user_jagdgebiete), '%d'));
            $query .= " AND e.jagdgebiet_id IN ($placeholders)";
            $params = array_merge($params, $user_jagdgebiete);
        }
        
        $query .= " ORDER BY e.erfassungsdatum DESC, e.created_at DESC";
        
        $results = $wpdb->get_results($wpdb->prepare($query, $params));
        
        $erfassungen = array();
        foreach ($results as $row) {
            $erfassungen[] = array(
                'id' => (int) $row->id,
                'wus_nummer' => $row->wus_nummer,
                'wildart' => array(
                    'id' => (int) $row->wildart_id,
                    'name' => $row->wildart_name ?: 'Unbekannt',
                    'code' => $row->wildart_code ?: ''
                ),
                'kategorie' => array(
                    'id' => (int) $row->kategorie_id,
                    'name' => $row->kategorie_name ?: 'Unbekannt',
                    'code' => $row->kategorie_code ?: ''
                ),
                'jagdgebiet' => array(
                    'id' => (int) $row->jagdgebiet_id,
                    'name' => $row->jagdgebiet_name ?: 'Unbekannt',
                    'code' => $row->jagdgebiet_code ?: ''
                ),
                'erfasser' => $row->erfasser_name ?: 'Unbekannt',
                'erfassungsdatum' => $row->erfassungsdatum,
                'bemerkungen' => $row->bemerkungen ?: '',
                'interne_notiz' => $row->interne_notiz ?: '',
                'created_at' => $row->created_at,
                'updated_at' => $row->updated_at
            );
        }
        
        return $erfassungen;
    }
    
    /**
     * Get single erfassung by ID
     */
    public function get_erfassung($id, $user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        $query = "
            SELECT e.*, w.name as wildart_name, w.code as wildart_code,
                   k.name as kategorie_name, k.code as kategorie_code,
                   j.name as jagdgebiet_name, j.code as jagdgebiet_code,
                   u.display_name as erfasser_name
            FROM $erfassungen_table e
            LEFT JOIN {$wpdb->prefix}ahgmh_wildart w ON e.wildart_id = w.id
            LEFT JOIN {$wpdb->prefix}ahgmh_kategorie k ON e.kategorie_id = k.id
            LEFT JOIN {$wpdb->prefix}ahgmh_jagdgebiet j ON e.jagdgebiet_id = j.id
            LEFT JOIN {$wpdb->users} u ON e.erfasser_id = u.ID
            WHERE e.id = %d
        ";
        
        $row = $wpdb->get_row($wpdb->prepare($query, $id));
        
        if (!$row) {
            return null;
        }
        
        // Check permissions
        $user_roles = wp_get_current_user()->roles;
        if (!in_array('administrator', $user_roles) && 
            !in_array('pr25_obmann', $user_roles) && 
            $row->erfasser_id != $user_id) {
            return null;
        }
        
        return array(
            'id' => (int) $row->id,
            'wus_nummer' => $row->wus_nummer,
            'wildart' => array(
                'id' => (int) $row->wildart_id,
                'name' => $row->wildart_name ?: 'Unbekannt',
                'code' => $row->wildart_code ?: ''
            ),
            'kategorie' => array(
                'id' => (int) $row->kategorie_id,
                'name' => $row->kategorie_name ?: 'Unbekannt',
                'code' => $row->kategorie_code ?: ''
            ),
            'jagdgebiet' => array(
                'id' => (int) $row->jagdgebiet_id,
                'name' => $row->jagdgebiet_name ?: 'Unbekannt',
                'code' => $row->jagdgebiet_code ?: ''
            ),
            'erfasser' => $row->erfasser_name ?: 'Unbekannt',
            'erfassungsdatum' => $row->erfassungsdatum,
            'bemerkungen' => $row->bemerkungen ?: '',
            'interne_notiz' => $row->interne_notiz ?: '',
            'created_at' => $row->created_at,
            'updated_at' => $row->updated_at
        );
    }
    
    /**
     * Create new erfassung
     */
    public function create_erfassung($data, $user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        // Validate WUS number
        $wus_validation = $this->stammdaten->validate_wus_number($data['wus_nummer']);
        if (!$wus_validation['valid']) {
            return new WP_Error('invalid_wus', $wus_validation['message'], array('status' => 400));
        }
        
        // Validate required fields
        $required_fields = array('wus_nummer', 'wildart_id', 'kategorie_id', 'jagdgebiet_id');
        foreach ($required_fields as $field) {
            if (empty($data[$field])) {
                return new WP_Error('missing_field', "Feld '$field' ist erforderlich", array('status' => 400));
            }
        }
        
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        // Check if table exists, create if not
        if ($wpdb->get_var("SHOW TABLES LIKE '$erfassungen_table'") != $erfassungen_table) {
            $this->create_erfassungen_table();
        }
        
        $insert_data = array(
            'wus_nummer' => sanitize_text_field($data['wus_nummer']),
            'wildart_id' => (int) $data['wildart_id'],
            'kategorie_id' => (int) $data['kategorie_id'],
            'jagdgebiet_id' => (int) $data['jagdgebiet_id'],
            'erfasser_id' => $user_id,
            'erfassungsdatum' => current_time('Y-m-d'),
            'bemerkungen' => sanitize_textarea_field($data['bemerkungen'] ?? ''),
            'interne_notiz' => sanitize_textarea_field($data['interne_notiz'] ?? ''),
            'created_at' => current_time('mysql'),
            'updated_at' => current_time('mysql')
        );
        
        $result = $wpdb->insert($erfassungen_table, $insert_data);
        
        if ($result === false) {
            return new WP_Error('database_error', 'Fehler beim Speichern der Erfassung', array('status' => 500));
        }
        
        $erfassung_id = $wpdb->insert_id;
        return $this->get_erfassung($erfassung_id, $user_id);
    }
    
    /**
     * Update existing erfassung
     */
    public function update_erfassung($id, $data, $user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        // Check if erfassung exists and user has permission
        $existing = $this->get_erfassung($id, $user_id);
        if (!$existing) {
            return new WP_Error('not_found', 'Erfassung nicht gefunden', array('status' => 404));
        }
        
        // Validate WUS number if changed
        if (isset($data['wus_nummer']) && $data['wus_nummer'] != $existing['wus_nummer']) {
            $wus_validation = $this->stammdaten->validate_wus_number($data['wus_nummer'], $id);
            if (!$wus_validation['valid']) {
                return new WP_Error('invalid_wus', $wus_validation['message'], array('status' => 400));
            }
        }
        
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        $update_data = array();
        $allowed_fields = array('wus_nummer', 'wildart_id', 'kategorie_id', 'jagdgebiet_id', 'bemerkungen', 'interne_notiz');
        
        foreach ($allowed_fields as $field) {
            if (isset($data[$field])) {
                if (in_array($field, array('bemerkungen', 'interne_notiz'))) {
                    $update_data[$field] = sanitize_textarea_field($data[$field]);
                } else {
                    $update_data[$field] = sanitize_text_field($data[$field]);
                }
            }
        }
        
        if (!empty($update_data)) {
            $update_data['updated_at'] = current_time('mysql');
            
            $result = $wpdb->update(
                $erfassungen_table,
                $update_data,
                array('id' => $id),
                array_fill(0, count($update_data), '%s'),
                array('%d')
            );
            
            if ($result === false) {
                return new WP_Error('database_error', 'Fehler beim Aktualisieren der Erfassung', array('status' => 500));
            }
        }
        
        return $this->get_erfassung($id, $user_id);
    }
    
    /**
     * Delete erfassung
     */
    public function delete_erfassung($id, $user_id = null) {
        if (!$user_id) {
            $user_id = get_current_user_id();
        }
        
        // Check if erfassung exists and user has permission
        $existing = $this->get_erfassung($id, $user_id);
        if (!$existing) {
            return new WP_Error('not_found', 'Erfassung nicht gefunden', array('status' => 404));
        }
        
        global $wpdb;
        $erfassungen_table = $wpdb->prefix . 'ahgmh_erfassungen';
        
        $result = $wpdb->delete($erfassungen_table, array('id' => $id), array('%d'));
        
        if ($result === false) {
            return new WP_Error('database_error', 'Fehler beim Löschen der Erfassung', array('status' => 500));
        }
        
        return array('success' => true);
    }
    
    /**
     * Create erfassungen table if it doesn't exist
     */
    private function create_erfassungen_table() {
        global $wpdb;
        
        $table_name = $wpdb->prefix . 'ahgmh_erfassungen';
        
        $charset_collate = $wpdb->get_charset_collate();
        
        $sql = "CREATE TABLE $table_name (
            id mediumint(9) NOT NULL AUTO_INCREMENT,
            wus_nummer varchar(7) NOT NULL,
            wildart_id int(11) NOT NULL,
            kategorie_id int(11) NOT NULL,
            jagdgebiet_id int(11) NOT NULL,
            erfasser_id bigint(20) NOT NULL,
            erfassungsdatum date NOT NULL,
            bemerkungen text,
            interne_notiz text,
            created_at datetime DEFAULT CURRENT_TIMESTAMP,
            updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            PRIMARY KEY (id),
            UNIQUE KEY wus_nummer (wus_nummer),
            KEY wildart_id (wildart_id),
            KEY kategorie_id (kategorie_id),
            KEY jagdgebiet_id (jagdgebiet_id),
            KEY erfasser_id (erfasser_id),
            KEY erfassungsdatum (erfassungsdatum)
        ) $charset_collate;";
        
        require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
        dbDelta($sql);
    }
    
    /**
     * Fallback data for development
     */
    private function get_fallback_erfassungen() {
        return array(
            array(
                'id' => 1,
                'wus_nummer' => '1234567',
                'wildart' => array('id' => 1, 'name' => 'Rotwild', 'code' => 'RW'),
                'kategorie' => array('id' => 2, 'name' => 'Schmaltier', 'code' => 'W1'),
                'jagdgebiet' => array('id' => 1, 'name' => 'Jagdgebiet Nord', 'code' => 'JG_N'),
                'erfasser' => 'Max Mustermann',
                'erfassungsdatum' => '2025-01-15',
                'bemerkungen' => 'Testerfassung',
                'interne_notiz' => 'Interne Notiz',
                'created_at' => '2025-01-15 10:00:00',
                'updated_at' => '2025-01-15 10:00:00'
            )
        );
    }
}
